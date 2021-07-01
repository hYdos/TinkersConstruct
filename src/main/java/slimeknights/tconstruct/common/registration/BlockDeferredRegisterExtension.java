package slimeknights.tconstruct.common.registration;

import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.PressurePlateBlock.Sensitivity;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WoodButtonBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import slimeknights.mantle.registration.deferred.BlockDeferredRegister;
import slimeknights.mantle.registration.object.BuildingBlockObject;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.shared.item.BurnableBlockItem;
import slimeknights.tconstruct.shared.item.BurnableTallBlockItem;
import slimeknights.tconstruct.world.block.StrippableLogBlock;
import slimeknights.tconstruct.world.block.WoodenDoorBlock;

import java.util.function.Function;
import java.util.function.Supplier;

public class BlockDeferredRegisterExtension extends BlockDeferredRegister {

  public BlockDeferredRegisterExtension(String modID) {
    super(modID);
  }

  /**
   * Creates a new metal item object
   * @param name           Metal name
   * @param tagName        Name to use for tags for this block
   * @param blockSupplier  Supplier for the block
   * @param blockItem      Block item
   * @param itemProps      Properties for the item
   * @return  Metal item object
   */
  public MetalItemObject registerMetal(String name, String tagName, Supplier<Block> blockSupplier, Function<Block,? extends BlockItem> blockItem, Item.Properties itemProps) {
    ItemObject<Block> block = register(name + "_block", blockSupplier, blockItem);
    Supplier<Item> itemSupplier = () -> new Item(itemProps);
    RegistryObject<Item> ingot = itemRegister.register(name + "_ingot", itemSupplier);
    RegistryObject<Item> nugget = itemRegister.register(name + "_nugget", itemSupplier);
    return new MetalItemObject(tagName, block, ingot, nugget);
  }

  /**
   * Creates a new metal item object
   * @param name           Metal name
   * @param blockSupplier  Supplier for the block
   * @param blockItem      Block item
   * @param itemProps      Properties for the item
   * @return  Metal item object
   */
  public MetalItemObject registerMetal(String name, Supplier<Block> blockSupplier, Function<Block,? extends BlockItem> blockItem, Item.Properties itemProps) {
    return registerMetal(name, name, blockSupplier, blockItem, itemProps);
  }

  /**
   * Creates a new metal item object
   * @param name        Metal name
   * @param tagName     Name to use for tags for this block
   * @param blockProps  Properties for the block
   * @param blockItem   Block item
   * @param itemProps   Properties for the item
   * @return  Metal item object
   */
  public MetalItemObject registerMetal(String name, String tagName, BlockBehaviour.Properties blockProps, Function<Block,? extends BlockItem> blockItem, Item.Properties itemProps) {
    return registerMetal(name, tagName, () -> new Block(blockProps), blockItem, itemProps);
  }

  /**
   * Creates a new metal item object
   * @param name        Metal name
   * @param blockProps  Properties for the block
   * @param blockItem   Block item
   * @param itemProps   Properties for the item
   * @return  Metal item object
   */
  public MetalItemObject registerMetal(String name, BlockBehaviour.Properties blockProps, Function<Block,? extends BlockItem> blockItem, Item.Properties itemProps) {
    return registerMetal(name, name, blockProps, blockItem, itemProps);
  }

