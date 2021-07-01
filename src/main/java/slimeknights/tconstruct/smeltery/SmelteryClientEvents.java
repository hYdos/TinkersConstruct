package slimeknights.tconstruct.smeltery;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import slimeknights.mantle.client.model.FaucetFluidLoader;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.ClientEventBase;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.client.model.block.CastingModel;
import slimeknights.tconstruct.library.client.model.block.ChannelModel;
import slimeknights.tconstruct.library.client.model.block.FluidTextureModel;
import slimeknights.tconstruct.library.client.model.block.MelterModel;
import slimeknights.tconstruct.library.client.model.block.TankModel;
import slimeknights.tconstruct.library.client.util.FluidTooltipHandler;
import slimeknights.tconstruct.smeltery.client.SingleItemScreenFactory;
import slimeknights.tconstruct.smeltery.client.inventory.AlloyerScreen;
import slimeknights.tconstruct.smeltery.client.inventory.HeatingStructureScreen;
import slimeknights.tconstruct.smeltery.client.inventory.MelterScreen;
import slimeknights.tconstruct.smeltery.client.render.CastingTileEntityRenderer;
import slimeknights.tconstruct.smeltery.client.render.ChannelTileEntityRenderer;
import slimeknights.tconstruct.smeltery.client.render.FaucetTileEntityRenderer;
import slimeknights.tconstruct.smeltery.client.render.HeatingStructureTileEntityRenderer;
import slimeknights.tconstruct.smeltery.client.render.MelterTileEntityRenderer;
import slimeknights.tconstruct.smeltery.client.render.TankTileEntityRenderer;
import slimeknights.tconstruct.smeltery.item.TankItem;
import slimeknights.tconstruct.smeltery.tileentity.DrainTileEntity;
import slimeknights.tconstruct.smeltery.tileentity.DuctTileEntity;
import slimeknights.tconstruct.smeltery.tileentity.ITankTileEntity;
import slimeknights.tconstruct.smeltery.tileentity.tank.ISmelteryTankHandler;

@SuppressWarnings("unused")
@EventBusSubscriber(modid= TConstruct.modID, value= Dist.CLIENT, bus= Bus.MOD)
public class SmelteryClientEvents extends ClientEventBase {
  /**
   * Called by TinkerClient to add the resource listeners, runs during constructor
   */
  public static void addResourceListener(ReloadableResourceManager manager) {
    FaucetFluidLoader.initialize();
  }

  @SubscribeEvent
  static void clientSetup(final FMLClientSetupEvent event) {
    // render layers
    RenderType cutout = RenderType.cutout();
    // seared
    // casting
    ItemBlockRenderTypes.setRenderLayer(TinkerSmeltery.searedFaucet.get(), cutout);
    ItemBlockRenderTypes.setRenderLayer(TinkerSmeltery.searedBasin.get(), cutout);
    ItemBlockRenderTypes.setRenderLayer(TinkerSmeltery.searedTable.get(), cutout);
    // controller
    ItemBlockRenderTypes.setRenderLayer(TinkerSmeltery.searedMelter.get(), cutout);
    ItemBlockRenderTypes.setRenderLayer(TinkerSmeltery.smelteryController.get(), cutout);
    ItemBlockRenderTypes.setRenderLayer(TinkerSmeltery.foundryController.get(), cutout);
    // peripherals
    ItemBlockRenderTypes.setRenderLayer(TinkerSmeltery.searedDrain.get(), cutout);
    ItemBlockRenderTypes.setRenderLayer(TinkerSmeltery.searedDuct.get(), cutout);
    TinkerSmeltery.searedTank.forEach(tank -> ItemBlockRenderTypes.setRenderLayer(tank, cutout));
    ItemBlockRenderTypes.setRenderLayer(TinkerSmeltery.searedLantern.get(), cutout);
    ItemBlockRenderTypes.setRenderLayer(TinkerSmeltery.searedGlass.get(), cutout);
    ItemBlockRenderTypes.setRenderLayer(TinkerSmeltery.searedGlassPane.get(), cutout);
    // scorched
    // casting
    ItemBlockRenderTypes.setRenderLayer(TinkerSmeltery.scorchedFaucet.get(), cutout);
    ItemBlockRenderTypes.setRenderLayer(TinkerSmeltery.scorchedBasin.get(), cutout);
    ItemBlockRenderTypes.setRenderLayer(TinkerSmeltery.scorchedTable.get(), cutout);
    // controller
    ItemBlockRenderTypes.setRenderLayer(TinkerSmeltery.scorchedAlloyer.get(), cutout);
    // peripherals
    ItemBlockRenderTypes.setRenderLayer(TinkerSmeltery.scorchedDrain.get(), cutout);
    ItemBlockRenderTypes.setRenderLayer(TinkerSmeltery.scorchedDuct.get(), cutout);
    TinkerSmeltery.scorchedTank.forEach(tank -> ItemBlockRenderTypes.setRenderLayer(tank, cutout));
    ItemBlockRenderTypes.setRenderLayer(TinkerSmeltery.scorchedLantern.get(), cutout);
    ItemBlockRenderTypes.setRenderLayer(TinkerSmeltery.scorchedGlass.get(), cutout);
    ItemBlockRenderTypes.setRenderLayer(TinkerSmeltery.scorchedGlassPane.get(), cutout);

    // TESRs
    ClientRegistry.bindTileEntityRenderer(TinkerSmeltery.tank.get(), TankTileEntityRenderer::new);
    ClientRegistry.bindTileEntityRenderer(TinkerSmeltery.faucet.get(), FaucetTileEntityRenderer::new);
    ClientRegistry.bindTileEntityRenderer(TinkerSmeltery.channel.get(), ChannelTileEntityRenderer::new);
    ClientRegistry.bindTileEntityRenderer(TinkerSmeltery.table.get(), CastingTileEntityRenderer::new);
    ClientRegistry.bindTileEntityRenderer(TinkerSmeltery.basin.get(), CastingTileEntityRenderer::new);
    ClientRegistry.bindTileEntityRenderer(TinkerSmeltery.melter.get(), MelterTileEntityRenderer::new);
    ClientRegistry.bindTileEntityRenderer(TinkerSmeltery.alloyer.get(), TankTileEntityRenderer::new);
    ClientRegistry.bindTileEntityRenderer(TinkerSmeltery.smeltery.get(), HeatingStructureTileEntityRenderer::new);
    ClientRegistry.bindTileEntityRenderer(TinkerSmeltery.foundry.get(), HeatingStructureTileEntityRenderer::new);

    // screens
    MenuScreens.register(TinkerSmeltery.melterContainer.get(), MelterScreen::new);
    MenuScreens.register(TinkerSmeltery.smelteryContainer.get(), HeatingStructureScreen::new);
    MenuScreens.register(TinkerSmeltery.singleItemContainer.get(), new SingleItemScreenFactory());
    MenuScreens.register(TinkerSmeltery.alloyerContainer.get(), AlloyerScreen::new);

    FluidTooltipHandler.init();
  }

