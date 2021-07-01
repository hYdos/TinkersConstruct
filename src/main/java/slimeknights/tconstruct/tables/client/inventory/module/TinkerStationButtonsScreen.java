package slimeknights.tconstruct.tables.client.inventory.module;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import slimeknights.tconstruct.library.client.Icons;
import slimeknights.tconstruct.tables.client.SlotInformationLoader;
import slimeknights.tconstruct.tables.client.inventory.SlotButtonItem;
import slimeknights.tconstruct.tables.client.inventory.library.slots.SlotInformation;
import slimeknights.tconstruct.tables.client.inventory.table.TinkerStationScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;

public class TinkerStationButtonsScreen extends SideButtonsScreen {

  protected final TinkerStationScreen parent;
  protected int selected = 0;
  private int style = 0;

  public static final int WOOD_STYLE = 2;
  public static final int METAL_STYLE = 1;

  public TinkerStationButtonsScreen(TinkerStationScreen parent, AbstractContainerMenu container, Inventory playerInventory, Component title) {
    super(parent, container, playerInventory, title, TinkerStationScreen.COLUMN_COUNT, false);

    this.parent = parent;
  }

  @Override
  public void updatePosition(int parentX, int parentY, int parentSizeX, int parentSizeY) {
    super.updatePosition(parentX, parentY, parentSizeX, parentSizeY);

    int index = 0;
    this.buttonCount = 0;

    Button.OnPress onPressed = button -> {
      for (AbstractWidget widget : TinkerStationButtonsScreen.this.buttons) {
        if (widget instanceof SlotButtonItem) {
          ((SlotButtonItem) widget).pressed = false;
        }
      }

      if (button instanceof SlotButtonItem) {
        SlotButtonItem slotInformationButton = (SlotButtonItem) button;

        slotInformationButton.pressed = true;

        TinkerStationButtonsScreen.this.selected = slotInformationButton.buttonId;

        TinkerStationButtonsScreen.this.parent.onToolSelection(slotInformationButton.data);
      }
    };

    for (SlotInformation slotInformation : SlotInformationLoader.getSlotInformationList()) {
      SlotButtonItem slotButtonItem = null;
      if (slotInformation.isRepair()) {
        // there are multiple repair slots, one for each relevant size
        if (slotInformation.getPoints().size() == parent.getMaxInputs()) {
          slotButtonItem = new SlotButtonItem(index++, -1, -1, new TranslatableComponent("gui.tconstruct.repair"), slotInformation, onPressed) {
            @Override
            protected void drawIcon(PoseStack matrices, Minecraft minecraft) {
              minecraft.getTextureManager().bind(Icons.ICONS);
              Icons.ANVIL.draw(matrices, this.x, this.y);
            }
          };
        }
      }
      // only slow tools if few enough inputs
      else if (slotInformation.getPoints().size() <= parent.getMaxInputs()) {
        slotButtonItem = new SlotButtonItem(index++, -1, -1, slotInformation.getToolForRendering(), slotInformation, onPressed);
      }

      // may skip some tools
      if (slotButtonItem != null) {
        this.addInfoButton(slotButtonItem);
        if (index - 1 == selected) {
          slotButtonItem.pressed = true;
        }
      }
    }

    super.updatePosition(parentX, parentY, parentSizeX, parentSizeY);
  }

  public void addInfoButton(SlotButtonItem slotButtonItem) {
    this.shiftButton(slotButtonItem, 0, -18 * this.style);
    this.addSideButton(slotButtonItem);
  }

  public void shiftStyle(int style) {
    for (AbstractWidget widget : this.buttons) {
      if (widget instanceof SlotButtonItem) {
        this.shiftButton((SlotButtonItem) widget, 0, -18);
      }
    }

    this.style = style;
  }

  protected void shiftButton(SlotButtonItem button, int xd, int yd) {
    button.setGraphics(Icons.BUTTON.shift(xd, yd),
      Icons.BUTTON_HOVERED.shift(xd, yd),
      Icons.BUTTON_PRESSED.shift(xd, yd),
      Icons.ICONS);
  }

  public List<AbstractWidget> getButtons() {
    return this.buttons;
  }
}
