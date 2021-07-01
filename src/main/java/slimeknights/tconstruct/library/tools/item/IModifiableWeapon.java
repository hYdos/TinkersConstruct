package slimeknights.tconstruct.library.tools.item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import slimeknights.tconstruct.library.tools.helper.ToolAttackContext;
import slimeknights.tconstruct.library.tools.helper.ToolAttackUtil;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;

/**
 * Interface for extra hooks needed on modifyable weapons
 */
public interface IModifiableWeapon {
  /**
   * Actually deal damage to the entity we hit. Can be overridden for special behaviour
   *
   * @return True if the entity was hit. Usually the return value of {@link Entity#hurt(DamageSource, float)}
   */
  default boolean dealDamage(IModifierToolStack stack, ToolAttackContext context, float damage) {
    return ToolAttackUtil.dealDefaultDamage(context.getAttacker(), context.getTarget(), damage);
  }
}
