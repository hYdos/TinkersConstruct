package slimeknights.tconstruct.smeltery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import slimeknights.mantle.block.InventoryBlock;
import slimeknights.tconstruct.smeltery.block.component.SearedBlock;

/** Shared logic for all multiblock structure controllers */
public abstract class ControllerBlock extends InventoryBlock {
  public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
  public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
  public static final BooleanProperty IN_STRUCTURE = SearedBlock.IN_STRUCTURE;
  protected ControllerBlock(Properties builder) {
    super(builder);
    this.registerDefaultState(this.defaultBlockState().setValue(ACTIVE, false).setValue(IN_STRUCTURE, false));
  }


  /*
   * Block state
   */

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING, ACTIVE, IN_STRUCTURE);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
  }

  @Deprecated
  @Override
  public BlockState rotate(BlockState state, Rotation rotation) {
    return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
  }

  @Deprecated
  @Override
  public BlockState mirror(BlockState state, Mirror mirror) {
    return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
  }


  /*
   * Tile Entity interaction
   */

  /** @return True if the GUI can be opened */
  protected boolean canOpenGui(BlockState state) {
    return state.getValue(IN_STRUCTURE);
  }

  @Override
  protected boolean openGui(Player player, Level world, BlockPos pos) {
    BlockState state = world.getBlockState(pos);
    if (state.getBlock() == this && canOpenGui(state)) {
      return super.openGui(player, world, pos);
    }
    return false;
  }



  /*
   * Particles
   */

  /**
   * Spawns fire particles at the given location
   * @param world  World instance
   * @param state  Block state
   * @param x      Block X position
   * @param y      Block Y position
   * @param z      Block Z position
   * @param front  Block front
   * @param side   Block side offset
   */
  protected void spawnFireParticles(LevelAccessor world, BlockState state, double x, double y, double z, double front, double side) {
    spawnFireParticles(world, state, x, y, z, front, side, ParticleTypes.FLAME);
  }

  /**
   * Spawns fire particles at the given location
   * @param world     World instance
   * @param state     Block state
   * @param x         Block X position
   * @param y         Block Y position
   * @param z         Block Z position
   * @param front     Block front
   * @param side      Block side offset
   * @param particle  Particle to draw
   */
  protected void spawnFireParticles(LevelAccessor world, BlockState state, double x, double y, double z, double front, double side, ParticleOptions particle) {
    switch(state.getValue(FACING)) {
      case WEST:
        world.addParticle(ParticleTypes.SMOKE, x - front, y, z + side, 0.0D, 0.0D, 0.0D);
        world.addParticle(particle,            x - front, y, z + side, 0.0D, 0.0D, 0.0D);
        break;
      case EAST:
        world.addParticle(ParticleTypes.SMOKE, x + front, y, z + side, 0.0D, 0.0D, 0.0D);
        world.addParticle(particle,            x + front, y, z + side, 0.0D, 0.0D, 0.0D);
        break;
      case NORTH:
        world.addParticle(ParticleTypes.SMOKE, x + side, y, z - front, 0.0D, 0.0D, 0.0D);
        world.addParticle(particle,            x + side, y, z - front, 0.0D, 0.0D, 0.0D);
        break;
      case SOUTH:
        world.addParticle(ParticleTypes.SMOKE, x + side, y, z + front, 0.0D, 0.0D, 0.0D);
        world.addParticle(particle,            x + side, y, z + front, 0.0D, 0.0D, 0.0D);
        break;
    }
  }
}
