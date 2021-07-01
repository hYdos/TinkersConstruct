package slimeknights.tconstruct.library.recipe.partbuilder;

import com.google.gson.JsonObject;
import slimeknights.mantle.recipe.RecipeHelper;
import slimeknights.tconstruct.common.recipe.LoggingRecipeSerializer;
import slimeknights.tconstruct.library.tinkering.IMaterialItem;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class PartRecipeSerializer extends LoggingRecipeSerializer<PartRecipe> {
  @Override
  public PartRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
    String group = GsonHelper.getAsString(json, "group", "");
    Pattern pattern = new Pattern(GsonHelper.getAsString(json, "pattern"));
    int cost = GsonHelper.getAsInt(json, "cost");

    // output fetch as a material item, its an error if it does not implement that interface
    JsonObject output = GsonHelper.getAsJsonObject(json, "result");
    IMaterialItem item = RecipeHelper.deserializeItem(GsonHelper.getAsString(output, "item"), "result", IMaterialItem.class);
    int count = GsonHelper.getAsInt(output, "count", 1);

    return new PartRecipe(recipeId, group, pattern, cost, item, count);
  }

  @Nullable
  @Override
  protected PartRecipe readSafe(ResourceLocation recipeId, FriendlyByteBuf buffer) {
    String group = buffer.readUtf(Short.MAX_VALUE);
    Pattern pattern = new Pattern(buffer.readUtf(Short.MAX_VALUE));
    int cost = buffer.readInt();
    // output must be a material item
    IMaterialItem item = RecipeHelper.readItem(buffer, IMaterialItem.class);
    int count = buffer.readByte();
    return new PartRecipe(recipeId, group, pattern, cost, item, count);
  }

  @Override
  protected void writeSafe(FriendlyByteBuf buffer, PartRecipe recipe) {
    buffer.writeUtf(recipe.group);
    buffer.writeUtf(recipe.pattern.toString());
    buffer.writeInt(recipe.cost);
    RecipeHelper.writeItem(buffer, recipe.output);
    buffer.writeByte(recipe.outputCount);
  }
}
