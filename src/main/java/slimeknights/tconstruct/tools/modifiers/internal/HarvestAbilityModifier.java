package slimeknights.tconstruct.tools.modifiers.internal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event.Result;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.SingleUseModifier;
import slimeknights.tconstruct.library.tools.events.TinkerToolEvent.ToolHarvestEvent;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.helper.ToolHarvestLogic.AOEMatchType;
import slimeknights.tconstruct.library.tools.item.IModifiableHarvest;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class HarvestAbilityModifier extends SingleUseModifier {
  private final int priority;
  
  public HarvestAbilityModifier(int color, int priority) {
    super(color);
    this.priority = priority;
  }
  
  @Override
  public int getPriority() {
    return priority;
  }

  @Override
  public boolean shouldDisplay(boolean advanced) {
    return priority > Short.MIN_VALUE;
  }
  
  /**
   * Harvests a block that is harvested on interaction, such a berry bushes
   * @param context  Item use context of the original block clicked
   * @param world    World instance
   * @param state    State to harvest
   * @param pos      Position to harvest
   * @param player   Player instance
   * @return  True if harvested
   */

  private static boolean harvestInteract(UseOnContext context, ServerLevel world, BlockState state, BlockPos pos, @Nullable Player player) {
    if (player == null) {
      return false;
    }
    BlockHitResult trace = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos, false);
    InteractionResult result = state.use(world, player, context.getHand(), trace);
    return result.consumesAction();
  }

  /**
   * Harvests a stackable block, like sugar cane or kelp
   * @param world   World instance
   * @param state   Block state
   * @param pos     Block position
   * @param player  Player instance
   * @return True if the block was harvested
   */
  private static boolean harvestStackable(ServerLevel world, BlockState state, BlockPos pos, @Nullable Player player) {
    // if the block below is the same, break this block
    if (world.getBlockState(pos.below()).getBlock() == state.getBlock()) {
      world.destroyBlock(pos, true, player);
      return true;
    } else {
      // if the block above is the same, break it
      BlockPos up = pos.above();
      if (world.getBlockState(up).getBlock() == state.getBlock()) {
        world.destroyBlock(up, true, player);
        return true;
      }
    }
    return false;
  }

  /**
   * Tries harvesting a normal crop, that is a crop that goes through a set number of stages and is broken to drop produce and seeds
   * @param stack   Tool stack
   * @param world   World instance
   * @param state   Block state
   * @param pos     Block position
   * @param player  Player instance
   * @return  True if the crop was successfully harvested
   */
  private static boolean harvestCrop(ItemStack stack, ServerLevel world, BlockState state, BlockPos pos, @Nullable Player player) {
    Block block = state.getBlock();
    BlockState replant;
    // if crops block, its easy
    if (block instanceof CropBlock) {
      CropBlock crops = (CropBlock)block;
      if (!crops.isMaxAge(state)) {
        return false;
      }
      replant = crops.getStateForAge(0);
    } else {
      // try to find an age property
      IntegerProperty age = null;
      for (Property<?> prop : state.getProperties()) {
        if (prop.getName().equals("age") && prop instanceof IntegerProperty) {
          age = (IntegerProperty)prop;
          break;
        }
      }
      // must have an age property
      if (age == null) {
        return false;
      } else {
        // property must have 0 as valid
        Collection<Integer> allowedValues = age.getPossibleValues();
        if (!allowedValues.contains(0)) {
          return false;
        }
        // crop must be max age
        int maxAge = age.getPossibleValues().stream().max(Integer::compareTo).orElse(Integer.MAX_VALUE);
        if (state.getValue(age) < maxAge) {
          return false;
        }
        replant = state.setValue(age, 0);
      }
    }

    // crop is fully grown, get loot context
    LootContext.Builder lootContext = new LootContext.Builder(world)
      .withRandom(world.random)
      .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
      .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
      .withOptionalParameter(LootContextParams.BLOCK_ENTITY, world.getBlockEntity(pos));
    // find drops
    List<ItemStack> drops = state.getDrops(lootContext);

    // find a seed to remove from the drops
    Iterator<ItemStack> iterator = drops.iterator();
    boolean hasSeed = false;
    while (iterator.hasNext()) {
      ItemStack drop = iterator.next();
      if (TinkerTags.Items.SEEDS.contains(drop.getItem())) {
        hasSeed = true;
        drop.shrink(1);
        if (drop.isEmpty()) {
          iterator.remove();
        }
        break;
      }
    }

    // if we found one, replant, no seed means break
    if (hasSeed) {
      world.setBlockAndUpdate(pos, replant);
      state.spawnAfterBreak(world, pos, stack);
      // set block state will not play sounds, destory block will
      world.playSound(null, pos, state.getSoundType(world, pos, player).getBreakSound(), SoundSource.BLOCKS, 1.0f, 1.0f);
    } else {
      world.destroyBlock(pos, false);
    }

    // drop items
    for (ItemStack drop : drops) {
      Block.popResource(world, pos, drop);
    }

    return true;
  }

  /**
   * Tries to harvest the crop at the given position
   * @param context  Item use context of the original block clicked
   * @param world    World instance
   * @param state    State to harvest
   * @param pos      Position to harvest
   * @param stack    Stack used to break
   * @return  True if harvested
   */
  private static boolean harvest(UseOnContext context, ItemStack stack, IModifierToolStack tool, ServerLevel world, BlockState state, BlockPos pos, @Nullable Player player) {
    // first, check main harvestable tag
    Block block = state.getBlock();
    if (!TinkerTags.Blocks.HARVESTABLE.contains(block)) {
      return false;
    }
    // try harvest event
    boolean didHarvest = false;
    Result result = new ToolHarvestEvent(stack, tool, context, world, state, pos, player).fire();
    if (result != Result.DEFAULT) {
      didHarvest = result == Result.ALLOW;

      // crops that work based on right click interact (berry bushes)
    } else if (TinkerTags.Blocks.HARVESTABLE_INTERACT.contains(block)) {
      didHarvest = harvestInteract(context, world, state, pos, player);

      // next, try sugar cane like blocks
    } else if (TinkerTags.Blocks.HARVESTABLE_STACKABLE.contains(block)) {
      didHarvest = harvestStackable(world, state, pos, player);

      // normal crops like wheat or carrots
    } else if (TinkerTags.Blocks.HARVESTABLE_CROPS.contains(block)) {
      didHarvest = harvestCrop(stack, world, state, pos, player);
    }

    // if we successfully harvested, run the modifier hook
    if (didHarvest) {
      for (ModifierEntry entry : tool.getModifierList()) {
        IHarvestModifier harvest = entry.getModifier().getModule(IHarvestModifier.class);
        if (harvest != null) {
          harvest.afterHarvest(tool, entry.getLevel(), context, world, state, pos);
        }
      }
    }

    return didHarvest;
  }

  @Override
  public InteractionResult beforeBlockUse(IModifierToolStack tool, int level, UseOnContext context) {
    if (tool.isBroken()) {
      return InteractionResult.PASS;
    }

    ItemStack stack = context.getItemInHand();
    Item item = stack.getItem();
    if (item instanceof IModifiableHarvest) {
      IModifiableHarvest toolCore = (IModifiableHarvest) item;
      Player player = context.getPlayer();
      if (player != null && player.isShiftKeyDown()) {
        return InteractionResult.PASS;
      }
  
      // try harvest first
      Level world = context.getLevel();
      BlockPos pos = context.getClickedPos();
      BlockState state = world.getBlockState(pos);
      if (TinkerTags.Blocks.HARVESTABLE.contains(state.getBlock())) {
        if (world instanceof ServerLevel) {
          boolean survival = player == null || !player.isCreative();
          ServerLevel server = (ServerLevel)world;
  
          // try harvesting the crop, if successful and survival, damage the tool
          boolean didHarvest = false;
          boolean broken = false;
          if (harvest(context, stack, tool, server, state, pos, player)) {
            didHarvest = true;
            broken = survival && ToolDamageUtil.damage(tool, 1, player, stack);
          }
  
          // if we have a player, try doing AOE harvest
          if (!broken && player != null) {
            for (BlockPos newPos : toolCore.getToolHarvestLogic().getAOEBlocks(tool, stack, player, state, world, pos, context.getClickedFace(), AOEMatchType.TRANSFORM)) {
              // try harvesting the crop, if successful and survival, damage the tool
              if (harvest(context, stack, tool, server, world.getBlockState(newPos), newPos, player)) {
                didHarvest = true;
                if (survival && ToolDamageUtil.damage(tool, 1, player, stack)) {
                  broken = true;
                  break;
                }
              }
            }
          }
          // animations
          if (player != null) {
            if (didHarvest) {
              player.sweepAttack();
            }
            if (broken) {
              player.broadcastBreakEvent(context.getHand());
            }
          }
        }
        return InteractionResult.SUCCESS;
      }
    }
    return InteractionResult.PASS;
  }

  public interface IHarvestModifier {
    /**
     * Called after a block is successfully harvested
     * @param tool     Tool used in harvesting
     * @param level    Tool level
     * @param context  Item use context, cooresponds to the original targeted position
     * @param world    Server world instance
     * @param state    State before it was harvested
     * @param pos      Position that was harvested, may be different from the context
     */
    void afterHarvest(IModifierToolStack tool, int level, UseOnContext context, ServerLevel world, BlockState state, BlockPos pos);
  }
}
