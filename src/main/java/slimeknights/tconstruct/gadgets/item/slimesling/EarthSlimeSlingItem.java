package slimeknights.tconstruct.gadgets.item.slimesling;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.library.SlimeBounceHandler;
import slimeknights.tconstruct.shared.block.SlimeType;

public class EarthSlimeSlingItem extends BaseSlimeSlingItem {

  public EarthSlimeSlingItem(Properties props) {
    super(props, SlimeType.EARTH);
  }

  /** Called when the player stops using an Item (stops holding the right mouse button). */
  @Override
  public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
    if (!(entityLiving instanceof Player)) {
      return;
    }

    Player player = (Player) entityLiving;
    float f = getForce(stack, timeLeft);

    // check if player was targeting a block
    BlockHitResult mop = getPlayerPOVHitResult(worldIn, player, ClipContext.Fluid.NONE);
    if (mop.getType() == HitResult.Type.BLOCK) {
      player.getCooldowns().addCooldown(stack.getItem(), 3);

      // we fling the inverted player look vector
      Vec3 vec = player.getLookAngle().normalize();
      player.push(vec.x * -f,
        vec.y * -f / 3f,
        vec.z * -f);

      playerServerMovement(player);
      SlimeBounceHandler.addBounceHandler(player);
      onSuccess(player, stack);
    } else {
      playMissSound(player);
    }
  }
}
