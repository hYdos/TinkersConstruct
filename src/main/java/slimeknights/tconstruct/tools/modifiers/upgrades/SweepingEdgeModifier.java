package slimeknights.tconstruct.tools.modifiers.upgrades;

import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.modifiers.IncrementalModifier;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class SweepingEdgeModifier extends IncrementalModifier {
  private static final Component SWEEPING_BONUS = Util.makeTranslation("modifier", "sweeping_edge.damage");

  public SweepingEdgeModifier() {
    super(0x888888);
  }

  /** Gets the damage dealt by this tool, boosted properly by sweeping */
  public float getSweepingDamage(IModifierToolStack toolStack, float baseDamage) {
    int level = toolStack.getModifierLevel(this);
    float sweepingDamage = 1;
    if (level > 0) {
      sweepingDamage += (1 - 1f / (getScaledLevel(toolStack, level) + 1)) * baseDamage;
    }
    return sweepingDamage;
  }

  @Override
  public void addInformation(IModifierToolStack tool, int level, List<Component> tooltip, boolean isAdvanced, boolean detailed) {
    float amount = 1 - 1f / (getScaledLevel(tool, level) + 1);
    tooltip.add(applyStyle(new TextComponent(Util.dfPercent.format(amount)).append(" ").append(SWEEPING_BONUS)));
  }
}
