package slimeknights.tconstruct.library.tools.helper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;

/** Context for harvest related modifier hooks */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ToolHarvestContext {
  /** World containing the harvested block */
  private final ServerLevel world;
  /** Living entity harvesting the block */
  private final LivingEntity living;
  /** Player harvesting the block, null if not a player */
  @Nullable
  private final ServerPlayer player;
  /** State being harvested */
  private final BlockState state;
  /** Position being harvested */
  private final BlockPos pos;
  /** Side of the block being hit */
  private final Direction sideHit;
  /** If true, this block can be harvested by the tool */
  @Accessors(fluent = true)
  private final boolean canHarvest;
  /** If true, the tool is effective on the block */
  private final boolean isEffective;

  /* AOE context */
  /** If true, this block is not the originally targeted block */
  private final boolean isAOE;
  /** Originally targeted position for AOE blocks. Will be the same as {@link #pos} for the original block */
  private final BlockPos targetedPos;
  /** Originally targeted block state. Will be the same as {@link #state} for the original block */
  private final BlockState targetedState;

  public ToolHarvestContext(ServerLevel world, ServerPlayer player, BlockState state, BlockPos pos, Direction sideHit, boolean canHarvest, boolean isEffective) {
    this.world = world;
    this.living = player;
    this.player = player;
    this.state = state;
    this.pos = pos;
    this.canHarvest = canHarvest;
    this.isEffective = isEffective;
    this.sideHit = sideHit;
    this.isAOE = false;
    this.targetedPos = pos;
    this.targetedState = state;
  }

  public ToolHarvestContext(ServerLevel world, LivingEntity living, BlockState state, BlockPos pos, Direction sideHit, boolean canHarvest, boolean isEffective) {
    this.world = world;
    this.living = living;
    this.player = living instanceof ServerPlayer ? (ServerPlayer) living : null;
    this.state = state;
    this.pos = pos;
    this.canHarvest = canHarvest;
    this.isEffective = isEffective;
    this.sideHit = sideHit;
    this.isAOE = false;
    this.targetedPos = pos;
    this.targetedState = state;
  }

  /** Creates a copy of this context for the given position */
  public ToolHarvestContext forPosition(BlockPos pos, BlockState state) {
    return new ToolHarvestContext(this.world, this.living, this.player, state, pos, this.sideHit, state.canHarvestBlock(world, pos, player), true, true, this.targetedPos, this.targetedState);
  }
}
