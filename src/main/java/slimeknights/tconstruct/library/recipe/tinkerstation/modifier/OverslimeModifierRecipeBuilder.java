package slimeknights.tconstruct.library.recipe.tinkerstation.modifier;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.tools.TinkerModifiers;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Builder for overslime recipes
 */
@RequiredArgsConstructor(staticName = "modifier")
public class OverslimeModifierRecipeBuilder extends AbstractRecipeBuilder<OverslimeModifierRecipeBuilder> {
  private final Ingredient ingredient;
  private final int restoreAmount;

  /** Creates a new builder for the given item */
  public static OverslimeModifierRecipeBuilder modifier(ItemLike item, int restoreAmount) {
    return modifier(Ingredient.of(item), restoreAmount);
  }

  @Override
  public void build(Consumer<net.minecraft.data.recipes.FinishedRecipe> consumer) {
    ItemStack[] stacks = ingredient.getItems();
    if (stacks.length == 0) {
      throw new IllegalStateException("Empty ingredient not allowed");
    }
    build(consumer, Objects.requireNonNull(stacks[0].getItem().getRegistryName()));
  }

  @Override
  public void build(Consumer<net.minecraft.data.recipes.FinishedRecipe> consumer, ResourceLocation id) {
    if (ingredient == Ingredient.EMPTY) {
      throw new IllegalStateException("Empty ingredient not allowed");
    }
    ResourceLocation advancementId = buildOptionalAdvancement(id, "modifiers");
    consumer.accept(new FinishedRecipe(id, advancementId));
  }

  private class FinishedRecipe extends AbstractFinishedRecipe {
    public FinishedRecipe(ResourceLocation ID, @Nullable ResourceLocation advancementID) {
      super(ID, advancementID);
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      json.add("ingredient", ingredient.toJson());
      json.addProperty("restore_amount", restoreAmount);
    }

    @Override
    public RecipeSerializer<?> getType() {
      return TinkerModifiers.overslimeSerializer.get();
    }
  }
}
