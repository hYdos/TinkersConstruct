package slimeknights.tconstruct.world.block;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LayerLightEngine;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.world.TinkerWorld;

import javax.annotation.Nullable;
import java.util.Random;

public class SlimeGrassBlock extends SnowyDirtBlock implements BonemealableBlock {
  @Getter
  private final SlimeType foliageType;
  public SlimeGrassBlock(Properties properties, SlimeType foliageType) {
    super(properties);
    this.foliageType = foliageType;
  }

  /* Bonemeal interactions */

  @Override
  public boolean isValidBonemealTarget(BlockGetter world, BlockPos pos, BlockState state, boolean isClient) {
    return world.getBlockState(pos.above()).isAir(world, pos);
  }

  @Override
  public boolean isBonemealSuccess(Level worldIn, Random rand, BlockPos pos, BlockState state) {
    return true;
  }

  @Override
  public void performBonemeal(ServerLevel world, Random rand, BlockPos pos, BlockState state) {
    // based on vanilla logic, reimplemented to switch plant types
    BlockPos up = pos.above();
    mainLoop:
    for (int i = 0; i < 128; i++) {
      // locate target
      BlockPos target = up;
      for (int j = 0; j < i / 16; j++) {
        target = target.offset(rand.nextInt(3) - 1, (rand.nextInt(3) - 1) * rand.nextInt(3) / 2, rand.nextInt(3) - 1);
        if (!world.getBlockState(target.below()).is(TinkerTags.Blocks.SLIMY_GRASS) || world.getBlockState(target).isCollisionShapeFullBlock(world, pos)) {
          continue mainLoop;
        }
      }
      // grow the plants if empty
      if (world.isEmptyBlock(target)) {
        BlockState plantState;
        if (rand.nextInt(8) == 0) {
          plantState = TinkerWorld.slimeFern.get(this.foliageType).defaultBlockState();
        } else {
          plantState = TinkerWorld.slimeTallGrass.get(this.foliageType).defaultBlockState();
        }

        if (plantState.canSurvive(world, target)) {
          world.setBlock(target, plantState, 3);
        }
      }
    }
  }

  /* Spreading */

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void randomTick(BlockState state, ServerLevel world, BlockPos pos, Random random) {
    // based on vanilla logic, reimplemented to remove dirt hardcode
    // prevent loading unloaded chunks
    if (!world.isAreaLoaded(pos, 3)) return;

    // if this is no longer valid grass, destroy
    if (!isValidPos(state, world, pos)) {
      world.setBlockAndUpdate(pos, getDirtState(state));
    } else if (world.getMaxLocalRawBrightness(pos.above()) >= 9) {
      // otherwise, attempt spreading
      for (int i = 0; i < 4; ++i) {
        BlockPos newGrass = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
        BlockState newState = this.getStateFromDirt(world.getBlockState(newGrass));
        if (newState != null && canSpread(newState, world, newGrass)) {
          world.setBlockAndUpdate(newGrass, newState.setValue(SNOWY, world.getBlockState(newGrass.above()).is(Blocks.SNOW)));
        }
      }
    }
  }

  /** Checks if the position can be slime grass */
  private static boolean isValidPos(BlockState targetState, LevelReader world, BlockPos pos) {
    BlockPos above = pos.above();
    BlockState aboveState = world.getBlockState(above);
    // under snow is fine
    if (aboveState.is(Blocks.SNOW) && aboveState.getValue(SnowLayerBlock.LAYERS) == 1) {
      return true;
    }
    // under liquid is not fine
    if (aboveState.getFluidState().getAmount() == 8) {
      return false;
    }
    // fallback to light level check
    return LayerLightEngine.getLightBlockInto(world, targetState, pos, aboveState, above, Direction.UP, aboveState.getLightBlock(world, above)) < world.getMaxLightLevel();
  }

  /** Checks if the grass at the given position can spread */
  private static boolean canSpread(BlockState state, LevelReader world, BlockPos pos) {
    BlockPos above = pos.above();
    return isValidPos(state, world, pos) && !world.getFluidState(above).is(FluidTags.WATER);
  }


  /* Helpers */

  /**
   * Gets the dirt state for the given grass state
   * @param grassState  Grass state
   * @return Dirt state
   */
  public static BlockState getDirtState(BlockState grassState) {
    Block block = grassState.getBlock();
    for (SlimeType type : SlimeType.values()) {
      if (TinkerWorld.slimeGrass.get(type).contains(block)) {
        return TinkerWorld.allDirt.get(type).defaultBlockState();
      }
    }
    // includes vanilla slime grass
    return Blocks.DIRT.defaultBlockState();
  }

  /**
   * Gets the grass state for this plus the given dirt state
   * @param dirtState  dirt state
   * @return Grass state, null if cannot spread there
   */
  @Nullable
  private BlockState getStateFromDirt(BlockState dirtState) {
    Block block = dirtState.getBlock();
    for (SlimeType type : SlimeType.values()) {
      if (TinkerWorld.allDirt.get(type) == block) {
        return TinkerWorld.slimeGrass.get(type).get(this.foliageType).defaultBlockState();
      }
    }
    return null;
  }

  @Override
  public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
    if (this.foliageType != SlimeType.ICHOR) {
      super.fillItemCategory(group, items);
    }
  }
}
