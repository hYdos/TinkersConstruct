package slimeknights.tconstruct.library.recipe.modifiers;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.recipe.EntityIngredient;
import slimeknights.mantle.recipe.ItemOutput;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.tools.TinkerModifiers;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

/** Builder for entity melting recipes */
@RequiredArgsConstructor(staticName = "severing")
public class SeveringRecipeBuilder extends AbstractRecipeBuilder<SeveringRecipeBuilder> {
  private final EntityIngredient ingredient;
  private final ItemOutput output;
  private boolean isAgeable = false;
  private ItemOutput childOutput = null;

  /** Creates a new builder from an item */
  public static SeveringRecipeBuilder severing(EntityIngredient ingredient, ItemLike output) {
    return SeveringRecipeBuilder.severing(ingredient, ItemOutput.fromItem(output));
  }

  /**
   * Makes this an ageable severing recipe
   * @param childOutput  Output when a child, if null just does no output for children
   * @return  Builder instance
   */
  public SeveringRecipeBuilder setChildOutput(@Nullable ItemOutput childOutput) {
    this.isAgeable = true;
    this.childOutput = childOutput;
    return this;
  }

  @Override
  public void build(Consumer<net.minecraft.data.recipes.FinishedRecipe> consumer) {
    build(consumer, Objects.requireNonNull(output.get().getItem().getRegistryName()));
  }

  @Override
  public void build(Consumer<net.minecraft.data.recipes.FinishedRecipe> consumer, ResourceLocation id) {
    ResourceLocation advancementId = this.buildOptionalAdvancement(id, "severing");
    consumer.accept(new FinishedRecipe(id, advancementId));
  }

  private class FinishedRecipe extends AbstractFinishedRecipe {
    public FinishedRecipe(ResourceLocation ID, @Nullable ResourceLocation advancementID) {
      super(ID, advancementID);
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      json.add("entity", ingredient.serialize());
      if (isAgeable) {
        json.add("adult_result", output.serialize());
        if (childOutput != null) {
          json.add("child_result", childOutput.serialize());
        }
      } else {
        json.add("result", output.serialize());
      }
    }

    @Override
    public RecipeSerializer<?> getType() {
      return isAgeable ? TinkerModifiers.ageableSeveringSerializer.get() : TinkerModifiers.severingSerializer.get();
    }
  }
}
