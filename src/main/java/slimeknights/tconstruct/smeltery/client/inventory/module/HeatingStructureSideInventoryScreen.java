package slimeknights.tconstruct.smeltery.client.inventory.module;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import slimeknights.mantle.client.screen.ScalableElementScreen;
import slimeknights.tconstruct.smeltery.client.inventory.HeatingStructureScreen;
import slimeknights.tconstruct.smeltery.tileentity.HeatingStructureTileEntity;
import slimeknights.tconstruct.tables.client.inventory.module.SideInventoryScreen;
import slimeknights.tconstruct.tables.inventory.SideInventoryContainer;

public class HeatingStructureSideInventoryScreen extends SideInventoryScreen<HeatingStructureScreen,SideInventoryContainer<? extends HeatingStructureTileEntity>> {
  public static final ResourceLocation SLOT_LOCATION = HeatingStructureScreen.BACKGROUND;

  // TODO: read from a proper place
  public HeatingStructureSideInventoryScreen(HeatingStructureScreen parent, SideInventoryContainer<? extends HeatingStructureTileEntity> container, Inventory playerInventory, int slotCount, int columns) {
    super(parent, container, playerInventory, TextComponent.EMPTY, slotCount, columns, false, true);
    slot = new ScalableElementScreen(0, 166, 22, 18, 256, 256);
    slotEmpty = new ScalableElementScreen(22, 166, 22, 18, 256, 256);
    yOffset = 0;
  }

  @Override
  protected boolean shouldDrawName() {
    return false;
  }

  @Override
  protected void updateSlots() {
    // adjust for the heat bar
    xOffset += 4;
    super.updateSlots();
    xOffset -= 4;
  }

  @Override
  protected int drawSlots(PoseStack matrices, int xPos, int yPos) {
    assert minecraft != null;
    minecraft.getTextureManager().bind(SLOT_LOCATION);
    int ret = super.drawSlots(matrices, xPos, yPos);
    minecraft.getTextureManager().bind(GENERIC_INVENTORY);
    return ret;
  }

  @Override
  public void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
    super.renderLabels(matrices, mouseX, mouseY);
  }

  @Override
  protected void renderTooltip(PoseStack matrices, int mouseX, int mouseY) {
    super.renderTooltip(matrices, mouseX, mouseY);
    if (parent.melting != null) {
      parent.melting.drawHeatTooltips(matrices, mouseX, mouseY);
    }
  }
}
