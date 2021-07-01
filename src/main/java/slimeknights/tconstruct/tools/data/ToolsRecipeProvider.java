package slimeknights.tconstruct.tools.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.mantle.recipe.ItemOutput;
import slimeknights.mantle.recipe.data.CompoundIngredient;
import slimeknights.mantle.recipe.ingredient.IngredientWithout;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.data.BaseRecipeProvider;
import slimeknights.tconstruct.common.registration.CastItemObject;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.library.materials.MaterialId;
import slimeknights.tconstruct.library.materials.MaterialValues;
import slimeknights.tconstruct.library.recipe.casting.ItemCastingRecipeBuilder;
import slimeknights.tconstruct.library.recipe.casting.material.CompositeCastingRecipeBuilder;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialCastingRecipeBuilder;
import slimeknights.tconstruct.library.recipe.ingredient.MaterialIngredient;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeBuilder;
import slimeknights.tconstruct.library.recipe.molding.MoldingRecipeBuilder;
import slimeknights.tconstruct.library.recipe.partbuilder.PartRecipeBuilder;
import slimeknights.tconstruct.library.recipe.tinkerstation.building.ToolBuildingRecipeBuilder;
import slimeknights.tconstruct.library.tinkering.IMaterialItem;
import slimeknights.tconstruct.library.tools.item.ToolCore;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.tools.TinkerToolParts;
import slimeknights.tconstruct.tools.TinkerTools;
import slimeknights.tconstruct.world.TinkerWorld;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ToolsRecipeProvider extends BaseRecipeProvider {
  public ToolsRecipeProvider(DataGenerator generator) {
    super(generator);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Tool Recipes";
  }

  @Override
  protected void buildShapelessRecipes(Consumer<FinishedRecipe> consumer) {
    this.addMaterialsRecipes(consumer);
    this.addPartRecipes(consumer);
    this.addTinkerStationRecipes(consumer);
  }

  private void addPartRecipes(Consumer<FinishedRecipe> consumer) {
    addPartRecipe(consumer, TinkerToolParts.repairKit, 2, TinkerSmeltery.repairKitCast);
    // head
    addPartRecipe(consumer, TinkerToolParts.pickaxeHead, 2, TinkerSmeltery.pickaxeHeadCast);
    addPartRecipe(consumer, TinkerToolParts.hammerHead, 8, TinkerSmeltery.hammerHeadCast);
    addPartRecipe(consumer, TinkerToolParts.smallAxeHead, 2, TinkerSmeltery.smallAxeHeadCast);
    addPartRecipe(consumer, TinkerToolParts.broadAxeHead, 8, TinkerSmeltery.broadAxeHeadCast);
    addPartRecipe(consumer, TinkerToolParts.smallBlade, 2, TinkerSmeltery.smallBladeCast);
    addPartRecipe(consumer, TinkerToolParts.broadBlade, 8, TinkerSmeltery.broadBladeCast);
    // other parts
    addPartRecipe(consumer, TinkerToolParts.toolBinding, 1, TinkerSmeltery.toolBindingCast);
    addPartRecipe(consumer, TinkerToolParts.largePlate, 4, TinkerSmeltery.largePlateCast);
    addPartRecipe(consumer, TinkerToolParts.toolHandle, 1, TinkerSmeltery.toolHandleCast);
    addPartRecipe(consumer, TinkerToolParts.toughHandle, 3, TinkerSmeltery.toughHandleCast);
  }

  private void addMaterialsRecipes(Consumer<FinishedRecipe> consumer) {
    // tier 1
    registerMaterial(consumer, MaterialIds.wood, Ingredient.of(Tags.Items.RODS_WOODEN), 1, 2, "wood/sticks");
    registerMaterial(consumer, MaterialIds.wood, new IngredientWithout(Ingredient.of(ItemTags.PLANKS), Ingredient.of(TinkerTags.Items.SLIMY_PLANKS)),
                     1, 1, "wood/planks");
    registerMaterial(consumer, MaterialIds.wood, new IngredientWithout(Ingredient.of(ItemTags.LOGS), Ingredient.of(TinkerTags.Items.SLIMY_LOGS)),
                     4, 1, ItemOutput.fromStack(new ItemStack(Items.STICK, 2)), "wood/logs");
    registerMaterial(consumer, MaterialIds.stone, CompoundIngredient.from(
      Ingredient.of(Tags.Items.STONE), Ingredient.of(Tags.Items.COBBLESTONE), Ingredient.of(Blocks.BLACKSTONE, Blocks.POLISHED_BLACKSTONE)), 1, 1, "stone");
    registerMaterial(consumer, MaterialIds.flint, Ingredient.of(Items.FLINT, Blocks.BASALT, Blocks.POLISHED_BASALT), 1, 1, "flint");
    registerMaterial(consumer, MaterialIds.bone, Ingredient.of(Tags.Items.BONES), 1, 1, "bone");
    registerMaterial(consumer, MaterialIds.necroticBone, Ingredient.of(TinkerTags.Items.WITHER_BONES), 1, 1, "necrotic_bone");
    // tier 2
    registerMetalMaterial(consumer, MaterialIds.iron, "iron", false);
    registerMaterial(consumer, MaterialIds.searedStone, Ingredient.of(TinkerSmeltery.searedBrick), 1, 2, "seared_stone/brick");
    registerMaterial(consumer, MaterialIds.searedStone, Ingredient.of(TinkerTags.Items.SEARED_BLOCKS), 2, 1, ItemOutput.fromItem(TinkerSmeltery.searedBrick), "seared_stone/block");
    registerMaterial(consumer, MaterialIds.scorchedStone, Ingredient.of(TinkerSmeltery.scorchedBrick), 1, 2, "scorched_stone/brick");
    registerMaterial(consumer, MaterialIds.scorchedStone, Ingredient.of(TinkerTags.Items.SCORCHED_BLOCKS), 2, 1, ItemOutput.fromItem(TinkerSmeltery.scorchedBrick), "scorched_stone/block");
    registerMetalMaterial(consumer, MaterialIds.copper, "copper", false);
    registerMaterial(consumer, MaterialIds.slimewood, Ingredient.of(TinkerTags.Items.SLIMY_PLANKS), 1, 1, "slimewood/planks");
    registerMaterial(consumer, MaterialIds.slimewood, Ingredient.of(TinkerWorld.greenheart.getLogItemTag()), 4, 1, ItemOutput.fromItem(TinkerWorld.greenheart), "slimewood/greenheart_logs");
    registerMaterial(consumer, MaterialIds.slimewood, Ingredient.of(TinkerWorld.skyroot.getLogItemTag()), 4, 1, ItemOutput.fromItem(TinkerWorld.skyroot), "slimewood/skyroot_logs");
    registerMaterial(consumer, MaterialIds.slimewood, Ingredient.of(TinkerWorld.bloodshroom.getLogItemTag()), 4, 1, ItemOutput.fromItem(TinkerWorld.bloodshroom), "slimewood/bloodshroom_logs");
    registerMetalMaterial(consumer, MaterialIds.roseGold, "rose_gold", false);
    // tier 3
    registerMetalMaterial(consumer, MaterialIds.slimesteel, "slimesteel", false);
    registerMaterial(consumer, MaterialIds.nahuatl, Ingredient.of(Items.OBSIDIAN), 1, 1, "nahuatl");
    registerMetalMaterial(consumer, MaterialIds.tinkersBronze, "silicon_bronze", false);
    registerMetalMaterial(consumer, MaterialIds.pigIron, "pig_iron", false);
    registerMaterial(consumer, MaterialIds.pigIron, Ingredient.of(TinkerCommons.bacon), 1, 4, "pig_iron/bacon");

    // tier 2 (nether)
    // tier 3 (nether)
    registerMetalMaterial(consumer, MaterialIds.cobalt, "cobalt", false);
    // tier 4
    registerMetalMaterial(consumer, MaterialIds.queensSlime, "queens_slime", false);
    registerMetalMaterial(consumer, MaterialIds.manyullyn,   "manyullyn",    false);
    registerMetalMaterial(consumer, MaterialIds.hepatizon,   "hepatizon",    false);
    //registerMetalMaterial(consumer, MaterialIds.soulsteel,   "soulsteel",    false);

    // tier 2 (end)
    //registerMaterial(consumer, MaterialIds.endstone, Ingredient.fromItems(Blocks.END_STONE), 1, 1, "endstone");

    // tier 2 (mod compat)
    registerMetalMaterial(consumer, MaterialIds.silver,   "silver",  true);
    registerMetalMaterial(consumer, MaterialIds.lead,     "lead",     true);
    registerMetalMaterial(consumer, MaterialIds.electrum, "electrum", true);
    // tier 3 (mod integration)
    registerMetalMaterial(consumer, MaterialIds.bronze,     "bronze",     true);
    registerMetalMaterial(consumer, MaterialIds.steel,      "steel",      true);
    registerMetalMaterial(consumer, MaterialIds.constantan, "constantan", true);

    //registerMaterial(consumer, MaterialIds.string, Ingredient.fromTag(Tags.Items.STRING), 1, 1, "string");
    //registerMaterial(consumer, MaterialIds.slimevine_sky, Ingredient.fromItems(TinkerWorld.skySlimeVine), 1, 1, "slimevine_sky");
    //registerMaterial(consumer, MaterialIds.slimevine_ender, Ingredient.fromItems(TinkerWorld.enderSlimeVine), 1, 1, "slimevine_ender");
  }

  private void addTinkerStationRecipes(Consumer<FinishedRecipe> consumer) {
    registerBuildingRecipe(consumer, TinkerTools.pickaxe);
    registerBuildingRecipe(consumer, TinkerTools.sledgeHammer);
    registerBuildingRecipe(consumer, TinkerTools.veinHammer);

    registerBuildingRecipe(consumer, TinkerTools.mattock);
    registerBuildingRecipe(consumer, TinkerTools.excavator);

    registerBuildingRecipe(consumer, TinkerTools.handAxe);
    registerBuildingRecipe(consumer, TinkerTools.broadAxe);

    registerBuildingRecipe(consumer, TinkerTools.kama);
    registerBuildingRecipe(consumer, TinkerTools.scythe);

    registerBuildingRecipe(consumer, TinkerTools.dagger);
    registerBuildingRecipe(consumer, TinkerTools.sword);
    registerBuildingRecipe(consumer, TinkerTools.cleaver);
  }

  private void registerBuildingRecipe(Consumer<FinishedRecipe> consumer, Supplier<? extends ToolCore> sup) {
    // Base data
    ToolCore toolCore = sup.get();
    String name = Objects.requireNonNull(toolCore.getRegistryName()).getPath();

    ToolBuildingRecipeBuilder.toolBuildingRecipe(toolCore)
      .build(consumer, location("tools/building/" + name));
  }


  /* Helpers */


  /**
   * Adds a recipe to craft a part
   * @param consumer  Recipe consumer
   * @param sup       Part to be crafted
   * @param cost      Part cost
   * @param cast      Part cast
   */
  private void addPartRecipe(Consumer<FinishedRecipe> consumer, Supplier<? extends IMaterialItem> sup, int cost, CastItemObject cast) {
    String folder = "tools/parts/";
    // Base data
    IMaterialItem part = sup.get();
    String name = Objects.requireNonNull(part.asItem().getRegistryName()).getPath();

    // Part Builder
    PartRecipeBuilder.partRecipe(part)
                     .setPattern(location(name))
                     .setCost(cost)
                     .build(consumer, location(folder + "builder/" + name));

    // Material Casting
    String castingFolder = folder + "casting/";
    MaterialCastingRecipeBuilder.tableRecipe(part)
                                .setItemCost(cost)
                                .setCast(cast.getMultiUseTag(), false)
                                .build(consumer, location(castingFolder + name + "_gold_cast"));
    MaterialCastingRecipeBuilder.tableRecipe(part)
                                .setItemCost(cost)
                                .setCast(cast.getSingleUseTag(), true)
                                .build(consumer, location(castingFolder + name + "_sand_cast"));
    CompositeCastingRecipeBuilder.table(part, cost)
                                 .build(consumer, location(castingFolder + name + "_composite"));

    // Cast Casting
    MaterialIngredient ingredient = MaterialIngredient.fromItem(part);
    String partName = Objects.requireNonNull(part.asItem().getRegistryName()).getPath();
    ItemCastingRecipeBuilder.tableRecipe(cast)
                            .setFluidAndTime(new FluidStack(TinkerFluids.moltenGold.get(), MaterialValues.INGOT))
                            .setCast(ingredient, true)
                            .setSwitchSlots()
                            .build(consumer, location("smeltery/casting/casts/" + partName));

    // sand cast molding
    MoldingRecipeBuilder.moldingTable(cast.getSand())
                        .setMaterial(TinkerSmeltery.blankCast.getSand())
                        .setPattern(ingredient, false)
                        .build(consumer, location("smeltery/casting/sand_casts/" + partName));
    MoldingRecipeBuilder.moldingTable(cast.getRedSand())
                        .setMaterial(TinkerSmeltery.blankCast.getRedSand())
                        .setPattern(ingredient, false)
                        .build(consumer, location("smeltery/casting/red_sand_casts/" + partName));
  }

  /**
   * Registers a material recipe
   * @param consumer  Recipe consumer
   * @param material  Material ID
   * @param input     Recipe input
   * @param value     Material value
   * @param needed    Number of items needed
   * @param saveName  Material save name
   */
  private void registerMaterial(Consumer<FinishedRecipe> consumer, MaterialId material, Ingredient input, int value, int needed, String saveName) {
    registerMaterial(consumer, material, input, value, needed, null, saveName);
  }

  /**
   * Registers a material recipe
   * @param consumer  Recipe consumer
   * @param material  Material ID
   * @param input     Recipe input
   * @param value     Material value
   * @param needed    Number of items needed
   * @param saveName  Material save name
   */
  private void registerMaterial(Consumer<FinishedRecipe> consumer, MaterialId material, Ingredient input, int value, int needed, @Nullable ItemOutput leftover, String saveName) {
    MaterialRecipeBuilder builder = MaterialRecipeBuilder.materialRecipe(material)
                                                         .setIngredient(input)
                                                         .setValue(value)
                                                         .setNeeded(needed);
    if (leftover != null) {
      builder.setLeftover(leftover);
    }
    builder.build(consumer, location("tools/materials/" + saveName));
  }

  /**
   * Register ingots, nuggets, and blocks for a metal material
   * @param consumer  Consumer instance
   * @param material  Material
   * @param name      Material name
   */
  private void registerMetalMaterial(Consumer<FinishedRecipe> consumer, MaterialId material, String name, boolean optional) {
    Consumer<FinishedRecipe> wrapped = optional ? withCondition(consumer, tagCondition("ingots/" + name)) : consumer;
    String matName = material.getPath();
    Tag<Item> ingotTag = getTag("forge", "ingots/" + name);
    registerMaterial(wrapped, material, Ingredient.of(ingotTag), 1, 1, matName + "/ingot");
    wrapped = optional ? withCondition(consumer, tagCondition("nuggets/" + name)) : consumer;
    registerMaterial(wrapped, material, Ingredient.of(getTag("forge", "nuggets/" + name)), 1, 9, matName + "/nugget");
    wrapped = optional ? withCondition(consumer, tagCondition("storage_blocks/" + name)) : consumer;
    registerMaterial(wrapped, material, Ingredient.of(getTag("forge", "storage_blocks/" + name)), 9, 1, ItemOutput.fromTag(ingotTag, 1), matName + "/block");
  }
}
