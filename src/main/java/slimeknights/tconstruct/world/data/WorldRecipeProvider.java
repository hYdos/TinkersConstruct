package slimeknights.tconstruct.world.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import slimeknights.tconstruct.common.data.BaseRecipeProvider;
import slimeknights.tconstruct.common.registration.WoodBlockObject;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.world.TinkerWorld;

import java.util.function.Consumer;

public class WorldRecipeProvider extends BaseRecipeProvider {
  public WorldRecipeProvider(DataGenerator generator) {
    super(generator);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct World Recipes";
  }

  @Override
  protected void buildShapelessRecipes(Consumer<FinishedRecipe> consumer) {
    // Add recipe for all slimeball <-> congealed and slimeblock <-> slimeball
    // only earth slime recipe we need here slime
    ShapedRecipeBuilder.shaped(TinkerWorld.congealedSlime.get(SlimeType.EARTH))
                       .define('#', SlimeType.EARTH.getSlimeBallTag())
                       .patternLine("##")
                       .patternLine("##")
                       .addCriterion("has_item", has(SlimeType.EARTH.getSlimeBallTag()))
                       .setGroup("tconstruct:congealed_slime")
                       .build(consumer, location("common/slime/earth/congealed"));

    // does not need green as its the fallback
    for (SlimeType slimeType : SlimeType.TINKER) {
      ResourceLocation name = location("common/slime/" + slimeType.getSerializedName() + "/congealed");
      ShapedRecipeBuilder.shaped(TinkerWorld.congealedSlime.get(slimeType))
                         .define('#', slimeType.getSlimeBallTag())
                         .patternLine("##")
                         .patternLine("##")
                         .addCriterion("has_item", has(slimeType.getSlimeBallTag()))
                         .setGroup("tconstruct:congealed_slime")
                         .build(consumer, name);
      ResourceLocation blockName = location("common/slime/" + slimeType.getSerializedName() + "/slimeblock");
      ShapedRecipeBuilder.shaped(TinkerWorld.slime.get(slimeType))
                         .define('#', slimeType.getSlimeBallTag())
                         .patternLine("###")
                         .patternLine("###")
                         .patternLine("###")
                         .addCriterion("has_item", has(slimeType.getSlimeBallTag()))
                         .setGroup("slime_blocks")
                         .build(consumer, blockName);
      // green already can craft into slime balls
      ShapelessRecipeBuilder.shapeless(TinkerCommons.slimeball.get(slimeType), 9)
                            .requires(TinkerWorld.slime.get(slimeType))
                            .unlockedBy("has_item", has(TinkerWorld.slime.get(slimeType)))
                            .group("tconstruct:slime_balls")
                            .save(consumer, "tconstruct:common/slime/" + slimeType.getSerializedName() + "/slimeball_from_block");
    }
    // all types of congealed need a recipe to a block
    for (SlimeType slimeType : SlimeType.values()) {
      ShapelessRecipeBuilder.shapeless(TinkerCommons.slimeball.get(slimeType), 4)
                            .requires(TinkerWorld.congealedSlime.get(slimeType))
                            .unlockedBy("has_item", has(TinkerWorld.congealedSlime.get(slimeType)))
                            .group("tconstruct:slime_balls")
                            .save(consumer, "tconstruct:common/slime/" + slimeType.getSerializedName() + "/slimeball_from_congealed");
    }

    // craft other slime based items, forge does not automatically add recipes using the tag anymore
    ShapedRecipeBuilder.shaped(Blocks.STICKY_PISTON)
                       .pattern("#")
                       .pattern("P")
                       .define('#', Tags.Items.SLIMEBALLS)
                       .define('P', Blocks.PISTON)
                       .unlockedBy("has_slime_ball", has(Tags.Items.SLIMEBALLS))
                       .save(consumer, location("common/slime/sticky_piston"));
    ShapedRecipeBuilder.shaped(Items.LEAD, 2)
                       .define('~', Items.STRING)
                       .define('O', Tags.Items.SLIMEBALLS)
                       .pattern("~~ ")
                       .pattern("~O ")
                       .pattern("  ~")
                       .unlockedBy("has_slime_ball", has(Tags.Items.SLIMEBALLS))
                       .save(consumer, location("common/slime/lead"));
    ShapelessRecipeBuilder.shapeless(Items.MAGMA_CREAM)
                          .requires(Items.BLAZE_POWDER)
                          .requires(Tags.Items.SLIMEBALLS)
                          .unlockedBy("has_blaze_powder", has(Items.BLAZE_POWDER))
                          .save(consumer, location("common/slime/magma_cream"));

    // wood
    String woodFolder = "world/wood/";
    registerWoodRecipes(consumer, TinkerWorld.greenheart,  woodFolder + "greenheart/");
    registerWoodRecipes(consumer, TinkerWorld.skyroot,     woodFolder + "skyroot/");
    registerWoodRecipes(consumer, TinkerWorld.bloodshroom, woodFolder + "bloodshroom/");
  }

