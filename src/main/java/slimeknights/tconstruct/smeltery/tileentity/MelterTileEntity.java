package slimeknights.tconstruct.smeltery.tileentity;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import slimeknights.mantle.client.model.data.SinglePropertyData;
import slimeknights.mantle.tileentity.NamableTileEntity;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.client.model.ModelProperties;
import slimeknights.tconstruct.library.fluid.FluidTankAnimated;
import slimeknights.tconstruct.library.materials.MaterialValues;
import slimeknights.tconstruct.library.utils.NBTTags;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.ControllerBlock;
import slimeknights.tconstruct.smeltery.block.MelterBlock;
import slimeknights.tconstruct.smeltery.inventory.MelterContainer;
import slimeknights.tconstruct.smeltery.tileentity.module.FuelModule;
import slimeknights.tconstruct.smeltery.tileentity.module.MeltingModuleInventory;

import javax.annotation.Nullable;
import java.util.Collections;

public class MelterTileEntity extends NamableTileEntity implements ITankTileEntity, TickableBlockEntity {
  /** Max capacity for the tank */
  private static final int TANK_CAPACITY = MaterialValues.METAL_BLOCK;
  /* tags */
  private static final String TAG_FUEL = "fuel";
  private static final String TAG_TEMPERATURE = "temperature";
  private static final String TAG_INVENTORY = "inventory";

  /* Tank */
  /** Internal fluid tank output */
  @Getter
  protected final FluidTankAnimated tank = new FluidTankAnimated(TANK_CAPACITY, this);
  /** Capability holder for the tank */
  private final LazyOptional<IFluidHandler> tankHolder = LazyOptional.of(() -> tank);
  /** Tank data for the model */
  @Getter
  private final IModelData modelData = new SinglePropertyData<>(ModelProperties.FLUID_TANK, tank);
  /** Last comparator strength to reduce block updates */
  @Getter @Setter
  private int lastStrength = -1;

  /** Internal tick counter */
  private int tick;

  /* Heating */
  /** Handles all the melting needs */
  @Getter
  private final MeltingModuleInventory meltingInventory = new MeltingModuleInventory(this, tank, Config.COMMON.melterNuggetsPerOre::get, 3);
  /** Capability holder for the tank */
  private final LazyOptional<IItemHandler> inventoryHolder = LazyOptional.of(() -> meltingInventory);

  /** Fuel handling logic */
  @Getter
  private final FuelModule fuelModule = new FuelModule(this, () -> Collections.singletonList(this.pos.down()));

  /** Main constructor */
  public MelterTileEntity() {
    this(TinkerSmeltery.melter.get());
  }

  /** Extendable constructor */
  @SuppressWarnings("WeakerAccess")
  protected MelterTileEntity(BlockEntityType<? extends MelterTileEntity> type) {
    super(type, new TranslatableComponent(Util.makeTranslationKey("gui", "melter")));
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inv, Player playerEntity) {
    return new MelterContainer(id, inv, this);
  }

  /*
   * Tank methods
   */

  @Override
  public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
    if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
      return tankHolder.cast();
    }
    if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
      return inventoryHolder.cast();
    }
    return super.getCapability(capability, facing);
  }

  @Override
  protected void invalidateCaps() {
    super.invalidateCaps();
    this.tankHolder.invalidate();
    this.inventoryHolder.invalidate();
  }

  /*
   * Melting
   */

  /** Checks if the tile entity is active */
  private boolean isFormed() {
    BlockState state = this.getBlockState();
    return state.hasProperty(MelterBlock.IN_STRUCTURE) && state.getValue(MelterBlock.IN_STRUCTURE);
  }

  @Override
  public void tick() {
    if(!isServerWorld()) {
      return;
    }

    // are we fully formed?
    if (isFormed()) {

      switch (tick) {
        // tick 0: find fuel
        case 0:
          if (!fuelModule.hasFuel() && meltingInventory.canHeat(fuelModule.findFuel(false))) {
            fuelModule.findFuel(true);
          }
        // tick 2: heat items and consume fuel
        case 2: {
          assert level != null;
          BlockState state = getBlockState();
          boolean hasFuel = fuelModule.hasFuel();
          // update the active state
          if (state.getValue(ControllerBlock.ACTIVE) != hasFuel) {
            level.setBlockAndUpdate(worldPosition, state.setValue(ControllerBlock.ACTIVE, hasFuel));
            // update the heater below
            BlockPos down = worldPosition.below();
            BlockState downState = level.getBlockState(down);
            if (TinkerTags.Blocks.FUEL_TANKS.contains(downState.getBlock()) && downState.hasProperty(ControllerBlock.ACTIVE) && downState.getValue(ControllerBlock.ACTIVE) != hasFuel) {
              level.setBlockAndUpdate(down, downState.setValue(ControllerBlock.ACTIVE, hasFuel));
            }
          }
          // heat items
          if (hasFuel) {
            meltingInventory.heatItems(fuelModule.getTemperature());
            fuelModule.decreaseFuel(1);
          } else {
            meltingInventory.coolItems();
          }
        }
      }
      tick = (tick + 1) % 4;
    }
  }


  /*
   * NBT
   */

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  public void load(BlockState state, CompoundTag tag) {
    super.load(state, tag);
    tank.readFromNBT(tag.getCompound(NBTTags.TANK));
    fuelModule.readFromNBT(tag);
    if (tag.contains(TAG_INVENTORY, NBT.TAG_COMPOUND)) {
      meltingInventory.readFromNBT(tag.getCompound(TAG_INVENTORY));
    }
  }

  @Override
  public void writeSynced(CompoundTag tag) {
    super.writeSynced(tag);
    tag.put(NBTTags.TANK, tank.writeToNBT(new CompoundTag()));
    tag.put(TAG_INVENTORY, meltingInventory.writeToNBT());
  }

  @Override
  public CompoundTag save(CompoundTag tag) {
    tag = super.save(tag);
    fuelModule.writeToNBT(tag);
    return tag;
  }

  /*
   * Helpers
   */
  /** Checks if we are on a server world */
  private boolean isServerWorld() {
    return this.getLevel() != null && !this.getLevel().isClientSide;
  }
}
