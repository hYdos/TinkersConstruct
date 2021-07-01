package slimeknights.tconstruct.library.recipe.material;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import slimeknights.mantle.recipe.ItemOutput;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.tconstruct.common.recipe.LoggingRecipeSerializer;
import slimeknights.tconstruct.library.materials.MaterialId;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Serialiser for {@link MaterialRecipe}
 */
public class MaterialRecipeSerializer extends LoggingRecipeSerializer<MaterialRecipe> {
  private static final ItemOutput EMPTY = ItemOutput.fromStack(ItemStack.EMPTY);

  /**
   * Gets a material ID from JSON
   * @param json  Json parent
   * @param key  Key to get
   * @return  Material id
   */
  public static MaterialId getMaterial(JsonObject json, String key) {
    String materialId = GsonHelper.getAsString(json, key);
    if (materialId.isEmpty()) {
      throw new JsonSyntaxException("Material ID at " + key + " must not be empty");
    }
    return new MaterialId(materialId);
  }

  @Override
  public MaterialRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
    String group = GsonHelper.getAsString(json, "group", "");
    Ingredient ingredient = Ingredient.fromJson(JsonHelper.getElement(json, "ingredient"));
    int value = GsonHelper.getAsInt(json, "value", 1);
    int needed = GsonHelper.getAsInt(json, "needed", 1);
    MaterialId materialId = getMaterial(json, "material");
    ItemOutput leftover = EMPTY;
    if (value > 1 && json.has("leftover")) {
      leftover = ItemOutput.fromJson(json.get("leftover"));
    }
    return new MaterialRecipe(recipeId, group, ingredient, value, needed, new MaterialId(materialId), leftover);
  }

  @Nullable
  @Override
  protected MaterialRecipe readSafe(ResourceLocation recipeId, FriendlyByteBuf buffer) {
    String group = buffer.readUtf(Short.MAX_VALUE);
    Ingredient ingredient = Ingredient.fromNetwork(buffer);
    int value = buffer.readInt();
    int needed = buffer.readInt();
    String materialId = buffer.readUtf(Short.MAX_VALUE);
    ItemOutput leftover = ItemOutput.read(buffer);
    return new MaterialRecipe(recipeId, group, ingredient, value, needed, new MaterialId(materialId), leftover);
  }

  @Override
  protected void writeSafe(FriendlyByteBuf buffer, MaterialRecipe recipe) {
    buffer.writeUtf(recipe.group);
    recipe.ingredient.toNetwork(buffer);
    buffer.writeInt(recipe.value);
    buffer.writeInt(recipe.needed);
    buffer.writeUtf(recipe.materialId.toString());
    recipe.leftover.write(buffer);
  }
}