  /**
   * Registers recipes relevant to wood
   * @param consumer  Recipe consumer
   * @param wood      Wood types
   * @param folder    Wood folder
   */
  private void registerWoodRecipes(Consumer<FinishedRecipe> consumer, WoodBlockObject wood, String folder) {
    // planks
    ShapelessRecipeBuilder.shapeless(wood, 4).requires(wood.getLogItemTag())
                          .setGroup("planks")
                          .addCriterion("has_log", has(wood.getLogItemTag()))
                          .build(consumer, location(folder + "planks"));
    ShapedRecipeBuilder.shaped(wood.getSlab(), 6)
                       .define('#', wood)
                       .pattern("###")
                       .group("wooden_slab")
                       .unlockedBy("has_planks", has(wood))
                       .save(consumer, location(folder + "slab"));
    ShapedRecipeBuilder.shaped(wood.getStairs(), 4)
                       .define('#', wood)
                       .pattern("#  ").pattern("## ").pattern("###")
                       .group("wooden_stairs").unlockedBy("has_planks", has(wood))
                       .save(consumer, location(folder + "stairs"));
    // log to stripped
    ShapedRecipeBuilder.shaped(wood.getWood(), 3)
                       .define('#', wood.getLog())
                       .pattern("##").pattern("##")
                       .group("bark")
                       .unlockedBy("has_log", has(wood.getLog()))
                       .save(consumer, location(folder + "log_to_wood"));
    ShapedRecipeBuilder.shaped(wood.getStrippedWood(), 3)
                       .define('#', wood.getStrippedLog())
                       .pattern("##").pattern("##")
                       .group("bark")
                       .unlockedBy("has_log", has(wood.getStrippedLog()))
                       .save(consumer, location(folder + "stripped_log_to_wood"));
    // doors
    ShapedRecipeBuilder.shaped(wood.getFence(), 3)
                       .define('#', Tags.Items.RODS_WOODEN).define('W', wood)
                       .pattern("W#W").pattern("W#W")
                       .group("wooden_fence")
                       .unlockedBy("has_planks", has(wood))
                       .save(consumer, location(folder + "fence"));
    ShapedRecipeBuilder.shaped(wood.getFenceGate())
                       .define('#', Items.STICK).define('W', wood)
                       .pattern("#W#").pattern("#W#")
                       .group("wooden_fence_gate")
                       .unlockedBy("has_planks", has(wood))
                       .save(consumer, location(folder + "fence_gate"));
    ShapedRecipeBuilder.shaped(wood.getDoor(), 3)
                       .define('#', wood)
                       .pattern("##").pattern("##").pattern("##")
                       .group("wooden_door")
                       .unlockedBy("has_planks", has(wood))
                       .save(consumer, location(folder + "door"));
    ShapedRecipeBuilder.shaped(wood.getTrapdoor(), 2)
                       .define('#', wood)
                       .pattern("###").pattern("###")
                       .group("wooden_trapdoor")
                       .unlockedBy("has_planks", has(wood))
                       .save(consumer, location(folder + "trapdoor"));
    // buttons
    ShapelessRecipeBuilder.shapeless(wood.getButton())
                          .requires(wood)
                          .group("wooden_button")
                          .unlockedBy("has_planks", has(wood))
                          .save(consumer, location(folder + "button"));
    ShapedRecipeBuilder.shaped(wood.getPressurePlate())
                       .define('#', wood)
                       .pattern("##")
                       .group("wooden_pressure_plate")
                       .unlockedBy("has_planks", has(wood))
                       .save(consumer, location(folder + "pressure_plate"));
  }
}
