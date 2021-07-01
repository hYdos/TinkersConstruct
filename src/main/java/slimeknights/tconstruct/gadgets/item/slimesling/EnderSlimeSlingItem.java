package slimeknights.tconstruct.gadgets.item.slimesling;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.shared.block.SlimeType;

public class EnderSlimeSlingItem extends BaseSlimeSlingItem {

  public EnderSlimeSlingItem(Properties props) {
    super(props, SlimeType.ENDER);
  }

  /** Called when the player stops using an Item (stops holding the right mouse button). */
  @Override
  public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
    if (!(entityLiving instanceof Player)) {
      return;
    }

    Player player = (Player) entityLiving;
    float f = getForce(stack, timeLeft);

    Vec3 look = player.getLookAngle();
    double offX = look.x * f;
    double offY = look.y * f + 1; // add extra to help with bad collisions
    double offZ = look.z * f;

    // find teleport target
    BlockPos furthestPos = null;
    while (Math.abs(offX) > .5 || Math.abs(offY) > .5 || Math.abs(offZ) > .5) { // while not too close to player
      BlockPos posAttempt = new BlockPos(player.getX() + offX, player.getY() + offY, player.getZ() + offZ);

      // if we do not have a position yet, see if this one is valid
      if (furthestPos == null) {
        if (worldIn.getWorldBorder().isWithinBounds(posAttempt) && !worldIn.getBlockState(posAttempt).isSuffocating(worldIn, posAttempt)) {
          furthestPos = posAttempt;
        }
      } else {
        // if we already have a position, clear if the new one is unbreakable
        if (worldIn.getBlockState(posAttempt).getDestroySpeed(worldIn, posAttempt) == -1) {
          furthestPos = null;
        }
      }

      // update for next iteration
      offX -= (Math.abs(offX) > .25 ? (offX >= 0 ? 1 : -1) * .25 : 0);
      offY -= (Math.abs(offY) > .25 ? (offY >= 0 ? 1 : -1) * .25 : 0);
      offZ -= (Math.abs(offZ) > .25 ? (offZ >= 0 ? 1 : -1) * .25 : 0);
    }

    // get furthest teleportable block
    if (furthestPos != null) {
      player.getCooldowns().addCooldown(stack.getItem(), 3);
      player.setPos(furthestPos.getX() + 0.5f, furthestPos.getY(), furthestPos.getZ() + 0.5f);

      // particle effect from EnderPearlEntity
      for (int i = 0; i < 32; ++i) {
        worldIn.addParticle(ParticleTypes.PORTAL, player.getX(), player.getY() + worldIn.random.nextDouble() * 2.0D, player.getZ(), worldIn.random.nextGaussian(), 0.0D, worldIn.random.nextGaussian());
      }
      playerServerMovement(player);
      player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1f, 1f);
      onSuccess(player, stack);
    } else {
      playMissSound(player);
    }
  }
}
