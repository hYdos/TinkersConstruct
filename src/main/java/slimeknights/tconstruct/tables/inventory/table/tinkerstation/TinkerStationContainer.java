package slimeknights.tconstruct.tables.inventory.table.tinkerstation;

import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tools.ToolDefinition;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.inventory.BaseStationContainer;
import slimeknights.tconstruct.tables.inventory.table.LazyResultSlot;
import slimeknights.tconstruct.tables.tileentity.table.TinkerStationTileEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TinkerStationContainer extends BaseStationContainer<TinkerStationTileEntity> {
  @Getter
  private final List<Slot> inputSlots;
  private final LazyResultSlot resultSlot;

  /**
   * Standard constructor
   * @param id    Window ID
   * @param inv   Player inventory
   * @param tile  Relevant tile entity
   */
  public TinkerStationContainer(int id, Inventory inv, @Nullable TinkerStationTileEntity tile) {
    super(TinkerTables.tinkerStationContainer.get(), id, inv, tile);

    // unfortunately, nothing works with no tile
    if (tile != null) {
      // send the player the current recipe, as we only sync to open containers
      tile.syncRecipe(inv.player);


      inputSlots = new ArrayList<>();
      inputSlots.add(this.addSlot(new TinkerableSlot(tile, TinkerStationTileEntity.TINKER_SLOT, 0, 0)));

      int index;
      for (index = 0; index < tile.getContainerSize() - 1; index++) {
        inputSlots.add(this.addSlot(new TinkerStationInputSlot(tile, index + TinkerStationTileEntity.INPUT_SLOT, 0, 0)));
      }

      // add result slot, will fetch result cache
      this.addSlot(this.resultSlot = new LazyResultSlot(tile.getCraftingResult(), 124, 37));
    }
    else {
      // requirement for final variable
      this.resultSlot = null;
      this.inputSlots = Collections.emptyList();
    }

    this.addInventorySlots();
  }

  /**
   * Factory constructor
   * @param id   Window ID
   * @param inv  Player inventory
   * @param buf  Buffer for fetching tile
   */
  public TinkerStationContainer(int id, Inventory inv, FriendlyByteBuf buf) {
    this(id, inv, getTileEntityFromBuf(buf, TinkerStationTileEntity.class));
  }

  @Override
  protected int getInventoryYOffset() {
    return 92;
  }

  @Override
  public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
    return slot != this.resultSlot && super.canTakeItemForPickAll(stack, slot);
  }

  /**
   * Updates the active slots from the screen
   * @param activeSlots     Active slots
   * @param mainSlotHidden  If true, main slot is hidden
   * @param filter          Slot filter to apply, if null clear any filters
   */
  public void setToolSelection(int activeSlots, boolean mainSlotHidden, @Nullable ToolDefinition filter) {
    assert this.tile != null;

    if (activeSlots > this.tile.getContainerSize()) {
      activeSlots = this.tile.getContainerSize();
    }

    for (int i = 0; i < this.tile.getContainerSize(); i++) {
      Slot slot = this.slots.get(i);

      if (slot instanceof TinkerStationSlot) {
        // activate or deactivate the slots
        TinkerStationSlot slotToolPart = (TinkerStationSlot) slot;
        boolean isHidden = i == TinkerStationTileEntity.TINKER_SLOT ? mainSlotHidden : i > activeSlots;
        if (isHidden) {
          slotToolPart.deactivate();
        }
        else {
          slotToolPart.activate();
        }

        // update the filters
        if (slot instanceof TinkerStationInputSlot) {
          TinkerStationInputSlot inputSlot = (TinkerStationInputSlot) slot;
          Item filterItem = null;
          if (!isHidden && filter != null && i <= filter.getRequiredComponents().size()) {
            filterItem = filter.getRequiredComponents().get(i - 1).asItem();
          }
          inputSlot.setFilter(filterItem);
        }
      }
    }
  }

  public ItemStack getResult() {
    return this.resultSlot.getItem();
  }
}
