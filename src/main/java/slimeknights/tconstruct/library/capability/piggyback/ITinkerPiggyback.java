package slimeknights.tconstruct.library.capability.piggyback;

import net.minecraft.world.entity.player.Player;

public interface ITinkerPiggyback {

  void setRiddenPlayer(Player player);

  void updatePassengers();
}
