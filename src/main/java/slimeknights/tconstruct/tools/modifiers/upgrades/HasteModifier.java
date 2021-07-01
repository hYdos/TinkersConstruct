package slimeknights.tconstruct.tools.modifiers.upgrades;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import slimeknights.tconstruct.library.modifiers.IncrementalModifier;
import slimeknights.tconstruct.library.tools.ToolDefinition;
import slimeknights.tconstruct.library.tools.nbt.IModDataReadOnly;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

public class HasteModifier extends IncrementalModifier {
  public HasteModifier() {
    super(0xAA0F01);
  }

  @Override
  public Component getDisplayName(int level) {
    // displays special names for levels of haste
    if (level <= 5) {
      return applyStyle(new TranslatableComponent(getTranslationKey() + "." + level));
    }
    return super.getDisplayName(level);
  }

  @Override
  public void addToolStats(ToolDefinition toolDefinition, StatsNBT baseStats, IModDataReadOnly persistentData, IModDataReadOnly volatileData, int level, ModifierStatsBuilder builder) {
    float scaledLevel = getScaledLevel(persistentData, level);
    // currently gives +5 speed per level
    // for comparison, vanilla gives +2, 5, 10, 17, 26 for efficiency I to V
    // 5 per level gives us          +5, 10, 15, 20, 25 for 5 levels
    ToolStats.MINING_SPEED.add(builder, scaledLevel * 5f);
    // maxes at 150%, number chosen to be comparable DPS to quartz
    ToolStats.ATTACK_SPEED.multiply(builder, 1 + scaledLevel * 0.1f);
  }
}
