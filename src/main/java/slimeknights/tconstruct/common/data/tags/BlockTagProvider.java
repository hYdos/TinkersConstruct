package slimeknights.tconstruct.common.data.tags;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.registration.MetalItemObject;
import slimeknights.tconstruct.common.registration.WoodBlockObject;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.TinkerMaterials;
import slimeknights.tconstruct.shared.block.ClearStainedGlassBlock.GlassColor;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.data.SmelteryCompat;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.world.TinkerWorld;

public class BlockTagProvider extends BlockTagsProvider {

  public BlockTagProvider(DataGenerator generatorIn, ExistingFileHelper existingFileHelper) {
    super(generatorIn, TConstruct.modID, existingFileHelper);
  }

  @Override
  protected void addTags() {
    this.addCommon();
    this.addTools();
    this.addWorld();
    this.addSmeltery();
    this.addFluids();
  }

  private void addCommon() {
    // ores
    addMetalTags(TinkerMaterials.copper);
    addMetalTags(TinkerMaterials.cobalt);
    // tier 3
    addMetalTags(TinkerMaterials.slimesteel);
    addMetalTags(TinkerMaterials.tinkersBronze);
    addMetalTags(TinkerMaterials.roseGold);
    addMetalTags(TinkerMaterials.pigIron);
    // tier 4
    addMetalTags(TinkerMaterials.queensSlime);
    addMetalTags(TinkerMaterials.manyullyn);
    addMetalTags(TinkerMaterials.hepatizon);
    addMetalTags(TinkerMaterials.soulsteel);
    // tier 5
    addMetalTags(TinkerMaterials.knightslime);
    this.tag(BlockTags.BEACON_BASE_BLOCKS).add(TinkerModifiers.silkyJewelBlock.get());

    // glass
    this.tag(Tags.Blocks.GLASS_COLORLESS).add(TinkerCommons.clearGlass.get());
    this.tag(Tags.Blocks.GLASS_PANES_COLORLESS).add(TinkerCommons.clearGlassPane.get());
    addGlass(TinkerCommons.clearStainedGlass, "glass/", tag(Tags.Blocks.STAINED_GLASS));
    addGlass(TinkerCommons.clearStainedGlassPane, "glass_panes/", tag(Tags.Blocks.STAINED_GLASS_PANES));
    // impermeable for all glass
    TagAppender<Block> impermeable = tag(BlockTags.IMPERMEABLE);
    impermeable.add(TinkerCommons.clearGlass.get(), TinkerCommons.soulGlass.get(), TinkerSmeltery.searedGlass.get());
    TinkerCommons.clearStainedGlass.forEach(impermeable::add);

    // soul speed on glass
    this.tag(BlockTags.SOUL_SPEED_BLOCKS).add(TinkerCommons.soulGlass.get(), TinkerCommons.soulGlassPane.get());
    this.tag(BlockTags.SOUL_FIRE_BASE_BLOCKS).add(TinkerCommons.soulGlass.get());

    TagsProvider.TagAppender<Block> builder = this.tag(TinkerTags.Blocks.ANVIL_METAL)
        // tier 3
        .addTag(TinkerMaterials.slimesteel.getBlockTag())
        .addTag(TinkerMaterials.tinkersBronze.getBlockTag())
        .addTag(TinkerMaterials.roseGold.getBlockTag())
        .addTag(TinkerMaterials.pigIron.getBlockTag())
        // tier 4
        .addTag(TinkerMaterials.queensSlime.getBlockTag())
        .addTag(TinkerMaterials.manyullyn.getBlockTag())
        .addTag(TinkerMaterials.hepatizon.getBlockTag())
        .addTag(Tags.Blocks.STORAGE_BLOCKS_NETHERITE);
    for (SmelteryCompat compat : SmelteryCompat.values()) {
      if (!compat.isOre()) {
        builder.addOptionalTag(new ResourceLocation("forge", "storage_blocks/" + compat.getName()));
      }
    }
  }

