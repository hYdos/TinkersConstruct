package slimeknights.tconstruct.smeltery.block;

import slimeknights.mantle.util.TileEntityHelper;
import slimeknights.tconstruct.smeltery.tileentity.SmelteryTileEntity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Random;

public class SmelteryControllerBlock extends ControllerBlock {
  public SmelteryControllerBlock(Properties properties) {
    super(properties);
  }

  @Override
  public BlockEntity createTileEntity(BlockState blockState, BlockGetter iBlockReader) {
    return new SmelteryTileEntity();
  }

  @Override
  public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    // check structure
    TileEntityHelper.getTile(SmelteryTileEntity.class, worldIn, pos).ifPresent(SmelteryTileEntity::updateStructure);
  }

  @Override
  @Deprecated
  public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
    if (!newState.is(this)) {
      TileEntityHelper.getTile(SmelteryTileEntity.class, worldIn, pos).ifPresent(SmelteryTileEntity::invalidateStructure);
    }
    super.onRemove(state, worldIn, pos, newState, isMoving);
  }

  @Override
  public void animateTick(BlockState state, Level world, BlockPos pos, Random rand) {
    if (state.getValue(ACTIVE)) {
      double x = pos.getX() + 0.5D;
      double y = (double) pos.getY() + (rand.nextFloat() * 6F + 2F) / 16F;
      double z = pos.getZ() + 0.5D;
      double frontOffset = 0.52D;
      double sideOffset = rand.nextDouble() * 0.6D - 0.3D;
      spawnFireParticles(world, state, x, y, z, frontOffset, sideOffset);
    }
  }


  /* No rotation if in a structure  */

  @Deprecated
  @Override
  public BlockState rotate(BlockState state, Rotation rotation) {
    if (state.getValue(IN_STRUCTURE)) {
      return state;
    }
    return super.rotate(state, rotation);
  }

  @Deprecated
  @Override
  public BlockState mirror(BlockState state, Mirror mirror) {
    if (state.getValue(IN_STRUCTURE)) {
      return state;
    }
    return super.mirror(state, mirror);
  }
}
