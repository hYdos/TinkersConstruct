package slimeknights.tconstruct.tables.client.inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import slimeknights.mantle.client.screen.ElementScreen;
import slimeknights.mantle.client.screen.MultiModuleScreen;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.client.Icons;
import slimeknights.tconstruct.library.network.TinkerNetwork;
import slimeknights.tconstruct.tables.block.ITinkerStationBlock;
import slimeknights.tconstruct.tables.client.inventory.module.SideInventoryScreen;
import slimeknights.tconstruct.tables.client.inventory.module.TinkerTabsScreen;
import slimeknights.tconstruct.tables.inventory.BaseStationContainer;
import slimeknights.tconstruct.tables.inventory.SideInventoryContainer;
import slimeknights.tconstruct.tables.network.StationTabPacket;

public class BaseStationScreen<TILE extends BlockEntity & Container, CONTAINER extends BaseStationContainer<TILE>> extends MultiModuleScreen<CONTAINER> {
  protected static final Component COMPONENT_WARNING = Util.makeTranslation("gui", "warning");
  protected static final Component COMPONENT_ERROR = Util.makeTranslation("gui", "error");

  public static final ResourceLocation BLANK_BACK = Util.getResource("textures/gui/blank.png");

  protected final TILE tile;
  protected final CONTAINER container;
  protected TinkerTabsScreen tabsScreen;

  public BaseStationScreen(CONTAINER container, Inventory playerInventory, Component title) {
    super(container, playerInventory, title);
    this.tile = container.getTile();
    this.container = container;

    this.tabsScreen = new TinkerTabsScreen(this, container, playerInventory, title);
    this.addModule(this.tabsScreen);

    if (this.tile != null) {
      Level world = this.tile.getLevel();

      if (world != null) {
        for (Pair<BlockPos, BlockState> pair : container.stationBlocks) {
          BlockState state = pair.getRight();
          BlockPos blockPos = pair.getLeft();
          ItemStack stack = state.getBlock().getPickBlock(state, null, world, blockPos, playerInventory.player);
          this.tabsScreen.addTab(stack, blockPos);
        }
      }
    }

    // preselect the correct tab
    for (int i = 0; i < this.tabsScreen.tabData.size(); i++) {
      if (this.tabsScreen.tabData.get(i).equals(this.tile.getBlockPos())) {
        this.tabsScreen.tabs.selected = i;
      }
    }
  }

  public TILE getTileEntity() {
    return this.tile;
  }

  protected void drawIcon(PoseStack matrices, Slot slot, ElementScreen element) {
    this.minecraft.getTextureManager().bind(Icons.ICONS);
    element.draw(matrices, slot.x + this.cornerX - 1, slot.y + this.cornerY - 1);
  }

  protected void drawIconEmpty(PoseStack matrices, Slot slot, ElementScreen element) {
    if (slot.hasItem()) {
      return;
    }

    this.drawIcon(matrices, slot, element);
  }

  public void onTabSelection(int selection) {
    if (selection < 0 || selection > this.tabsScreen.tabData.size()) {
      return;
    }

    Level world = this.tile.getLevel();

    if (world == null) {
      return;
    }

    BlockPos pos = this.tabsScreen.tabData.get(selection);
    BlockState state = world.getBlockState(pos);

    if (state.getBlock() instanceof ITinkerStationBlock) {
      BlockEntity te = this.tile.getLevel().getBlockEntity(pos);
      TinkerNetwork.getInstance().sendToServer(new StationTabPacket(pos));

      // sound!
      assert this.minecraft != null;
      this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
  }

  public void error(Component message) {
  }

  public void warning(Component message) {
  }

  public void updateDisplay() {
  }

  protected void addChestSideInventory() {
    SideInventoryContainer<?> sideInventoryContainer = container.getSubContainer(SideInventoryContainer.class);
    if (sideInventoryContainer != null) {
      // no title if missing one
      Component sideInventoryName = TextComponent.EMPTY;
      BlockEntity te = sideInventoryContainer.getTile();
      if (te instanceof MenuProvider) {
        sideInventoryName = ((MenuProvider) te).getDisplayName();
      }

      this.addModule(new SideInventoryScreen<>(this, sideInventoryContainer, inventory, sideInventoryName, sideInventoryContainer.getSlotCount(), sideInventoryContainer.getColumns()));
    }
  }
}
