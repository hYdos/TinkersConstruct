package slimeknights.tconstruct.world;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.worldgen.Features;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration.Predicates;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.DepthAverageConfigation;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.PlantType;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import org.apache.logging.log4j.Logger;
import slimeknights.mantle.item.BlockTooltipItem;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.mantle.util.SupplierItemGroup;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.common.registration.WoodBlockObject;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.utils.HarvestLevels;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.world.block.BloodSlimeBlock;
import slimeknights.tconstruct.world.block.CongealedSlimeBlock;
import slimeknights.tconstruct.world.block.SlimeDirtBlock;
import slimeknights.tconstruct.world.block.SlimeGrassBlock;
import slimeknights.tconstruct.world.block.SlimeLeavesBlock;
import slimeknights.tconstruct.world.block.SlimeSaplingBlock;
import slimeknights.tconstruct.world.block.SlimeTallGrassBlock;
import slimeknights.tconstruct.world.block.SlimeVineBlock;
import slimeknights.tconstruct.world.block.StickySlimeBlock;
import slimeknights.tconstruct.world.data.WorldRecipeProvider;
import slimeknights.tconstruct.world.entity.EnderSlimeEntity;
import slimeknights.tconstruct.world.entity.SkySlimeEntity;
import slimeknights.tconstruct.world.item.SlimeGrassSeedItem;
import slimeknights.tconstruct.world.worldgen.trees.SlimeTree;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Contains blocks and items relevant to structures and world gen
 */
@SuppressWarnings("unused")
public final class TinkerWorld extends TinkerModule {

  /** Tab for anything generated in the world */
  @SuppressWarnings("WeakerAccess")
  public static final CreativeModeTab TAB_WORLD = new SupplierItemGroup(TConstruct.modID, "world", () -> new ItemStack(TinkerWorld.cobaltOre));
  static final Logger log = Util.getLogger("tinker_world");

  public static final PlantType SLIME_PLANT_TYPE = PlantType.get("slime");

  /*
   * Block base properties
   */
  private static final Item.Properties WORLD_PROPS = new Item.Properties().tab(TAB_WORLD);
  private static final Function<Block, ? extends BlockItem> DEFAULT_BLOCK_ITEM = (b) -> new BlockItem(b, WORLD_PROPS);
  private static final Function<Block, ? extends BlockItem> TOOLTIP_BLOCK_ITEM = (b) -> new BlockTooltipItem(b, WORLD_PROPS);

  /*
   * Blocks
   */
  // ores
  private static final Block.Properties NETHER_ORE = builder(Material.STONE, ToolType.PICKAXE, SoundType.NETHER_ORE).requiresCorrectToolForDrops().harvestLevel(HarvestLevels.DIAMOND).strength(10.0F);
  public static final ItemObject<Block> cobaltOre = BLOCKS.register("cobalt_ore", () -> new Block(NETHER_ORE), DEFAULT_BLOCK_ITEM);

  private static final Block.Properties OVERWORLD_ORE = builder(Material.STONE, ToolType.PICKAXE, SoundType.STONE).requiresCorrectToolForDrops().harvestLevel(HarvestLevels.STONE).strength(3.0F, 3.0F);
  public static final ItemObject<Block> copperOre = BLOCKS.register("copper_ore", OVERWORLD_ORE, DEFAULT_BLOCK_ITEM);

