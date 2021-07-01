package slimeknights.tconstruct.smeltery.inventory;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import slimeknights.mantle.inventory.ItemHandlerSlot;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.utils.ValidZeroIntReference;
import slimeknights.tconstruct.shared.inventory.TriggeringBaseContainer;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.tileentity.AlloyerTileEntity;
import slimeknights.tconstruct.smeltery.tileentity.module.alloying.MixerAlloyTank;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class AlloyerContainer extends TriggeringBaseContainer<AlloyerTileEntity> {
  @Getter
  private boolean hasFuelSlot = false;
  public AlloyerContainer(int id, @Nullable Inventory inv, @Nullable AlloyerTileEntity alloyer) {
    super(TinkerSmeltery.alloyerContainer.get(), id, inv, alloyer);

    // create slots
    if (alloyer != null) {
      // refresh cache of neighboring tanks
      Level world = alloyer.getLevel();
      if (world != null && world.isClientSide) {
        MixerAlloyTank alloyTank = alloyer.getAlloyTank();
        for (Direction direction : Direction.values()) {
          if (direction != Direction.DOWN) {
            alloyTank.refresh(direction, true);
          }
        }
      }

      // add fuel slot if present
      BlockPos down = alloyer.getBlockPos().below();
      if (world != null && world.getBlockState(down).is(TinkerTags.Blocks.FUEL_TANKS)) {
        BlockEntity te = world.getBlockEntity(down);
        if (te != null) {
          hasFuelSlot = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).filter(handler -> {
            this.addSlot(new ItemHandlerSlot(handler, 0, 151, 32));
            return true;
          }).isPresent();
        }
      }

      this.addInventorySlots();

      // syncing
      Consumer<DataSlot> referenceConsumer = this::addDataSlot;
      ValidZeroIntReference.trackIntArray(referenceConsumer, alloyer.getFuelModule());
    }
  }

  public AlloyerContainer(int id, Inventory inv, FriendlyByteBuf buf) {
    this(id, inv, getTileEntityFromBuf(buf, AlloyerTileEntity.class));
  }
}
