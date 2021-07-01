package slimeknights.tconstruct.gadgets.item;

import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class FancyItemFrameItem extends Item {

  private final TriFunction<? extends HangingEntity, Level, BlockPos, Direction> entityProvider;

  public FancyItemFrameItem(TriFunction<? extends HangingEntity, Level, BlockPos, Direction> entityProvider) {
    super(new Properties().tab(CreativeModeTab.TAB_DECORATIONS));
    this.entityProvider = entityProvider;
  }

  /**
   * Called when this item is used when targetting a Block
   */
  @Override
  @Nonnull
  public InteractionResult useOn(UseOnContext context) {
    BlockPos pos = context.getClickedPos();
    Direction facing = context.getClickedFace();
    BlockPos placeLocation = pos.relative(facing);
    Player player = context.getPlayer();
    ItemStack stack = context.getItemInHand();
    if (player != null && !this.canPlace(player, facing, stack, placeLocation)) {
      return InteractionResult.FAIL;
    } else {
      Level world = context.getLevel();
      HangingEntity frame = this.entityProvider.apply(world, placeLocation, facing);

      CompoundTag tag = stack.getTag();
      if (tag != null) {
        EntityType.updateCustomEntityTag(world, player, frame, tag);
      }

      if (frame.survives()) {
        if (!world.isClientSide) {
          frame.playPlacementSound();
          world.addFreshEntity(frame);
        }

        stack.shrink(1);
      }

      return InteractionResult.SUCCESS;
    }
  }

  private boolean canPlace(Player player, Direction facing, ItemStack stack, BlockPos pos) {
    return !Level.isOutsideBuildHeight(pos) && player.mayUseItemAt(pos, facing, stack);
  }

  @FunctionalInterface
  public interface TriFunction<R, T, U, V> {

    R apply(T t, U u, V v);
  }
}
