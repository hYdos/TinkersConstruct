package slimeknights.tconstruct.common.recipe;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import lombok.NoArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.shared.TinkerCommons;

@NoArgsConstructor
public class BlockOrEntityCondition implements LootItemCondition {
  public static final ResourceLocation ID = Util.getResource("block_or_entity");
  public static final BlockOrEntityCondition INSTANCE = new BlockOrEntityCondition();
  public static final Serializer SERIALIZER = new Serializer();

  @Override
  public LootItemConditionType getType() {
    return TinkerCommons.lootBlockOrEntity;
  }

  @Override
  public boolean test(LootContext lootContext) {
    return lootContext.hasParam(LootContextParams.THIS_ENTITY) || lootContext.hasParam(LootContextParams.BLOCK_STATE);
  }

  private static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<BlockOrEntityCondition> {
    @Override
    public void serialize(JsonObject json, BlockOrEntityCondition loot, JsonSerializationContext context) { }

    @Override
    public BlockOrEntityCondition deserialize(JsonObject loot, JsonDeserializationContext context) {
      return BlockOrEntityCondition.INSTANCE;
    }
  }
}
