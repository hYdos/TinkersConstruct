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
 * Modifier model for a modifier that changes its texture when broken
 */
public class BreakableModifierModel implements IBakedModifierModel {
  /** Constant unbaked model instance, as they are all the same */
  public static final IUnbakedModifierModel UNBAKED_INSTANCE = (smallGetter, largeGetter) -> {
    Material normalSmall = smallGetter.apply("");
    Material brokenSmall = smallGetter.apply("_broken");
    Material normalLarge = smallGetter.apply("");
    Material brokenLarge = smallGetter.apply("_broken");
    // we need both to exist for this to work
    if (normalSmall != null || brokenSmall != null || normalLarge != null || brokenLarge != null) {
      return new BreakableModifierModel(normalSmall, brokenSmall, normalLarge, brokenLarge);
    }
    return null;
  };


  /** Textures for this model */
  private final Material[] sprites;
  /* Caches of the small quad list */
  @SuppressWarnings("unchecked")
  private final ImmutableList<BakedQuad>[] quadCache = new ImmutableList[4];
  public BreakableModifierModel(@Nullable Material normalSmall, @Nullable Material brokenSmall, @Nullable Material normalLarge, @Nullable Material brokenLarge) {
    this.sprites = new Material[] {normalSmall, brokenSmall, normalLarge, brokenLarge};
  }

  @Override
  public ImmutableList<BakedQuad> getQuads(IModifierToolStack tool, ModifierEntry entry, Function<Material,TextureAtlasSprite> spriteGetter, Transformation transforms, boolean isLarge) {
    // first get the cache index
    int index = (isLarge ? 2 : 0) | (tool.isBroken() ? 1 : 0);
    // if not cached, build
    if (quadCache[index] == null) {
      if (sprites[index] == null) {
        quadCache[index] = ImmutableList.of();
      } else {
        quadCache[index] = ItemLayerModel.getQuadsForSprite(-1, spriteGetter.apply(sprites[index]), transforms);
      }
    }
    return quadCache[index];
  }
}
