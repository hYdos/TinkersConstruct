package slimeknights.tconstruct.gadgets.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import slimeknights.tconstruct.gadgets.TinkerGadgets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FancyItemFrameEntity extends ItemFrame implements IEntityAdditionalSpawnData {

  private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(FancyItemFrameEntity.class, EntityDataSerializers.INT);
  private static final String TAG_VARIANT = "Variant";

  public FancyItemFrameEntity(EntityType<? extends FancyItemFrameEntity> type, Level world) {
    super(type, world);
  }

  public FancyItemFrameEntity(Level worldIn, BlockPos blockPos, Direction face, int variant) {
    super(TinkerGadgets.itemFrameEntity.get(), worldIn);
    this.pos = blockPos;
    this.setDirection(face);
    this.entityData.set(VARIANT, variant);
  }

  @Override
  protected void defineSynchedData() {
    super.defineSynchedData();

    this.entityData.define(VARIANT, 0);
  }

  public FrameType getFrameType() {
    return FrameType.byId(this.getVariantIndex());
  }

  public int getVariantIndex() {
    return this.entityData.get(VARIANT);
  }

  @Nullable
  @Override
  public ItemEntity spawnAtLocation(@Nonnull ItemStack stack, float offset) {
    if (stack.getItem() == Items.ITEM_FRAME) {
      stack = new ItemStack(FrameType.getFrameFromType(this.getFrameType()));
    }
    return super.spawnAtLocation(stack, offset);
  }

  @Nonnull
  @Override
  public ItemStack getPickedResult(HitResult target) {
    ItemStack held = this.getItem();
    if (held.isEmpty()) {
      return new ItemStack(FrameType.getFrameFromType(this.getFrameType()));
    } else {
      return held.copy();
    }
  }

  @Override
  public void addAdditionalSaveData(CompoundTag compound) {
    super.addAdditionalSaveData(compound);
    compound.putInt(TAG_VARIANT, this.getVariantIndex());
  }

  @Override
  public void readAdditionalSaveData(CompoundTag compound) {
    super.readAdditionalSaveData(compound);
    this.entityData.set(VARIANT, compound.getInt(TAG_VARIANT));
  }

  @Nonnull
  @Override
  public Packet<?> getAddEntityPacket() {
    return NetworkHooks.getEntitySpawningPacket(this);
  }

  @Override
  public void writeSpawnData(FriendlyByteBuf buffer) {
    buffer.writeVarInt(this.getVariantIndex());
    buffer.writeBlockPos(this.pos);
    buffer.writeVarInt(this.direction.get3DDataValue());
  }

  @Override
  public void readSpawnData(FriendlyByteBuf buffer) {
    this.entityData.set(VARIANT, buffer.readVarInt());
    this.pos = buffer.readBlockPos();
    this.setDirection(Direction.from3DDataValue(buffer.readVarInt()));
  }

  private static void removeClickEvents(Component text) {
    if (text instanceof MutableComponent) {
      ((MutableComponent)text).withStyle((p_213318_0_) -> p_213318_0_.withClickEvent(null))
          .getSiblings().forEach(FancyItemFrameEntity::removeClickEvents);
    }
  }

  @Override
  protected Component getTypeName() {
    return new TranslatableComponent(FrameType.getFrameFromType(this.getFrameType()).getDescriptionId());
  }
}
