package slimeknights.tconstruct.tables.client.inventory.module;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import slimeknights.mantle.client.screen.ElementScreen;
import slimeknights.mantle.client.screen.ModuleScreen;
import slimeknights.mantle.client.screen.MultiModuleScreen;
import slimeknights.mantle.client.screen.ScalableElementScreen;
import slimeknights.mantle.client.screen.SliderWidget;
import slimeknights.mantle.inventory.BaseContainer;
import slimeknights.tconstruct.library.Util;

public class SideInventoryScreen<P extends MultiModuleScreen<?>, C extends AbstractContainerMenu> extends ModuleScreen<P,C> {

  protected ScalableElementScreen overlap = GenericScreen.overlap;
  protected ElementScreen overlapTopLeft = GenericScreen.overlapTopLeft;
  protected ElementScreen overlapTopRight = GenericScreen.overlapTopRight;
  protected ElementScreen overlapBottomLeft = GenericScreen.overlapBottomLeft;
  protected ElementScreen overlapBottomRight = GenericScreen.overlapBottomRight;
  protected ElementScreen overlapTop = new ElementScreen(7, 0, 7, 7, 64, 64); // same as borderTop but only 7 wide

  protected ScalableElementScreen textBackground = GenericScreen.textBackground;

  protected ScalableElementScreen slot = GenericScreen.slot;
  protected ScalableElementScreen slotEmpty = GenericScreen.slotEmpty;

  protected ElementScreen sliderNormal = GenericScreen.sliderNormal;
  protected ElementScreen sliderLow = GenericScreen.sliderLow;
  protected ElementScreen sliderHigh = GenericScreen.sliderHigh;
  protected ElementScreen sliderTop = GenericScreen.sliderTop;
  protected ElementScreen sliderBottom = GenericScreen.sliderBottom;
  protected ScalableElementScreen sliderBackground = GenericScreen.sliderBackground;

  protected static final ResourceLocation GENERIC_INVENTORY = Util.getResource("textures/gui/generic.png");

  protected BorderWidget border = new BorderWidget();

  protected int columns;
  @Getter
  protected int slotCount;

  protected int firstSlotId;
  protected int lastSlotId;

  protected int yOffset;
  protected int xOffset;
  protected boolean connected;

  protected SliderWidget slider = new SliderWidget(sliderNormal, sliderHigh, sliderLow, sliderTop, sliderBottom, sliderBackground);

  public SideInventoryScreen(P parent, C container, Inventory playerInventory, Component title, int slotCount, int columns) {
    this(parent, container, playerInventory, title, slotCount, columns, false, false);
  }

  public SideInventoryScreen(P parent, C container, Inventory playerInventory, Component title, int slotCount, int columns, boolean rightSide, boolean connected) {
    super(parent, container, playerInventory, title, rightSide, false);

    this.connected = connected;

    this.columns = columns;
    this.slotCount = slotCount;

    this.imageWidth = columns * this.slot.w + this.border.w * 2;
    this.imageHeight = this.calcCappedYSize(this.slot.h * 10);

    if (connected) {
      if (this.right) {
        this.border.cornerTopLeft = this.overlapTopLeft;
        this.border.borderLeft = this.overlap;
        this.border.cornerBottomLeft = this.overlapBottomLeft;
      }
      else {
        this.border.cornerTopRight = this.overlapTopRight;
        this.border.borderRight = this.overlap;
        this.border.cornerBottomRight = this.overlapBottomRight;
      }
    }

    this.yOffset = 0;

    this.updateSlots();
  }

  protected boolean shouldDrawName() {
    return this.menu instanceof BaseContainer;
  }

  @Override
  public boolean shouldDrawSlot(Slot slot) {
    if (slot.getSlotIndex() >= this.slotCount) {
      return false;
    }

    // all visible
    if (!this.slider.isEnabled()) {
      return true;
    }

    return this.firstSlotId <= slot.getSlotIndex() && this.lastSlotId > slot.getSlotIndex();
  }

  @Override
  public boolean isHovering(Slot slotIn, double mouseX, double mouseY) {
    return super.isHovering(slotIn, mouseX, mouseY) && this.shouldDrawSlot(slotIn);
  }

  public void updateSlotCount(int newSlotCount) {
    // don't do extra stuff if it's not needed
    if (this.slotCount == newSlotCount) {
      return;
    }

    this.slotCount = newSlotCount;
    // called twice to get correct slider calculation
    this.updatePosition(this.parent.cornerX, this.parent.cornerY, this.parent.realWidth, this.parent.realHeight);
    this.updatePosition(this.parent.cornerX, this.parent.cornerY, this.parent.realWidth, this.parent.realHeight);
  }

