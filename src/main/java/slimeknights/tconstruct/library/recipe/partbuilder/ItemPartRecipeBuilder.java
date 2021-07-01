package slimeknights.tconstruct.library.recipe.partbuilder;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.recipe.ItemOutput;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.library.materials.MaterialId;
import slimeknights.tconstruct.tables.TinkerTables;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

@RequiredArgsConstructor(staticName = "item")
public class ItemPartRecipeBuilder extends AbstractRecipeBuilder<ItemPartRecipeBuilder> {
  private final MaterialId materialId;
  private final ResourceLocation pattern;
  private final int cost;
  private final ItemOutput result;

  @Override
  public void build(Consumer<net.minecraft.data.recipes.FinishedRecipe> consumer) {
    build(consumer, Objects.requireNonNull(result.get().getItem().getRegistryName()));
  }

  @Override
  public void build(Consumer<net.minecraft.data.recipes.FinishedRecipe> consumer, ResourceLocation id) {
    ResourceLocation advancementId = buildOptionalAdvancement(id, "parts");
    consumer.accept(new FinishedRecipe(id, advancementId));
  }

  private class FinishedRecipe extends AbstractFinishedRecipe {
    public FinishedRecipe(ResourceLocation ID, @Nullable ResourceLocation advancementID) {
      super(ID, advancementID);
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      json.addProperty("material", materialId.toString());
      json.addProperty("pattern", pattern.toString());
      json.addProperty("cost", cost);
      json.add("result", result.serialize());
    }

    @Override
    public RecipeSerializer<?> getType() {
      return TinkerTables.itemPartBuilderSerializer.get();
    }
  }
}
