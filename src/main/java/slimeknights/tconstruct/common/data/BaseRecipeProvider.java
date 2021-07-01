package slimeknights.tconstruct.common.data;

import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.Tag.Named;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;
import net.minecraftforge.registries.IForgeRegistryEntry;
import slimeknights.mantle.recipe.data.ConsumerWrapperBuilder;
import slimeknights.mantle.registration.object.BuildingBlockObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;
import slimeknights.tconstruct.TConstruct;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Shared logic for each module's recipe provider
 */
public abstract class BaseRecipeProvider extends RecipeProvider implements IConditionBuilder {
  public BaseRecipeProvider(DataGenerator generator) {
    super(generator);
  }

  @Override
  protected abstract void buildShapelessRecipes(Consumer<FinishedRecipe> consumer);

  @Override
  public abstract String getName();


  /* Location helpers */

  /**
   * Gets a resource location for Tinkers
   * @param id  Location path
   * @return  Location for Tinkers
   */
  protected static ResourceLocation location(String id) {
    return new ResourceLocation(TConstruct.modID, id);
  }

  /**
   * Gets a resource location string for Tinkers
   * @param id  Location path
   * @return  Location for Tinkers
   */
  protected static String locationString(String id) {
    return TConstruct.modID + ":" + id;
  }

  /**
   * Prefixes the resource location path with the given value
   * @param item    Item registry name to use
   * @param prefix  Prefix value
   * @return  Resource location path
   */
  protected static ResourceLocation wrap(ItemLike item, String prefix, String suffix) {
    ResourceLocation loc = Objects.requireNonNull(item.asItem().getRegistryName());
    return location(prefix + loc.getPath() + suffix);
  }

  /**
   * Prefixes the resource location path with the given value
   * @param entry    Item registry name to use
   * @param prefix  Prefix value
   * @return  Resource location path
   */
  protected static ResourceLocation wrapR(Supplier<? extends IForgeRegistryEntry<?>> entry, String prefix, String suffix) {
    ResourceLocation loc = Objects.requireNonNull(entry.get().getRegistryName());
    return location(prefix + loc.getPath() + suffix);
  }

  /**
   * Prefixes the resource location path with the given value
   * @param item    Item registry name to use
   * @param prefix  Prefix value
   * @return  Resource location path
   */
  protected static ResourceLocation prefix(ItemLike item, String prefix) {
    ResourceLocation loc = Objects.requireNonNull(item.asItem().getRegistryName());
    return location(prefix + loc.getPath());
  }

  /**
   * Prefixes the resource location path with the given value
   * @param entry   Entry registry name to use
   * @param prefix  Prefix value
   * @return  Resource location path
   */
  protected static ResourceLocation prefixR(Supplier<? extends IForgeRegistryEntry<?>> entry, String prefix) {
    ResourceLocation loc = Objects.requireNonNull(entry.get().getRegistryName());
    return location(prefix + loc.getPath());
  }

  /**
   * Prefixes the resource location path with the given value
   * @param entry   Entry registry name to use
   * @param prefix  Prefix value
   * @return  Resource location path
   */
  protected static ResourceLocation prefixR(IForgeRegistryEntry<?> entry, String prefix) {
    ResourceLocation loc = Objects.requireNonNull(entry.getRegistryName());
    return location(prefix + loc.getPath());
  }

  /**
   * Gets a tag by name
   * @param modId  Mod ID for tag
   * @param name   Tag name
   * @return  Tag instance
   */
  protected static Named<Item> getTag(String modId, String name) {
    return ItemTags.bind(modId + ":" + name);
  }


  /* Recipe helpers */

  /**
   * Registers generic building block recipes for slabs and stairs
   * @param consumer  Recipe consumer
   * @param building  Building object instance
   */
  protected void registerSlabStair(Consumer<FinishedRecipe> consumer, BuildingBlockObject building, String folder, boolean addStonecutter) {
    Item item = building.asItem();
    CriterionTriggerInstance hasBlock = has(item);
    // slab
    ItemLike slab = building.getSlab();
    ShapedRecipeBuilder.shaped(slab, 6)
                       .define('B', item)
                       .pattern("BBB")
                       .unlockedBy("has_item", hasBlock)
                       .group(Objects.requireNonNull(slab.asItem().getRegistryName()).toString())
                       .save(consumer, wrap(item, folder, "_slab"));
    // stairs
    ItemLike stairs = building.getStairs();
    ShapedRecipeBuilder.shaped(stairs, 4)
                       .define('B', item)
                       .pattern("B  ")
                       .pattern("BB ")
                       .pattern("BBB")
                       .unlockedBy("has_item", hasBlock)
                       .group(Objects.requireNonNull(stairs.asItem().getRegistryName()).toString())
                       .save(consumer, wrap(item, folder, "_stairs"));

    // only add stonecutter if relevant
    if (addStonecutter) {
      Ingredient ingredient = Ingredient.of(item);
      SingleItemRecipeBuilder.stonecutting(ingredient, slab, 2)
                             .unlocks("has_item", hasBlock)
                             .save(consumer, wrap(item, folder, "_slab_stonecutter"));
      SingleItemRecipeBuilder.stonecutting(ingredient, stairs)
                             .unlocks("has_item", hasBlock)
                             .save(consumer, wrap(item, folder, "_stairs_stonecutter"));
    }
  }