  @Override
  public void updatePosition(int parentX, int parentY, int parentSizeX, int parentSizeY) {
    // at most as big as the parent
    this.imageHeight = this.calcCappedYSize(parentSizeY - 10);
    // slider needed?
    if (this.getDisplayedRows() < this.getTotalRows()) {
      this.slider.enable();
      this.imageWidth = this.columns * this.slot.w + this.slider.width + 2 * this.border.w;
    }
    else {
      this.slider.disable();
      this.imageWidth = this.columns * this.slot.w + this.border.w * 2;
    }

    // update position
    super.updatePosition(parentX, parentY, parentSizeX, parentSizeY);

    // connected needs to move to the side
    if (this.connected) {
      if (this.yOffset == 0) {
        if (this.right) {
          this.border.cornerTopLeft = this.overlapTop;
        }
        else {
          this.border.cornerTopRight = this.overlapTop;
        }
      }

      this.xOffset = (this.border.w - 1) * (this.right ? -1 : 1);
      this.leftPos += this.xOffset;
    }
    else {
      this.xOffset = 0;
    }

    // move it a bit
    this.topPos += this.yOffset;

    this.border.setPosition(this.leftPos, this.topPos);
    this.border.setSize(this.imageWidth, this.imageHeight);

    int y = this.topPos + this.border.h;
    int h = this.imageHeight - this.border.h * 2;

    if (this.shouldDrawName()) {
      y += this.textBackground.h;
      h -= this.textBackground.h;
    }

    this.slider.setPosition(this.leftPos + this.columns * this.slot.w + this.border.w, y);
    this.slider.setSize(h);
    this.slider.setSliderParameters(0, this.getTotalRows() - this.getDisplayedRows(), 1);

    this.updateSlots();
  }

  private int getDisplayedRows() {
    return slider.height / slot.h;
  }

  private int getTotalRows() {
    int total = this.slotCount / this.columns;

    if (this.slotCount % this.columns != 0) {
      total++;
    }

    return total;
  }

  private int calcCappedYSize(int max) {
    int h = this.slot.h * this.getTotalRows();

    h = this.border.getHeightWithBorder(h);

    if (this.shouldDrawName()) {
      h += this.textBackground.h;
    }

    // not higher than the max
    while (h > max) {
      h -= this.slot.h;
    }

    return h;
  }

  // updates slot visibility
  protected void updateSlots() {
    this.firstSlotId = this.slider.getValue() * this.columns;
    this.lastSlotId = Math.min(this.slotCount, this.firstSlotId + getDisplayedRows() * this.columns);

    int xd = this.border.w + this.xOffset;
    int yd = this.border.h + this.yOffset;

    if (shouldDrawName()) {
      yd += this.textBackground.h;
    }

    for (Slot slot : this.menu.slots) {
      if (this.shouldDrawSlot(slot)) {
        // calc position of the slot
        int offset = slot.getSlotIndex() - this.firstSlotId;
        int x = (offset % this.columns) * this.slot.w;
        int y = (offset / this.columns) * this.slot.h;

        slot.x = xd + x + 1;
        slot.y = yd + y + 1;

        if (this.right) {
          slot.x += this.parent.realWidth;
        }
        else {
          slot.x -= this.imageWidth;
        }
      }
      else {
        slot.x = 0;
        slot.y = 0;
      }
    }
  }

  @Override
  public void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
    if (this.shouldDrawName()) {
      this.font.draw(matrices, this.getTitle().getString(), this.border.w, this.border.h - 1, 0x404040);
    }
  }

  @Override
  protected void renderBg(PoseStack matrices, float partialTicks, int mouseX, int mouseY) {
    this.leftPos += this.border.w;
    this.topPos += this.border.h;

    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    this.minecraft.getTextureManager().bind(GENERIC_INVENTORY);

    int x = this.leftPos;
    int y = this.topPos;
    int midW = this.imageWidth - this.border.w * 2;

    this.border.draw(matrices);

    if (this.shouldDrawName()) {
      this.textBackground.drawScaledX(matrices, x, y, midW);
      y += this.textBackground.h;
    }

    this.minecraft.getTextureManager().bind(GENERIC_INVENTORY);
    this.drawSlots(matrices, x, y);

    // slider
    if (this.slider.isEnabled()) {
      this.slider.update(mouseX, mouseY);
      this.slider.draw(matrices);

      this.updateSlots();
    }

    this.leftPos -= this.border.w;
    this.topPos -= this.border.h;
  }

  protected int drawSlots(PoseStack matrices, int xPos, int yPos) {
    int width = this.columns * this.slot.w;
    int height = this.imageHeight - this.border.h * 2;
    int fullRows = (this.lastSlotId - this.firstSlotId) / this.columns;
    int y;

    for (y = 0; y < fullRows * this.slot.h && y < height; y += this.slot.h) {
      this.slot.drawScaledX(matrices, xPos, yPos + y, width);
    }

    // draw partial row and unused slots
    int slotsLeft = (this.lastSlotId - this.firstSlotId) % this.columns;

    if (slotsLeft > 0) {
      this.slot.drawScaledX(matrices, xPos, yPos + y, slotsLeft * this.slot.w);
      // empty slots that don't exist
      this.slotEmpty.drawScaledX(matrices, xPos + slotsLeft * this.slot.w, yPos + y, width - slotsLeft * this.slot.w);
    }

    return width;
  }

  @Override
  public boolean handleMouseClicked(double mouseX, double mouseY, int mouseButton) {
    if (mouseButton == 0 && this.slider.isEnabled()) {
      this.slider.handleMouseClicked((int) mouseX, (int) mouseY, mouseButton);
    }

    return super.handleMouseClicked(mouseX, mouseY, mouseButton);
  }

  @Override
  public boolean handleMouseReleased(double mouseX, double mouseY, int state) {
    if (this.slider.isEnabled()) {
      this.slider.handleMouseReleased();
    }

    return super.handleMouseReleased(mouseX, mouseY, state);
  }

  @Override
  public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollData) {
    if (!this.slider.isEnabled()) {
      return super.handleMouseScrolled(mouseX, mouseY, scrollData);
    }

    return this.slider.mouseScrolled(scrollData, !this.isMouseOverFullSlot(mouseX, mouseY) && this.isMouseInModule((int) mouseX, (int) mouseY));
  }
}
