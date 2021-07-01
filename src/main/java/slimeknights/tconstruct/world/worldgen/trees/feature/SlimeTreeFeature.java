package slimeknights.tconstruct.world.worldgen.trees.feature;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedRW;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import slimeknights.tconstruct.world.TinkerWorld;
import slimeknights.tconstruct.world.worldgen.trees.config.BaseSlimeTreeFeatureConfig;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class SlimeTreeFeature extends Feature<BaseSlimeTreeFeatureConfig> {

  public SlimeTreeFeature(Codec<BaseSlimeTreeFeatureConfig> codec) {
    super(codec);
  }

  @Override
  public final boolean generate(WorldGenLevel seedReader, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, BaseSlimeTreeFeatureConfig config) {
    Set<BlockPos> set = Sets.newHashSet();
    Set<BlockPos> set1 = Sets.newHashSet();
    Set<BlockPos> set2 = Sets.newHashSet();
    BoundingBox mutableboundingbox = BoundingBox.getUnknownBox();
    boolean flag = this.place(seedReader, random, blockPos, set, set1, mutableboundingbox, config);

    if (mutableboundingbox.x0 <= mutableboundingbox.x1 && flag && !set.isEmpty()) {
      DiscreteVoxelShape voxelshapepart = this.func_236403_a_(seedReader, mutableboundingbox, set, set2);
      StructureTemplate.updateShapeAtEdge(seedReader, 3, voxelshapepart, mutableboundingbox.x0, mutableboundingbox.y0, mutableboundingbox.z0);
      return true;
    }
    else {
      return false;
    }
  }

  private boolean place(LevelSimulatedRW generationReader, Random rand, BlockPos positionIn, Set<BlockPos> trunkBlockPosSet, Set<BlockPos> p_225557_5_, BoundingBox boundingBoxIn, BaseSlimeTreeFeatureConfig configIn) {
    int height = rand.nextInt(configIn.randomHeight) + configIn.baseHeight;

    if (!(generationReader instanceof LevelAccessor)) {
      return false;
    }

//    BlockPos blockpos;
//    if (!configIn.forcePlacement) {
//      int oceanFloorHeight = generationReader.getHeight(Heightmap.Type.OCEAN_FLOOR, positionIn).getY();
//
//      blockpos = new BlockPos(positionIn.getX(), oceanFloorHeight, positionIn.getZ());
//    }
//    else {
//      blockpos = positionIn;
//    }

    if (positionIn.getY() >= 1 && positionIn.getY() + height + 1 <= 256) {
      if (!isDirtOrFarmlandAt(generationReader, positionIn.below())) {
        return false;
      }
      else {
        this.setDirtAt(generationReader, positionIn.below(), positionIn);
        this.placeTrunk(generationReader, rand, height, positionIn, trunkBlockPosSet, boundingBoxIn, configIn);
        this.placeCanopy(generationReader, rand, height, positionIn, trunkBlockPosSet, boundingBoxIn, configIn);

        return true;
      }
    }
    else {
      return false;
    }
  }

  protected void setDirtAt(LevelSimulatedRW reader, BlockPos pos, BlockPos origin) {
    if (!(reader instanceof LevelAccessor)) {
      return;
    }

    ((LevelAccessor) reader).getBlockState(pos).getBlock().onPlantGrow(((LevelAccessor) reader).getBlockState(pos), (LevelAccessor) reader, pos, origin);
  }

  protected void placeTrunk(LevelSimulatedRW worldIn, Random randomIn, int treeHeight, BlockPos blockPos, Set<BlockPos> blockPosSet, BoundingBox mutableBoundingBoxIn, BaseSlimeTreeFeatureConfig treeFeatureConfigIn) {
    while (treeHeight > 0) {
      this.setLog(worldIn, randomIn, blockPos, blockPosSet, mutableBoundingBoxIn, treeFeatureConfigIn);

      blockPos = blockPos.above();
      treeHeight--;
    }
  }

  protected void placeCanopy(LevelSimulatedRW worldIn, Random randomIn, int treeHeight, BlockPos blockPos, Set<BlockPos> blockPosSet, BoundingBox mutableBoundingBoxIn, BaseSlimeTreeFeatureConfig treeFeatureConfigIn) {
    blockPos = blockPos.above(treeHeight);
    for (int i = 0; i < 4; i++) {
      this.placeDiamondLayer(worldIn, randomIn, i + 1, blockPos.below(i), blockPosSet, mutableBoundingBoxIn, treeFeatureConfigIn);
    }

    blockPos = blockPos.below(3);

    this.placeAir(worldIn, randomIn, blockPos.offset(+4, 0, 0), blockPosSet, mutableBoundingBoxIn);
    this.placeAir(worldIn, randomIn, blockPos.offset(-4, 0, 0), blockPosSet, mutableBoundingBoxIn);
    this.placeAir(worldIn, randomIn, blockPos.offset(0, 0, +4), blockPosSet, mutableBoundingBoxIn);
    this.placeAir(worldIn, randomIn, blockPos.offset(0, 0, -4), blockPosSet, mutableBoundingBoxIn);

    if (treeFeatureConfigIn.hasVines) {
      this.placeAir(worldIn, randomIn, blockPos.offset(+1, 0, +1), blockPosSet, mutableBoundingBoxIn);
      this.placeAir(worldIn, randomIn, blockPos.offset(+1, 0, -1), blockPosSet, mutableBoundingBoxIn);
      this.placeAir(worldIn, randomIn, blockPos.offset(-1, 0, +1), blockPosSet, mutableBoundingBoxIn);
      this.placeAir(worldIn, randomIn, blockPos.offset(-1, 0, -1), blockPosSet, mutableBoundingBoxIn);
    }

    //Drippers
    // stuck with only one block down because of leaf decay distance
    blockPos = blockPos.below();
    this.setLeaf(worldIn, randomIn, blockPos.offset(+3, 0, 0), blockPosSet, mutableBoundingBoxIn, treeFeatureConfigIn);
    this.setLeaf(worldIn, randomIn, blockPos.offset(-3, 0, 0), blockPosSet, mutableBoundingBoxIn, treeFeatureConfigIn);
    this.setLeaf(worldIn, randomIn, blockPos.offset(0, 0, -3), blockPosSet, mutableBoundingBoxIn, treeFeatureConfigIn);
    this.setLeaf(worldIn, randomIn, blockPos.offset(0, 0, +3), blockPosSet, mutableBoundingBoxIn, treeFeatureConfigIn);

    if (!treeFeatureConfigIn.hasVines) {
      this.setLeaf(worldIn, randomIn, blockPos.offset(+1, 0, +1), blockPosSet, mutableBoundingBoxIn, treeFeatureConfigIn);
      this.setLeaf(worldIn, randomIn, blockPos.offset(-3, 0, 0), blockPosSet, mutableBoundingBoxIn, treeFeatureConfigIn);
      this.setLeaf(worldIn, randomIn, blockPos.offset(-1, 0, +1), blockPosSet, mutableBoundingBoxIn, treeFeatureConfigIn);
      this.setLeaf(worldIn, randomIn, blockPos.offset(-1, 0, -1), blockPosSet, mutableBoundingBoxIn, treeFeatureConfigIn);
    }

    if (treeFeatureConfigIn.hasVines) {
      blockPos = blockPos.below();
      this.placeVine(worldIn, randomIn, blockPos.offset(+3, 0, 0), blockPosSet, mutableBoundingBoxIn,
        this.getRandomizedVine(randomIn, blockPos, treeFeatureConfigIn).setValue(VineBlock.UP, true));

      this.placeVine(worldIn, randomIn, blockPos.offset(-3, 0, 0), blockPosSet, mutableBoundingBoxIn,
        this.getRandomizedVine(randomIn, blockPos, treeFeatureConfigIn).setValue(VineBlock.UP, true));

      this.placeVine(worldIn, randomIn, blockPos.offset(0, 0, -3), blockPosSet, mutableBoundingBoxIn,
        this.getRandomizedVine(randomIn, blockPos, treeFeatureConfigIn).setValue(VineBlock.UP, true));

      this.placeVine(worldIn, randomIn, blockPos.offset(0, 0, +3), blockPosSet, mutableBoundingBoxIn,
        this.getRandomizedVine(randomIn, blockPos, treeFeatureConfigIn).setValue(VineBlock.UP, true));

      BlockState randomVine = this.getRandomizedVine(randomIn, blockPos, treeFeatureConfigIn);
      this.placeVine(worldIn, randomIn, blockPos.offset(+2, 1, +2), blockPosSet, mutableBoundingBoxIn,
        randomVine.setValue(VineBlock.UP, true));
      this.placeVine(worldIn, randomIn, blockPos.offset(+2, 0, +2), blockPosSet, mutableBoundingBoxIn,
        randomVine);

      randomVine = this.getRandomizedVine(randomIn, blockPos, treeFeatureConfigIn);
      this.placeVine(worldIn, randomIn, blockPos.offset(+2, 1, -2), blockPosSet, mutableBoundingBoxIn,
        randomVine.setValue(VineBlock.UP, true));
      this.placeVine(worldIn, randomIn, blockPos.offset(+2, 0, -2), blockPosSet, mutableBoundingBoxIn,
        randomVine);

      randomVine = this.getRandomizedVine(randomIn, blockPos, treeFeatureConfigIn);
      this.placeVine(worldIn, randomIn, blockPos.offset(-2, 1, +2), blockPosSet, mutableBoundingBoxIn,
        randomVine.setValue(VineBlock.UP, true));
      this.placeVine(worldIn, randomIn, blockPos.offset(-2, 0, +2), blockPosSet, mutableBoundingBoxIn,
        randomVine);

      randomVine = this.getRandomizedVine(randomIn, blockPos, treeFeatureConfigIn);
      this.placeVine(worldIn, randomIn, blockPos.offset(-2, 1, -2), blockPosSet, mutableBoundingBoxIn,
        randomVine.setValue(VineBlock.UP, true));
      this.placeVine(worldIn, randomIn, blockPos.offset(-2, 0, -2), blockPosSet, mutableBoundingBoxIn,
        randomVine);
    }
  }

  private void placeDiamondLayer(LevelSimulatedRW worldIn, Random randomIn, int range, BlockPos blockPos, Set<BlockPos> blockPosSet, BoundingBox mutableBoundingBoxIn, BaseSlimeTreeFeatureConfig treeFeatureConfigIn) {
    for (int x = -range; x <= range; x++) {
      for (int z = -range; z <= range; z++) {
        if (Math.abs(x) + Math.abs(z) <= range) {
          BlockPos blockpos = blockPos.offset(x, 0, z);
          this.setLeaf(worldIn, randomIn, blockpos, blockPosSet, mutableBoundingBoxIn, treeFeatureConfigIn);
        }
      }
    }
  }

  protected boolean setLog(LevelSimulatedRW worldIn, Random randomIn, BlockPos blockPos, Set<BlockPos> blockPosSet, BoundingBox mutableBoundingBoxIn, BaseSlimeTreeFeatureConfig treeFeatureConfigIn) {
    if (!isAirOrLeavesAt(worldIn, blockPos)) {
      return false;
    }
    else {
      this.setBlock(worldIn, blockPos, treeFeatureConfigIn.trunkProvider.getState(randomIn, blockPos));
      mutableBoundingBoxIn.expand(new BoundingBox(blockPos, blockPos));
      blockPosSet.add(blockPos.immutable());
      return true;
    }
  }

  protected boolean placeAir(LevelSimulatedRW worldIn, Random randomIn, BlockPos blockPos, Set<BlockPos> blockPosSet, BoundingBox mutableBoundingBoxIn) {
    if (!isAirOrLeavesAt(worldIn, blockPos)) {
      return false;
    }
    else {
      this.setBlock(worldIn, blockPos, Blocks.AIR.defaultBlockState());
      mutableBoundingBoxIn.expand(new BoundingBox(blockPos, blockPos));
      blockPosSet.add(blockPos.immutable());
      return true;
    }
  }

  protected boolean setLeaf(LevelSimulatedRW worldIn, Random random, BlockPos blockPos, Set<BlockPos> blockPosSet, BoundingBox mutableBoundingBoxIn, BaseSlimeTreeFeatureConfig treeFeatureConfigIn) {
    if (!isAirOrLeavesAt(worldIn, blockPos)) {
      return false;
    }
    else {
      this.setBlock(worldIn, blockPos, treeFeatureConfigIn.leavesProvider.getState(random, blockPos));
      mutableBoundingBoxIn.expand(new BoundingBox(blockPos, blockPos));
      blockPosSet.add(blockPos.immutable());
      return true;
    }
  }

  protected boolean placeVine(LevelSimulatedRW worldIn, Random random, BlockPos blockPos, Set<BlockPos> blockPosSet, BoundingBox mutableBoundingBoxIn, BlockState vineState) {
    if (!isAirOrLeavesAt(worldIn, blockPos)) {
      return false;
    }
    else {
      this.setBlock(worldIn, blockPos, vineState);
      mutableBoundingBoxIn.expand(new BoundingBox(blockPos, blockPos));
      blockPosSet.add(blockPos.immutable());
      return true;
    }
  }

  private BlockState getRandomizedVine(Random random, BlockPos blockPos, BaseSlimeTreeFeatureConfig config) {
    BlockState state = config.vinesProvider.getState(random, blockPos);

    BooleanProperty[] sides = new BooleanProperty[] { VineBlock.NORTH, VineBlock.EAST, VineBlock.SOUTH, VineBlock.WEST };

    for (BooleanProperty side : sides) {
      state = state.setValue(side, false);
    }

    for (int i = random.nextInt(3) + 1; i > 0; i--) {
      state = state.setValue(sides[random.nextInt(sides.length)], true);
    }

    return state;
  }

  public static boolean isEmptyOrLogAt(LevelSimulatedReader reader, BlockPos blockPos) {
    return isReplaceableAt(reader, blockPos) || reader.isStateAtPosition(blockPos, (p_236417_0_) -> p_236417_0_.is(BlockTags.LOGS));
  }

  private static boolean isVineAt(LevelSimulatedReader reader, BlockPos blockPos) {
    return reader.isStateAtPosition(blockPos, (p_236415_0_) -> p_236415_0_.is(Blocks.VINE));
  }

  private static boolean isWaterAt(LevelSimulatedReader reader, BlockPos blockPos) {
    return reader.isStateAtPosition(blockPos, (p_236413_0_) -> p_236413_0_.is(Blocks.WATER));
  }

  public static boolean isAirOrLeavesAt(LevelSimulatedReader reader, BlockPos blockPos) {
    return reader.isStateAtPosition(blockPos, (p_236411_0_) -> p_236411_0_.isAir() || p_236411_0_.is(BlockTags.LEAVES));
  }

  private static boolean isDirtOrFarmlandAt(LevelSimulatedReader p_236418_0_, BlockPos blockPos) {
    return p_236418_0_.isStateAtPosition(blockPos, (p_236409_0_) -> {
      Block block = p_236409_0_.getBlock();
      return (TinkerWorld.slimeDirt.contains(block) || TinkerWorld.vanillaSlimeGrass.contains(block) || TinkerWorld.earthSlimeGrass.contains(block) || TinkerWorld.skySlimeGrass.contains(block) || TinkerWorld.enderSlimeGrass.contains(block) || TinkerWorld.ichorSlimeGrass.contains(block));
    });
  }

  private static boolean isTallPlantAt(LevelSimulatedReader reader, BlockPos blockPos) {
    return reader.isStateAtPosition(blockPos, (p_236406_0_) -> {
      Material material = p_236406_0_.getMaterial();
      return material == Material.REPLACEABLE_PLANT;
    });
  }

  public static boolean isReplaceableAt(LevelSimulatedReader reader, BlockPos blockPos) {
    return isAirOrLeavesAt(reader, blockPos) || isTallPlantAt(reader, blockPos) || isWaterAt(reader, blockPos);
  }

  public static void setBlockStateAt(LevelWriter writer, BlockPos blockPos, BlockState state) {
    writer.setBlock(blockPos, state, 19);
  }

  private DiscreteVoxelShape func_236403_a_(LevelAccessor world, BoundingBox boundingBox, Set<BlockPos> logs, Set<BlockPos> leaves) {
    List<Set<BlockPos>> list = Lists.newArrayList();
    DiscreteVoxelShape voxelshapepart = new BitSetDiscreteVoxelShape(boundingBox.getXSpan(), boundingBox.getYSpan(), boundingBox.getZSpan());
    int i = 6;

    for (int j = 0; j < 6; ++j) {
      list.add(Sets.newHashSet());
    }

    BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

    for (BlockPos blockpos : Lists.newArrayList(leaves)) {
      if (boundingBox.isInside(blockpos)) {
        voxelshapepart.setFull(blockpos.getX() - boundingBox.x0, blockpos.getY() - boundingBox.y0, blockpos.getZ() - boundingBox.z0, true, true);
      }
    }

    for (BlockPos blockpos1 : Lists.newArrayList(logs)) {
      if (boundingBox.isInside(blockpos1)) {
        voxelshapepart.setFull(blockpos1.getX() - boundingBox.x0, blockpos1.getY() - boundingBox.y0, blockpos1.getZ() - boundingBox.z0, true, true);
      }

      for (Direction direction : Direction.values()) {
        mutable.setWithOffset(blockpos1, direction);
        if (!logs.contains(mutable)) {
          BlockState blockstate = world.getBlockState(mutable);
          if (blockstate.hasProperty(BlockStateProperties.DISTANCE)) {
            list.get(0).add(mutable.immutable());
            setBlockStateAt(world, mutable, blockstate.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(1)));
            if (boundingBox.isInside(mutable)) {
              voxelshapepart.setFull(mutable.getX() - boundingBox.x0, mutable.getY() - boundingBox.y0, mutable.getZ() - boundingBox.z0, true, true);
            }
          }
        }
      }
    }

    for (int l = 1; l < 6; ++l) {
      Set<BlockPos> set = list.get(l - 1);
      Set<BlockPos> set1 = list.get(l);

      for (BlockPos blockpos2 : set) {
        if (boundingBox.isInside(blockpos2)) {
          voxelshapepart.setFull(blockpos2.getX() - boundingBox.x0, blockpos2.getY() - boundingBox.y0, blockpos2.getZ() - boundingBox.z0, true, true);
        }

        for (Direction direction1 : Direction.values()) {
          mutable.setWithOffset(blockpos2, direction1);
          if (!set.contains(mutable) && !set1.contains(mutable)) {
            BlockState blockstate1 = world.getBlockState(mutable);
            if (blockstate1.hasProperty(BlockStateProperties.DISTANCE)) {
              int k = blockstate1.getValue(BlockStateProperties.DISTANCE);
              if (k > l + 1) {
                BlockState blockstate2 = blockstate1.setValue(BlockStateProperties.DISTANCE, Integer.valueOf(l + 1));
                setBlockStateAt(world, mutable, blockstate2);
                if (boundingBox.isInside(mutable)) {
                  voxelshapepart.setFull(mutable.getX() - boundingBox.x0, mutable.getY() - boundingBox.y0, mutable.getZ() - boundingBox.z0, true, true);
                }

                set1.add(mutable.immutable());
              }
            }
          }
        }
      }
    }

    return voxelshapepart;
  }
}
