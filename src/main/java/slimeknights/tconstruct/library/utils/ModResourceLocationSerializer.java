package slimeknights.tconstruct.library.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

/**
 * Extension to Resource Location serializer to change the default mod ID
 */
public class ModResourceLocationSerializer extends ResourceLocation.Serializer {

  private final String modId;
  public ModResourceLocationSerializer(String modId) {
    this.modId = modId;
  }

  @Override
  public ResourceLocation deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
    String loc = GsonHelper.convertToString(element, "location");
    if (!loc.contains(":")) {
      loc = modId + ":" + loc;
    }
    return new ResourceLocation(loc);
  }
}
