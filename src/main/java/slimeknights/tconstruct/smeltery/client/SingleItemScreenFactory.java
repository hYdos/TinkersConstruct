package slimeknights.tconstruct.smeltery.client;

import slimeknights.mantle.client.screen.BackgroundContainerScreen;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.smeltery.inventory.SingleItemContainer;

import javax.annotation.Nullable;
import net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Screen factory for the single item container, one container for multiple backgrounds
 */
public class SingleItemScreenFactory implements ScreenConstructor<SingleItemContainer,BackgroundContainerScreen<SingleItemContainer>> {
  private static final int HEIGHT = 133;
  private static final ResourceLocation DEFAULT = Util.getResource("textures/gui/blank.png");

  /**
   * Gets the background path for the given tile
   * @param tile  Tile
   * @return  Background path
   */
  private static ResourceLocation getBackground(@Nullable BlockEntity tile) {
    if (tile != null) {
      ResourceLocation id = tile.getType().getRegistryName();
      if (id != null) {
        return new ResourceLocation(id.getNamespace(), String.format("textures/gui/%s.png", id.getPath()));
      }
    }
    return DEFAULT;
  }

  @Override
  public BackgroundContainerScreen<SingleItemContainer> create(SingleItemContainer container, Inventory inventory, Component name) {
    return new BackgroundContainerScreen<>(container, inventory, name, HEIGHT, getBackground(container.getTile()));
  }
}
