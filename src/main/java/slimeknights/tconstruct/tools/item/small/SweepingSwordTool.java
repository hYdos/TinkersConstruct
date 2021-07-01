package slimeknights.tconstruct.tools.item.small;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import slimeknights.tconstruct.library.tools.ToolDefinition;
import slimeknights.tconstruct.library.tools.helper.ToolAttackContext;
import slimeknights.tconstruct.library.tools.helper.ToolAttackUtil;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;
import slimeknights.tconstruct.tools.TinkerModifiers;

/** Sword that also has a sweep attack */
public class SweepingSwordTool extends SwordTool {
  public SweepingSwordTool(Properties properties, ToolDefinition toolDefinition) {
    super(properties, toolDefinition);
  }

  /** Gets the bonus area of the sweep attack */
  protected double getSweepRange(IModifierToolStack tool) {
    return tool.getModifierLevel(TinkerModifiers.expanded.get()) + 1;
  }

  // sword sweep attack
  @Override
  public boolean dealDamage(IModifierToolStack tool, ToolAttackContext context, float damage) {
    // deal damage first
    boolean hit = super.dealDamage(tool, context, damage);

    // sweep code from EntityPlayer#attackTargetEntityWithCurrentItem()
    // basically: no crit, no sprinting and has to stand on the ground for sweep. Also has to move regularly slowly
    LivingEntity attacker = context.getAttacker();
    if (hit && context.isFullyCharged() && !attacker.isSprinting() && !context.isCritical() && attacker.isOnGround() && (attacker.walkDist - attacker.walkDistO) < attacker.getSpeed()) {
      // loop through all nearby entities
      double range = getSweepRange(tool);
      // if the modifier is missing, sweeping damage will be 0, so easiest to let it fully control this
      float sweepDamage = TinkerModifiers.sweeping.get().getSweepingDamage(tool, damage);
      Entity target = context.getTarget();
      for (LivingEntity aoeTarget : attacker.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(range, 0.25D, range))) {
        if (aoeTarget != attacker && aoeTarget != target && !attacker.isAlliedTo(aoeTarget)
            && (!(aoeTarget instanceof ArmorStand) || !((ArmorStand) aoeTarget).isMarker()) && attacker.distanceToSqr(aoeTarget) < 10.0D + range) {
          aoeTarget.knockback(0.4F, Mth.sin(attacker.yRot * ((float) Math.PI / 180F)), -Mth.cos(attacker.yRot * ((float) Math.PI / 180F)));
          ToolAttackUtil.dealDefaultDamage(attacker, aoeTarget, sweepDamage);
        }
      }

      attacker.level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, attacker.getSoundSource(), 1.0F, 1.0F);
      if (attacker instanceof Player) {
        ((Player) attacker).sweepAttack();
      }
    }

    return hit;
  }
}