  // slime
  private static final Block.Properties SLIME = Block.Properties.copy(Blocks.SLIME_BLOCK);
  public static final EnumObject<SlimeType, SlimeBlock> slime = new EnumObject.Builder<SlimeType, SlimeBlock>(SlimeType.class)
    .putDelegate(SlimeType.EARTH, Blocks.SLIME_BLOCK.delegate)
    // sky slime: sticks to anything, but will not pull back
    .put(SlimeType.SKY,   BLOCKS.register("sky_slime", () -> new StickySlimeBlock(SLIME, (state, other) -> true), TOOLTIP_BLOCK_ITEM))
    // ichor: does not stick to self, but sticks to anything else
    .put(SlimeType.ICHOR, BLOCKS.register("ichor_slime", () -> new StickySlimeBlock(SLIME, (state, other) -> other.getBlock() != state.getBlock()), TOOLTIP_BLOCK_ITEM))
    // ender: only sticks to self
    .put(SlimeType.ENDER, BLOCKS.register("ender_slime", () -> new StickySlimeBlock(SLIME, (state, other) -> other.getBlock() == state.getBlock()), TOOLTIP_BLOCK_ITEM))
    // blood slime: not sticky, and honey won't stick to it, good for bounce pads
    .put(SlimeType.BLOOD, BLOCKS.register("blood_slime", () -> new BloodSlimeBlock(SLIME), TOOLTIP_BLOCK_ITEM))
    .build();
  private static final Block.Properties CONGEALED_SLIME = builder(Material.CLAY, ToolType.SHOVEL, SoundType.SLIME_BLOCK).strength(0.5F).friction(0.5F);
  public static final EnumObject<SlimeType, CongealedSlimeBlock> congealedSlime = BLOCKS.registerEnum(SlimeType.values(), "congealed_slime", (type) -> new CongealedSlimeBlock(CONGEALED_SLIME), TOOLTIP_BLOCK_ITEM);

  // island blocks
  private static final Block.Properties SLIME_DIRT = builder(Material.DIRT, ToolType.SHOVEL, SoundType.SLIME_BLOCK).strength(0.55F);
  private static final Block.Properties SLIME_GRASS = builder(Material.GRASS, ToolType.SHOVEL, SoundType.SLIME_BLOCK).strength(0.65F).randomTicks();
  public static final EnumObject<SlimeType, Block> slimeDirt = BLOCKS.registerEnum(SlimeType.TRUE_SLIME, "slime_dirt", (type) -> new SlimeDirtBlock(SLIME_DIRT), TOOLTIP_BLOCK_ITEM);
  public static final EnumObject<SlimeType, Block> allDirt = new EnumObject.Builder<SlimeType, Block>(SlimeType.class).put(SlimeType.BLOOD, Blocks.DIRT.delegate).putAll(slimeDirt).build();
  public static final EnumObject<SlimeType, SlimeGrassBlock> vanillaSlimeGrass = BLOCKS.registerEnum(SlimeType.values(), "vanilla_slime_grass", (type) -> new SlimeGrassBlock(SLIME_GRASS, type), TOOLTIP_BLOCK_ITEM);
  public static final EnumObject<SlimeType, SlimeGrassBlock> earthSlimeGrass = BLOCKS.registerEnum(SlimeType.values(), "earth_slime_grass", (type) -> new SlimeGrassBlock(SLIME_GRASS, type), TOOLTIP_BLOCK_ITEM);
  public static final EnumObject<SlimeType, SlimeGrassBlock> skySlimeGrass = BLOCKS.registerEnum(SlimeType.values(), "sky_slime_grass", (type) -> new SlimeGrassBlock(SLIME_GRASS, type), TOOLTIP_BLOCK_ITEM);
  public static final EnumObject<SlimeType, SlimeGrassBlock> enderSlimeGrass = BLOCKS.registerEnum(SlimeType.values(), "ender_slime_grass", (type) -> new SlimeGrassBlock(SLIME_GRASS, type), TOOLTIP_BLOCK_ITEM);
  public static final EnumObject<SlimeType, SlimeGrassBlock> ichorSlimeGrass = BLOCKS.registerEnum(SlimeType.values(), "ichor_slime_grass", (type) -> new SlimeGrassBlock(SLIME_GRASS, type), TOOLTIP_BLOCK_ITEM);
  /** Map of dirt type to slime grass type. Each slime grass is a map from foliage to grass type */
  public static final Map<SlimeType, EnumObject<SlimeType, SlimeGrassBlock>> slimeGrass;
  static {
    slimeGrass = new EnumMap<>(SlimeType.class);
    slimeGrass.put(SlimeType.BLOOD, vanillaSlimeGrass); // not exact match, but whatever
    slimeGrass.put(SlimeType.EARTH, earthSlimeGrass);
    slimeGrass.put(SlimeType.SKY, skySlimeGrass);
    slimeGrass.put(SlimeType.ENDER, enderSlimeGrass);
    slimeGrass.put(SlimeType.ICHOR, ichorSlimeGrass);
  }
  public static final EnumObject<SlimeType, SlimeGrassSeedItem> slimeGrassSeeds = ITEMS.registerEnum(SlimeType.values(), "slime_grass_seeds", type -> new SlimeGrassSeedItem(WORLD_PROPS, type));

