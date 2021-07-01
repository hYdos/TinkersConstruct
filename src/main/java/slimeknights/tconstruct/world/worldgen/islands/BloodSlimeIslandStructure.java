package slimeknights.tconstruct.world.worldgen.islands;

import com.google.common.collect.ImmutableList;
import slimeknights.tconstruct.world.worldgen.islands.variants.IIslandVariant;
import slimeknights.tconstruct.world.worldgen.islands.variants.IslandVariants;

import java.util.List;
import java.util.Random;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;

public class BloodSlimeIslandStructure extends AbstractIslandStructure {
  private static final List<MobSpawnSettings.SpawnerData> STRUCTURE_MONSTERS = ImmutableList.of(
    new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 150, 4, 6)
  );

  @Override
  public GenerationStep.Decoration step() {
    return GenerationStep.Decoration.UNDERGROUND_DECORATION;
  }

  @Override
  public IIslandVariant getVariant(Random random) {
    return IslandVariants.BLOOD;
  }

  @Override
  public List<MobSpawnSettings.SpawnerData> getDefaultSpawnList() {
    return STRUCTURE_MONSTERS;
  }

  @Override
  protected int getHeight(ChunkGenerator generator, Rotation rotation, int x, int z, Random random) {
    return Math.max(generator.getSeaLevel() - 7, 0);
  }
}
