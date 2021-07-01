package slimeknights.tconstruct.library.client.renderer.font;

import slimeknights.tconstruct.library.Util;

import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;

import static java.awt.Color.HSBtoRGB;

// TODO: extract?
public class CustomFontColor {

  public static final TextColor MAX = valueToColor(1, 1);
  private static final UnaryOperator<Style> APPLY_MAX = style -> style.withColor(MAX);

  private CustomFontColor() {}

  /**
   * Takes a value between 0.0 and 1.0.
   * Returns a color between red and green, depending on the value. 1.0 is green.
   * If the value goes above 1.0 it continues along the color spectrum.
   */
  public static TextColor valueToColor(float value, float max) {
    // 0.0 -> 0 = red
    // 1.0 -> 1/3 = green
    // 1.5 -> 1/2 = aqua
    float hue = Mth.clamp(((value / max) / 3), 0.01f, 0.5f);
    return TextColor.fromRgb(HSBtoRGB(hue, 0.65f, 0.8f));
  }

  public static Component formatPartialAmount(int value, int max) {
    return new TextComponent(Util.df.format(value))
      .withStyle(style -> style.withColor(CustomFontColor.valueToColor(value, max)))
      .append(new TextComponent(" / ").withStyle(ChatFormatting.GRAY))
      .append(new TextComponent(Util.df.format(max)).withStyle(APPLY_MAX));
  }
}
