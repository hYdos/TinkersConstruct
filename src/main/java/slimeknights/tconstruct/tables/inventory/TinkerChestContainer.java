package slimeknights.tconstruct.tables.inventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.tileentity.chest.TinkerChestTileEntity;

import javax.annotation.Nullable;

public class TinkerChestContainer extends BaseStationContainer<TinkerChestTileEntity> {
  protected SideInventoryContainer<TinkerChestTileEntity> inventory;
  public TinkerChestContainer(int id, Inventory inv, @Nullable TinkerChestTileEntity tileEntity) {
    super(TinkerTables.tinkerChestContainer.get(), id, inv, tileEntity);
    // columns don't matter since they get set by gui
    if (this.tile != null) {
      this.inventory = new DynamicChestInventory(TinkerTables.tinkerChestContainer.get(), this.containerId, inv, this.tile, 8, 18, 8);
      this.addSubContainer(inventory, true);
    }
    this.addInventorySlots();
  }

  public TinkerChestContainer(int id, Inventory inv, FriendlyByteBuf buf) {
    this(id, inv, getTileEntityFromBuf(buf, TinkerChestTileEntity.class));
  }

  /** Resizable inventory */
  public static class DynamicChestInventory extends SideInventoryContainer<TinkerChestTileEntity> {
    public DynamicChestInventory(MenuType<?> containerType, int windowId, Inventory inv, TinkerChestTileEntity tile, int x, int y, int columns) {
      super(containerType, windowId, inv, tile, x, y, columns);
      // add the theoretically possible slots
      while (this.slots.size() < tile.getMaxInventory()) {
        this.addSlot(this.createSlot(new EmptyHandler(), this.slots.size(), 0, 0));
      }
    }

    @Override
    protected Slot createSlot(IItemHandler inventory, int index, int x, int y) {
      if (this.tile == null) {
        return super.createSlot(inventory, index, x, y);
      }
      return new ChestSlot(this.tile, index, x, y);
    }
  }

  /** Slot to filter chest contents */
  public static class ChestSlot extends Slot {
    public final TinkerChestTileEntity chest;
    public ChestSlot(TinkerChestTileEntity tileEntity, int index, int xPosition, int yPosition) {
      super(tileEntity, index, xPosition, yPosition);
      this.chest = tileEntity;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
      return this.chest.canPlaceItem(this.getSlotIndex(), stack);
    }
  }
}
