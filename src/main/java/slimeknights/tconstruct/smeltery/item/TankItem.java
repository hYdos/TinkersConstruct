package slimeknights.tconstruct.smeltery.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import slimeknights.mantle.item.BlockTooltipItem;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.client.util.FluidTooltipHandler;
import slimeknights.tconstruct.library.materials.MaterialValues;
import slimeknights.tconstruct.library.utils.NBTTags;
import slimeknights.tconstruct.smeltery.tileentity.TankTileEntity;

import javax.annotation.Nullable;
import java.util.List;

public class TankItem extends BlockTooltipItem {
  private static final String KEY_FLUID = Util.makeTranslationKey("block", "tank.fluid");
  private static final String KEY_MB = Util.makeTranslationKey("block", "tank.mb");
  private static final String KEY_INGOTS = Util.makeTranslationKey("block", "tank.ingots");
  private static final String KEY_MIXED = Util.makeTranslationKey("block", "tank.mixed");

  private final boolean limitStackSize;
  public TankItem(Block blockIn, Properties builder, boolean limitStackSize) {
    super(blockIn, builder);
    this.limitStackSize = limitStackSize;
  }

  @Override
  public int getItemStackLimit(ItemStack stack) {
    if (!limitStackSize) {
      return super.getItemStackLimit(stack);
    }
    FluidTank tank = getFluidTank(stack);
    return tank.isEmpty() ? 64 : 16;
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
    if (stack.hasTag()) {
      FluidTank tank = getFluidTank(stack);
      if (tank.getFluidAmount() > 0) {
        tooltip.add(new TranslatableComponent(KEY_FLUID, tank.getFluid().getDisplayName()).withStyle(ChatFormatting.GRAY));
        int amount = tank.getFluidAmount();
        if (tank.getCapacity() % MaterialValues.INGOT != 0 || Screen.hasShiftDown()) {
          tooltip.add(new TranslatableComponent(KEY_MB, amount).withStyle(ChatFormatting.GRAY));
        } else {
          int ingots = amount / MaterialValues.INGOT;
          int mb = amount % MaterialValues.INGOT;
          if (mb == 0) {
            tooltip.add(new TranslatableComponent(KEY_INGOTS, ingots).withStyle(ChatFormatting.GRAY));
          } else {
            tooltip.add(new TranslatableComponent(KEY_MIXED, ingots, mb).withStyle(ChatFormatting.GRAY));
          }
          tooltip.add(FluidTooltipHandler.HOLD_SHIFT);
        }

      }
    }
    else {
      super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
  }

  @Nullable
  @Override
  public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
    return new TankItemFluidHandler(stack);
  }

  /**
   * Sets the tank to the given stack
   * @param stack  Stack
   * @param tank   Tank instance
   * @return  Stack with tank
   */
  public static ItemStack setTank(ItemStack stack, FluidTank tank) {
    if (tank.isEmpty()) {
      CompoundTag nbt = stack.getTag();
      if (nbt != null) {
        nbt.remove(NBTTags.TANK);
        if (nbt.isEmpty()) {
          stack.setTag(null);
        }
      }
    } else {
      stack.getOrCreateTag().put(NBTTags.TANK, tank.writeToNBT(new CompoundTag()));
    }
    return stack;
  }

  /**
   * Gets the tank for the given stack
   * @param stack  Tank stack
   * @return  Tank stored in the stack
   */
  public static FluidTank getFluidTank(ItemStack stack) {
    FluidTank tank = new FluidTank(TankTileEntity.getCapacity(stack.getItem()));
    if (stack.hasTag()) {
      assert stack.getTag() != null;
      tank.readFromNBT(stack.getTag().getCompound(NBTTags.TANK));
    }
    return tank;
  }
}
