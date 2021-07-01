package slimeknights.tconstruct.tools.modifiers.ability;

import slimeknights.tconstruct.library.modifiers.SingleUseModifier;
import slimeknights.tconstruct.library.tools.helper.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;

import java.util.function.BiConsumer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class SilkyModifier extends SingleUseModifier {
  public SilkyModifier() {
    super(0xF7CDBB);
  }

  @Override
  public void applyHarvestEnchantments(IModifierToolStack tool, int level, ToolHarvestContext context, BiConsumer<Enchantment,Integer> consumer) {
    consumer.accept(Enchantments.SILK_TOUCH, 1);
  }
}
