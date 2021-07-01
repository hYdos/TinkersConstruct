package slimeknights.tconstruct.library.recipe.modifiers;

import com.google.gson.JsonObject;
import slimeknights.mantle.recipe.EntityIngredient;
import slimeknights.mantle.recipe.ItemOutput;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.tconstruct.common.recipe.LoggingRecipeSerializer;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class AgeableSeveringRecipe extends SeveringRecipe {
  @Nullable
  private final ItemOutput childOutput;
  public AgeableSeveringRecipe(ResourceLocation id, EntityIngredient ingredient, ItemOutput adultOutput, @Nullable ItemOutput childOutput) {
    super(id, ingredient, adultOutput);
    this.childOutput = childOutput;
  }

  @Override
  public ItemStack getOutput(Entity entity) {
    if (entity instanceof LivingEntity && ((LivingEntity) entity).isBaby()) {
      return childOutput == null ? ItemStack.EMPTY : childOutput.get().copy();
    }
    return getOutput().copy();
  }

  /** Serializer for this recipe */
  public static class Serializer extends LoggingRecipeSerializer<AgeableSeveringRecipe> {
    @Override
    public AgeableSeveringRecipe fromJson(ResourceLocation id, JsonObject json) {
      EntityIngredient ingredient = EntityIngredient.deserialize(JsonHelper.getElement(json, "entity"));
      ItemOutput adult = ItemOutput.fromJson(JsonHelper.getElement(json, "adult_result"));
      ItemOutput child = null;
      if (json.has("child_result")) {
        child = ItemOutput.fromJson(JsonHelper.getElement(json, "child_result"));
      }
      return new AgeableSeveringRecipe(id, ingredient, adult, child);
    }

    @Nullable
    @Override
    protected AgeableSeveringRecipe readSafe(ResourceLocation id, FriendlyByteBuf buffer) {
      EntityIngredient ingredient = EntityIngredient.read(buffer);
      ItemOutput adult = ItemOutput.read(buffer);
      ItemOutput child = null;
      if (buffer.readBoolean()) {
        child = ItemOutput.read(buffer);
      }
      return new AgeableSeveringRecipe(id, ingredient, adult, child);
    }

    @Override
    protected void writeSafe(FriendlyByteBuf buffer, AgeableSeveringRecipe recipe) {
      recipe.ingredient.write(buffer);
      recipe.output.write(buffer);
      if (recipe.childOutput == null) {
        buffer.writeBoolean(false);
      } else {
        buffer.writeBoolean(true);
        recipe.childOutput.write(buffer);
      }
    }
  }
}
