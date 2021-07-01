package slimeknights.tconstruct.tables.recipe;

import com.mojang.datafixers.util.Pair;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.materials.IMaterial;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;
import slimeknights.tconstruct.library.tinkering.IMaterialItem;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tools.TinkerToolParts;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Recipe using repair kits in the crafting table */
public class CraftingTableRepairKitRecipe extends CustomRecipe {
  public CraftingTableRepairKitRecipe(ResourceLocation id) {
    super(id);
  }

  /**
   * Gets the tool stack and the repair kit material from the crafting grid
   * @param inv  Crafting inventory
   * @return  Relavant inputs, or null if invalid
   */
  @Nullable
  private Pair<ToolStack, IMaterial> getRelevantInputs(CraftingContainer inv) {
    ToolStack tool = null;
    IMaterial material = null;
    for (int i = 0; i < inv.getContainerSize(); i++) {
      ItemStack stack = inv.getItem(i);
      if (stack.isEmpty()) {
        continue;
      }
      // repair kit - update material
      if (stack.getItem() == TinkerToolParts.repairKit.get()) {
        // already found repair kit
        if (material != null) {
          return null;
        }
        IMaterial inputMaterial = IMaterialItem.getMaterialFromStack(stack);
        // if the material is invalid, also fail
        if (inputMaterial == IMaterial.UNKNOWN) {
          return null;
        }
        material = inputMaterial;
      } else if (TinkerTags.Items.MULTIPART_TOOL.contains(stack.getItem())) {
        // cannot repair multiple tools
        if (tool != null) {
          return null;
        }
        // tool must be damaged
        tool = ToolStack.from(stack);
        if (!tool.isBroken() && tool.getDamage() == 0) {
          return null;
        }
      } else {
        // unknown item input
        return null;
      }
    }
    if (tool == null || material == null) {
      return null;
    }
    return Pair.of(tool, material);
  }

  @Override
  public boolean matches(CraftingContainer inv, Level worldIn) {
    Pair<ToolStack, IMaterial> inputs = getRelevantInputs(inv);
    return inputs != null && TinkerStationRepairRecipe.getRepairIndex(inputs.getFirst(), inputs.getSecond()) >= 0;
  }

  @Override
  public ItemStack getCraftingResult(CraftingContainer inv) {
    Pair<ToolStack, IMaterial> inputs = getRelevantInputs(inv);
    if (inputs == null) {
      TConstruct.log.error("Recipe repair on {} failed to find items after matching", getId());
      return ItemStack.EMPTY;
    }

    // first identify materials and durablity
    ToolStack tool = inputs.getFirst().copy();
    IMaterial repairMaterial = inputs.getSecond();
    MaterialStatsId repairStats = TinkerStationRepairRecipe.getDefaultStatsId(tool, repairMaterial);
    IMaterial primaryMaterial = tool.getMaterial( tool.getDefinition().getRepairParts()[0]);
    // vanilla says 25% durability per ingot, repair kits are worth 2 ingots
    float repairAmount = MaterialRecipe.getRepairDurability(repairMaterial.getIdentifier(), repairStats) / 2f;
    if (repairAmount > 0) {
      if (repairMaterial != primaryMaterial) {
        repairAmount /= tool.getDefinition().getBaseStatDefinition().getPrimaryHeadWeight();
      }

      // adjust the factor based on modifiers
      // main example is wood, +25% per level
      for (ModifierEntry entry : tool.getModifierList()) {
        repairAmount = entry.getModifier().getRepairFactor(tool, entry.getLevel(), repairAmount);
        if (repairAmount <= 0) {
          // failed to repair
          return tool.createStack();
        }
      }

      // repair the tool
      ToolDamageUtil.repair(tool, (int)repairAmount);
    }
    // return final stack
    return tool.createStack();
  }

  @Override
  public boolean canCraftInDimensions(int width, int height) {
    return width * height >= 2;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerTables.craftingTableRepairSerializer.get();
  }
}
