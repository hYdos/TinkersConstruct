package slimeknights.tconstruct.tables.inventory.table.tinkerstation;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.tinkering.ITinkerStationDisplay;

/** Slot holding the tool in the tinker station */
public class TinkerableSlot extends TinkerStationSlot {
  public TinkerableSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
    super(inventoryIn, index, xPosition, yPosition);
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    // dormant slots don't take any items, they can only be taken out of
    if (this.isDormant()) {
      return false;
    }
    // TODO: should this use the tag as well?
    return stack != ItemStack.EMPTY && stack.getItem() instanceof ITinkerStationDisplay;
  }
}
