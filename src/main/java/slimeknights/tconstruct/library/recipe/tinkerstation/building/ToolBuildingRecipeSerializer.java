package slimeknights.tconstruct.library.recipe.tinkerstation.building;

import com.google.gson.JsonObject;
import slimeknights.mantle.recipe.RecipeHelper;
import slimeknights.tconstruct.common.recipe.LoggingRecipeSerializer;
import slimeknights.tconstruct.library.tools.item.ToolCore;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class ToolBuildingRecipeSerializer extends LoggingRecipeSerializer<ToolBuildingRecipe> {

  @Override
  public ToolBuildingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
    String group = GsonHelper.getAsString(json, "group", "");
    // output fetch as a toolcore item, its an error if it does not implement that interface
    ToolCore item = RecipeHelper.deserializeItem(GsonHelper.getAsString(json, "result"), "result", ToolCore.class);
    return new ToolBuildingRecipe(recipeId, group, item);
  }

  @Nullable
  @Override
  protected ToolBuildingRecipe readSafe(ResourceLocation recipeId, FriendlyByteBuf buffer) {
    String group = buffer.readUtf(Short.MAX_VALUE);
    ToolCore result = RecipeHelper.readItem(buffer, ToolCore.class);
    return new ToolBuildingRecipe(recipeId, group, result);
  }

  @Override
  protected void writeSafe(FriendlyByteBuf buffer, ToolBuildingRecipe recipe) {
    buffer.writeUtf(recipe.group);
    RecipeHelper.writeItem(buffer, recipe.output);
  }
}
