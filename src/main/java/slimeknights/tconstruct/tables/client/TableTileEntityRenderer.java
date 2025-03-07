package slimeknights.tconstruct.tables.client;

import slimeknights.mantle.client.model.inventory.ModelItem;
import slimeknights.mantle.client.model.util.ModelHelper;
import slimeknights.mantle.client.render.RenderingHelper;
import slimeknights.tconstruct.library.client.model.block.TableModel;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Same as {@link slimeknights.mantle.client.render.InventoryTileEntityRenderer}, but uses {@link TableModel}.
 * TODO: migrate to an interface in Mantle
 * @param <T>  Tile entity type
 */
public class TableTileEntityRenderer<T extends BlockEntity & Container> extends BlockEntityRenderer<T> {
  public TableTileEntityRenderer(BlockEntityRenderDispatcher dispatcher) {
    super(dispatcher);
  }

  @Override
  public void render(T inventory, float partialTicks, PoseStack matrices, MultiBufferSource buffer, int light, int combinedOverlayIn) {
    if (!inventory.isEmpty()) {
      BlockState state = inventory.getBlockState();
      TableModel.BakedModel model = ModelHelper.getBakedModel(state, TableModel.BakedModel.class);
      if (model != null) {
        boolean isRotated = RenderingHelper.applyRotation(matrices, state);
        List<ModelItem> modelItems = model.getItems();

        for(int i = 0; i < modelItems.size(); ++i) {
          RenderingHelper.renderItem(matrices, buffer, inventory.getItem(i), modelItems.get(i), light);
        }

        if (isRotated) {
          matrices.popPose();
        }
      }
    }
  }

  @Override
  public boolean shouldRenderOffScreen(T tile) {
    return !tile.isEmpty();
  }
}
