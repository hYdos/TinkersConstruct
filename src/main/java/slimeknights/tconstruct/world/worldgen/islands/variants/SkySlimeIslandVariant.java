package slimeknights.tconstruct.world.worldgen.islands.variants;

import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.world.TinkerStructures;
import slimeknights.tconstruct.world.TinkerWorld;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import java.util.Objects;
import java.util.Random;

/**
 * Island variant for sky slime islands
 */
public class SkySlimeIslandVariant extends AbstractSlimeIslandVariant {
  public SkySlimeIslandVariant(int index, SlimeType dirtType) {
    super(index, dirtType, SlimeType.SKY);
  }

  @Override
  public ResourceLocation getStructureName(String variantName) {
    return Util.getResource("slime_islands/sky/" + dirtType.getSerializedName() + "_" + variantName);
  }

  @Override
  protected SlimeType getCongealedSlimeType(Random random) {
    return random.nextBoolean() ? SlimeType.SKY : SlimeType.EARTH;
  }

  @Override
  public BlockState getLakeFluid() {
    return Objects.requireNonNull(TinkerFluids.skySlime.getBlock()).defaultBlockState();
  }

  @Nullable
  @Override
  public BlockState getVines() {
    return TinkerWorld.skySlimeVine.get().defaultBlockState();
  }

  @Nullable
  @Override
  public ConfiguredFeature<?,?> getTreeFeature(Random random) {
    return TinkerStructures.SKY_SLIME_ISLAND_TREE;
  }
}