  /**
   * Registers everything needed to make a new wood type, including a wood type
   * @param name         Wood name
   * @param planksColor  Color of the planks
   * @param barkColor    Bark color
   * @param sound        Sound for the wood
   * @param group        Item group for the wood
   * @return  Wood object
   */
  public WoodBlockObject registerWood(String name, Material planksMaterial, MaterialColor planksColor, SoundType plankSound, ToolType planksTool, Material barkMaterial, MaterialColor barkColor, SoundType barkSound, CreativeModeTab group) {
    //WoodType woodType = WoodType.create(resourceName(name));
    Item.Properties itemProps = new Item.Properties().tab(group);

    // many of these are already burnable via tags, but simplier to set them all here
    Function<Integer, Function<? super Block, ? extends BlockItem>> burnableItem;
    Function<Integer, Function<? super Block, ? extends BlockItem>> burnableTallItem;
    if (barkMaterial.isFlammable()) {
      burnableItem     = burnTime -> block -> new BurnableBlockItem(block, itemProps, burnTime);
      burnableTallItem = burnTime -> block -> new BurnableTallBlockItem(block, itemProps, burnTime);
    } else {
      Function<? super Block, ? extends BlockItem> defaultItemBlock = block -> new BlockItem(block, itemProps);
      burnableItem = burnTime -> defaultItemBlock;
      burnableTallItem = burnTime -> block -> new DoubleHighBlockItem(block, itemProps);
    }

    // planks
    Function<? super Block, ? extends BlockItem> burnable300 = burnableItem.apply(300);
    BlockBehaviour.Properties planksProps = BlockBehaviour.Properties.of(planksMaterial, planksColor).harvestTool(planksTool).strength(2.0f, 3.0f).sound(plankSound);
    BuildingBlockObject planks = registerBuilding(name + "_planks", planksProps, burnable300);
    ItemObject<FenceBlock> fence = register(name + "_fence", () -> new FenceBlock(Properties.copy(planks.get())), burnable300);
    // logs and wood
    Supplier<? extends RotatedPillarBlock> stripped = () -> new RotatedPillarBlock(BlockBehaviour.Properties.of(planksMaterial, planksColor).harvestTool(planksTool).strength(2.0f).sound(plankSound));
    ItemObject<RotatedPillarBlock> strippedLog = register("stripped_" + name + "_log", stripped, burnable300);
    ItemObject<RotatedPillarBlock> strippedWood = register("stripped_" + name + "_wood", stripped, burnable300);
    ItemObject<RotatedPillarBlock> log = register(name + "_log", () -> new StrippableLogBlock(strippedLog,
      BlockBehaviour.Properties.of(barkMaterial, state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? planksColor : barkColor)
                              .harvestTool(ToolType.AXE).strength(2.0f).sound(barkSound)), burnable300);
    ItemObject<RotatedPillarBlock> wood = register(name + "_wood", () -> new StrippableLogBlock(strippedWood, BlockBehaviour.Properties.of(barkMaterial, barkColor).harvestTool(ToolType.AXE).strength(2.0f).sound(barkSound)), burnable300);

    // doors
    ItemObject<DoorBlock> door = register(name + "_door", () -> new WoodenDoorBlock(BlockBehaviour.Properties.of(planksMaterial, planksColor).harvestTool(planksTool).strength(3.0F).sound(plankSound).noOcclusion()), burnableTallItem.apply(200));
    ItemObject<TrapDoorBlock> trapdoor = register(name + "_trapdoor", () -> new TrapDoorBlock(BlockBehaviour.Properties.of(planksMaterial, planksColor).harvestTool(planksTool).strength(3.0F).sound(SoundType.WOOD).noOcclusion().isValidSpawn(Blocks::never)), burnable300);
    ItemObject<FenceGateBlock> fenceGate = register(name + "_fence_gate", () -> new FenceGateBlock(planksProps), burnable300);
    // redstone
    BlockBehaviour.Properties redstoneProps = BlockBehaviour.Properties.of(planksMaterial, planksColor).harvestTool(planksTool).noCollission().strength(0.5F).sound(plankSound);
    ItemObject<PressurePlateBlock> pressurePlate = register(name + "_pressure_plate", () -> new PressurePlateBlock(Sensitivity.EVERYTHING, redstoneProps), burnable300);
    ItemObject<WoodButtonBlock> button = register(name + "_button", () -> new WoodButtonBlock(redstoneProps), burnableItem.apply(100));
    // signs
    //RegistryObject<StandingSignBlock> standingSign = registerNoItem(name + "_sign", () -> new StandingSignBlock(AbstractBlock.Properties.create(material, planksColor).doesNotBlockMovement().hardnessAndResistance(1.0F).sound(sound), woodType));
    //RegistryObject<WallSignBlock> wallSign = registerNoItem(name + "_wall_sign", () -> new WallSignBlock(AbstractBlock.Properties.create(material, planksColor).doesNotBlockMovement().hardnessAndResistance(1.0F).sound(sound).lootFrom(standingSign), woodType));
    //RegistryObject<SignItem> signItem = this.itemRegister.register(name + "_sign", () -> new SignItem(new Item.Properties().maxStackSize(16).group(group), standingSign.get(), wallSign.get()));
    // finally, return
    return new WoodBlockObject(resource(name), /*woodType,*/ planks, log, strippedLog, wood, strippedWood, fence, fenceGate, door, trapdoor, pressurePlate, button/*, standingSign, wallSign*/);
  }
}
