package slimeknights.tconstruct.gadgets;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.MinecraftForge;
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
import slimeknights.tconstruct.gadgets.block.FoodCakeBlock;
import slimeknights.tconstruct.gadgets.block.PunjiBlock;
import slimeknights.tconstruct.gadgets.data.GadgetRecipeProvider;
import slimeknights.tconstruct.gadgets.entity.EflnBallEntity;
import slimeknights.tconstruct.gadgets.entity.FancyItemFrameEntity;
import slimeknights.tconstruct.gadgets.entity.FrameType;
import slimeknights.tconstruct.gadgets.entity.GlowballEntity;
import slimeknights.tconstruct.gadgets.entity.shuriken.FlintShurikenEntity;
import slimeknights.tconstruct.gadgets.entity.shuriken.QuartzShurikenEntity;
import slimeknights.tconstruct.gadgets.item.EflnBallItem;
import slimeknights.tconstruct.gadgets.item.FancyItemFrameItem;
import slimeknights.tconstruct.gadgets.item.GlowBallItem;
import slimeknights.tconstruct.gadgets.item.PiggyBackPackItem;
import slimeknights.tconstruct.gadgets.item.PiggyBackPackItem.CarryPotionEffect;
import slimeknights.tconstruct.gadgets.item.ShurikenItem;
import slimeknights.tconstruct.gadgets.item.SlimeBootsItem;
import slimeknights.tconstruct.gadgets.item.slimesling.BaseSlimeSlingItem;
import slimeknights.tconstruct.gadgets.item.slimesling.EarthSlimeSlingItem;
import slimeknights.tconstruct.gadgets.item.slimesling.EnderSlimeSlingItem;
import slimeknights.tconstruct.gadgets.item.slimesling.IchorSlimeSlingItem;
import slimeknights.tconstruct.gadgets.item.slimesling.SkySlimeSlingItem;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.capability.piggyback.CapabilityTinkerPiggyback;
import slimeknights.tconstruct.shared.TinkerFood;
import slimeknights.tconstruct.shared.block.SlimeType;

import java.util.function.Function;

/**
 * Contains any special tools unrelated to the base tools
 */
@SuppressWarnings("unused")
public final class TinkerGadgets extends TinkerModule {
  /** Tab for all special tools added by the mod */
  public static final CreativeModeTab TAB_GADGETS = new SupplierItemGroup(TConstruct.modID, "gadgets", () -> new ItemStack(TinkerGadgets.slimeSling.get(SlimeType.EARTH)));
  static final Logger log = Util.getLogger("tinker_gadgets");

  /*
   * Block base properties
   */
  private static final Item.Properties GADGET_PROPS = new Item.Properties().tab(TAB_GADGETS);
  private static final Item.Properties UNSTACKABLE_PROPS = new Item.Properties().tab(TAB_GADGETS).stacksTo(1);
  private static final Function<Block,? extends BlockItem> DEFAULT_BLOCK_ITEM = (b) -> new BlockItem(b, GADGET_PROPS);
  private static final Function<Block,? extends BlockItem> TOOLTIP_BLOCK_ITEM = (b) -> new BlockTooltipItem(b, GADGET_PROPS);

  /*
   * Blocks
   */
  // TODO: moving to natura
  public static final ItemObject<PunjiBlock> punji = BLOCKS.register("punji", () -> new PunjiBlock(builder(Material.PLANT, NO_TOOL, SoundType.GRASS).strength(3.0F).noOcclusion()), HIDDEN_BLOCK_ITEM);

  /*
   * Items
   */
  public static final ItemObject<PiggyBackPackItem> piggyBackpack = ITEMS.register("piggy_backpack", () -> new PiggyBackPackItem(new Properties().tab(TinkerGadgets.TAB_GADGETS).stacksTo(16)));
  public static final EnumObject<FrameType,FancyItemFrameItem> itemFrame = ITEMS.registerEnum(FrameType.values(), "item_frame", (type) -> new FancyItemFrameItem(((world, pos, dir) -> new FancyItemFrameEntity(world, pos, dir, type.getId()))));
  // slime tools
  private static final Item.Properties SLING_PROPS = new Item.Properties().tab(TAB_GADGETS).stacksTo(1).durability(250);
  public static final EnumObject<SlimeType, BaseSlimeSlingItem> slimeSling = new EnumObject.Builder<SlimeType, BaseSlimeSlingItem>(SlimeType.class)
    .put(SlimeType.EARTH, ITEMS.register("earth_slime_sling", () -> new EarthSlimeSlingItem(SLING_PROPS)))
    .put(SlimeType.SKY, ITEMS.register("sky_slime_sling", () -> new SkySlimeSlingItem(SLING_PROPS)))
    .put(SlimeType.ICHOR, ITEMS.register("ichor_slime_sling", () -> new IchorSlimeSlingItem(SLING_PROPS)))
    .put(SlimeType.ENDER, ITEMS.register("ender_slime_sling", () -> new EnderSlimeSlingItem(SLING_PROPS)))
    .build();
  public static final EnumObject<SlimeType,SlimeBootsItem> slimeBoots = ITEMS.registerEnum(SlimeType.values(), "slime_boots", (type) -> new SlimeBootsItem(type, UNSTACKABLE_PROPS));
  // throwballs
  public static final ItemObject<GlowBallItem> glowBall = ITEMS.register("glow_ball", GlowBallItem::new);
  public static final ItemObject<EflnBallItem> efln = ITEMS.register("efln_ball", EflnBallItem::new);

