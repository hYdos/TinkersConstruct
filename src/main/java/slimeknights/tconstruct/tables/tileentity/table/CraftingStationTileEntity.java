package slimeknights.tconstruct.tables.tileentity.table;

import lombok.Getter;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.hooks.BasicEventHooks;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.network.TinkerNetwork;
import slimeknights.tconstruct.shared.inventory.ConfigurableInvWrapperCapability;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.inventory.table.CraftingStationContainer;
import slimeknights.tconstruct.tables.network.UpdateCraftingRecipePacket;
import slimeknights.tconstruct.tables.tileentity.table.crafting.CraftingInventoryWrapper;
import slimeknights.tconstruct.tables.tileentity.table.crafting.LazyResultInventory;

import javax.annotation.Nullable;
import java.util.Collections;

public class CraftingStationTileEntity extends RetexturedTableTileEntity implements LazyResultInventory.ILazyCrafter {
  public static final Component UNCRAFTABLE = Util.makeTranslation("gui", "crafting_station.uncraftable");

  /** Last crafted crafting recipe */
  @Nullable
  private CraftingRecipe lastRecipe;
  /** Result inventory, lazy loads results */
  @Getter
  private final LazyResultInventory craftingResult;
  /** Crafting inventory for the recipe calls */
  private final CraftingInventoryWrapper craftingInventory;

  public CraftingStationTileEntity() {
    super(TinkerTables.craftingStationTile.get(), "gui.tconstruct.crafting_station", 9);
    this.itemHandler = new ConfigurableInvWrapperCapability(this, false, false);
    this.itemHandlerCap = LazyOptional.of(() -> this.itemHandler);
    this.craftingInventory = new CraftingInventoryWrapper(this, 3, 3);
    this.craftingResult = new LazyResultInventory(this);
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player playerEntity) {
    return new CraftingStationContainer(menuId, playerInventory, this);
  }

  @Override
  public AABB getRenderBoundingBox() {
    return new AABB(worldPosition, worldPosition.offset(1, 2, 1));
  }

  /* Crafting */

  @Override
  public ItemStack calcResult(@Nullable Player player) {
    if (this.level == null || isEmpty()) {
      return ItemStack.EMPTY;
    }
    // assume empty unless we learn otherwise
    ItemStack result = ItemStack.EMPTY;
    if (!this.level.isClientSide && this.level.getServer() != null) {
      RecipeManager manager = this.level.getServer().getRecipeManager();

      // first, try the cached recipe
      CraftingRecipe recipe = lastRecipe;
      // if it does not match, find a new recipe
      // note we intentionally have no player access during matches, that could lead to an unstable recipe
      if (recipe == null || !recipe.matches(this.craftingInventory, this.level)) {
        recipe = manager.getRecipeFor(RecipeType.CRAFTING, this.craftingInventory, this.level).orElse(null);
      }

      // if we have a recipe, fetch its result
      if (recipe != null) {
        ForgeHooks.setCraftingPlayer(player);
        result = recipe.assemble(this.craftingInventory);
        ForgeHooks.setCraftingPlayer(null);

        // sync if the recipe is different
        if (recipe != lastRecipe) {
          this.lastRecipe = recipe;
          this.syncToRelevantPlayers(this::syncRecipe);
        }
      }
    }
    else if (this.lastRecipe != null && this.lastRecipe.matches(this.craftingInventory, this.level)) {
      ForgeHooks.setCraftingPlayer(player);
      result = this.lastRecipe.assemble(this.craftingInventory);
      ForgeHooks.setCraftingPlayer(null);
    }
    return result;
  }

  /**
   * Gets the player sensitive crafting result, also validating the player has access to this recipe
   * @param player  Player
   * @return  Player sensitive result
   */
  public ItemStack getResultForPlayer(Player player) {
    ForgeHooks.setCraftingPlayer(player);
    CraftingRecipe recipe = this.lastRecipe; // local variable just to prevent race conditions if the field changes, though that is unlikely

    // try matches again now that we have player access
    if (recipe == null || this.level == null || !recipe.matches(craftingInventory, level)) {
      ForgeHooks.setCraftingPlayer(null);
      return ItemStack.EMPTY;
    }

    // check if the player has access to the recipe, if not give up
    // Disabled because this is an absolute mess of logic, and the gain is rather small, treating this like a furnace instead
    // note the gamerule is client side only anyways, so you would have to sync it, such as in the container
    // if you want limited crafting, disable the crafting station, the design of the station is incompatible with the game rule and vanilla syncing
//    if (!recipe.isDynamic() && world.getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING)) {
//      // mojang, why can't PlayerEntity just have a RecipeBook getter, why must I go through the sided classes? grr
//      boolean locked;
//      if (!world.isRemote) {
//        locked = player instanceof ServerPlayerEntity && !((ServerPlayerEntity) player).getRecipeBook().isUnlocked(recipe);
//      } else {
//        locked = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> player instanceof ClientPlayerEntity && !((ClientPlayerEntity) player).getRecipeBook().isUnlocked(recipe));
//      }
//      // if the player cannot craft this, block crafting
//      if (locked) {
//        ForgeHooks.setCraftingPlayer(null);
//        return ItemStack.EMPTY;
//      }
//    }

    ItemStack result = recipe.assemble(craftingInventory);
    ForgeHooks.setCraftingPlayer(null);
    return result;
  }

