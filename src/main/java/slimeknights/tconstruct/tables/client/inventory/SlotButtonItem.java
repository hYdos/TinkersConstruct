package slimeknights.tconstruct.tables.client.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.client.screen.ElementScreen;
import slimeknights.tconstruct.library.client.Icons;
import slimeknights.tconstruct.tables.client.inventory.library.slots.SlotInformation;

public class SlotButtonItem extends Button {

  protected static final ElementScreen BUTTON_PRESSED_GUI = new ElementScreen(144, 216, 18, 18, 256, 256);
  protected static final ElementScreen BUTTON_NORMAL_GUI = new ElementScreen(144 + 18 * 2, 216, 18, 18, 256, 256);
  protected static final ElementScreen BUTTON_HOVER_GUI = new ElementScreen(144 + 18 * 4, 216, 18, 18, 256, 256);

  private final ItemStack icon;
  public final SlotInformation data;
  public boolean pressed;
  public final int buttonId;

  private ElementScreen pressedGui = BUTTON_PRESSED_GUI;
  private ElementScreen normalGui = BUTTON_NORMAL_GUI;
  private ElementScreen hoverGui = BUTTON_HOVER_GUI;
  private ResourceLocation backgroundLocation = Icons.ICONS;

  public SlotButtonItem(int buttonId, int x, int y, Component text, SlotInformation data, OnPress onPress) {
    super(x, y, 18, 18, text, onPress);

    this.icon = null;
    this.data = data;
    this.buttonId = buttonId;
  }

  public SlotButtonItem(int buttonId, int x, int y, ItemStack icon, SlotInformation data, OnPress onPress) {
    super(x, y, 18, 18, icon.getHoverName(), onPress);

    this.icon = icon;
    this.data = data;
    this.buttonId = buttonId;
  }

  public SlotButtonItem setGraphics(ElementScreen normal, ElementScreen hover, ElementScreen pressed, ResourceLocation background) {
    this.pressedGui = pressed;
    this.normalGui = normal;
    this.hoverGui = hover;
    this.backgroundLocation = background;

    return this;
  }

  @Override
  public void renderButton(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    Minecraft.getInstance().getTextureManager().bind(this.backgroundLocation);

    if (this.visible) {
      this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

      if (this.pressed) {
        this.pressedGui.draw(matrices, this.x, this.y);
      } else if (this.isHovered) {
        this.hoverGui.draw(matrices, this.x, this.y);
      } else {
        this.normalGui.draw(matrices, this.x, this.y);
      }

      this.drawIcon(matrices, Minecraft.getInstance());
    }
  }

  protected void drawIcon(PoseStack matrices, Minecraft mc) {
    mc.getItemRenderer().renderGuiItem(this.icon, this.x + 1, this.y + 1);
  }
}
