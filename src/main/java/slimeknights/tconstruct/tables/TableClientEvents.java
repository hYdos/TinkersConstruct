package slimeknights.tconstruct.tables;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.ClientEventBase;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.client.model.block.TableModel;
import slimeknights.tconstruct.tables.client.PatternGuiTextureLoader;
import slimeknights.tconstruct.tables.client.SlotInformationLoader;
import slimeknights.tconstruct.tables.client.TableTileEntityRenderer;
import slimeknights.tconstruct.tables.client.inventory.TinkerChestScreen;
import slimeknights.tconstruct.tables.client.inventory.table.CraftingStationScreen;
import slimeknights.tconstruct.tables.client.inventory.table.PartBuilderScreen;
import slimeknights.tconstruct.tables.client.inventory.table.TinkerStationScreen;

@SuppressWarnings("unused")
@EventBusSubscriber(modid=TConstruct.modID, value=Dist.CLIENT, bus=Bus.MOD)
public class TableClientEvents extends ClientEventBase {

  /**
   * Called by TinkerClient to add the resource listeners, runs during constructor
   */
  public static void addResourceListener(ReloadableResourceManager manager) {
    manager.registerReloadListener(PatternGuiTextureLoader.INSTANCE);
    manager.registerReloadListener(SlotInformationLoader.INSTANCE);
  }

  @SubscribeEvent
  static void registerModelLoader(ModelRegistryEvent event) {
    ModelLoaderRegistry.registerLoader(Util.getResource("table"), TableModel.LOADER);
  }

  @SubscribeEvent
  static void setupClient(final FMLClientSetupEvent event) {
    MenuScreens.register(TinkerTables.craftingStationContainer.get(), CraftingStationScreen::new);
    MenuScreens.register(TinkerTables.tinkerStationContainer.get(), TinkerStationScreen::new);
    MenuScreens.register(TinkerTables.partBuilderContainer.get(), PartBuilderScreen::new);
    MenuScreens.register(TinkerTables.tinkerChestContainer.get(), TinkerChestScreen::new);

    ClientRegistry.bindTileEntityRenderer(TinkerTables.craftingStationTile.get(), TableTileEntityRenderer::new);
    ClientRegistry.bindTileEntityRenderer(TinkerTables.tinkerStationTile.get(), TableTileEntityRenderer::new);
    ClientRegistry.bindTileEntityRenderer(TinkerTables.partBuilderTile.get(), TableTileEntityRenderer::new);
  }
}
