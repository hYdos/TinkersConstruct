package slimeknights.tconstruct.tables.tileentity.chest;

import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.tables.TinkerTables;

public class CastChestTileEntity extends TinkerChestTileEntity {
  public CastChestTileEntity() {
    super(TinkerTables.castChestTile.get(), Util.makeTranslationKey("gui", "cast_chest"), DEFAULT_MAX, 4);
  }

  @Override
  public boolean canPlaceItem(int slot, ItemStack itemstack) {
    // check if there is no other slot containing that item
    for (int i = 0; i < this.getContainerSize(); i++) {
      if (ItemStack.isSame(itemstack, this.getItem(i))) {
        return i == slot;
      }
    }
    return TinkerTags.Items.GOLD_CASTS.contains(itemstack.getItem());
  }
}