  private void addTools() {
    // vanilla is not tagged, so tag it
    this.tag(TinkerTags.Blocks.WORKBENCHES)
        .add(Blocks.CRAFTING_TABLE, TinkerTables.craftingStation.get())
        .addOptionalTag(new ResourceLocation("forge:workbench")); // some mods use a non-standard name here, so support it I guess
    this.tag(TinkerTags.Blocks.TABLES)
        .add(TinkerTables.craftingStation.get(), TinkerTables.partBuilder.get(), TinkerTables.tinkerStation.get());

    this.tag(BlockTags.GUARDED_BY_PIGLINS)
        .add(TinkerModifiers.silkyJewelBlock.get())
        .addTag(TinkerMaterials.roseGold.getBlockTag());

    // can harvest crops and sugar cane
    this.tag(TinkerTags.Blocks.HARVESTABLE_STACKABLE)
        .add(Blocks.SUGAR_CANE, Blocks.KELP_PLANT);
    this.tag(TinkerTags.Blocks.HARVESTABLE_CROPS)
        .addTag(BlockTags.CROPS)
        .addOptionalTag(new ResourceLocation("forge", "crops"))
        .add(Blocks.NETHER_WART);
    this.tag(TinkerTags.Blocks.HARVESTABLE_INTERACT)
        .add(Blocks.SWEET_BERRY_BUSH);
    this.tag(TinkerTags.Blocks.HARVESTABLE)
        .add(Blocks.PUMPKIN, Blocks.BEEHIVE, Blocks.BEE_NEST)
        .addTag(TinkerTags.Blocks.HARVESTABLE_CROPS)
        .addTag(TinkerTags.Blocks.HARVESTABLE_INTERACT)
        .addTag(TinkerTags.Blocks.HARVESTABLE_STACKABLE);
    // just logs for lumber axe, but modpack makers can add more
    this.tag(TinkerTags.Blocks.TREE_LOGS).addTag(BlockTags.LOGS);
  }


  private void addWorld() {
    TagsProvider.TagAppender<Block> slimeBlockBuilder = this.tag(TinkerTags.Blocks.SLIME_BLOCK);
    TagsProvider.TagAppender<Block> congealedBuilder = this.tag(TinkerTags.Blocks.CONGEALED_SLIME);
    this.tag(TinkerTags.Blocks.SLIMY_LOGS)
        .addTag(TinkerWorld.greenheart.getLogBlockTag())
        .addTag(TinkerWorld.skyroot.getLogBlockTag())
        .addTag(TinkerWorld.bloodshroom.getLogBlockTag());
    this.tag(TinkerTags.Blocks.SLIMY_PLANKS)
        .add(TinkerWorld.greenheart.get(), TinkerWorld.skyroot.get(), TinkerWorld.bloodshroom.get());
    TagsProvider.TagAppender<Block> treeTrunkBuilder = this.tag(TinkerTags.Blocks.SLIMY_TREE_TRUNKS)
                                                       .addTag(TinkerTags.Blocks.SLIMY_LOGS);
    for (SlimeType type : SlimeType.values()) {
      slimeBlockBuilder.add(TinkerWorld.slime.get(type));
      Block congealed = TinkerWorld.congealedSlime.get(type);
      congealedBuilder.add(congealed);
      treeTrunkBuilder.add(congealed); // for old worlds
    }

    TagsProvider.TagAppender<Block> leavesBuilder = this.tag(TinkerTags.Blocks.SLIMY_LEAVES);
    TagsProvider.TagAppender<Block> saplingBuilder = this.tag(TinkerTags.Blocks.SLIMY_SAPLINGS);
    for (SlimeType type : SlimeType.values()) {
      leavesBuilder.add(TinkerWorld.slimeLeaves.get(type));
      saplingBuilder.add(TinkerWorld.slimeSapling.get(type));
    }
    this.tag(BlockTags.LEAVES).addTag(TinkerTags.Blocks.SLIMY_LEAVES);
    this.tag(BlockTags.SAPLINGS).addTag(TinkerTags.Blocks.SLIMY_SAPLINGS);

    this.tag(Tags.Blocks.ORES)
        .addTag(TinkerTags.Blocks.ORES_COBALT)
        .addTag(TinkerTags.Blocks.ORES_COPPER);
    this.tag(TinkerTags.Blocks.ORES_COBALT).add(TinkerWorld.cobaltOre.get());
    this.tag(TinkerTags.Blocks.ORES_COPPER).add(TinkerWorld.copperOre.get());
    TagAppender<Block> slimyGrass = this.tag(TinkerTags.Blocks.SLIMY_GRASS);
    TinkerWorld.slimeGrass.forEach((slimeType, blockObj) -> blockObj.forEach(slimyGrass::add));

    // allow the enderman to hold more blocks
    TagsProvider.TagAppender<Block> endermanHoldable = this.tag(BlockTags.ENDERMAN_HOLDABLE);
    endermanHoldable.addTag(TinkerTags.Blocks.CONGEALED_SLIME).add(TinkerSmeltery.grout.get());
    TinkerWorld.slimeDirt.forEach(endermanHoldable::add);
    TinkerWorld.slimeGrass.forEach((key, type) -> type.forEach(endermanHoldable::add));

    this.tag(BlockTags.PLANKS).addTag(TinkerTags.Blocks.SLIMY_PLANKS);
    this.tag(BlockTags.LOGS).addTag(TinkerTags.Blocks.SLIMY_LOGS);
    addWoodTags(TinkerWorld.greenheart, true);
    addWoodTags(TinkerWorld.skyroot, true);
    addWoodTags(TinkerWorld.bloodshroom, false);
  }

