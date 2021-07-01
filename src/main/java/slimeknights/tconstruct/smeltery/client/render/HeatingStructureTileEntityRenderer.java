package slimeknights.tconstruct.smeltery.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.smeltery.block.ControllerBlock;
import slimeknights.tconstruct.smeltery.tileentity.HeatingStructureTileEntity;
import slimeknights.tconstruct.smeltery.tileentity.module.MeltingModuleInventory;
import slimeknights.tconstruct.smeltery.tileentity.multiblock.HeatingStructureMultiblock.StructureData;

public class HeatingStructureTileEntityRenderer extends BlockEntityRenderer<HeatingStructureTileEntity> {
 private static final float ITEM_SCALE = 15f/16f;
  public HeatingStructureTileEntityRenderer(BlockEntityRenderDispatcher rendererDispatcherIn) {
    super(rendererDispatcherIn);
  }

  @Override
  public void render(HeatingStructureTileEntity smeltery, float partialTicks, PoseStack matrices, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
    Level world = smeltery.getLevel();
    if (world == null) return;
    BlockState state = smeltery.getBlockState();
    if (!state.getValue(ControllerBlock.IN_STRUCTURE)) return;
    StructureData structure = smeltery.getStructure();
    if (structure == null) return;

    // relevant positions
    BlockPos pos = smeltery.getBlockPos();
    BlockPos minPos = structure.getMinInside();
    BlockPos maxPos = structure.getMaxInside();

    // offset to make rendering min pos relative
    matrices.pushPose();
    matrices.translate(minPos.getX() - pos.getX(), minPos.getY() - pos.getY(), minPos.getZ() - pos.getZ());
    // render tank fluids, use minPos for brightness
    SmelteryTankRenderer.renderFluids(matrices, buffer, smeltery.getTank(), minPos, maxPos, LevelRenderer.getLightColor(world, minPos));

    // render items
    int xd = 1 + maxPos.getX() - minPos.getX();
    int zd = 1 + maxPos.getZ() - minPos.getZ();
    int layer = xd * zd;
    Direction facing = state.getValue(ControllerBlock.FACING);
    Quaternion itemRotation = Vector3f.YP.rotationDegrees(-90.0F * (float)facing.get2DDataValue());
    MeltingModuleInventory inventory = smeltery.getMeltingInventory();
    for (int i = 0; i < inventory.getSlots(); i++) {
      ItemStack stack = inventory.getStackInSlot(i);
      if (!stack.isEmpty()) {
        // calculate position inside the smeltery from slot index
        int height = i / layer;
        int layerIndex = i % layer;
        int offsetX = layerIndex % xd;
        int offsetZ = layerIndex / xd;
        BlockPos itemPos = minPos.offset(offsetX, height, offsetZ);

        // offset to the slot position in the structure, scale, and rotate the item
        matrices.pushPose();
        matrices.translate(offsetX + 0.5f, height + 0.5f, offsetZ + 0.5f);
        matrices.mulPose(itemRotation);
        matrices.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        Minecraft.getInstance().getItemRenderer()
                 .renderStatic(stack, TransformType.NONE, LevelRenderer.getLightColor(world, itemPos),
                             OverlayTexture.NO_OVERLAY, matrices, buffer);
        matrices.popPose();
      }
    }

    matrices.popPose();
  }

  @Override
  public boolean isGlobalRenderer(HeatingStructureTileEntity tile) {
    return tile.getBlockState().getValue(ControllerBlock.IN_STRUCTURE) && tile.getStructure() != null;
  }
}
