package slimeknights.tconstruct.library.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.client.renderer.font.CustomFontColor;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.IToolStat;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnusedReturnValue")
@RequiredArgsConstructor
public class TooltipBuilder {
  /** Key for free modifiers localization */
  private final static String KEY_FREE_UPGRADES = Util.makeTranslationKey("tooltip", "tool.upgrades");
  private final static String KEY_FREE_ABILITIES = Util.makeTranslationKey("tooltip", "tool.abilities");

  private final static TextColor UPGRADE_COLOR = TextColor.fromRgb(0xFFCCBA47);
  private final static TextColor ABILITY_COLOR = TextColor.fromRgb(0xFFB8A0FF);
  /** Formatted broken string */
  private static final Component TOOLTIP_BROKEN = Util.makeTranslation("tooltip", "tool.broken").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED);
  /** Prefixed broken string */
  private static final Component TOOLTIP_BROKEN_PREFIXED = ToolStats.DURABILITY.getPrefix().append(TOOLTIP_BROKEN);

  private final ToolStack tool;
  @Getter
  private final List<Component> tooltips;

  public TooltipBuilder(ToolStack tool) {
    this.tool = tool;
    this.tooltips = new ArrayList<>();
  }

  /**
   * Adds the given text to the tooltip
   *
   * @param textComponent the text component to add
   * @return the tooltip builder
   */
  public TooltipBuilder add(Component textComponent) {
    this.tooltips.add(textComponent);

    return this;
  }

  /**
   * Adds the given stat to the tooltip
   *
   * @param stat  Stat to add
   * @return the tooltip builder
   */
  public TooltipBuilder add(IToolStat<?> stat) {
    this.tooltips.add(stat.formatValue(tool.getStats().getFloat(stat)));
    return this;
  }

  /** Applies formatting for durability with a reference durability */
  public static Component formatDurability(int durability, int ref, boolean textIfBroken) {
    if (textIfBroken && durability == 0) {
      return TOOLTIP_BROKEN_PREFIXED;
    }
    return ToolStats.DURABILITY.getPrefix().append(CustomFontColor.formatPartialAmount(durability, ref));
  }

  /**
   * Adds the durability to the tooltip
   *
   * @return the tooltip builder
   */
  public TooltipBuilder addDurability() {
    // never show broken text in this context
    this.tooltips.add(formatDurability(tool.getCurrentDurability(), tool.getStats().getInt(ToolStats.DURABILITY), false));
    return this;
  }

  /**
   * Adds the given stat to the tooltip, summing in the attribute value
   *
   * @return the tooltip builder
   */
  public TooltipBuilder addWithAttribute(IToolStat<?> stat, Attribute attribute) {
    float damage = (float) attribute.getDefaultValue();
    Player player = Minecraft.getInstance().player;
    if (player != null) {
      AttributeInstance instance = player.getAttribute(attribute);
      if (instance != null) {
        damage = (float) instance.getBaseValue();
      }
    }
    this.tooltips.add(ToolStats.ATTACK_DAMAGE.formatValue(damage + tool.getStats().getFloat(stat)));
    return this;
  }

  /**
   * Adds the current free modifiers to the tooltip
   *
   * @return the tooltip builder
   */
  public TooltipBuilder addFreeUpgrades() {
    int modifiers = tool.getFreeUpgrades();
    if (modifiers > 0) {
      this.tooltips.add(IToolStat.formatNumber(KEY_FREE_UPGRADES, UPGRADE_COLOR, modifiers));
    }

    return this;
  }

  /**
   * Adds the current free modifiers to the tooltip
   *
   * @return the tooltip builder
   */
  public TooltipBuilder addFreeAbilities() {
    int abilities = tool.getFreeAbilities();
    if (abilities > 0) {
      this.tooltips.add(IToolStat.formatNumber(KEY_FREE_ABILITIES, ABILITY_COLOR, abilities));
    }

    return this;
  }

  /**
   * Adds the modifier information to the tooltip
   *
   * @return the tooltip builder
   */
  public TooltipBuilder addModifierInfo(boolean advanced) {
    for (ModifierEntry entry : tool.getModifierList()) {
      if (entry.getModifier().shouldDisplay(advanced)) {
        this.tooltips.add(entry.getModifier().getDisplayName(tool, entry.getLevel()));
      }
    }
    return this;
  }
}
