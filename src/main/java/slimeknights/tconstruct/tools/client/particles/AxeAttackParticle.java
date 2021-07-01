package slimeknights.tconstruct.tools.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import slimeknights.tconstruct.library.client.particle.AttackParticle;

public class AxeAttackParticle extends AttackParticle {

  public AxeAttackParticle(ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet spriteList) {
    super(world, x, y, z, xSpeed, ySpeed, zSpeed, spriteList);

    this.quadSize = 0.4F;
  }

  public static class Factory implements ParticleProvider<SimpleParticleType> {

    private final SpriteSet spriteSet;

    public Factory(SpriteSet spriteSet) {
      this.spriteSet = spriteSet;
    }

    public Particle makeParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
      return new AxeAttackParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
    }
  }
}
