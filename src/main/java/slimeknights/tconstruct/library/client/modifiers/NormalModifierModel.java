package slimeknights.tconstruct.library.client.modifiers;

import com.google.common.collect.ImmutableList;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraftforge.client.model.ItemLayerModel;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Default modifier model loader, loads a single texture from the standard path
 */
public class NormalModifierModel implements IBakedModifierModel {
  /** Constant unbaked model instance, as they are all the same */
  public static final IUnbakedModifierModel UNBAKED_INSTANCE = (smallGetter, largeGetter) -> {
    Material smallTexture = smallGetter.apply("");
    Material largeTexture = largeGetter.apply("");
    if (smallTexture != null || largeTexture != null) {
      return new NormalModifierModel(smallTexture, largeTexture);
    }
    return null;
  };

  /** Textures to show */
  private final Material[] textures;
  /** Cache of quads */
  @SuppressWarnings("unchecked")
  private final ImmutableList<BakedQuad>[] quads = new ImmutableList[2];

  public NormalModifierModel(@Nullable Material smallTexture, @Nullable Material largeTexture) {
    this.textures = new Material[]{ smallTexture, largeTexture };
  }

  @Override
  public ImmutableList<BakedQuad> getQuads(IModifierToolStack tool, ModifierEntry entry, Function<Material,TextureAtlasSprite> spriteGetter, Transformation transforms, boolean isLarge) {
    int index = isLarge ? 1 : 0;
    if (quads[index] == null) {
      if (textures[index] == null) {
        quads[index] = ImmutableList.of();
      } else {
        quads[index] = ItemLayerModel.getQuadsForSprite(-1, spriteGetter.apply(textures[index]), transforms);
      }
    }
    return quads[index];
  }
}
