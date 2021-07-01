package slimeknights.tconstruct.shared.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.mantle.recipe.data.ConsumerWrapperBuilder;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.data.BaseRecipeProvider;
import slimeknights.tconstruct.common.json.ConfigEnabledCondition;
import slimeknights.tconstruct.common.registration.MetalItemObject;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.library.materials.MaterialValues;
import slimeknights.tconstruct.library.recipe.casting.ItemCastingRecipeBuilder;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.TinkerMaterials;
import slimeknights.tconstruct.shared.block.ClearStainedGlassBlock.GlassColor;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.world.TinkerWorld;

import java.util.function.Consumer;

public class CommonRecipeProvider extends BaseRecipeProvider {
  public CommonRecipeProvider(DataGenerator generator) {
    super(generator);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Common Recipes";
  }

  @Override
  protected void buildShapelessRecipes(Consumer<FinishedRecipe> consumer) {
    this.addCommonRecipes(consumer);
    this.addMaterialRecipes(consumer);
  }

  private void addCommonRecipes(Consumer<FinishedRecipe> consumer) {
    // firewood and lavawood
    String folder = "common/firewood/";
    registerSlabStair(consumer, TinkerCommons.blazewood, folder, false);
    registerSlabStair(consumer, TinkerCommons.lavawood, folder, false);

    // mud bricks
    registerSlabStair(consumer, TinkerCommons.mudBricks, "common/", false);

    // book
    ShapelessRecipeBuilder.shapeless(TinkerCommons.materialsAndYou)
                          .requires(Items.BOOK)
                          .requires(TinkerTables.pattern)
                          .unlockedBy("has_item", has(TinkerTables.pattern))
                          .save(consumer, prefix(TinkerCommons.materialsAndYou, "common/"));
    ShapelessRecipeBuilder.shapeless(TinkerCommons.tinkersGadgetry)
                          .requires(Items.BOOK)
                          .requires(TinkerTags.Items.SKY_SLIMEBALL)
                          .unlockedBy("has_item", has(TinkerTags.Items.SKY_SLIMEBALL))
                          .save(consumer, prefix(TinkerCommons.tinkersGadgetry, "common/"));
    ShapelessRecipeBuilder.shapeless(TinkerCommons.punySmelting)
                          .requires(Items.BOOK)
                          .requires(TinkerSmeltery.grout)
                          .unlockedBy("has_item", has(TinkerSmeltery.grout))
                          .save(consumer, prefix(TinkerCommons.punySmelting, "common/"));
    ItemCastingRecipeBuilder.tableRecipe(TinkerCommons.mightySmelting)
                            .setFluidAndTime(new FluidStack(TinkerFluids.searedStone.get(), MaterialValues.INGOT))
                            .setCast(Items.BOOK, true)
                            .build(consumer, prefix(TinkerCommons.mightySmelting, "common/"));
    ShapelessRecipeBuilder.shapeless(TinkerCommons.fantasticFoundry)
                          .requires(Items.BOOK)
                          .requires(TinkerSmeltery.netherGrout)
                          .unlockedBy("has_item", has(TinkerSmeltery.netherGrout))
                          .save(consumer, prefix(TinkerCommons.fantasticFoundry, "common/"));
    ShapelessRecipeBuilder.shapeless(TinkerCommons.encyclopedia)
                          .requires(Items.BOOK)
                          .requires(SlimeType.EARTH.getSlimeBallTag())
                          .addIngredient(SlimeType.SKY.getSlimeBallTag())
                          .addIngredient(SlimeType.BLOOD.getSlimeBallTag())
                          .addIngredient(Items.MAGMA_CREAM)
                          .addIngredient(SlimeType.ICHOR.getSlimeBallTag())
                          .addIngredient(SlimeType.ENDER.getSlimeBallTag())
                          .addCriterion("has_item", has(SlimeType.ENDER.getSlimeBallTag()))
                          .build(consumer, prefix(TinkerCommons.encyclopedia, "common/"));

    // glass
    folder = "common/glass/";
    ShapedRecipeBuilder.shaped(TinkerCommons.clearGlassPane, 16)
                       .define('#', TinkerCommons.clearGlass)
                       .pattern("###")
                       .pattern("###")
                       .unlockedBy("has_block", has(TinkerCommons.clearGlass))
                       .save(consumer, prefix(TinkerCommons.clearGlassPane, folder));
    for (GlassColor color : GlassColor.values()) {
      Block block = TinkerCommons.clearStainedGlass.get(color);
      ShapedRecipeBuilder.shaped(block, 8)
                         .define('#', TinkerCommons.clearGlass)
                         .define('X', color.getDye().getTag())
                         .pattern("###")
                         .pattern("#X#")
                         .pattern("###")
                         .group(locationString("stained_clear_glass"))
                         .unlockedBy("has_clear_glass", has(TinkerCommons.clearGlass))
                         .save(consumer, prefix(block, folder));
      Block pane = TinkerCommons.clearStainedGlassPane.get(color);
      ShapedRecipeBuilder.shaped(pane, 16)
                         .define('#', block)
                         .pattern("###")
                         .pattern("###")
                         .group(locationString("stained_clear_glass_pane"))
                         .unlockedBy("has_block", has(block))
                         .save(consumer, prefix(pane, folder));
      ShapedRecipeBuilder.shaped(pane, 8)
                         .define('#', TinkerCommons.clearGlassPane)
                         .define('X', color.getDye().getTag())
                         .pattern("###")
                         .pattern("#X#")
                         .pattern("###")
                         .group(locationString("stained_clear_glass_pane"))
                         .unlockedBy("has_clear_glass", has(TinkerCommons.clearGlassPane))
                         .save(consumer, wrap(pane, folder, "_from_panes"));
    }

    // vanilla recipes
    ShapelessRecipeBuilder.shapeless(Items.FLINT)
                          .requires(Blocks.GRAVEL)
                          .requires(Blocks.GRAVEL)
                          .requires(Blocks.GRAVEL)
                          .unlockedBy("has_item", has(Blocks.GRAVEL))
                          .save(
                            ConsumerWrapperBuilder.wrap()
                                                  .addCondition(ConfigEnabledCondition.GRAVEL_TO_FLINT)
                                                  .build(consumer),
                            location("common/flint"));
  }

