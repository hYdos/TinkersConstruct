package slimeknights.tconstruct.tools.modifiers.internal;

import lombok.Getter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.eventbus.api.Event.Result;
import slimeknights.tconstruct.library.modifiers.SingleUseModifier;
import slimeknights.tconstruct.library.tools.events.TinkerToolEvent.ToolShearEvent;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.helper.ToolDamageUtil;
import slimeknights.tconstruct.library.tools.helper.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;
import slimeknights.tconstruct.tools.TinkerModifiers;

public class ShearsAbilityModifier extends SingleUseModifier {
  private final int range;
  @Getter
  private final int priority;
  
  public ShearsAbilityModifier(int color, int range, int priority) {
    super(color);
    this.range = range;
    this.priority = priority;
  }

  @Override
  public boolean shouldDisplay(boolean advanced) {
    return priority > Short.MIN_VALUE;
  }
  
  /**
   * Swings the given's player hand
   *
   * @param player the current player
   * @param hand the given hand the tool is in
   */
  protected void swingTool(Player player, InteractionHand hand) {
    player.swing(hand);
    player.sweepAttack();
  }

  /**
   * Checks whether the tool counts as shears for modifier logic
   *
   * @param tool  Current tool instance
   */
  protected boolean isShears(IModifierToolStack tool) {
    return true;
  }

  @Override
  public InteractionResult onEntityUseFirst(IModifierToolStack tool, int level, Player player, Entity target, InteractionHand hand) {
    if (tool.isBroken()) {
      return InteractionResult.PASS;
    }
    ItemStack stack = player.getItemInHand(hand);

    // use looting instead of fortune, as that is our hook with entity access
    // modifier can always use tags or the nullable parameter to distinguish if needed
    int looting = ModifierUtil.getLootingLevel(tool, player, target, null);
    Level world = player.getCommandSenderWorld();
    if (isShears(tool) && this.shearEntity(stack, tool, world, player, target, looting)) {
      ToolDamageUtil.damageAnimated(tool, 1, player, hand);
      this.swingTool(player, hand);

      // AOE shearing
      int expanded = range + tool.getModifierLevel(TinkerModifiers.expanded.get());
      if (expanded > 0) {
        for (LivingEntity aoeTarget : player.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(expanded, 0.25D, expanded))) {
          if (aoeTarget != player && aoeTarget != target && (!(aoeTarget instanceof ArmorStand) || !((ArmorStand) aoeTarget).isMarker())) {
            if (this.shearEntity(stack, tool, world, player, aoeTarget, looting)) {
              if (ToolDamageUtil.damageAnimated(tool, 1, player, hand)) {
                break;
              }
            }
          }
        }
      }

      return InteractionResult.SUCCESS;
    }

    return InteractionResult.PASS;
  }

  /**
   * Tries to shear an given entity, returns false if it fails and true if it succeeds
   *
   * @param itemStack the current item stack
   * @param world the current world
   * @param player the current player
   * @param entity the entity to try to shear
   * @param fortune the fortune to apply to the sheared entity
   * @return if the sheering of the entity was performed or not
   */
  private boolean shearEntity(ItemStack itemStack, IModifierToolStack tool, Level world, Player player, Entity entity, int fortune) {
    // event to override entity shearing
    Result result = new ToolShearEvent(itemStack, tool, world, player, entity, fortune).fire();
    if (result != Result.DEFAULT) {
      return result == Result.ALLOW;
    }
    // fallback to forge shearable
    if (entity instanceof IForgeShearable) {
      IForgeShearable target = (IForgeShearable) entity;
      if (target.isShearable(itemStack, world, entity.blockPosition())) {
        if (!world.isClientSide) {
          target.onSheared(player, itemStack, world, entity.blockPosition(), fortune)
                .forEach(stack -> ToolShearEvent.dropItem(entity, stack));
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public Boolean removeBlock(IModifierToolStack tool, int level, ToolHarvestContext context) {
    BlockState state = context.getState();
    if (isShears(tool) && state.getBlock() instanceof TripWireBlock) {
      context.getWorld().setBlockState(context.getPos(), state.setValue(BlockStateProperties.DISARMED, Boolean.TRUE), 4);
    }
    return null;
  }
}
