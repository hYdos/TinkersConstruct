package slimeknights.tconstruct.tools.modifiers.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Constants.BlockFlags;
import slimeknights.tconstruct.common.network.UpdateNeighborsPacket;
import slimeknights.tconstruct.library.modifiers.SingleUseModifier;
import slimeknights.tconstruct.library.network.TinkerNetwork;
import slimeknights.tconstruct.library.tools.helper.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;

public class ExchangingModifier extends SingleUseModifier {
  public ExchangingModifier() {
    super(0x258474);
  }

  @Override
  public int getPriority() {
    // super low because we need to run after the shears ability modifier, and any other similar hooks
    return Short.MIN_VALUE - 20;
  }

  @Override
  public Boolean removeBlock(IModifierToolStack tool, int level, ToolHarvestContext context) {
    // must have blocks in the offhand
    ItemStack offhand = context.getLiving().getHeldItemOffhand();
    BlockState state = context.getState();
    Level world = context.getWorld();
    BlockPos pos = context.getPos();
    if ((!context.isEffective() && state.getDestroySpeed(world, pos) > 0) || offhand.isEmpty() || !(offhand.getItem() instanceof BlockItem)) {
      return null;
    }

    // from this point on, we are in charge of breaking the block, start by harvesting it so piglins get mad and stuff
    Player player = context.getPlayer();
    if (player != null) {
      state.getBlock().playerWillDestroy(world, pos, state, player);
    }

    // block is unchanged, stuck setting it to a temporary block before replacing, as otherwise we risk duplication with the TE and tryPlace will likely fail
    BlockItem blockItem = (BlockItem) offhand.getItem();
    BlockState fluidState = world.getFluidState(pos).createLegacyBlock();
    boolean placedBlock = false;
    if (state.getBlock() == blockItem.getBlock()) {
      // the 0 in the last parameter prevents neighbor updates, meaning torches won't drop
      // this is fine as the block will be replaced in the next step by the proper block,
      // however doing it in one step is probably more ideal for block updates, hence only doing it when needed
      placedBlock = world.setBlock(pos, fluidState, 0, 0);
      if (!placedBlock) {
        return false;
      }
    }

    // generate placing context
    Direction sideHit = context.getSideHit();
    // subtract the offsets instead of adding as the position is empty, want to "hit" a realistic location
    Vec3 hit = new Vec3((double)pos.getX() + 0.5D - sideHit.getStepX() * 0.5D, pos.getY() + 0.5D - sideHit.getStepY() * 0.5D, pos.getZ() + 0.5D - sideHit.getStepZ() * 0.5D);
    BlockPlaceContext blockUseContext = new BlockPlaceContext(world, player, InteractionHand.OFF_HAND, offhand, new BlockHitResult(hit, sideHit, pos, false));
    blockUseContext.replaceClicked = true; // force replacement, even if the position is not replacable (as it most always will be)

    // swap the block, it never goes to air so things like torches will remain
    InteractionResult success = blockItem.place(blockUseContext);
    if (success.consumesAction()) {
      if (!context.isAOE() && player != null) {
        TinkerNetwork.getInstance().sendTo(new UpdateNeighborsPacket(state, pos), player);
      }
      context.getLiving().swing(InteractionHand.OFF_HAND, false);
      return true;
    } else if (placedBlock) {
      // notify that the fluid was placed properly, as it was suppressed earlier, and placing again will fail to hit it
      state.updateIndirectNeighbourShapes(world, pos, BlockFlags.BLOCK_UPDATE, 511);
      fluidState.updateNeighbourShapes(world, pos, BlockFlags.BLOCK_UPDATE, 511);
      fluidState.updateIndirectNeighbourShapes(world, pos, BlockFlags.BLOCK_UPDATE, 511);
      return true;
    } else {
      // so we failed to place the new block for some reason, remove the old block to prevent dupes
      return world.setBlock(pos, fluidState, 3);
    }
  }
}
