package slimeknights.tconstruct.library.capability.piggyback;

import com.google.common.collect.Maps;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public class TinkerPiggybackSerializer implements ICapabilitySerializable<CompoundTag> {

  private final Player player;
  private final ITinkerPiggyback piggyback;
  private final LazyOptional<ITinkerPiggyback> providerCap;

  public TinkerPiggybackSerializer(@Nonnull Player player) {
    this.player = player;
    this.piggyback = new TinkerPiggybackHandler();
    this.piggyback.setRiddenPlayer(player);
    this.providerCap = LazyOptional.of(() -> this.piggyback);
  }

  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
    if (cap == CapabilityTinkerPiggyback.PIGGYBACK) {
      return this.providerCap.cast();
    }
    return LazyOptional.empty();
  }

  @Override
  public CompoundTag serializeNBT() {
    CompoundTag compoundNBT = new CompoundTag();
    ListTag riderList = new ListTag();

    // save riders
    for (Entity entity : this.player.getIndirectPassengers()) {
      String id = entity.getEncodeId();
      if (id != null && !"".equals(id)) {
        CompoundTag entityTag = new CompoundTag();
        CompoundTag entityDataTag = new CompoundTag();
        entity.saveWithoutId(entityDataTag);
        entityDataTag.putString("id", entity.getEncodeId());
        entityTag.putUUID("Attach", entity.getVehicle().getUUID());
        entityTag.put("Entity", entityDataTag);
        riderList.add(entityTag);
      }
    }

    compoundNBT.put("riders", riderList);

    if (riderList.isEmpty()) {
      return new CompoundTag();
    }

    return compoundNBT;
  }

  @Override
  public void deserializeNBT(CompoundTag nbt) {
    ListTag riderList = nbt.getList("riders", 10);

    Map<UUID, Entity> attachedTo = Maps.newHashMap();

    if (this.player.getCommandSenderWorld() instanceof ServerLevel) {
      ServerLevel serverWorld = (ServerLevel) this.player.getCommandSenderWorld();

      for (int i = 0; i < riderList.size(); i++) {
        CompoundTag entityTag = riderList.getCompound(i);
        Entity entity = EntityType.loadEntityRecursive(entityTag.getCompound("Entity"), serverWorld, (p_217885_1_) -> {
          return !serverWorld.addWithUUID(p_217885_1_) ? null : p_217885_1_;
        });
        if (entity != null) {
          UUID uuid = entityTag.getUUID("Attach");

          attachedTo.put(uuid, entity);
        }
      }
    }
  }
}
