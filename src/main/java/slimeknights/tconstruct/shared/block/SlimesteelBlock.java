package slimeknights.tconstruct.shared.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public class SlimesteelBlock extends Block {

  public SlimesteelBlock(Properties properties) {
    super(properties);
  }

  @Override
  public void fallOn(Level worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
    if (entityIn.isSuppressingBounce()) {
      super.fallOn(worldIn, pos, entityIn, fallDistance);
    } else {
      entityIn.causeFallDamage(fallDistance, 0.0F);
    }

  }

  @Override
  public void updateEntityAfterFallOn(BlockGetter worldIn, Entity entity) {
    if (entity.isSuppressingBounce()) {
      super.updateEntityAfterFallOn(worldIn, entity);
    } else {
      Vec3 vector3d = entity.getDeltaMovement();
      if (vector3d.y < 0) {
        double d0 = entity instanceof LivingEntity ? 0.75 : 0.6;
        entity.setDeltaMovement(vector3d.x, -vector3d.y * d0, vector3d.z);
      }
    }
  }
}
