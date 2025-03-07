package slimeknights.tconstruct.library.materials;

import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

/**
 * This is just a copy of ResourceLocation for type safety.
 */
public class MaterialId extends ResourceLocation {

  public MaterialId(String resourceName) {
    super(resourceName);
  }

  public MaterialId(String namespaceIn, String pathIn) {
    super(namespaceIn, pathIn);
  }

  public MaterialId(ResourceLocation resourceLocation) {
    super(resourceLocation.getNamespace(), resourceLocation.getPath());
  }

  /**
   * Creates a new material ID from the given string
   * @param string  String
   * @return  Material ID, or null if invalid
   */
  @Nullable
  public static MaterialId tryParse(String string) {
    try {
      return new MaterialId(string);
    } catch (ResourceLocationException resourcelocationexception) {
      return null;
    }
  }
}
