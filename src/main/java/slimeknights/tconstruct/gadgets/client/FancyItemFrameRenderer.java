package slimeknights.tconstruct.gadgets.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.common.MinecraftForge;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.gadgets.entity.FancyItemFrameEntity;
import slimeknights.tconstruct.gadgets.entity.FrameType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import java.util.HashMap;
import java.util.Map;

// TODO: needs so much cleanup
public class FancyItemFrameRenderer extends EntityRenderer<FancyItemFrameEntity> {

  private static final ResourceLocation MAP_BACKGROUND_TEXTURES = new ResourceLocation("textures/map/map_background.png");

  private static final Map<FrameType, ModelResourceLocation> LOCATIONS_MODEL = new HashMap<>();
  private static final Map<FrameType, ModelResourceLocation> LOCATIONS_MODEL_MAP = new HashMap<>();

  private final Minecraft mc = Minecraft.getInstance();
  private final ItemRenderer itemRenderer;
  private final ItemFrameRenderer defaultRenderer;

  public FancyItemFrameRenderer(EntityRenderDispatcher renderManagerIn, ItemRenderer itemRendererIn) {
    super(renderManagerIn);
    this.itemRenderer = itemRendererIn;
    this.defaultRenderer = (ItemFrameRenderer) renderManagerIn.renderers.get(EntityType.ITEM_FRAME);

    for (FrameType frameType : FrameType.values()) {
      // TODO: reinstate when Forge fixes itself
      // LOCATIONS_MODEL.put(color, new ModelResourceLocation(new ResourceLocation(TConstruct.modID, frameType.getName() + "_frame"), "map=false"));
      // LOCATIONS_MODEL_MAP.put(color, new ModelResourceLocation(new ResourceLocation(TConstruct.modID, frameType.getName() + "_frame"), "map=true"));

      LOCATIONS_MODEL.put(frameType, new ModelResourceLocation(new ResourceLocation(TConstruct.modID, frameType.getSerializedName() + "_frame_empty"), "inventory"));
      LOCATIONS_MODEL_MAP.put(frameType, new ModelResourceLocation(new ResourceLocation(TConstruct.modID, frameType.getSerializedName() + "_frame_map"), "inventory"));
    }
  }

  @Override
  public void render(FancyItemFrameEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
    super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    matrixStackIn.pushPose();
    Vec3 vec3d = this.getRenderOffset(entityIn, partialTicks);
    matrixStackIn.translate(-vec3d.x(), -vec3d.y(), -vec3d.z());
    Direction direction = entityIn.getDirection();
    matrixStackIn.translate((double) direction.getStepX() * 0.46875D, (double) direction.getStepY() * 0.46875D, (double) direction.getStepZ() * 0.46875D);
    matrixStackIn.mulPose(Vector3f.XP.rotationDegrees(entityIn.xRot));
    matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180.0F - entityIn.yRot));

    // render the frame
    FrameType frameType = entityIn.getFrameType();
    ItemStack stack = entityIn.getItem();
    // clear does not render the frame if filled
    if (frameType != FrameType.CLEAR || stack.isEmpty()) {
      BlockRenderDispatcher blockrendererdispatcher = this.mc.getBlockRenderer();
      ModelManager modelmanager = blockrendererdispatcher.getBlockModelShaper().getModelManager();
      ModelResourceLocation location = entityIn.getItem().getItem() instanceof MapItem ? LOCATIONS_MODEL_MAP.get(frameType) : LOCATIONS_MODEL.get(frameType);
      matrixStackIn.pushPose();
      matrixStackIn.translate(-0.5D, -0.5D, -0.5D);
      blockrendererdispatcher.getModelRenderer().renderModel(matrixStackIn.last(), bufferIn.getBuffer(Sheets.cutoutBlockSheet()), null, modelmanager.getModel(location), 1.0F, 1.0F, 1.0F, packedLightIn, OverlayTexture.NO_OVERLAY);
      matrixStackIn.popPose();
    }

    // render the item
    if (!stack.isEmpty()) {
      MapItemSavedData mapdata = MapItem.getOrCreateSavedData(stack, entityIn.level);
      matrixStackIn.translate(0.0D, 0.0D, 0.4375D);
      int i = mapdata != null ? entityIn.getRotation() % 4 * 2 : entityIn.getRotation();
      matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees((float) i * 360.0F / 8.0F));
      if (!MinecraftForge.EVENT_BUS.post(new RenderItemInFrameEvent(entityIn, this.defaultRenderer, matrixStackIn, bufferIn, packedLightIn))) {
        if (mapdata != null) {
          matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
          matrixStackIn.scale(0.0078125F, 0.0078125F, 0.0078125F);
          matrixStackIn.translate(-64.0D, -64.0D, 0.0D);
          matrixStackIn.translate(0.0D, 0.0D, -1.0D);
          this.mc.gameRenderer.getMapRenderer().render(matrixStackIn, bufferIn, mapdata, true, packedLightIn);
        } else {
          matrixStackIn.scale(0.5F, 0.5F, 0.5F);
          this.itemRenderer.renderStatic(stack, ItemTransforms.TransformType.FIXED, packedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn);
        }
      }
    }

    matrixStackIn.popPose();
  }

  @Nullable
  @Override
  public ResourceLocation getEntityTexture(@Nonnull FancyItemFrameEntity entity) {
    return null;
  }

  @Override
  public Vec3 getRenderOffset(FancyItemFrameEntity entityIn, float partialTicks) {
    return new Vec3((float) entityIn.getDirection().getStepX() * 0.3F, -0.25D, (float) entityIn.getDirection().getStepZ() * 0.3F);
  }

  @Override
  protected boolean canRenderName(FancyItemFrameEntity entity) {
    if (Minecraft.renderNames() && !entity.getItem().isEmpty() && entity.getItem().hasCustomHoverName() && this.entityRenderDispatcher.crosshairPickEntity == entity) {
      double d0 = this.entityRenderDispatcher.distanceToSqr(entity);
      float f = entity.isDiscrete() ? 32.0F : 64.0F;
      return d0 < (double) (f * f);
    } else {
      return false;
    }
  }

  @Override
  protected void renderName(FancyItemFrameEntity entityIn, Component displayNameIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
    super.renderNameTag(entityIn, entityIn.getItem().getHoverName(), matrixStackIn, bufferIn, packedLightIn);
  }
}
