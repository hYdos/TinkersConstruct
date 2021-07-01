package slimeknights.tconstruct.tools.modifiers.upgrades;

import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.modifiers.IncrementalModifier;
import slimeknights.tconstruct.library.tools.helper.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;

/** Shared logic for all modifiers that boost damage against a creature type */
public class ScaledTypeDamageModifier extends IncrementalModifier {
  private final MobType type;
  public ScaledTypeDamageModifier(int color, MobType type) {
    super(color);
    this.type = type;
  }

  /**
   * Method to check if this modifier is effective on the given entity
   * @param target  Entity
   * @return  True if effective
   */
  protected boolean isEffective(LivingEntity target) {
    return target.getMobType() == type;
  }

  @Override
  public float getEntityDamage(IModifierToolStack tool, int level, ToolAttackContext context, float baseDamage, float damage) {
    LivingEntity target = context.getLivingTarget();
    if (target != null && isEffective(target)) {
      damage += getScaledLevel(tool, level) * 2.5f;
    }
    return damage;
  }

  @Override
  public void addInformation(IModifierToolStack tool, int level, List<Component> tooltip, boolean isAdvanced, boolean detailed) {
    addDamageTooltip(this, tool, level, 2.5f, tooltip);
  }

  /**
   * Adds a tooltip showing the bonus damage and the type of damage dded
   * @param self         Modifier instance
   * @param tool         Tool instance
   * @param level        Current level
   * @param levelAmount  Bonus per level
   * @param tooltip      Tooltip
   */
  public static void addDamageTooltip(IncrementalModifier self, IModifierToolStack tool, int level, float levelAmount, List<Component> tooltip) {
    tooltip.add(self.applyStyle(new TextComponent("+" + Util.df.format(self.getScaledLevel(tool, level) * levelAmount))
                                  .append(" ")
                                  .append(new TranslatableComponent(self.getTranslationKey() + ".damage"))));
  }
}
