package slimeknights.tconstruct.smeltery.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Constants.BlockFlags;
import slimeknights.tconstruct.common.multiblock.IMasterLogic;
import slimeknights.tconstruct.common.multiblock.IServantLogic;
import slimeknights.tconstruct.common.multiblock.ServantTileEntity;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.component.SearedBlock;

import javax.annotation.Nullable;

/** Mostly extended to make type validaton easier, and the servant base class is not registered */
public class SmelteryComponentTileEntity extends ServantTileEntity {

  public SmelteryComponentTileEntity() {
    this(TinkerSmeltery.smelteryComponent.get());
  }

  protected SmelteryComponentTileEntity(BlockEntityType<?> tileEntityTypeIn) {
    super(tileEntityTypeIn);
  }

  @Override
  protected void setMaster(@Nullable BlockPos master, @Nullable Block block) {
    // update the master
    super.setMaster(master, block);

    // update the active state
    if (level != null) {
      BlockState currentState = getBlockState();
      boolean hasMaster = getMasterPos() != null;
      if (currentState.hasProperty(SearedBlock.IN_STRUCTURE) && currentState.getValue(SearedBlock.IN_STRUCTURE) != hasMaster) {
        level.setBlock(worldPosition, getBlockState().setValue(SearedBlock.IN_STRUCTURE, hasMaster), BlockFlags.BLOCK_UPDATE);
      }
    }
  }

  /**
   * Block method to update neighbors of a smeltery component when a new one is placed
   * @param world  World instance
   * @param pos    Location of new smeltery component
   */
  public static void updateNeighbors(Level world, BlockPos pos, BlockState state) {
    for (Direction direction : Direction.values()) {
      // if the neighbor is a master, notify it we exist
      BlockEntity tileEntity = world.getBlockEntity(pos.relative(direction));
      if (tileEntity instanceof IMasterLogic) {
        BlockEntity servant = world.getBlockEntity(pos);
        if (servant instanceof IServantLogic) {
          ((IMasterLogic) tileEntity).notifyChange((IServantLogic) servant, pos, state);
          break;
        }
        // if the neighbor is a servant, notify its master we exist
      } else if (tileEntity instanceof SmelteryComponentTileEntity) {
        SmelteryComponentTileEntity componentTileEntity = (SmelteryComponentTileEntity) tileEntity;
        if (componentTileEntity.hasMaster()) {
          componentTileEntity.notifyMasterOfChange(pos, state);
          break;
        }
      }
    }
  }
}
