package slimeknights.tconstruct.smeltery.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.util.TileEntityHelper;
import slimeknights.tconstruct.smeltery.tileentity.FaucetTileEntity;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Optional;
import java.util.Random;

public class FaucetBlock extends Block {
  public static final DirectionProperty FACING = BlockStateProperties.FACING_HOPPER;
  private static final EnumMap<Direction,VoxelShape> SHAPES = Maps.newEnumMap(ImmutableMap.of(
    Direction.DOWN,  Shapes.join(box( 4, 10,  4, 12, 16, 12), box( 6, 10,  6, 10, 16, 10), BooleanOp.ONLY_FIRST),
    Direction.NORTH, Shapes.join(box( 4,  4, 10, 12, 10, 16), box( 6,  6, 10, 10, 10, 16), BooleanOp.ONLY_FIRST),
    Direction.SOUTH, Shapes.join(box( 4,  4,  0, 12, 10,  6), box( 6,  6,  0, 10, 10,  6), BooleanOp.ONLY_FIRST),
    Direction.WEST,  Shapes.join(box(10,  4,  4, 16, 10, 12), box(10,  6,  6, 16, 10, 10), BooleanOp.ONLY_FIRST),
    Direction.EAST,  Shapes.join(box( 0,  4,  4,  6, 10, 12), box( 0,  6,  6,  6, 10, 10), BooleanOp.ONLY_FIRST)));

  public FaucetBlock(Properties properties) {
    super(properties);
    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
  }

  /* Blockstate */

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }

  @Nullable
  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    Direction dir = context.getClickedFace();
    if (dir == Direction.UP) {
      dir = Direction.DOWN;
    }
    return this.defaultBlockState().setValue(FACING, dir);
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
    return SHAPES.get(state.getValue(FACING));
  }


  /* Tile entity */

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Override
  public BlockEntity createTileEntity(BlockState state, BlockGetter world) {
    return new FaucetTileEntity();
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
    if (player.isShiftKeyDown()) {
      return InteractionResult.PASS;
    }
    getFaucet(worldIn, pos).ifPresent(FaucetTileEntity::activate);
    return InteractionResult.SUCCESS;
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
    if (worldIn.isClientSide()) {
      return;
    }
    getFaucet(worldIn, pos).ifPresent(faucet -> {
      faucet.neighborChanged(fromPos);
      faucet.handleRedstone(worldIn.hasNeighborSignal(pos));
    });
  }

  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand) {
    getFaucet(worldIn, pos).ifPresent(FaucetTileEntity::activate);
  }

  /**
   * Gets the facuet tile entity at the given position
   * @param world  World instance
   * @param pos    Faucet position
   * @return  Optional of faucet, empty if missing or wrong type
   */
  private Optional<FaucetTileEntity> getFaucet(Level world, BlockPos pos) {
    return TileEntityHelper.getTile(FaucetTileEntity.class, world, pos);
  }

  /* Display */

  /**
   * Adds particles to the faucet
   * @param state    Faucet state
   * @param worldIn  World instance
   * @param pos      Faucet position
   */
  private static void addParticles(BlockState state, LevelAccessor worldIn, BlockPos pos) {
    Direction direction = state.getValue(FACING);
    double x = (double)pos.getX() + 0.5D - 0.3D * (double)direction.getStepX();
    double y = (double)pos.getY() + 0.5D - 0.3D * (double)direction.getStepY();
    double z = (double)pos.getZ() + 0.5D - 0.3D * (double)direction.getStepZ();
    worldIn.addParticle(new DustParticleOptions(1.0F, 0.0F, 0.0F, 0.5f), x, y, z, 0.0D, 0.0D, 0.0D);
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
    getFaucet(worldIn, pos).ifPresent(faucet -> {
      if (faucet.isPouring() && faucet.getRenderFluid().isEmpty() && rand.nextFloat() < 0.25F) {
        addParticles(stateIn, worldIn, pos);
      }
    });
  }
}
