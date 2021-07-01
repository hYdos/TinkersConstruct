package slimeknights.tconstruct.library.tinkering;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.network.NetworkHooks;
import slimeknights.tconstruct.tools.TinkerTools;

public class IndestructibleEntityItem extends ItemEntity {

  public IndestructibleEntityItem(EntityType<? extends IndestructibleEntityItem> entityType, Level world) {
    super(entityType, world);
  }

  public IndestructibleEntityItem(Level worldIn, double x, double y, double z, ItemStack stack) {
    super(TinkerTools.indestructibleItem.get(), worldIn);
    this.setPos(x, y, z);
    this.yRot = this.random.nextFloat() * 360.0F;
    this.setDeltaMovement(this.random.nextDouble() * 0.2D - 0.1D, 0.2D, this.random.nextDouble() * 0.2D - 0.1D);
    this.setItem(stack);
    this.setExtendedLifetime();
  }

  @Override
  public Packet<?> getAddEntityPacket() {
    return NetworkHooks.getEntitySpawningPacket(this);
  }

  public void setPickupDelayFrom(Entity reference) {
    if (reference instanceof ItemEntity) {
      short pickupDelay = this.getPickupDelay((ItemEntity) reference);
      this.setPickUpDelay(pickupDelay);
    }
    setDeltaMovement(reference.getDeltaMovement());
  }

  /**
   * workaround for private access on pickup delay. We simply read it from the items NBT representation ;)
   */
  private short getPickupDelay(ItemEntity reference) {
    CompoundTag tag = new CompoundTag();
    reference.addAdditionalSaveData(tag);
    return tag.getShort("PickupDelay");
  }

  @Override
  public boolean fireImmune() {
    return true;
  }

  @Override
  public boolean hurt(DamageSource source, float amount) {
    // prevent any damage besides out of world
    return source.getMsgId().equals(DamageSource.OUT_OF_WORLD.msgId);
  }
/*
  @SubscribeEvent
  public void onExpire(ItemExpireEvent event) {
    if (event.getEntityItem() instanceof IndestructibleEntityItem) {
      event.setCanceled(true);
    }
  }*/
}
