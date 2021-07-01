package slimeknights.tconstruct.library.tools.stat;

import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

/**
 * This is just a copy of ResourceLocation for type safety.
 */
public class ToolStatId extends ResourceLocation {

  public ToolStatId(String namespaceIn, String pathIn) {
    super(namespaceIn, pathIn);
  }

  public ToolStatId(ResourceLocation resourceLocation) {
    super(resourceLocation.getNamespace(), resourceLocation.getPath());
  }

  protected ToolStatId(String value) {
    super(value);
  }

  /**
   * Tries to create a tool stat ID from the given string, for NBT parsing
   * @param string  String
   * @return  Tool stat ID, or null of invalid
   */
  @Nullable
  public static ToolStatId tryParse(String string) {
    try {
      return new ToolStatId(string);
    } catch (ResourceLocationException resourcelocationexception) {
      return null;
    }
  }
}
