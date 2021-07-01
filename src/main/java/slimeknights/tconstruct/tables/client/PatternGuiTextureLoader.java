package slimeknights.tconstruct.tables.client;

import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import slimeknights.tconstruct.library.client.util.ResourceValidator;

/**
 * Stitches all GUI part textures into the texture sheet
 */
public class PatternGuiTextureLoader extends ResourceValidator {
  /** Singleton instance */
  public static final PatternGuiTextureLoader INSTANCE = new PatternGuiTextureLoader();
  private PatternGuiTextureLoader() {
    super("textures/gui/tinker_pattern", "textures", ".png");
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onTextureStitch);
  }

  /**
   * Called during texture stitch to add the textures in
   * @param event
   */
  private void onTextureStitch(TextureStitchEvent.Pre event) {
    if (InventoryMenu.BLOCK_ATLAS.equals(event.getMap().location())) {
      this.resources.forEach(event::addSprite);
    }
  }
}
