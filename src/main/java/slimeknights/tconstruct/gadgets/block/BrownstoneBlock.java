package slimeknights.tconstruct.gadgets.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

public class BrownstoneBlock extends Block {

  public BrownstoneBlock() {
    super(Block.Properties.of(Material.STONE).strength(3.0F, 20.0F).sound(SoundType.STONE));
  }

  @Override
  public void stepOn(Level worldIn, BlockPos pos, Entity entityIn) {
    if (entityIn.isInWater()) {
      entityIn.setDeltaMovement(entityIn.getDeltaMovement().multiply(1.20D, 1.0D, 1.20D));
    } else {
      entityIn.setDeltaMovement(entityIn.getDeltaMovement().multiply(1.25D, 1.0D, 1.25D));
    }
  }
}
