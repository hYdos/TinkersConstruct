package slimeknights.tconstruct.smeltery.tileentity;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import slimeknights.mantle.tileentity.NamableTileEntity;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.fluid.FluidTankAnimated;
import slimeknights.tconstruct.library.materials.MaterialValues;
import slimeknights.tconstruct.library.utils.NBTTags;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.ControllerBlock;
import slimeknights.tconstruct.smeltery.block.MelterBlock;
import slimeknights.tconstruct.smeltery.inventory.AlloyerContainer;
import slimeknights.tconstruct.smeltery.tileentity.module.FuelModule;
import slimeknights.tconstruct.smeltery.tileentity.module.alloying.MixerAlloyTank;
import slimeknights.tconstruct.smeltery.tileentity.module.alloying.SingleAlloyingModule;

import javax.annotation.Nullable;
import java.util.Collections;

/**
 * Dedicated alloying block
 */
public class AlloyerTileEntity extends NamableTileEntity implements ITankTileEntity, TickableBlockEntity {
  /** Max capacity for the tank */
  private static final int TANK_CAPACITY = MaterialValues.METAL_BLOCK * 3;

  /** Tank for this mixer */
  @Getter
  protected final FluidTankAnimated tank = new FluidTankAnimated(TANK_CAPACITY, this);
  /* Capability for return */
  private final LazyOptional<IFluidHandler> tankHolder = LazyOptional.of(() -> tank);

  // modules
  /** Logic for a mixer alloying */
  @Getter
  private final MixerAlloyTank alloyTank = new MixerAlloyTank(this, tank);
  /** Base alloy logic */
  private final SingleAlloyingModule alloyingModule = new SingleAlloyingModule(this, alloyTank);
  /** Fuel handling logic */
  @Getter
  private final FuelModule fuelModule = new FuelModule(this, () -> Collections.singletonList(this.pos.down()));

  /** Last comparator strength to reduce block updates */
  @Getter @Setter
  private int lastStrength = -1;

  /** Internal tick counter */
  private int tick;

  public AlloyerTileEntity() {
    this(TinkerSmeltery.alloyer.get());
  }

  protected AlloyerTileEntity(BlockEntityType<?> type) {
    super(type, Util.makeTranslation("gui", "alloyer"));
  }

  /*
   * Capability
   */

  @Override
  public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
    if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
      return tankHolder.cast();
    }
    return super.getCapability(capability, facing);
  }

  @Override
  protected void invalidateCaps() {
    super.invalidateCaps();
    this.tankHolder.invalidate();
  }


  /*
   * Alloying
   */

  /** Checks if the tile entity is active */
  private boolean isFormed() {
    BlockState state = this.getBlockState();
    return state.hasProperty(MelterBlock.IN_STRUCTURE) && state.getValue(MelterBlock.IN_STRUCTURE);
  }

  @Override
  public void tick() {
    if (level == null || level.isClientSide || !isFormed()) {
      return;
    }

    switch (tick) {
      // tick 0: find fuel
      case 0:
        alloyTank.setTemperature(fuelModule.findFuel(false));
        if (!fuelModule.hasFuel() && alloyingModule.canAlloy()) {
          fuelModule.findFuel(true);
        }
        break;
        // tick 2: alloy alloys and consume fuel
      case 2: {
        BlockState state = getBlockState();
        boolean hasFuel = fuelModule.hasFuel();

        // update state for new fuel state
        if (state.getValue(ControllerBlock.ACTIVE) != hasFuel) {
          level.setBlockAndUpdate(worldPosition, state.setValue(ControllerBlock.ACTIVE, hasFuel));
          // update the heater below
          BlockPos down = worldPosition.below();
          BlockState downState = level.getBlockState(down);
          if (TinkerTags.Blocks.FUEL_TANKS.contains(downState.getBlock()) && downState.hasProperty(ControllerBlock.ACTIVE) && downState.getValue(ControllerBlock.ACTIVE) != hasFuel) {
            level.setBlockAndUpdate(down, downState.setValue(ControllerBlock.ACTIVE, hasFuel));
          }
        }

        // actual alloying
        if (hasFuel) {
          alloyTank.setTemperature(fuelModule.getTemperature());
          alloyingModule.doAlloy();
          fuelModule.decreaseFuel(1);
        }
        break;
      }
    }
    tick = (tick + 1) % 4;
  }

  /**
   * Called when a neighbor of this block is changed to update the tank cache
   * @param side  Side changed
   */
  public void neighborChanged(Direction side) {
    alloyTank.refresh(side, true);
  }

  /*
   * Display
   */

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inv, Player playerEntity) {
    return new AlloyerContainer(id, inv, this);
  }


  /*
   * NBT
   */

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  public void writeSynced(CompoundTag tag) {
    super.writeSynced(tag);
    tag.put(NBTTags.TANK, tank.writeToNBT(new CompoundTag()));
  }

  @Override
  public CompoundTag save(CompoundTag tag) {
    tag = super.save(tag);
    fuelModule.writeToNBT(tag);
    return tag;
  }

  @Override
  public void load(BlockState state, CompoundTag nbt) {
    super.load(state, nbt);
    tank.readFromNBT(nbt.getCompound(NBTTags.TANK));
    fuelModule.readFromNBT(nbt);
  }
}