  // plants
  private static final Block.Properties GRASS = builder(Material.REPLACEABLE_PLANT, NO_TOOL, SoundType.GRASS).instabreak().noCollission().randomTicks();
  public static final EnumObject<SlimeType, SlimeTallGrassBlock> slimeFern = BLOCKS.registerEnum(SlimeType.values(), "slime_fern", (type) -> new SlimeTallGrassBlock(GRASS, type, SlimeTallGrassBlock.SlimePlantType.FERN), DEFAULT_BLOCK_ITEM);
  public static final EnumObject<SlimeType, SlimeTallGrassBlock> slimeTallGrass = BLOCKS.registerEnum(SlimeType.values(), "slime_tall_grass", (type) -> new SlimeTallGrassBlock(GRASS, type, SlimeTallGrassBlock.SlimePlantType.TALL_GRASS), DEFAULT_BLOCK_ITEM);

  // wood
  public static final Material SLIME_WOOD = new Material.Builder(MaterialColor.CLAY).flammable().build(); // yep. flammable clay. New material so none of the existing tooltips try mining this
  public static final WoodBlockObject greenheart =  BLOCKS.registerWood("greenheart",  SLIME_WOOD,    MaterialColor.COLOR_LIGHT_GREEN, SoundType.SLIME_BLOCK, ToolType.SHOVEL, Material.WOOD,        MaterialColor.COLOR_GREEN,           SoundType.WOOD,   TAB_WORLD);
  public static final WoodBlockObject skyroot =     BLOCKS.registerWood("skyroot",     SLIME_WOOD,    MaterialColor.COLOR_CYAN, SoundType.SLIME_BLOCK, ToolType.SHOVEL, Material.WOOD,        MaterialColor.TERRACOTTA_CYAN, SoundType.WOOD,   TAB_WORLD);
  public static final WoodBlockObject bloodshroom = BLOCKS.registerWood("bloodshroom", Material.CLAY, MaterialColor.COLOR_RED,  SoundType.SLIME_BLOCK, ToolType.SHOVEL, Material.NETHER_WOOD, MaterialColor.COLOR_ORANGE,           SoundType.STEM, TAB_WORLD);

  // trees
  private static final Block.Properties SAPLING = builder(Material.PLANT, NO_TOOL, SoundType.GRASS).instabreak().noCollission().randomTicks();
  public static final EnumObject<SlimeType, SlimeSaplingBlock> slimeSapling = BLOCKS.registerEnum(SlimeType.values(), "slime_sapling", (type) -> new SlimeSaplingBlock(new SlimeTree(type), type, SAPLING), TOOLTIP_BLOCK_ITEM);
  private static final Block.Properties SLIME_LEAVES = builder(Material.LEAVES, NO_TOOL, SoundType.GRASS).strength(0.3f).randomTicks().noOcclusion().isValidSpawn((s, w, p, e) -> false);
  private static final Block.Properties SLIME_WART = builder(Material.GRASS, NO_TOOL, SoundType.WART_BLOCK).strength(0.6f).randomTicks().isValidSpawn((s, w, p, e) -> false);
  public static final EnumObject<SlimeType, SlimeLeavesBlock> slimeLeaves = BLOCKS.registerEnum(SlimeType.values(), "slime_leaves", type -> new SlimeLeavesBlock(type == SlimeType.BLOOD ? SLIME_WART : SLIME_LEAVES, type), DEFAULT_BLOCK_ITEM);

