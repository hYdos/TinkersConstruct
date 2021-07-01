package slimeknights.tconstruct.tables.client.inventory.library;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntity;
import slimeknights.mantle.client.screen.MultiModuleScreen;
import slimeknights.mantle.inventory.BaseContainer;

public class ScalingChestScreen<T extends BlockEntity & Container> extends DynInventoryScreen {

  protected final Container inventory;

  public ScalingChestScreen(MultiModuleScreen<?> parent, BaseContainer<T> container, Inventory playerInventory, Component title) {
    super(parent, container, playerInventory, title);

    this.inventory = container.getTile();
    if (this.inventory != null)
      this.slotCount = this.inventory.getContainerSize();
    else
      this.slotCount = 0;
    this.sliderActive = true;
  }

  @Override
  public void updatePosition(int parentX, int parentY, int parentSizeX, int parentSizeY) {
    this.leftPos = parentX + this.xOffset;
    this.topPos = parentY + this.yOffset;

    // calculate rows and columns from space
    this.columns = (this.imageWidth - this.slider.width) / slot.w;
    this.rows = this.imageHeight / slot.h;

    this.updateSlider();
    this.updateSlots();
  }

  @Override
  protected void updateSlider() {
    this.sliderActive = this.slotCount > this.columns * this.rows;
    super.updateSlider();
    this.slider.setEnabled(this.sliderActive);
    this.slider.show();
  }

  @Override
  public void update(int mouseX, int mouseY) {
    if (this.inventory == null) {
      this.slotCount = 0;
    } else {
      this.slotCount = this.inventory.getContainerSize();
    }
    super.update(mouseX, mouseY);

    this.updateSlider();
    this.slider.show();
    this.updateSlots();
  }

  @Override
  public boolean shouldDrawSlot(Slot slot) {
    if (this.inventory == null) {
      return false;
    }

    if (slot.getSlotIndex() >= this.inventory.getContainerSize()) {
      return false;
    }

    return super.shouldDrawSlot(slot);
  }

  @Override
  protected void renderLabels(PoseStack matrixStack, int x, int y) {
  }

}