  @SubscribeEvent
  static void registerModelLoaders(ModelRegistryEvent event) {
    ModelLoaderRegistry.registerLoader(Util.getResource("tank"), TankModel.LOADER);
    ModelLoaderRegistry.registerLoader(Util.getResource("casting"), CastingModel.LOADER);
    ModelLoaderRegistry.registerLoader(Util.getResource("melter"), MelterModel.LOADER);
    ModelLoaderRegistry.registerLoader(Util.getResource("channel"), ChannelModel.LOADER);
    ModelLoaderRegistry.registerLoader(Util.getResource("fluid_texture"), FluidTextureModel.LOADER);
  }

  @SubscribeEvent
  static void blockColors(ColorHandlerEvent.Block event) {
    BlockColors colors = event.getBlockColors();
    BlockColor handler = (state, world, pos, index) -> {
      if (pos != null && world != null) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ITankTileEntity) {
          FluidStack fluid = ((ITankTileEntity)te).getTank().getFluid();
          return fluid.getFluid().getAttributes().getColor(fluid);
        }
      }
      return -1;
    };
    TinkerSmeltery.searedTank.forEach(tank -> colors.register(handler, tank));
    TinkerSmeltery.scorchedTank.forEach(tank -> colors.register(handler, tank));
    colors.register(handler, TinkerSmeltery.searedLantern.get());
    colors.register(handler, TinkerSmeltery.scorchedLantern.get());
    colors.register(handler, TinkerSmeltery.searedMelter.get(), TinkerSmeltery.scorchedAlloyer.get());

    // color the extra fluid textures
    colors.register((state, world, pos, index) -> {
      if (index == 1 && world != null && pos != null) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof ISmelteryTankHandler) {
          FluidStack bottom = ((ISmelteryTankHandler)te).getTank().getFluidInTank(0);
          if (!bottom.isEmpty()) {
            return bottom.getFluid().getAttributes().getColor(bottom);
          }
        }
      }
      return -1;
    }, TinkerSmeltery.smelteryController.get(), TinkerSmeltery.foundryController.get());
    colors.register((state, world, pos, index) -> {
      if (index == 1 && world != null && pos != null) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof DrainTileEntity) {
          return ((DrainTileEntity)te).getDisplayFluid().getAttributes().getColor();
        }
      }
      return -1;
    }, TinkerSmeltery.searedDrain.get(), TinkerSmeltery.scorchedDrain.get());
    colors.register((state, world, pos, index) -> {
      if (index == 1 && world != null && pos != null) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof DuctTileEntity) {
          return ((DuctTileEntity)te).getItemHandler().getFluid().getAttributes().getColor();
        }
      }
      return -1;
    }, TinkerSmeltery.searedDuct.get(), TinkerSmeltery.scorchedDuct.get());
  }

  @SubscribeEvent
  static void itemColors(ColorHandlerEvent.Item event) {
    ItemColors itemColors = event.getItemColors();
    ItemColor handler = (stack, index) -> {
      FluidTank tank = TankItem.getFluidTank(stack);
      if (!tank.isEmpty()) {
        FluidStack fluid = tank.getFluid();
        return fluid.getFluid().getAttributes().getColor(fluid);
      }
      return -1;
    };
    TinkerSmeltery.searedTank.forEach(tank -> itemColors.register(handler, tank));
    TinkerSmeltery.scorchedTank.forEach(tank -> itemColors.register(handler, tank));
    itemColors.register(handler, TinkerSmeltery.searedLantern.get());
    itemColors.register(handler, TinkerSmeltery.scorchedLantern.get());
  }
}
