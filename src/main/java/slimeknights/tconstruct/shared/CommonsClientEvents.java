package slimeknights.tconstruct.shared;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.ClientEventBase;
import slimeknights.tconstruct.library.book.TinkerBook;
import slimeknights.tconstruct.shared.block.ClearStainedGlassBlock;
import slimeknights.tconstruct.shared.block.ClearStainedGlassBlock.GlassColor;

@EventBusSubscriber(modid = TConstruct.modID, value = Dist.CLIENT, bus = Bus.MOD)
public class CommonsClientEvents extends ClientEventBase {

  @SubscribeEvent
  static void clientSetup(final FMLClientSetupEvent event) {
    ItemBlockRenderTypes.setRenderLayer(TinkerCommons.glow.get(), RenderType.translucent());

    // glass
    ItemBlockRenderTypes.setRenderLayer(TinkerCommons.clearGlass.get(), RenderType.cutout());
    ItemBlockRenderTypes.setRenderLayer(TinkerCommons.clearGlassPane.get(), RenderType.cutout());
    for (GlassColor color : GlassColor.values()) {
      ItemBlockRenderTypes.setRenderLayer(TinkerCommons.clearStainedGlass.get(color), RenderType.translucent());
      ItemBlockRenderTypes.setRenderLayer(TinkerCommons.clearStainedGlassPane.get(color), RenderType.translucent());
    }
    ItemBlockRenderTypes.setRenderLayer(TinkerCommons.soulGlass.get(), RenderType.translucent());
    ItemBlockRenderTypes.setRenderLayer(TinkerCommons.soulGlassPane.get(), RenderType.translucent());
    ItemBlockRenderTypes.setRenderLayer(TinkerMaterials.soulsteel.get(), RenderType.translucent());
    ItemBlockRenderTypes.setRenderLayer(TinkerMaterials.slimesteel.get(), RenderType.translucent());

    Font unicode = unicodeFontRender();
    TinkerBook.MATERIALS_AND_YOU.fontRenderer = unicode;
    TinkerBook.TINKERS_GADGETRY.fontRenderer = unicode;
    TinkerBook.PUNY_SMELTING.fontRenderer = unicode;
    TinkerBook.MIGHTY_SMELTING.fontRenderer = unicode;
    TinkerBook.FANTASTIC_FOUNDRY.fontRenderer = unicode;
    TinkerBook.ENCYCLOPEDIA.fontRenderer = unicode;
  }

  @SubscribeEvent
  static void registerColorHandlers(ColorHandlerEvent.Item event) {
    // colors apply a constant tint to make models easier
    BlockColors blockColors = event.getBlockColors();
    ItemColors itemColors = event.getItemColors();
    for (GlassColor color : GlassColor.values()) {
      Block block = TinkerCommons.clearStainedGlass.get(color);
      Block pane = TinkerCommons.clearStainedGlassPane.get(color);
      blockColors.register((state, reader, pos, index) -> color.getColor(), block, pane);
      registerBlockItemColorAlias(blockColors, itemColors, block);
      registerBlockItemColorAlias(blockColors, itemColors, pane);
    }
  }

  private static Font unicodeRenderer;

  /** Gets the unicode font renderer */
  public static Font unicodeFontRender() {
    if (unicodeRenderer == null)
      unicodeRenderer = new Font(rl -> {
        FontManager resourceManager = Minecraft.getInstance().fontManager;
        return resourceManager.fontSets.get(Minecraft.UNIFORM_FONT);
      });

    return unicodeRenderer;
  }
}
