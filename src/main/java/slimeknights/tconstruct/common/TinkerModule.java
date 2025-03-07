package slimeknights.tconstruct.common;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.mantle.item.BlockTooltipItem;
import slimeknights.mantle.item.TooltipItem;
import slimeknights.mantle.registration.deferred.ContainerTypeDeferredRegister;
import slimeknights.mantle.registration.deferred.EntityTypeDeferredRegister;
import slimeknights.mantle.registration.deferred.TileEntityTypeDeferredRegister;
import slimeknights.mantle.util.SupplierItemGroup;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.registration.BlockDeferredRegisterExtension;
import slimeknights.tconstruct.common.registration.FluidDeferredRegisterExtension;
import slimeknights.tconstruct.common.registration.ItemDeferredRegisterExtension;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.block.SlimeType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Contains base helpers for all Tinker modules
 */
public abstract class TinkerModule {
  // deferred register instances
  protected static final BlockDeferredRegisterExtension BLOCKS = new BlockDeferredRegisterExtension(TConstruct.modID);
  protected static final ItemDeferredRegisterExtension ITEMS = new ItemDeferredRegisterExtension(TConstruct.modID);
  protected static final FluidDeferredRegisterExtension FLUIDS = new FluidDeferredRegisterExtension(TConstruct.modID);
  protected static final TileEntityTypeDeferredRegister TILE_ENTITIES = new TileEntityTypeDeferredRegister(TConstruct.modID);
  protected static final EntityTypeDeferredRegister ENTITIES = new EntityTypeDeferredRegister(TConstruct.modID);
  protected static final ContainerTypeDeferredRegister CONTAINERS = new ContainerTypeDeferredRegister(TConstruct.modID);
  protected static final DeferredRegister<MobEffect> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, TConstruct.modID);
  protected static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, TConstruct.modID);
  protected static final DeferredRegister<StructureFeature<?>> STRUCTURE_FEATURES = DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, TConstruct.modID);
  protected static final DeferredRegister<BlockStateProviderType<?>> BLOCK_STATE_PROVIDER_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_STATE_PROVIDER_TYPES, TConstruct.modID);
  protected static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TConstruct.modID);
  protected static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, TConstruct.modID);
  protected static final DeferredRegister<Modifier> MODIFIERS = DeferredRegister.create(Modifier.class, TConstruct.modID);
  protected static final DeferredRegister<GlobalLootModifierSerializer<?>> GLOBAL_LOOT_MODIFIERS = DeferredRegister.create(ForgeRegistries.LOOT_MODIFIER_SERIALIZERS, TConstruct.modID);

  // base block properties
  protected static final Block.Properties GENERIC_SAND_BLOCK = builder(Material.SAND, ToolType.SHOVEL, SoundType.SAND).strength(3.0f).friction(0.8F);
  protected static final Block.Properties GENERIC_METAL_BLOCK = builder(Material.METAL, ToolType.PICKAXE, SoundType.METAL).requiresCorrectToolForDrops().strength(5.0f);
  protected static final Block.Properties GENERIC_GEM_BLOCK = GENERIC_METAL_BLOCK;
  protected static final Block.Properties GENERIC_GLASS_BLOCK = builder(Material.GLASS, ToolType.PICKAXE, SoundType.GLASS)
    .requiresCorrectToolForDrops().strength(0.3F).noOcclusion().isValidSpawn(Blocks::never)
    .isRedstoneConductor(Blocks::never).isSuffocating(Blocks::never).isViewBlocking(Blocks::never);

  /** Creative tab for items that do not fit in another tab */
  @SuppressWarnings("WeakerAccess")
  public static final CreativeModeTab TAB_GENERAL = new SupplierItemGroup(TConstruct.modID, "general", () -> new ItemStack(TinkerCommons.slimeball.get(SlimeType.SKY)));

  // base item properties
  protected static final Item.Properties HIDDEN_PROPS = new Item.Properties();
  protected static final Item.Properties GENERAL_PROPS = new Item.Properties().tab(TAB_GENERAL);
  protected static final Function<Block,? extends BlockItem> HIDDEN_BLOCK_ITEM = (b) -> new BlockItem(b, HIDDEN_PROPS);
  protected static final Function<Block,? extends BlockItem> GENERAL_BLOCK_ITEM = (b) -> new BlockItem(b, GENERAL_PROPS);
  protected static final Function<Block,? extends BlockItem> GENERAL_TOOLTIP_BLOCK_ITEM = (b) -> new BlockTooltipItem(b, GENERAL_PROPS);
  protected static final Supplier<Item> TOOLTIP_ITEM = () -> new TooltipItem(GENERAL_PROPS);

  /** Called during construction to initialize the registers for this mod */
  public static void initRegisters() {
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    BLOCKS.register(bus);
    ITEMS.register(bus);
    FLUIDS.register(bus);
    TILE_ENTITIES.register(bus);
    ENTITIES.register(bus);
    CONTAINERS.register(bus);
    POTIONS.register(bus);
    FEATURES.register(bus);
    STRUCTURE_FEATURES.register(bus);
    BLOCK_STATE_PROVIDER_TYPES.register(bus);
    RECIPE_SERIALIZERS.register(bus);
    PARTICLE_TYPES.register(bus);
    MODIFIERS.register(bus);
    GLOBAL_LOOT_MODIFIERS.register(bus);
  }

  /**
   * This is a function that returns null, despite being nonnull. It is used on object holder fields to remove IDE warnings about constant null as it will be nonnull
   * @param <T>  Field type
   * @return  Null
   */
  @Nonnull
  @SuppressWarnings("ConstantConditions")
  public static <T> T injected() {
    return null;
  }

  /** Constant to use for blocks with no tool for more readable code */
  protected static final ToolType NO_TOOL = null;

  /**
   * We use this builder to ensure that our blocks all have the most important properties set.
   * This way it'll stick out if a block doesn't have a tooltype or sound set.
   * It may be a bit less clear at first, since the actual builder methods tell you what each value means,
   * but as long as we don't statically import the enums it should be just as readable.
   */
  protected static Block.Properties builder(Material material, @Nullable ToolType toolType, SoundType soundType) {
    //noinspection ConstantConditions
    return Block.Properties.of(material).harvestTool(toolType).sound(soundType);
  }

  /**
   * Creates a Tinkers Construct resource location
   * @param id  Resource path
   * @return  Tinkers Construct resource location
   */
  protected static ResourceLocation location(String id) {
    return new ResourceLocation(TConstruct.modID, id);
  }

  /**
   * Creates a Tinkers Construct resource location string
   * @param id  Resource path
   * @return  Tinkers Construct resource location string
   */
  protected static String locationString(String id) {
    return TConstruct.modID + ":" + id;
  }
}
