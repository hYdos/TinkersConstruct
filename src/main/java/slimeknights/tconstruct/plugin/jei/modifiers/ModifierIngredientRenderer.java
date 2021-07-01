package slimeknights.tconstruct.plugin.jei.modifiers;

import lombok.RequiredArgsConstructor;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;

import javax.annotation.Nullable;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;

@RequiredArgsConstructor
public class ModifierIngredientRenderer implements IIngredientRenderer<ModifierEntry> {
  private final int width;

  @Override
  public void render(PoseStack matrices, int x, int y, @Nullable ModifierEntry entry) {
    if (entry != null) {
      Component name = entry.getModifier().getDisplayName(entry.getLevel());
      Font fontRenderer = getFontRenderer(Minecraft.getInstance(), entry);
      x += (width - fontRenderer.width(name)) / 2;
      fontRenderer.drawShadow(matrices, name, x, y + 1, -1);
    }
  }

  @Override
  public List<Component> getTooltip(ModifierEntry entry, TooltipFlag iTooltipFlag) {
    return entry.getModifier().getDescriptionList();
  }
}