  // foods
  private static final BlockBehaviour.Properties CAKE = builder(Material.CAKE, NO_TOOL, SoundType.WOOL).strength(0.5F);
  public static final EnumObject<SlimeType,FoodCakeBlock> cake = BLOCKS.registerEnum(SlimeType.LIQUID, "cake", type -> new FoodCakeBlock(CAKE, TinkerFood.getCake(type)), block -> new BlockItem(block, UNSTACKABLE_PROPS));

  // Shurikens
  private static final Item.Properties THROWABLE_PROPS = new Item.Properties().stacksTo(16).tab(TAB_GADGETS);
  public static final ItemObject<ShurikenItem> quartzShuriken = ITEMS.register("quartz_shuriken", () -> new ShurikenItem(THROWABLE_PROPS, QuartzShurikenEntity::new));
  public static final ItemObject<ShurikenItem> flintShuriken = ITEMS.register("flint_shuriken", () -> new ShurikenItem(THROWABLE_PROPS, FlintShurikenEntity::new));

  /*
   * Entities
   */
  public static final RegistryObject<EntityType<FancyItemFrameEntity>> itemFrameEntity = ENTITIES.register("fancy_item_frame", () -> {
    return EntityType.Builder.<FancyItemFrameEntity>of(
      FancyItemFrameEntity::new, MobCategory.MISC)
      .sized(0.5F, 0.5F)
      .setTrackingRange(160)
      .setUpdateInterval(Integer.MAX_VALUE)
      .setCustomClientFactory((spawnEntity, world) -> new FancyItemFrameEntity(TinkerGadgets.itemFrameEntity.get(), world))
      .setShouldReceiveVelocityUpdates(false);
  });
  public static final RegistryObject<EntityType<GlowballEntity>> glowBallEntity = ENTITIES.register("glow_ball", () -> {
    return EntityType.Builder.<GlowballEntity>of(GlowballEntity::new, MobCategory.MISC)
      .sized(0.25F, 0.25F)
      .setTrackingRange(64)
      .setUpdateInterval(10)
      .setCustomClientFactory((spawnEntity, world) -> new GlowballEntity(TinkerGadgets.glowBallEntity.get(), world))
      .setShouldReceiveVelocityUpdates(true);
  });
  public static final RegistryObject<EntityType<EflnBallEntity>> eflnEntity = ENTITIES.register("efln_ball", () -> {
    return EntityType.Builder.<EflnBallEntity>of(EflnBallEntity::new, MobCategory.MISC)
      .sized(0.25F, 0.25F)
      .setTrackingRange(64)
      .setUpdateInterval(10)
      .setCustomClientFactory((spawnEntity, world) -> new EflnBallEntity(TinkerGadgets.eflnEntity.get(), world))
      .setShouldReceiveVelocityUpdates(true);
  });
  public static final RegistryObject<EntityType<QuartzShurikenEntity>> quartzShurikenEntity = ENTITIES.register("quartz_shuriken", () -> {
    return EntityType.Builder.<QuartzShurikenEntity>of(QuartzShurikenEntity::new, MobCategory.MISC)
      .sized(0.25F, 0.25F)
      .setTrackingRange(64)
      .setUpdateInterval(10)
      .setCustomClientFactory((spawnEntity, world) -> new QuartzShurikenEntity(TinkerGadgets.quartzShurikenEntity.get(), world))
      .setShouldReceiveVelocityUpdates(true);
  });
  public static final RegistryObject<EntityType<FlintShurikenEntity>> flintShurikenEntity = ENTITIES.register("flint_shuriken", () -> {
    return EntityType.Builder.<FlintShurikenEntity>of(FlintShurikenEntity::new, MobCategory.MISC)
      .sized(0.25F, 0.25F)
      .setTrackingRange(64)
      .setUpdateInterval(10)
      .setCustomClientFactory((spawnEntity, world) -> new FlintShurikenEntity(TinkerGadgets.flintShurikenEntity.get(), world))
      .setShouldReceiveVelocityUpdates(true);
  });

  /**
   * Potions
   */
  public static final RegistryObject<CarryPotionEffect> carryEffect = POTIONS.register("carry", CarryPotionEffect::new);

  /*
   * Events
   */
  @SubscribeEvent
  void commonSetup(final FMLCommonSetupEvent event) {
    CapabilityTinkerPiggyback.register();
    MinecraftForge.EVENT_BUS.register(new GadgetEvents());
    event.enqueueWork(() -> cake.forEach(block -> ComposterBlock.add(1.0f, block)));
  }

  @SubscribeEvent
  void gatherData(final GatherDataEvent event) {
    if (event.includeServer()) {
      DataGenerator datagenerator = event.getGenerator();
      datagenerator.addProvider(new GadgetRecipeProvider(datagenerator));
    }
  }
}