  private void addSmeltery() {
    // seared
    this.tag(TinkerTags.Blocks.SEARED_BRICKS).add(
      TinkerSmeltery.searedBricks.get(),
      TinkerSmeltery.searedFancyBricks.get(),
      TinkerSmeltery.searedTriangleBricks.get());
    this.tag(TinkerTags.Blocks.SEARED_BLOCKS)
        .add(TinkerSmeltery.searedStone.get(), TinkerSmeltery.searedCrackedBricks.get(), TinkerSmeltery.searedCobble.get(), TinkerSmeltery.searedPaver.get())
        .addTag(TinkerTags.Blocks.SEARED_BRICKS);
    this.tag(BlockTags.WALLS).add(TinkerSmeltery.searedBricks.getWall(), TinkerSmeltery.searedCobble.getWall());

    // scorched
    this.tag(TinkerTags.Blocks.SCORCHED_BLOCKS).add(
      TinkerSmeltery.scorchedStone.get(),
      TinkerSmeltery.polishedScorchedStone.get(),
      TinkerSmeltery.scorchedBricks.get(),
      TinkerSmeltery.scorchedRoad.get(),
      TinkerSmeltery.chiseledScorchedBricks.get());
    this.tag(BlockTags.FENCES).add(TinkerSmeltery.scorchedBricks.getFence());

    this.tag(TinkerTags.Blocks.CISTERN_CONNECTIONS)
        // cannot add channels as it requires a block state property to properly detect, look into a way to fix this later
        .add(TinkerSmeltery.searedFaucet.get(), TinkerSmeltery.scorchedFaucet.get());

    // tanks
    TagAppender<Block> searedTankBuilder = this.tag(TinkerTags.Blocks.SEARED_TANKS);
    TinkerSmeltery.searedTank.forEach(searedTankBuilder::add);
    TagAppender<Block> scorchedTankBuilder = this.tag(TinkerTags.Blocks.SCORCHED_TANKS);
    TinkerSmeltery.scorchedTank.forEach(scorchedTankBuilder::add);

    // structure tags
    // melter supports the heater as a tank
    this.tag(TinkerTags.Blocks.FUEL_TANKS)
        .add(TinkerSmeltery.searedHeater.get())
        .addTag(TinkerTags.Blocks.SEARED_TANKS)
        .addTag(TinkerTags.Blocks.SCORCHED_TANKS);
    this.tag(TinkerTags.Blocks.SMELTERY_TANKS).addTag(TinkerTags.Blocks.SEARED_TANKS);
    this.tag(TinkerTags.Blocks.FOUNDRY_TANKS).addTag(TinkerTags.Blocks.SCORCHED_TANKS);
    this.tag(TinkerTags.Blocks.ALLOYER_TANKS)
        .add(TinkerSmeltery.scorchedAlloyer.get(), TinkerSmeltery.searedMelter.get())
        .addTag(TinkerTags.Blocks.SEARED_TANKS)
        .addTag(TinkerTags.Blocks.SCORCHED_TANKS);

    // smeltery blocks
    // floor allows any basic seared blocks and all IO blocks
    this.tag(TinkerTags.Blocks.SMELTERY_FLOOR)
        .addTag(TinkerTags.Blocks.SEARED_BLOCKS)
        .add(TinkerSmeltery.searedDrain.get(), TinkerSmeltery.searedChute.get(), TinkerSmeltery.searedDuct.get());
    // wall allows seared blocks, tanks, glass, and IO
    this.tag(TinkerTags.Blocks.SMELTERY_WALL)
        .addTag(TinkerTags.Blocks.SEARED_BLOCKS)
        .addTag(TinkerTags.Blocks.SMELTERY_TANKS)
        .add(TinkerSmeltery.searedGlass.get(), TinkerSmeltery.searedLadder.get(),
             TinkerSmeltery.searedDrain.get(), TinkerSmeltery.searedChute.get(), TinkerSmeltery.searedDuct.get());
    // smeltery allows any of the three
    this.tag(TinkerTags.Blocks.SMELTERY)
        .addTag(TinkerTags.Blocks.SMELTERY_WALL)
        .addTag(TinkerTags.Blocks.SMELTERY_FLOOR)
        .addTag(TinkerTags.Blocks.SMELTERY_TANKS);

    // foundry blocks
    // floor allows any basic seared blocks and all IO blocks
    this.tag(TinkerTags.Blocks.FOUNDRY_FLOOR)
        .addTag(TinkerTags.Blocks.SCORCHED_BLOCKS)
        .add(TinkerSmeltery.scorchedDrain.get(), TinkerSmeltery.scorchedChute.get(), TinkerSmeltery.scorchedDuct.get());
    // wall allows seared blocks, tanks, glass, and IO
    this.tag(TinkerTags.Blocks.FOUNDRY_WALL)
        .addTag(TinkerTags.Blocks.SCORCHED_BLOCKS)
        .addTag(TinkerTags.Blocks.FOUNDRY_TANKS)
        .add(TinkerSmeltery.scorchedGlass.get(), TinkerSmeltery.scorchedLadder.get(),
             TinkerSmeltery.scorchedDrain.get(), TinkerSmeltery.scorchedChute.get(), TinkerSmeltery.scorchedDuct.get());
    // foundry allows any of the three
    this.tag(TinkerTags.Blocks.FOUNDRY)
        .addTag(TinkerTags.Blocks.FOUNDRY_WALL)
        .addTag(TinkerTags.Blocks.FOUNDRY_FLOOR)
        .addTag(TinkerTags.Blocks.FOUNDRY_TANKS);

    // climb seared ladder
    this.tag(BlockTags.CLIMBABLE).add(TinkerSmeltery.searedLadder.get(), TinkerSmeltery.scorchedLadder.get());
    this.tag(BlockTags.DRAGON_IMMUNE).add(TinkerCommons.obsidianPane.get());
  }

