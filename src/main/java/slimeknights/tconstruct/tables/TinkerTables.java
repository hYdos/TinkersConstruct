package slimeknights.tconstruct.tables;

import net.minecraft.data.DataGenerator;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import slimeknights.mantle.item.RetexturedBlockItem;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeSerializer;
import slimeknights.tconstruct.library.recipe.partbuilder.ItemPartRecipe;
import slimeknights.tconstruct.library.recipe.partbuilder.PartRecipeSerializer;
import slimeknights.tconstruct.library.recipe.tinkerstation.building.ToolBuildingRecipeSerializer;
import slimeknights.tconstruct.shared.block.TableBlock;
import slimeknights.tconstruct.tables.block.TinkerChestBlock;
import slimeknights.tconstruct.tables.block.table.CraftingStationBlock;
import slimeknights.tconstruct.tables.block.table.PartBuilderBlock;
import slimeknights.tconstruct.tables.block.table.TinkerStationBlock;
import slimeknights.tconstruct.tables.block.table.TinkersAnvilBlock;
import slimeknights.tconstruct.tables.data.TableRecipeProvider;
import slimeknights.tconstruct.tables.inventory.TinkerChestContainer;
import slimeknights.tconstruct.tables.inventory.table.CraftingStationContainer;
import slimeknights.tconstruct.tables.inventory.table.PartBuilderContainer;
import slimeknights.tconstruct.tables.inventory.table.tinkerstation.TinkerStationContainer;
import slimeknights.tconstruct.tables.item.TableBlockItem;
import slimeknights.tconstruct.tables.recipe.CraftingTableRepairKitRecipe;
import slimeknights.tconstruct.tables.recipe.TinkerStationDamagingRecipe;
import slimeknights.tconstruct.tables.recipe.TinkerStationPartSwapping;
import slimeknights.tconstruct.tables.recipe.TinkerStationRepairRecipe;
import slimeknights.tconstruct.tables.tileentity.chest.CastChestTileEntity;
import slimeknights.tconstruct.tables.tileentity.chest.ModifierChestTileEntity;
import slimeknights.tconstruct.tables.tileentity.chest.PartChestTileEntity;
import slimeknights.tconstruct.tables.tileentity.table.CraftingStationTileEntity;
import slimeknights.tconstruct.tables.tileentity.table.PartBuilderTileEntity;
import slimeknights.tconstruct.tables.tileentity.table.TinkerStationTileEntity;

import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

/**
 * Handles all the table for tool creation
 */
@SuppressWarnings("unused")
public final class TinkerTables extends TinkerModule {

  /*
   * Blocks
   */
  private static final Block.Properties WOOD_TABLE = builder(Material.WOOD, ToolType.AXE, SoundType.WOOD).strength(1.0F, 5.0F).noOcclusion();
  /** Call with .apply to set the tag type for a block item provider */
  private static final BiFunction<Tag<Item>,BooleanSupplier,Function<Block,RetexturedBlockItem>> RETEXTURED_BLOCK_ITEM = (tag, cond) -> block -> new TableBlockItem(block, tag, GENERAL_PROPS, cond);
  public static final ItemObject<TableBlock> craftingStation = BLOCKS.register("crafting_station", () -> new CraftingStationBlock(WOOD_TABLE), RETEXTURED_BLOCK_ITEM.apply(ItemTags.PLANKS, Config.COMMON.showAllTableVariants::get));
  public static final ItemObject<TableBlock> tinkerStation = BLOCKS.register("tinker_station", () -> new TinkerStationBlock(WOOD_TABLE, 4), RETEXTURED_BLOCK_ITEM.apply(ItemTags.PLANKS, Config.COMMON.showAllTableVariants::get));
  public static final ItemObject<TableBlock> partBuilder = BLOCKS.register("part_builder", () -> new PartBuilderBlock(WOOD_TABLE), RETEXTURED_BLOCK_ITEM.apply(ItemTags.PLANKS, Config.COMMON.showAllTableVariants::get));
  public static final ItemObject<TableBlock> modifierChest = BLOCKS.register("modifier_chest", () -> new TinkerChestBlock(WOOD_TABLE, ModifierChestTileEntity::new), GENERAL_BLOCK_ITEM);
  public static final ItemObject<TableBlock> partChest = BLOCKS.register("part_chest", () -> new TinkerChestBlock(WOOD_TABLE, PartChestTileEntity::new), GENERAL_BLOCK_ITEM);

