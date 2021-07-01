package slimeknights.tconstruct.library.materials.client;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

/**
 * Determines the type of texture used for rendering a specific material
 */
public interface IMaterialRenderInfo {

  TextureAtlasSprite getTexture(ResourceLocation baseTexture, String location);

  boolean isStitched();

  boolean useVertexColoring();

  int getVertexColor();

  // this actually would require its own thing, but we put it here for simplicity
  String getTextureSuffix();

  IMaterialRenderInfo setTextureSuffix(String suffix);

  // todo: copy material render info implementations over

}
