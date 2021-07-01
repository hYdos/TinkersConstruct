package slimeknights.tconstruct.plugin.jei.entity;

import lombok.RequiredArgsConstructor;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.TConstruct;

import javax.annotation.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Renderer for entity type ingredients
 */
@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
public class EntityIngredientRenderer implements IIngredientRenderer<EntityType> {
  /** Entity types that will not render, as they either errored or are the wrong type */
  private static final Set<EntityType<?>> IGNORED_ENTITIES = new HashSet<>();

  private final int size;

  /** Cache of entities for each entity type */
  private final Map<EntityType<?>,Entity> ENTITY_MAP = new HashMap<>();

  @Override
  public void render(PoseStack matrixStack, int x, int y, @Nullable EntityType type) {
    if (type != null) {
      Level world = Minecraft.getInstance().level;
      if (world != null && !IGNORED_ENTITIES.contains(type)) {
        Entity entity;
        // players cannot be created using the type, but we can use the client player
        // side effect is it renders armor/items
        if (type == EntityType.PLAYER) {
          entity = Minecraft.getInstance().player;
        } else {
          // entity is created with the client world, but the entity map is thrown away when JEI restarts so they should be okay I think
          entity = ENTITY_MAP.computeIfAbsent(type, t -> t.create(world));
        }
        // only can draw living entities, plus non-living ones don't get recipes anyways
        if (entity instanceof LivingEntity) {
          // scale down large mobs, but don't scale up small ones
          LivingEntity livingEntity = (LivingEntity) entity;
          int scale = size / 2;
          float height = entity.getBbHeight();
          float width = entity.getBbWidth();
          if (height > 2 || width > 2) {
            scale = (int)(size / Math.max(height, width));
          }
          // catch exceptions drawing the entity to be safe, any caught exceptions blacklist the entity
          try {
            InventoryScreen.renderEntityInInventory(x + size / 2, y + size, scale, 0, 10, livingEntity);
            return;
          } catch (Exception e) {
            TConstruct.log.error("Error drawing entity " + type.getRegistryName(), e);
            IGNORED_ENTITIES.add(type);
            ENTITY_MAP.remove(type);
          }
        } else {
          // not living, so might as well skip next time
          IGNORED_ENTITIES.add(type);
          ENTITY_MAP.remove(type);
        }
      }

      // fallback, draw a pink and black "spawn egg"
      Minecraft minecraft = Minecraft.getInstance();
      minecraft.getTextureManager().bind(EntityMeltingRecipeCategory.BACKGROUND_LOC);
      int offset = (size - 16) / 2;
      Screen.blit(matrixStack, x + offset, y + offset, 149f, 58f, 16, 16, 256, 256);
    }
  }

  @Override
  public List<Component> getTooltip(EntityType type, TooltipFlag flag) {
    List<Component> tooltip = new ArrayList<>();
    tooltip.add(type.getDescription());
    return tooltip;
  }
}
