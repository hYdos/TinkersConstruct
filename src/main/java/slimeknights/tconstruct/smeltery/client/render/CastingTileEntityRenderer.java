package slimeknights.tconstruct.smeltery.client.render;

import slimeknights.mantle.client.model.inventory.ModelItem;
import slimeknights.mantle.client.model.util.ModelHelper;
import slimeknights.mantle.client.render.FluidRenderer;
import slimeknights.mantle.client.render.RenderingHelper;
import slimeknights.tconstruct.library.client.RenderUtils;
import slimeknights.tconstruct.library.client.model.block.CastingModel;
import slimeknights.tconstruct.smeltery.client.util.CastingItemRenderTypeBuffer;
import slimeknights.tconstruct.smeltery.tileentity.CastingTileEntity;
import slimeknights.tconstruct.smeltery.tileentity.tank.CastingFluidHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class CastingTileEntityRenderer extends BlockEntityRenderer<CastingTileEntity> {
  public CastingTileEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
    super(dispatcher);
  }

  @Override
  public void render(CastingTileEntity casting, float partialTicks, PoseStack matrices, MultiBufferSource buffer, int light, int combinedOverlayIn) {
    BlockState state = casting.getBlockState();
    CastingModel.BakedModel model = ModelHelper.getBakedModel(state, CastingModel.BakedModel.class);
    if (model != null) {
      // rotate the matrix
      boolean isRotated = RenderingHelper.applyRotation(matrices, state);

      // if the recipe is in progress, start fading the item away
      int timer = casting.getTimer();
      int totalTime = casting.getRecipeTime();
      int itemOpacity = 0;
      int fluidOpacity = 0xFF;
      if (timer > 0 && totalTime > 0) {
        int opacity = (4 * 0xFF) * timer / totalTime;
        // fade item in
        itemOpacity = opacity / 4;

        // fade fluid and temperature out during last 10%
        if (opacity > 3 * 0xFF) {
          fluidOpacity = (4 * 0xFF) - opacity;
        } else {
          fluidOpacity = 0xFF;
        }
      }

      // render fluids
      CastingFluidHandler tank = casting.getTank();
      // if full, start rendering with opacity for progress
      if (tank.getFluid().getAmount() == tank.getCapacity()) {
        RenderUtils.renderTransparentCuboid(matrices, buffer, model.getFluid(), tank.getFluid(), fluidOpacity, light);
      } else {
        FluidRenderer.renderScaledCuboid(matrices, buffer, model.getFluid(), tank.getFluid(), 0, tank.getCapacity(), light, false);
      }

      // render items
      List<ModelItem> modelItems = model.getItems();
      // input is normal
      if (modelItems.size() >= 1) {
        RenderingHelper.renderItem(matrices, buffer, casting.getItem(0), modelItems.get(0), light);
      }

      // output may be the recipe output instead of the current item
      if (modelItems.size() >= 2) {
        ModelItem outputModel = modelItems.get(1);
        if(!outputModel.isHidden()) {
          // get output stack
          ItemStack output = casting.getItem(1);
          MultiBufferSource outputBuffer = buffer;
          if(itemOpacity > 0 && output.isEmpty()) {
            output = casting.getRecipeOutput();
            // apply a buffer wrapper to tint and add opacity
            outputBuffer = new CastingItemRenderTypeBuffer(buffer, itemOpacity, fluidOpacity);
          }
          RenderingHelper.renderItem(matrices, outputBuffer, output, outputModel, light);
        }
      }

      // pop back rotation
      if (isRotated) {
        matrices.popPose();
      }
    }
  }
}
