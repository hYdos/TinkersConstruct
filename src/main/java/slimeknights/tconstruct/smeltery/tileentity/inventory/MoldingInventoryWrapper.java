package slimeknights.tconstruct.smeltery.tileentity.inventory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import slimeknights.tconstruct.library.recipe.molding.IMoldingInventory;

/** Wrapper around an item handler for the sake of use as a molding inventory */
@RequiredArgsConstructor
public class MoldingInventoryWrapper implements IMoldingInventory {
  private final IItemHandler handler;
  private final int slot;

  @Getter @Setter
  private ItemStack pattern = ItemStack.EMPTY;

  @Override
  public ItemStack getMaterial() {
    return handler.getStackInSlot(slot);
  }
}