  private void addMaterialRecipes(Consumer<FinishedRecipe> consumer) {
    String folder = "common/materials/";

    // ores
    registerMineralRecipes(consumer, TinkerMaterials.copper, folder);
    registerMineralRecipes(consumer, TinkerMaterials.cobalt, folder);
    // tier 3
    registerMineralRecipes(consumer, TinkerMaterials.slimesteel,    folder);
    registerMineralRecipes(consumer, TinkerMaterials.tinkersBronze, folder);
    registerMineralRecipes(consumer, TinkerMaterials.roseGold,      folder);
    registerMineralRecipes(consumer, TinkerMaterials.pigIron,       folder);
    // tier 4
    registerMineralRecipes(consumer, TinkerMaterials.queensSlime, folder);
    registerMineralRecipes(consumer, TinkerMaterials.manyullyn,   folder);
    registerMineralRecipes(consumer, TinkerMaterials.hepatizon,   folder);
    //registerMineralRecipes(consumer, TinkerMaterials.soulsteel,   folder);
    registerPackingRecipe(consumer, "ingot", Items.NETHERITE_INGOT, "nugget", TinkerMaterials.netheriteNugget, folder);
    // tier 5
    //registerMineralRecipes(consumer, TinkerMaterials.knightslime, folder);

    // smelt ore into ingots, must use a blast furnace for nether ores
    ItemLike cobaltIngot = TinkerMaterials.cobalt.getIngot();
    SimpleCookingRecipeBuilder.blasting(Ingredient.of(TinkerWorld.cobaltOre), cobaltIngot, 1.5f, 200)
                        .unlockedBy("has_item", has(TinkerWorld.cobaltOre))
                        .save(consumer, wrap(cobaltIngot, folder, "_smelting"));
    ItemLike copperIngot = TinkerMaterials.copper.getIngot();
    SimpleCookingRecipeBuilder.smelting(Ingredient.of(TinkerWorld.copperOre), copperIngot, 1.5f, 200)
                        .unlockedBy("has_item", has(TinkerWorld.copperOre))
                        .save(consumer, wrap(copperIngot, folder, "_smelting"));
    SimpleCookingRecipeBuilder.blasting(Ingredient.of(TinkerWorld.copperOre), copperIngot, 1.5f, 100)
                        .unlockedBy("has_item", has(TinkerWorld.copperOre))
                        .save(consumer, wrap(copperIngot, folder, "_blasting"));
  }

  /**
   * Adds recipes to convert a block to ingot, ingot to block, and for nuggets
   * @param consumer  Recipe consumer
   * @param metal     Metal object
   * @param folder    Folder for recipes
   */
  protected void registerMineralRecipes(Consumer<FinishedRecipe> consumer, MetalItemObject metal, String folder) {
    ItemLike ingot = metal.getIngot();
    registerPackingRecipe(consumer, "block", metal.get(), "ingot", ingot, metal.getIngotTag(), folder);
    registerPackingRecipe(consumer, "ingot", ingot, "nugget", metal.getNugget(), metal.getNuggetTag(), folder);
  }
}
