package slimeknights.tconstruct.library.tools.stat;

import com.google.common.collect.Streams;
import lombok.RequiredArgsConstructor;
import slimeknights.tconstruct.library.MaterialRegistry;
import slimeknights.tconstruct.library.materials.IMaterial;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.tools.IToolPart;
import slimeknights.tconstruct.library.tools.ToolBaseStatDefinition;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Extendable utilities for a stats builder. Includes
 * <p>
 * It's encouraged to use this for the base of your calculation, and then modify the result as needed.
 */
@RequiredArgsConstructor
public abstract class AbstractToolStatsBuilder {
  /** Tool base stats, primarily for bonuses. The stat builder is responsible for using the bonuses */
  protected final ToolBaseStatDefinition baseStats;

  /**
   * Called after bonuses are processed to set the unique stats for this builder
   * @param builder  Stats builder
   */
  protected abstract void setStats(StatsNBT.Builder builder);

  /**
   * Checks if the given stat type is handled by this builder and should be skipped in the bonuses
   * @param stat  Stat type
   * @return  True if handled
   */
  protected abstract boolean handles(IToolStat<?> stat);

  /** Builds default stats */
  public StatsNBT buildStats() {
    StatsNBT.Builder builder = StatsNBT.builder();
    // start by adding in all relevant bonuses that are not handled elsewhere.
    // the handled check is needed becuase the immutable map builder does not like duplicate keys
    for (FloatToolStat stat : baseStats.getAllBonuses()) {
      if (!handles(stat)) {
        builder.set(stat, stat.getDefaultValue() + baseStats.getBonus(stat));
      }
    }
    setStats(builder);
    return builder.build();
  }


  /* Helpers */

  /**
   * Fetches the given stat from the material, getting the default stats if missing
   * @param statsId            Stat type
   * @param material           Material type
   * @param requiredComponent  Tool part requirement
   * @param <T>  Stat type
   * @return  Stat, or default if the part type accepts it, null if the part type does not
   */
  @Nullable
  public static <T extends IMaterialStats> T fetchStatsOrDefault(MaterialStatsId statsId, IMaterial material, IToolPart requiredComponent) {
    if (statsId.equals(requiredComponent.getStatType())) {
      return MaterialRegistry.getInstance().<T>getMaterialStats(material.getIdentifier(), statsId)
        .orElseGet(() -> MaterialRegistry.getInstance().getDefaultStats(statsId));
    } else {
      return null;
    }
  }

  /**
   * Gets a list of all stats for the given part type
   * @param statsId             Stat type
   * @param materials           Materials list
   * @param requiredComponents  List of required components, filters stat types
   * @param <T>  Type of stats
   * @return  List of stats
   */
  public static <T extends IMaterialStats> List<T> listOfCompatibleWith(MaterialStatsId statsId, List<IMaterial> materials, List<IToolPart> requiredComponents) {
    return Streams.zip(materials.stream(), requiredComponents.stream(),
                       (material, partMaterialType) -> AbstractToolStatsBuilder.<T>fetchStatsOrDefault(statsId, material, partMaterialType))
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());
  }

  /**
   * Gets the average value from a list of stat types
   * @param stats       Stat list
   * @param statGetter  Function to get the value
   * @param <T>  Material type
   * @return  Average value
   */
  public static <T extends IMaterialStats> double getAverageValue(List<T> stats, Function<T, ? extends Number> statGetter) {
    return getAverageValue(stats, statGetter, 0);
  }

  /**
   * Gets the average value from a list of stat types
   * @param stats         Stat list
   * @param statGetter    Function to get the value
   * @param missingValue  Default value to use for missing stats
   * @param <T>  Material type
   * @return  Average value
   */
  public static <T extends IMaterialStats, N extends Number> double getAverageValue(List<T> stats, Function<T, N> statGetter, double missingValue) {
    return stats.stream()
                .mapToDouble(value -> statGetter.apply(value).doubleValue())
                .average()
                .orElse(missingValue);
  }
}
