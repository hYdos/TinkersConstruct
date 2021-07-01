package slimeknights.tconstruct.library.client.materials;

import lombok.Data;
import net.minecraft.resources.ResourceLocation;

@Data
public class MaterialRenderInfoJson {
  private final ResourceLocation texture;
  private final String[] fallbacks;
  private final String color;
  private final boolean skipUniqueTexture;
}
