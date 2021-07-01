package slimeknights.tconstruct.library.tools.stat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat.FloatBuilder;

/**
 * Tool stat representing a float value, used for most numbers
 */
@Getter
public class FloatToolStat implements IToolStat<FloatBuilder> {
  /** Name of this tool stat */
  private final ToolStatId name;
  /** Color for this stat type */
  private final TextColor color;
  /** Gets the default value for this stat */
  private final float defaultValue;
  /** Min value for this stat */
  private final float minValue;
  /** Max value for this stat */
  private final float maxValue;

  public FloatToolStat(ToolStatId name, int color, float defaultValue, float minValue, float maxValue) {
    this.name = name;
    this.color = TextColor.fromRgb(color);
    this.defaultValue = defaultValue;
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  @Override
  public float clamp(float value) {
    return Mth.clamp(value, getMinValue(), getMaxValue());
  }

  @Override
  public FloatBuilder makeBuilder() {
    return new FloatBuilder();
  }

  /**
   * Adds the stat by the given value
   * @param builder  Builder instance
   * @param value    Amount to add
   */
  public void add(ModifierStatsBuilder builder, double value) {
    builder.updateStat(this, b -> b.add += value);
  }

  /**
   * Multiplies the stat by the given value. Multiplication is applied after all addiiton
   * @param builder  Builder instance
   * @param factor   Amount to multiply
   */
  public void multiply(ModifierStatsBuilder builder, double factor) {
    builder.updateStat(this, b -> b.multiply *= factor);
  }

  @Override
  public float build(FloatBuilder builder, float value) {
    return (value + builder.add) * builder.multiply;
  }

  @Override
  public Component formatValue(float number) {
    return IToolStat.formatNumber(Util.makeTranslationKey("tool_stat", getName()), getColor(), number);
  }

  @Override
  public String toString() {
    return "FloatToolStat{" + name + '}';
  }

  /** Internal builder to store the add and multiply value */
  @NoArgsConstructor
  protected static class FloatBuilder {
    /** Value summed with the base, applies first */
    private float add = 0;
    /** Value multiplied by the sum, applies second */
    private float multiply = 1;
  }
}
