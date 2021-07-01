package slimeknights.tconstruct.library.client.model.block;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import slimeknights.mantle.client.model.inventory.ModelItem;
import slimeknights.mantle.client.model.util.SimpleBlockModel;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * This model contains a list of items to display in the TESR, plus a single scalable fluid that can either be statically rendered or rendered in the TESR
 */
public class MelterModel extends TankModel {
  /** Shared loader instance */
  public static final Loader LOADER = new Loader();

  private final List<ModelItem> items;
  @SuppressWarnings("WeakerAccess")
  protected MelterModel(SimpleBlockModel model, @Nullable SimpleBlockModel gui, IncrementalFluidCuboid fluid, List<ModelItem> items) {
    super(model, gui, fluid, false);
    this.items = items;
  }

  @Override
  public net.minecraft.client.resources.model.BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material,TextureAtlasSprite> spriteGetter, ModelState transform, ItemOverrides overrides, ResourceLocation location) {
    net.minecraft.client.resources.model.BakedModel baked = model.bakeModel(owner, transform, overrides, spriteGetter, location);
    // bake the GUI model if present
    net.minecraft.client.resources.model.BakedModel bakedGui = baked;
    if (gui != null) {
      bakedGui = gui.bakeModel(owner, transform, overrides, spriteGetter, location);
    }
    return new BakedModel(owner, transform, baked, bakedGui, this);
  }

  /** Baked variant to allow access to items */
  public static final class BakedModel extends TankModel.BakedModel<MelterModel> {
    @SuppressWarnings("WeakerAccess")
    protected BakedModel(IModelConfiguration owner, ModelState transforms, net.minecraft.client.resources.model.BakedModel baked, net.minecraft.client.resources.model.BakedModel gui, MelterModel original) {
      super(owner, transforms, baked, gui, original);
    }

    /**
     * Gets a list of items used in inventory display
     * @return  Item list
     */
    public List<ModelItem> getItems() {
      return this.original.items;
    }
  }

  /** Loader for this model */
  public static class Loader implements IModelLoader<TankModel> {
    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {}

    @Override
    public TankModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
      SimpleBlockModel model = SimpleBlockModel.deserialize(deserializationContext, modelContents);
      SimpleBlockModel gui = null;
      if (modelContents.has("gui")) {
        gui = SimpleBlockModel.deserialize(deserializationContext, GsonHelper.getAsJsonObject(modelContents, "gui"));
      }
      IncrementalFluidCuboid fluid = IncrementalFluidCuboid.fromJson(GsonHelper.getAsJsonObject(modelContents, "fluid"));
      List<ModelItem> items = ModelItem.listFromJson(modelContents, "items");
      return new MelterModel(model, gui, fluid, items);
    }
  }
}
