package slimeknights.tconstruct.world.entity;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.world.TinkerWorld;

public class EnderSlimeEntity extends Slime {
  public EnderSlimeEntity(EntityType<? extends EnderSlimeEntity> type, Level worldIn) {
    super(type, worldIn);
  }

  @Override
  protected ParticleOptions getParticleType() {
    return TinkerWorld.enderSlimeParticle.get();
  }

  /** Randomly teleports an entity, mostly copied from chorus fruit */
  private static void teleport(LivingEntity living) {
    double posX = living.getX();
    double posY = living.getY();
    double posZ = living.getZ();

    for(int i = 0; i < 16; ++i) {
      double x = posX + (living.getRandom().nextDouble() - 0.5D) * 16.0D;
      double y = Mth.clamp(posY + (double)(living.getRandom().nextInt(16) - 8), 0.0D, living.getCommandSenderWorld().getHeight() - 1);
      double z = posZ + (living.getRandom().nextDouble() - 0.5D) * 16.0D;
      if (living.isPassenger()) {
        living.stopRiding();
      }

      if (living.randomTeleport(x, y, z, true)) {
        SoundEvent soundevent = SoundEvents.ENDERMAN_TELEPORT; // TODO: unique sound
        living.getCommandSenderWorld().playSound(null, posX, posY, posZ, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);
        living.playSound(soundevent, 1.0F, 1.0F);
        break;
      }
    }
  }

  @Override
  public void doEnchantDamageEffects(LivingEntity slime, Entity target) {
    super.doEnchantDamageEffects(slime, target);
    if (target instanceof LivingEntity) {
      teleport((LivingEntity) target);
    }
  }

  @Override
  protected void actuallyHurt(DamageSource damageSrc, float damageAmount) {
    float oldHealth = getHealth();
    super.actuallyHurt(damageSrc, damageAmount);
    if (isAlive() && getHealth() < oldHealth) {
      teleport(this);
    }
  }
}
