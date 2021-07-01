package slimeknights.tconstruct.shared.inventory;

import slimeknights.mantle.inventory.MultiModuleContainer;
import slimeknights.tconstruct.shared.TinkerCommons;

import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Container that triggers the criteria instance */
public class TriggeringMultiModuleContainer<TILE extends BlockEntity> extends MultiModuleContainer<TILE> {
  public TriggeringMultiModuleContainer(MenuType<?> type, int id, @Nullable Inventory inv, @Nullable TILE tile) {
    super(type, id, inv, tile);
    TinkerCommons.CONTAINER_OPENED_TRIGGER.trigger(tile, inv);
  }
}
