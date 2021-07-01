package slimeknights.tconstruct.smeltery.block;

import slimeknights.tconstruct.smeltery.tileentity.CastingTileEntity;

import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CastingBasinBlock extends AbstractCastingBlock {

  private static final VoxelShape SHAPE = Shapes.join(
    Shapes.block(),
    Shapes.or(
      Block.box(0.0D, 0.0D, 5.0D, 16.0D, 2.0D, 11.0D),
      Block.box(5.0D, 0.0D, 0.0D, 11.0D, 2.0D, 16.0D),
      Block.box(2.0D, 0.0D, 3.0D, 14.0D, 3.0D, 14.0D),
      Block.box(7.0D, 5.0D, 0.0D, 9.0D, 13.0D, 16.0D),
      Block.box(0.0D, 5.0D, 7.0D, 16.0D, 13.0D, 9.0D),
      Block.box(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D)),
    BooleanOp.ONLY_FIRST);

  public CastingBasinBlock(Properties builder) {
    super(builder);
  }

  @Deprecated
  @Override
  public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
    return SHAPE;
  }

  @Nonnull
  @Override
  public BlockEntity createTileEntity(BlockState blockState, BlockGetter iBlockReader) {
    return new CastingTileEntity.Basin();
  }
}
