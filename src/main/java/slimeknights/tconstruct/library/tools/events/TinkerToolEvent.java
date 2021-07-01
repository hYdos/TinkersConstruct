package slimeknights.tconstruct.library.tools.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.util.Random;

@AllArgsConstructor
@Getter
public abstract class TinkerToolEvent extends Event {
  private final ItemStack stack;
  private final IModifierToolStack tool;
  public TinkerToolEvent(ItemStack stack) {
    this.stack = stack;
    this.tool = ToolStack.from(stack);
  }

  /**
   * Event fired when a kama tries to harvest a crop. Set result to {@link Result#ALLOW} if you handled the harvest yourself. Set the result to {@link Result#DENY} if the block cannot be harvested.
   */
  @HasResult
  @Getter
  public static class ToolHarvestEvent extends TinkerToolEvent {
    /** Item context, note this is the original context, so some information (such as position) may not be accurate */
    private final UseOnContext context;
    private final ServerLevel world;
    private final BlockState state;
    private final BlockPos pos;
    @Nullable
    private final Player player;
    public ToolHarvestEvent(ItemStack stack, IModifierToolStack tool, UseOnContext context, ServerLevel world, BlockState state, BlockPos pos, @Nullable Player player) {
      super(stack, tool);
      this.context = context;
      this.world = world;
      this.state = state;
      this.pos = pos;
      this.player = player;
    }

    /** Fires this event and posts the result */
    public Result fire() {
      MinecraftForge.EVENT_BUS.post(this);
      return this.getResult();
    }
  }

  /**
   * Event fired when a kama or scythe tries to shear an entity
   */
  @HasResult
  @Getter
  public static class ToolShearEvent extends TinkerToolEvent {
    private final Level world;
    private final Player player;
    private final Entity target;
    private final int fortune;
    public ToolShearEvent(ItemStack stack, IModifierToolStack tool, Level world, Player player, Entity target, int fortune) {
      super(stack, tool);
      this.world = world;
      this.player = player;
      this.target = target;
      this.fortune = fortune;
    }

    /** Fires this event and posts the result */
    public Result fire() {
      MinecraftForge.EVENT_BUS.post(this);
      return this.getResult();
    }

    /** Drops an item at the entity position */
    public static void dropItem(Entity target, ItemStack stack) {
      ItemEntity ent = target.spawnAtLocation(stack, 1.0F);
      if (ent != null) {
        Random rand = target.level.random;
        ent.setDeltaMovement(ent.getDeltaMovement().add((rand.nextFloat() - rand.nextFloat()) * 0.1F,
                                          rand.nextFloat() * 0.05F,
                                          (rand.nextFloat() - rand.nextFloat()) * 0.1F));
      }
    }
  }
}