  // slime vines
  private static final Block.Properties VINE = builder(Material.REPLACEABLE_PLANT, NO_TOOL, SoundType.GRASS).strength(0.3F).noCollission().randomTicks();
  public static final ItemObject<SlimeVineBlock> enderSlimeVine = BLOCKS.register("ender_slime_vine", () -> new SlimeVineBlock(VINE, SlimeType.ENDER), DEFAULT_BLOCK_ITEM);
  public static final ItemObject<SlimeVineBlock> skySlimeVine = BLOCKS.register("sky_slime_vine", () -> new SlimeVineBlock(VINE, SlimeType.SKY), DEFAULT_BLOCK_ITEM);

  /*
   * Entities
   */
  // our own copy of the slime to make spawning a bit easier
  public static final RegistryObject<EntityType<Slime>> earthSlimeEntity = ENTITIES.register("earth_slime", () ->
    EntityType.Builder.of(Slime::new, MobCategory.MONSTER)
                      .setShouldReceiveVelocityUpdates(true)
                      .setUpdateInterval(5)
                      .setTrackingRange(64)
                      .sized(2.04F, 2.04F)
                      .setCustomClientFactory((spawnEntity, world) -> TinkerWorld.earthSlimeEntity.get().create(world)));
  public static final RegistryObject<EntityType<SkySlimeEntity>> skySlimeEntity = ENTITIES.registerWithEgg("sky_slime", () ->
    EntityType.Builder.of(SkySlimeEntity::new, MobCategory.MONSTER)
                      .setShouldReceiveVelocityUpdates(true)
                      .setUpdateInterval(5)
                      .setTrackingRange(64)
                      .sized(2.04F, 2.04F)
                      .setCustomClientFactory((spawnEntity, world) -> TinkerWorld.skySlimeEntity.get().create(world)), 0x47eff5, 0xacfff4);
  public static final RegistryObject<EntityType<EnderSlimeEntity>> enderSlimeEntity = ENTITIES.registerWithEgg("ender_slime", () ->
    EntityType.Builder.of(EnderSlimeEntity::new, MobCategory.MONSTER)
                      .setShouldReceiveVelocityUpdates(true)
                      .setUpdateInterval(5)
                      .setTrackingRange(64)
                      .sized(2.04F, 2.04F)
                      .setCustomClientFactory((spawnEntity, world) -> TinkerWorld.enderSlimeEntity.get().create(world)), 0x6300B0, 0xD37CFF);

  /*
   * Particles
   */
  public static final RegistryObject<SimpleParticleType> skySlimeParticle = PARTICLE_TYPES.register("sky_slime", () -> new SimpleParticleType(false));
  public static final RegistryObject<SimpleParticleType> enderSlimeParticle = PARTICLE_TYPES.register("ender_slime", () -> new SimpleParticleType(false));

  /*
   * Features
   */
  public static ConfiguredFeature<?, ?> COPPER_ORE_FEATURE;
  public static ConfiguredFeature<?, ?> COBALT_ORE_FEATURE_SMALL;
  public static ConfiguredFeature<?, ?> COBALT_ORE_FEATURE_LARGE;

  /*
   * Events
   */

  @SubscribeEvent
  void entityAttributes(EntityAttributeCreationEvent event) {
    event.put(earthSlimeEntity.get(), Monster.createMonsterAttributes().build());
    event.put(skySlimeEntity.get(), Monster.createMonsterAttributes().build());
    event.put(enderSlimeEntity.get(), Monster.createMonsterAttributes().build());
  }

  /** Sets all fire info for the given wood */
  private static void setWoodFireInfo(FireBlock fireBlock, WoodBlockObject wood) {
    // planks
    fireBlock.setFlammable(wood.get(), 5, 20);
    fireBlock.setFlammable(wood.getSlab(), 5, 20);
    fireBlock.setFlammable(wood.getStairs(), 5, 20);
    fireBlock.setFlammable(wood.getFence(), 5, 20);
    fireBlock.setFlammable(wood.getFenceGate(), 5, 20);
    // logs
    fireBlock.setFlammable(wood.getLog(), 5, 5);
    fireBlock.setFlammable(wood.getStrippedLog(), 5, 5);
    fireBlock.setFlammable(wood.getWood(), 5, 5);
    fireBlock.setFlammable(wood.getStrippedWood(), 5, 5);
  }

