package slimeknights.tconstruct.library.modifiers;

import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

/**
 * This is just a copy of ResourceLocation for type safety.
 */
public class ModifierId extends ResourceLocation {

  public ModifierId(String resourceName) {
    super(resourceName);
  }

  public ModifierId(String namespaceIn, String pathIn) {
    super(namespaceIn, pathIn);
  }

  public ModifierId(ResourceLocation resourceLocation) {
    super(resourceLocation.getNamespace(), resourceLocation.getPath());
  }

  /**
   * Creates a new modifier ID from the given string
   * @param string  String
   * @return  Material ID, or null if invalid
   */
  @Nullable
  public static ModifierId tryParse(String string) {
    try {
      return new ModifierId(string);
    } catch (ResourceLocationException resourcelocationexception) {
      return null;
    }
  }
}