  /**
   * Registers generic building block recipes for slabs, stairs, and walls
   * @param consumer  Recipe consumer
   * @param building  Building object instance
   */
  protected void registerSlabStairWall(Consumer<FinishedRecipe> consumer, WallBuildingBlockObject building, String folder, boolean addStonecutter) {
    registerSlabStair(consumer, building, folder, addStonecutter);
    // wall
    Item item = building.asItem();
    CriterionTriggerInstance hasBlock = has(item);
    ItemLike wall = building.getWall();
    ShapedRecipeBuilder.shaped(wall, 6)
                       .define('B', item)
                       .pattern("BBB")
                       .pattern("BBB")
                       .unlockedBy("has_item", hasBlock)
                       .group(Objects.requireNonNull(wall.asItem().getRegistryName()).toString())
                       .save(consumer, wrap(item, folder, "_wall"));
    // only add stonecutter if relevant
    if (addStonecutter) {
      Ingredient ingredient = Ingredient.of(item);
      SingleItemRecipeBuilder.stonecutting(ingredient, wall)
                             .unlocks("has_item", hasBlock)
                             .save(consumer, wrap(item, folder, "_wall_stonecutter"));
    }
  }

  /**
   * Registers a recipe packing a small item into a large one
   * @param consumer   Recipe consumer
   * @param large      Large item
   * @param small      Small item
   * @param largeName  Large name
   * @param smallName  Small name
   * @param folder     Recipe folder
   */
  protected void registerPackingRecipe(Consumer<FinishedRecipe> consumer, String largeName, ItemLike large, String smallName, ItemLike small, String folder) {
    // ingot to block
    ShapedRecipeBuilder.shaped(large)
                       .define('#', small)
                       .pattern("###")
                       .pattern("###")
                       .pattern("###")
                       .unlockedBy("has_item", has(small))
                       .group(Objects.requireNonNull(large.asItem().getRegistryName()).toString())
                       .save(consumer, wrap(large, folder, String.format("_from_%ss", smallName)));
    // block to ingot
    ShapelessRecipeBuilder.shapeless(small, 9)
                          .requires(large)
                          .unlockedBy("has_item", has(large))
                          .group(Objects.requireNonNull(small.asItem().getRegistryName()).toString())
                          .save(consumer, wrap(small, folder, String.format("_from_%s", largeName)));
  }

  /**
   * Registers a recipe packing a small item into a large one
   * @param consumer   Recipe consumer
   * @param largeItem  Large item
   * @param smallItem  Small item
   * @param smallTag   Tag for small item
   * @param largeName  Large name
   * @param smallName  Small name
   * @param folder     Recipe folder
   */
  protected void registerPackingRecipe(Consumer<FinishedRecipe> consumer, String largeName, ItemLike largeItem, String smallName, ItemLike smallItem, Tag<Item> smallTag, String folder) {
    // ingot to block
    // note our item is in the center, any mod allowed around the edges
    ShapedRecipeBuilder.shaped(largeItem)
                       .define('#', smallTag)
                       .define('*', smallItem)
                       .pattern("###")
                       .pattern("#*#")
                       .pattern("###")
                       .unlockedBy("has_item", has(smallItem))
                       .group(Objects.requireNonNull(largeItem.asItem().getRegistryName()).toString())
                       .save(consumer, wrap(largeItem, folder, String.format("_from_%ss", smallName)));
    // block to ingot
    ShapelessRecipeBuilder.shapeless(smallItem, 9)
                          .requires(largeItem)
                          .unlockedBy("has_item", has(largeItem))
                          .group(Objects.requireNonNull(smallItem.asItem().getRegistryName()).toString())
                          .save(consumer, wrap(smallItem, folder, String.format("_from_%s", largeName)));
  }


  /* conditions */

  /**
   * Creates a consumer instance with the added conditions
   * @param consumer    Base consumer
   * @param conditions  Extra conditions
   * @return  Wrapped consumer
   */
  protected static Consumer<FinishedRecipe> withCondition(Consumer<FinishedRecipe> consumer, ICondition... conditions) {
    ConsumerWrapperBuilder builder = ConsumerWrapperBuilder.wrap();
    for (ICondition condition : conditions) {
      builder.addCondition(condition);
    }
    return builder.build(consumer);
  }

  /**
   * Creates a condition for a tag existing
   * @param name  Forge tag name
   * @return  Condition for tag existing
   */
  protected static ICondition tagCondition(String name) {
    return new NotCondition(new TagEmptyCondition("forge", name));
  }
}