  @SubscribeEvent
  void commonSetup(final FMLCommonSetupEvent event) {
    SpawnPlacements.register(earthSlimeEntity.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.WORLD_SURFACE, SkySlimeEntity::canSpawnHere);
    SpawnPlacements.register(skySlimeEntity.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.WORLD_SURFACE, SkySlimeEntity::canSpawnHere);
    SpawnPlacements.register(enderSlimeEntity.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.WORLD_SURFACE, SkySlimeEntity::canSpawnHere);

    // compostables
    event.enqueueWork(() -> {
      slimeLeaves.forEach(block -> ComposterBlock.add(0.35f, block));
      slimeSapling.forEach(block -> ComposterBlock.add(0.35f, block));
      slimeTallGrass.forEach(block -> ComposterBlock.add(0.35f, block));
      slimeFern.forEach(block -> ComposterBlock.add(0.65f, block));
      slimeGrassSeeds.forEach(block -> ComposterBlock.add(0.35F, block));
      ComposterBlock.add(0.5f, skySlimeVine);
      ComposterBlock.add(0.5f, enderSlimeVine);
    });

    // flammability
    event.enqueueWork(() -> {
      FireBlock fireblock = (FireBlock)Blocks.FIRE;
      // wood
      setWoodFireInfo(fireblock, greenheart);
      setWoodFireInfo(fireblock, skyroot);
      // plants
      BiConsumer<SlimeType, Block> plantFireInfo = (type, block) -> {
        if (type != SlimeType.BLOOD && type != SlimeType.ICHOR) {
          fireblock.setFlammable(block, 30, 60);
        }
      };
      slimeLeaves.forEach(plantFireInfo);
      slimeTallGrass.forEach(plantFireInfo);
      slimeFern.forEach(plantFireInfo);
      // vines
      fireblock.setFlammable(skySlimeVine.get(), 15, 100);
      fireblock.setFlammable(enderSlimeVine.get(), 15, 100);
    });

    // ores
    event.enqueueWork(() -> {
      COPPER_ORE_FEATURE = Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, location("copper_ore"),
                                             Feature.ORE.configured(new OreConfiguration(Predicates.NATURAL_STONE, TinkerWorld.copperOre.get().defaultBlockState(), 9))
                                                        .decorated(FeatureDecorator.RANGE.configured(new RangeDecoratorConfiguration(40, 0, 60)))
                                                        .squared()
                                                        .count(Config.COMMON.veinCountCopper.get()));
      // small veins, standard distribution
      COBALT_ORE_FEATURE_SMALL = Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, location("cobalt_ore_small"),
                                                   Feature.ORE.configured(new OreConfiguration(Predicates.NETHERRACK, cobaltOre.get().defaultBlockState(), 4))
                                                              .decorated(Features.Decorators.RANGE_10_20_ROOFED)
                                                              .squared().count(Config.COMMON.veinCountCobalt.get() / 2));
      // large veins, around y=16, up to 48
      COBALT_ORE_FEATURE_LARGE = Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, location("cobalt_ore_large"),
                                                   Feature.ORE.configured(new OreConfiguration(Predicates.NETHERRACK, cobaltOre.get().defaultBlockState(), 8))
                                                              .decorated(FeatureDecorator.DEPTH_AVERAGE.configured(new DepthAverageConfigation(32, 16)))
                                                              .squared().count(Config.COMMON.veinCountCobalt.get() / 2));
    });
  }

  @SubscribeEvent
  void gatherData(final GatherDataEvent event) {
    if (event.includeServer()) {
      DataGenerator datagenerator = event.getGenerator();
      datagenerator.addProvider(new WorldRecipeProvider(datagenerator));
    }
  }
}