  private void addFluids() {
    this.tag(BlockTags.STRIDER_WARM_BLOCKS).add(TinkerFluids.magma.getBlock(), TinkerFluids.blazingBlood.getBlock());
  }

  @Override
  public String getName() {
    return "Tinkers Construct Block Tags";
  }

  /**
   * Adds relevant tags for a metal object
   * @param metal  Metal object
   */
  private void addMetalTags(MetalItemObject metal) {
    this.tag(metal.getBlockTag()).add(metal.get());
    this.tag(BlockTags.BEACON_BASE_BLOCKS).addTag(metal.getBlockTag());
    this.tag(Tags.Blocks.STORAGE_BLOCKS).addTag(metal.getBlockTag());
  }

  /** Adds tags for a glass item object */
  private void addGlass(EnumObject<GlassColor,? extends Block> blockObj, String tagPrefix, TagAppender<Block> blockTag) {
    blockObj.forEach((color, block) -> {
      blockTag.add(block);
      this.tag(BlockTags.createOptional(new ResourceLocation("forge", tagPrefix + color))).add(block);
    });
  }

  /** Adds all tags relevant to the given wood object */
  private void addWoodTags(WoodBlockObject object, boolean doesBurn) {
    // planks, handled by slimy planks tag
    //this.getOrCreateBuilder(BlockTags.PLANKS).add(object.get());
    this.tag(BlockTags.WOODEN_SLABS).add(object.getSlab());
    this.tag(BlockTags.WOODEN_STAIRS).add(object.getStairs());
    // logs
    this.tag(object.getLogBlockTag()).add(object.getLog(), object.getStrippedLog(), object.getWood(), object.getStrippedWood());

    // doors
    this.tag(BlockTags.WOODEN_FENCES).add(object.getFence());
    this.tag(Tags.Blocks.FENCES_WOODEN).add(object.getFence());
    this.tag(BlockTags.FENCE_GATES).add(object.getFenceGate());
    this.tag(Tags.Blocks.FENCE_GATES_WOODEN).add(object.getFenceGate());
    this.tag(BlockTags.WOODEN_DOORS).add(object.getDoor());
    this.tag(BlockTags.WOODEN_TRAPDOORS).add(object.getTrapdoor());
    // redstone
    this.tag(BlockTags.WOODEN_BUTTONS).add(object.getButton());
    this.tag(BlockTags.WOODEN_PRESSURE_PLATES).add(object.getPressurePlate());

    if (doesBurn) {
      // regular logs is handled by slimy logs tag
      this.tag(BlockTags.LOGS_THAT_BURN).addTag(object.getLogBlockTag());
    } else {
      this.tag(BlockTags.NON_FLAMMABLE_WOOD)
          .add(object.get(), object.getSlab(), object.getStairs(),
               object.getFence(), object.getFenceGate(), object.getDoor(), object.getTrapdoor(),
               object.getPressurePlate(), object.getButton())
          .addTag(object.getLogBlockTag());
    }
  }
}
