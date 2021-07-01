package slimeknights.tconstruct.common.data.loot;

import net.minecraft.data.loot.EntityLoot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.ConstantIntValue;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraftforge.registries.ForgeRegistries;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.world.TinkerWorld;

import java.util.Objects;
import java.util.stream.Collectors;

public class EntityLootTableProvider extends EntityLoot {

  @Override
  protected Iterable<EntityType<?>> getKnownEntities() {
    return ForgeRegistries.ENTITIES.getValues().stream()
                                   .filter((block) -> TConstruct.modID.equals(Objects.requireNonNull(block.getRegistryName()).getNamespace()))
                                   .collect(Collectors.toList());
  }

  @Override
  protected void addTables() {
    this.add(TinkerWorld.earthSlimeEntity.get(), dropSlimeballs(SlimeType.EARTH));
    this.add(TinkerWorld.skySlimeEntity.get(), dropSlimeballs(SlimeType.SKY));
    this.add(TinkerWorld.enderSlimeEntity.get(), dropSlimeballs(SlimeType.ENDER));
  }

  private static LootTable.Builder dropSlimeballs(SlimeType type) {
    return LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                                         .setRolls(ConstantIntValue.exactly(1))
                                         .add(LootItem.lootTableItem(TinkerCommons.slimeball.get(type))
                                                                .apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
                                                                .apply(LootingEnchantFunction.lootingMultiplier(RandomValueBounds.between(0.0F, 1.0F)))));
  }
}
