package slimeknights.tconstruct.common.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.registries.GameData;
import slimeknights.mantle.network.packet.IThreadsafePacket;

/**
 * Packet to notify neighbors that a block changed, used when breaking blocks in weird contexts that vanilla suppresses updates in for some reason
 */
@RequiredArgsConstructor
public class UpdateNeighborsPacket implements IThreadsafePacket {
  private final BlockState state;
  private final BlockPos pos;

  public UpdateNeighborsPacket(FriendlyByteBuf buffer) {
    this.state = GameData.getBlockStateIDMap().byId(buffer.readVarInt());
    this.pos = buffer.readBlockPos();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(Block.getId(state));
    buffer.writeBlockPos(pos);
  }

  @Override
  public void handleThreadsafe(Context context) {
    HandleClient.handle(this);
  }

  private static class HandleClient {
    private static void handle(UpdateNeighborsPacket packet) {
      Level world = Minecraft.getInstance().level;
      if (world != null) {
        packet.state.updateNeighbourShapes(world, packet.pos, BlockFlags.BLOCK_UPDATE, 511);
        packet.state.updateIndirectNeighbourShapes(world, packet.pos, BlockFlags.BLOCK_UPDATE, 511);
      }
    }
  }
}
