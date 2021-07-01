package slimeknights.tconstruct.world.worldgen.trees;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.world.TinkerStructures;
import slimeknights.tconstruct.world.worldgen.trees.config.BaseSlimeTreeFeatureConfig;

import javax.annotation.Nullable;
import java.util.Random;

public class SlimeTree extends AbstractTreeGrower {

  private final SlimeType foliageType;

  public SlimeTree(SlimeType foliageType) {
    this.foliageType = foliageType;
  }

  @Deprecated
  @Nullable
  @Override
  protected ConfiguredFeature<TreeConfiguration, ?> getConfiguredFeature(Random randomIn, boolean largeHive) {
    return null;
  }

  /**
   * Get a {@link net.minecraft.world.level.levelgen.feature.ConfiguredFeature} of tree
   */
  @Nullable
  public ConfiguredFeature<BaseSlimeTreeFeatureConfig, ?> getSlimeTreeFeature(Random randomIn, boolean largeHive) {
    switch (this.foliageType) {
      case EARTH:
        return TinkerStructures.EARTH_SLIME_TREE;
      case SKY:
        return TinkerStructures.SKY_SLIME_TREE;
      case ENDER:
        return TinkerStructures.ENDER_SLIME_TREE;
      case BLOOD:
        return TinkerStructures.BLOOD_SLIME_TREE;
      case ICHOR:
        return TinkerStructures.ICHOR_SLIME_TREE;
    }

    return null;
  }

  @Override
  public boolean growTree(ServerLevel world, ChunkGenerator chunkGenerator, BlockPos pos, BlockState state, Random rand) {
    ConfiguredFeature<BaseSlimeTreeFeatureConfig, ?> configuredFeature = this.getSlimeTreeFeature(rand, this.hasFlowers(world, pos));
    if (configuredFeature == null) {
      return false;
    }
    else {
      world.setBlock(pos, Blocks.AIR.defaultBlockState(), 4);

      configuredFeature.config.forcePlacement();

      if (configuredFeature.place(world, chunkGenerator, rand, pos)) {
        return true;
      }
      else {
        world.setBlock(pos, state, 4);
        return false;
      }
    }
  }

  private boolean hasFlowers(LevelAccessor world, BlockPos pos) {
    for (BlockPos blockpos : BlockPos.MutableBlockPos.betweenClosed(pos.below().north(2).west(2), pos.above().south(2).east(2))) {
      if (world.getBlockState(blockpos).is(BlockTags.FLOWERS)) {
        return true;
      }
    }

    return false;
  }

}
