package slimeknights.tconstruct.library.materials;

import lombok.Getter;
import net.minecraft.Util;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;

@Getter
public class Material implements IMaterial {
  /** Default white color */
  protected static final TextColor WHITE = TextColor.fromRgb(0xFFFFFF);

  /** This resource location uniquely identifies a material. */
  private final MaterialId identifier;
  /** Materials tier, mostly for sorting right now */
  private final int tier;
  /** Materials order within the tier, for sorting */
  private final int sortOrder;

  /** Material can be crafted into parts in the PartBuilder */
  private final boolean craftable;
  /** Key used for localizing the material */
  private final String translationKey;
  /** the text color for this material */
  private final TextColor color;
  /** if true, this material is hidden */
  private final boolean hidden;

  /**
   * Materials should only be created by the MaterialManager, except when used for data gen
   * They're synced over the network and other classes might lead to unexpected behaviour.
   */
  public Material(ResourceLocation identifier, int tier, int order, boolean craftable, TextColor color, boolean hidden) {
    this.identifier = new MaterialId(identifier);
    this.tier = tier;
    this.sortOrder = order;
    this.craftable = craftable;
    this.translationKey = Util.makeDescriptionId("material", identifier);
    this.color = color;
    this.hidden = hidden;
  }

  protected Material(ResourceLocation identifier, boolean craftable, boolean hidden) {
    this(identifier, 0, -1, craftable, WHITE, hidden);
  }

  @Override
  public String toString() {
    return "Material{" + identifier + '}';
  }
}
