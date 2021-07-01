package slimeknights.tconstruct.tables.tileentity.chest;

import slimeknights.mantle.tileentity.InventoryTileEntity;
import slimeknights.tconstruct.tables.inventory.TinkerChestContainer;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;

public abstract class TinkerChestTileEntity extends InventoryTileEntity {
  /** Default maximum size */
  protected static final int DEFAULT_MAX = 256;
  /** Current visual size of the inventory */
  private int actualSize = 1;

  public TinkerChestTileEntity(BlockEntityType<?> tileEntityTypeIn, String name) {
    this(tileEntityTypeIn, name, DEFAULT_MAX, 64);
  }

  public TinkerChestTileEntity(BlockEntityType<?> tileEntityTypeIn, String name, int inventorySize, int maxStackSize) {
    super(tileEntityTypeIn, new TranslatableComponent(name), inventorySize, maxStackSize);
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player playerEntity) {
    return new TinkerChestContainer(menuId, playerInventory, this);
  }

  @Override
  public abstract boolean canPlaceItem(int slot, ItemStack itemstack);

  @Override
  public int getContainerSize() {
    return actualSize;
  }

  /** Gets the maximum size for this inventory */
  public int getMaxInventory() {
    return super.getContainerSize();
  }

  @Override
  public void readInventoryFromNBT(CompoundTag tag) {
    // we need to set it to max because the loading code uses getSizeInventory and we want to load all stacks
    this.actualSize = getMaxInventory();
    super.readInventoryFromNBT(tag);

    // recalculate actual size from inventory:
    // decrease until it matches
    while (this.actualSize > 0 && this.getItem(this.actualSize - 1).isEmpty()) {
      this.actualSize--;
    }

    this.actualSize++; // add empty slot
  }

  @Override
  public void setItem(int slot, ItemStack itemstack) {
    // adjustment from possible external stuff (looking at you there, hoppers >:()
    if (slot > this.actualSize && !itemstack.isEmpty()) {
      this.actualSize = slot + 1;
    }

    // non-empty and gets put into the last slot?
    if (slot == this.actualSize - 1 && !itemstack.isEmpty()) {
      // expand slots until the last visible slot is empty (could be something was in there through faulty state)
      do {
        this.actualSize++;
      } while (!this.getItem(this.actualSize - 1).isEmpty());
    }
    // empty, gets taken from the slot before the last visible slot?
    else if (slot >= this.actualSize - 2 && itemstack.isEmpty()) {
      // decrease inventory size so that 1 free slot after the last non-empty slot is left
      while (this.actualSize - 2 >= 0 && this.getItem(this.actualSize - 2).isEmpty()) {
        this.actualSize--;
      }
    }

    // actually put the thing in/out
    super.setItem(slot, itemstack);
  }
}
