package slimeknights.tconstruct.tables.block.table;

import slimeknights.mantle.block.RetexturedBlock;
import slimeknights.tconstruct.tables.block.TinkerTableBlock;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public abstract class RetexturedTableBlock extends TinkerTableBlock {
  public RetexturedTableBlock(Properties builder) {
    super(builder);
  }

  @Override
  public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(world, pos, state, placer, stack);
    RetexturedBlock.updateTextureBlock(world, pos, stack);
  }

  @Override
  public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
    return RetexturedBlock.getPickBlock(world, pos, state);
  }
}
