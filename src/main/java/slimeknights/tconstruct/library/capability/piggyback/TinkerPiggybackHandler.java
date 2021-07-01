package slimeknights.tconstruct.library.capability.piggyback;

import slimeknights.tconstruct.library.network.TinkerNetwork;

import java.util.List;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class TinkerPiggybackHandler implements ITinkerPiggyback {

  private Player riddenPlayer;
  private List<Entity> lastPassengers;

  @Override
  public void setRiddenPlayer(Player player) {
    this.riddenPlayer = player;
  }

  @Override
  public void updatePassengers() {
    if (this.riddenPlayer != null) {
      // tell the player itself if his riders changed serverside
      if (!this.riddenPlayer.getPassengers().equals(this.lastPassengers)) {
        if (this.riddenPlayer instanceof ServerPlayer) {
          TinkerNetwork.getInstance().sendVanillaPacket(this.riddenPlayer, new ClientboundSetPassengersPacket(this.riddenPlayer));
        }
      }
      this.lastPassengers = this.riddenPlayer.getPassengers();
    }
  }
}