  private static final Block.Properties METAL_TABLE = builder(Material.HEAVY_METAL, ToolType.PICKAXE, SoundType.ANVIL).requiresCorrectToolForDrops().strength(5.0F, 1200.0F).noOcclusion();
  public static final ItemObject<TableBlock> tinkersAnvil = BLOCKS.register("tinkers_anvil", () -> new TinkersAnvilBlock(METAL_TABLE, 6), RETEXTURED_BLOCK_ITEM.apply(TinkerTags.Items.ANVIL_METAL, Config.COMMON.showAllAnvilVariants::get));
  public static final ItemObject<TableBlock> scorchedAnvil = BLOCKS.register("scorched_anvil", () -> new TinkersAnvilBlock(METAL_TABLE, 6), RETEXTURED_BLOCK_ITEM.apply(TinkerTags.Items.ANVIL_METAL, Config.COMMON.showAllAnvilVariants::get));
  private static final Block.Properties STONE_TABLE = builder(Material.STONE, ToolType.PICKAXE, SoundType.METAL).requiresCorrectToolForDrops().strength(3.0F, 9.0F).noOcclusion();
  public static final ItemObject<TableBlock> castChest = BLOCKS.register("cast_chest", () -> new TinkerChestBlock(STONE_TABLE, CastChestTileEntity::new), GENERAL_BLOCK_ITEM);

  /*
   * Items
   */
  public static final ItemObject<Item> pattern = ITEMS.register("pattern", GENERAL_PROPS);

  /*
   * Tile entites
   */
  public static final RegistryObject<BlockEntityType<CraftingStationTileEntity>> craftingStationTile = TILE_ENTITIES.register("crafting_station", CraftingStationTileEntity::new, craftingStation);
  public static final RegistryObject<BlockEntityType<TinkerStationTileEntity>> tinkerStationTile = TILE_ENTITIES.register("tinker_station", TinkerStationTileEntity::new, builder -> {
    builder.add(tinkerStation.get(), tinkersAnvil.get(), scorchedAnvil.get());
  });
  public static final RegistryObject<BlockEntityType<PartBuilderTileEntity>> partBuilderTile = TILE_ENTITIES.register("part_builder", PartBuilderTileEntity::new, partBuilder);
  public static final RegistryObject<BlockEntityType<ModifierChestTileEntity>> modifierChestTile = TILE_ENTITIES.register("modifier_chest", ModifierChestTileEntity::new, modifierChest);
  public static final RegistryObject<BlockEntityType<PartChestTileEntity>> partChestTile = TILE_ENTITIES.register("part_chest", PartChestTileEntity::new, partChest);
  public static final RegistryObject<BlockEntityType<CastChestTileEntity>> castChestTile = TILE_ENTITIES.register("cast_chest", CastChestTileEntity::new, castChest);

  /*
   * Containers
   */
  public static final RegistryObject<MenuType<CraftingStationContainer>> craftingStationContainer = CONTAINERS.register("crafting_station", CraftingStationContainer::new);
  public static final RegistryObject<MenuType<TinkerStationContainer>> tinkerStationContainer = CONTAINERS.register("tinker_station", TinkerStationContainer::new);
  public static final RegistryObject<MenuType<PartBuilderContainer>> partBuilderContainer = CONTAINERS.register("part_builder", PartBuilderContainer::new);
  public static final RegistryObject<MenuType<TinkerChestContainer>> tinkerChestContainer = CONTAINERS.register("tinker_chest", TinkerChestContainer::new);

  /*
   * Recipes
   */
  public static final RegistryObject<PartRecipeSerializer> partRecipeSerializer = RECIPE_SERIALIZERS.register("part_builder", PartRecipeSerializer::new);
  public static final RegistryObject<ItemPartRecipe.Serializer> itemPartBuilderSerializer = RECIPE_SERIALIZERS.register("item_part_builder", ItemPartRecipe.Serializer::new);
  public static final RegistryObject<MaterialRecipeSerializer> materialRecipeSerializer = RECIPE_SERIALIZERS.register("material", MaterialRecipeSerializer::new);
  public static final RegistryObject<ToolBuildingRecipeSerializer> toolBuildingRecipeSerializer = RECIPE_SERIALIZERS.register("tool_building", ToolBuildingRecipeSerializer::new);
  public static final RegistryObject<SimpleRecipeSerializer<TinkerStationRepairRecipe>> tinkerStationRepairSerializer = RECIPE_SERIALIZERS.register("tinker_station_repair", () -> new SimpleRecipeSerializer<>(TinkerStationRepairRecipe::new));
  public static final RegistryObject<SimpleRecipeSerializer<CraftingTableRepairKitRecipe>> craftingTableRepairSerializer = RECIPE_SERIALIZERS.register("crafting_table_repair", () -> new SimpleRecipeSerializer<>(CraftingTableRepairKitRecipe::new));
  public static final RegistryObject<SimpleRecipeSerializer<TinkerStationPartSwapping>> tinkerStationPartSwappingSerializer = RECIPE_SERIALIZERS.register("tinker_station_part_swapping", () -> new SimpleRecipeSerializer<>(TinkerStationPartSwapping::new));
  public static final RegistryObject<TinkerStationDamagingRecipe.Serializer> tinkerStationDamagingSerializer = RECIPE_SERIALIZERS.register("tinker_station_damaging", TinkerStationDamagingRecipe.Serializer::new);

  @SubscribeEvent
  void gatherData(final GatherDataEvent event) {
    if (event.includeServer()) {
      DataGenerator datagenerator = event.getGenerator();
      datagenerator.addProvider(new TableRecipeProvider(datagenerator));
    }
  }
}