  /**
   * Removes the result from this inventory, updating inputs and triggering recipe hooks
   * @param player  Player taking result
   * @param result  Result removed
   * @param amount  Number of times crafted
   */
  public void takeResult(Player player, ItemStack result, int amount) {
    CraftingRecipe recipe = this.lastRecipe; // local variable just to prevent race conditions if the field changes, though that is unlikely
    if (recipe == null || this.level == null) {
      return;
    }

    // fire crafting events
    if (!recipe.isSpecial()) {
      // unlock the recipe if it was not unlocked, so it shows in the recipe book
      player.awardRecipes(Collections.singleton(recipe));
    }
    result.onCraftedBy(this.level, player, amount);
    BasicEventHooks.firePlayerCraftingEvent(player, result, this.craftingInventory);

    // update all slots in the inventory
    // remove remaining items
    ForgeHooks.setCraftingPlayer(player);
    NonNullList<ItemStack> remaining = recipe.getRemainingItems(craftingInventory);
    ForgeHooks.setCraftingPlayer(null);
    for (int i = 0; i < remaining.size(); ++i) {
      ItemStack original = this.getItem(i);
      ItemStack newStack = remaining.get(i);

      // if the slot contains a stack, decrease by 1
      if (!original.isEmpty()) {
        original.shrink(1);
      }

      // if we have a new item, try merging it in
      if (!newStack.isEmpty()) {
        // if empty, set directly
        if (original.isEmpty()) {
          this.setItem(i, newStack);
        }
        else if (ItemStack.isSame(original, newStack) && ItemStack.tagMatches(original, newStack)) {
          // if matching, merge
          newStack.grow(original.getCount());
          this.setItem(i, newStack);
        }
        else {
          // otherwise, drop the item as the player
          if (!player.inventory.add(newStack)) {
            player.drop(newStack, false);
          }
        }
      }
    }
  }

  /** Sends a message alerting the player this item is currently uncraftable, typically due to gamerules */
  public void notifyUncraftable(Player player) {
    // if empty, send a message so the player is more aware of why they cannot craft it, sent to chat as status bar is not visible
    // TODO: consider moving into the UI somewhere
    if (level != null && !level.isClientSide) {
      player.displayClientMessage(CraftingStationTileEntity.UNCRAFTABLE, false);
    }
  }

  @Override
  public ItemStack onCraft(Player player, ItemStack result, int amount) {
    int originalSize = result.getCount(); // may be larger than the output count if the player is holding a stack
    // going to refetch result, so just start at empty
    result = ItemStack.EMPTY;

    if (amount > 0) {
      // get the player sensitive result
      result = getResultForPlayer(player);
      if (!result.isEmpty()) {
        // update the inputs and trigger recipe hooks
        takeResult(player, result, amount);
      }
      // if the player was holding this item, increase the count to match
      if (originalSize > 0) {
        result.setCount(result.getCount() + player.inventory.getCarried().getCount() - originalSize);
      }
    }
    // the return value ultimately does nothing, so manually set the result into the player
    player.inventory.setCarried(result);
    if (result.isEmpty()) {
      notifyUncraftable(player);
    }
    return result;
  }

  @Override
  public void setItem(int slot, ItemStack itemstack) {
    super.setItem(slot, itemstack);
    // clear the crafting result when the matrix changes so we recalculate the result
    this.craftingResult.clearContent();
  }


  /* Syncing */

  /**
   * Sends the current recipe to the given player
   * @param player  Player to send an update to
   */
  public void syncRecipe(Player player) {
    // must have a last recipe and a server world
    if (this.lastRecipe != null && this.level != null && !this.level.isClientSide && player instanceof ServerPlayer) {
      TinkerNetwork.getInstance().sendTo(new UpdateCraftingRecipePacket(this.worldPosition, this.lastRecipe), (ServerPlayer) player);
    }
  }

  /**
   * Updates the recipe from the server
   * @param recipe  New recipe
   */
  public void updateRecipe(CraftingRecipe recipe) {
    this.lastRecipe = recipe;
    this.craftingResult.clearContent();
  }
}
