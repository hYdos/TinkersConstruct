package slimeknights.tconstruct.world.worldgen.islands;

import com.google.common.collect.ImmutableList;
import slimeknights.tconstruct.world.TinkerWorld;
import slimeknights.tconstruct.world.worldgen.islands.variants.IIslandVariant;
import slimeknights.tconstruct.world.worldgen.islands.variants.IslandVariants;

import java.util.List;
import java.util.Random;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;

public class EnderSlimeIslandStructure extends AbstractIslandStructure {
  private final List<SpawnerData> monsters = ImmutableList.of(new MobSpawnSettings.SpawnerData(TinkerWorld.enderSlimeEntity.get(), 30, 4, 4));

  @Override
  public List<SpawnerData> getDefaultSpawnList() {
    return monsters;
  }

  @Override
  public IIslandVariant getVariant(Random random) {
    return IslandVariants.ENDER;
  }
}
