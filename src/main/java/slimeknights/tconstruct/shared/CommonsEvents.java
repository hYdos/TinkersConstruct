package slimeknights.tconstruct.shared;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.world.TinkerWorld;

@SuppressWarnings("unused")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Mod.EventBusSubscriber(modid = TConstruct.modID)
public class CommonsEvents {

  // Slimy block jump stuff
  @SubscribeEvent
  static void onLivingJump(LivingEvent.LivingJumpEvent event) {
    if (event.getEntity() == null) {
      return;
    }

    // check if we jumped from a slime block
    BlockPos pos = new BlockPos(event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ());
    if (event.getEntity().getCommandSenderWorld().isEmptyBlock(pos)) {
      pos = pos.below();
    }
    BlockState state = event.getEntity().getCommandSenderWorld().getBlockState(pos);
    Block block = state.getBlock();

    if (TinkerWorld.congealedSlime.contains(block)) {
      bounce(event.getEntity(), 0.25f);
    } else if (TinkerWorld.slimeDirt.contains(block) || TinkerWorld.vanillaSlimeGrass.contains(block) || TinkerWorld.earthSlimeGrass.contains(block) || TinkerWorld.skySlimeGrass.contains(block) || TinkerWorld.enderSlimeGrass.contains(block) || TinkerWorld.ichorSlimeGrass.contains(block)) {
      bounce(event.getEntity(), 0.06f);
    }
  }

  private static void bounce(Entity entity, float amount) {
    entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, (double) amount, 0.0D));
    entity.playSound(SoundEvents.SLIME_SQUISH, 0.5f + amount, 1f);
  }

  /** Tag for players who have received the book */
  private static final String TAG_PLAYER_HAS_BOOK = Util.prefix("spawned_book");

  @SubscribeEvent
  static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    if (Config.COMMON.shouldSpawnWithTinkersBook.get()) {
      CompoundTag playerData = event.getPlayer().getPersistentData();
      CompoundTag data = TagUtil.getTagSafe(playerData, Player.PERSISTED_NBT_TAG);

      if (!data.getBoolean(TAG_PLAYER_HAS_BOOK)) {
        ItemHandlerHelper.giveItemToPlayer(event.getPlayer(), new ItemStack(TinkerCommons.materialsAndYou.get()));
        data.putBoolean(TAG_PLAYER_HAS_BOOK, true);
        playerData.put(Player.PERSISTED_NBT_TAG, data);
      }
    }
  }
}
