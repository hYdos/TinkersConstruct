package slimeknights.tconstruct.library.client.model.block;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import lombok.AllArgsConstructor;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import slimeknights.mantle.client.model.RetexturedModel;
import slimeknights.mantle.client.model.util.DynamicBakedWrapper;
import slimeknights.mantle.client.model.util.SimpleBlockModel;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.tconstruct.smeltery.tileentity.tank.IDisplayFluidListener;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

/**
 * Model that replaces fluid textures with the fluid from model data
 */
@AllArgsConstructor
public class FluidTextureModel implements IModelGeometry<FluidTextureModel> {
  public static final Loader LOADER = new Loader();

  private final SimpleBlockModel model;
  private final Set<String> fluids;

  @Override
  public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation,UnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
    return model.getTextures(owner, modelGetter, missingTextureErrors);
  }

  @Override
  public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material,TextureAtlasSprite> spriteGetter, ModelState transform, ItemOverrides overrides, ResourceLocation modelLocation) {
    BakedModel baked = model.bakeModel(owner, transform, overrides, spriteGetter, modelLocation);
    return new Baked(baked, model, owner, transform, RetexturedModel.getAllRetextured(owner, model, this.fluids));
  }

  /** Baked wrapper class */
  private static class Baked extends DynamicBakedWrapper<BakedModel> {
    private final Map<Fluid,BakedModel> cache = new HashMap<>();
    private final SimpleBlockModel model;
    private final IModelConfiguration owner;
    private final ModelState transform;
    private final Set<String> fluids;
    protected Baked(BakedModel originalModel, SimpleBlockModel model, IModelConfiguration owner, ModelState transform, Set<String> fluids) {
      super(originalModel);
      this.model = model;
      this.owner = owner;
      this.transform = transform;
      this.fluids = fluids;
    }

    /** Retextures a model for the given fluid */
    private BakedModel getRetexturedModel(Fluid fluid) {
      return this.model.bakeDynamic(new RetexturedModel.RetexturedConfiguration(this.owner, this.fluids, fluid.getAttributes().getStillTexture()), this.transform);
    }

    /** Gets a retextured model for the given fluid, using the cached model if possible */
    private BakedModel getCachedModel(Fluid fluid) {
      return this.cache.computeIfAbsent(fluid, this::getRetexturedModel);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, Random random, IModelData data) {
      Fluid fluid = data.getData(IDisplayFluidListener.PROPERTY);
      if (fluid != null && fluid != Fluids.EMPTY) {
        return getCachedModel(fluid).getQuads(state, direction, random, data);
      }
      return originalModel.getQuads(state, direction, random, data);
    }
  }

  /** Model loader class */
  private static class Loader implements IModelLoader<FluidTextureModel> {
    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {}

    @Override
    public FluidTextureModel read(JsonDeserializationContext context, JsonObject json) {
      SimpleBlockModel model = SimpleBlockModel.deserialize(context, json);
      Set<String> fluids = ImmutableSet.copyOf(JsonHelper.parseList(json, "fluids", GsonHelper::convertToString));
      return new FluidTextureModel(model, fluids);
    }
  }
}
