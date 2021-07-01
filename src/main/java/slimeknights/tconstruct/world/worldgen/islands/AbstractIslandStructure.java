package slimeknights.tconstruct.world.worldgen.islands;

import slimeknights.tconstruct.world.worldgen.islands.variants.IIslandVariant;

import java.util.Objects;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public abstract class AbstractIslandStructure extends StructureFeature<NoneFeatureConfiguration> {
  protected static final String[] SIZES = new String[] { "0x1x0", "2x2x4", "4x1x6", "8x1x11", "11x1x11" };
  public AbstractIslandStructure() {
    super(NoneFeatureConfiguration.CODEC);
  }

  @Override
  public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
    return DefaultStart::new;
  }

  @Override
  public String getFeatureName() {
    return Objects.requireNonNull(getRegistryName()).toString();
  }

  @Override
  public GenerationStep.Decoration step() {
    return GenerationStep.Decoration.SURFACE_STRUCTURES;
  }

  /** Gets the variant for this island */
  protected abstract IIslandVariant getVariant(Random random);

  /** Gets the height to generate this island */
  protected int getHeight(ChunkGenerator generator, Rotation rotation, int x, int z, Random random) {
    int xOffset;
    int yOffset;
    switch (rotation) {
      case CLOCKWISE_90:
        xOffset = -5;
        yOffset = 5;
        break;
      case CLOCKWISE_180:
        xOffset = -5;
        yOffset = -5;
        break;
      case COUNTERCLOCKWISE_90:
        xOffset = 5;
        yOffset = -5;
        break;
      default:
        xOffset = 5;
        yOffset = 5;
        break;
    }

    // determine height
    int minXMinZ = generator.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG);
    int minXMaxZ = generator.getFirstOccupiedHeight(x, z + yOffset, Heightmap.Types.WORLD_SURFACE_WG);
    int maxXMinZ = generator.getFirstOccupiedHeight(x + xOffset, z, Heightmap.Types.WORLD_SURFACE_WG);
    int maxXMaxZ = generator.getFirstOccupiedHeight(x + xOffset, z + yOffset, Heightmap.Types.WORLD_SURFACE_WG);
    return Math.min(Math.min(minXMinZ, minXMaxZ), Math.min(maxXMinZ, maxXMaxZ)) + 50 + random.nextInt(50) + 11;
  }

  public class DefaultStart extends StructureStart<NoneFeatureConfiguration> {
    public DefaultStart(StructureFeature<NoneFeatureConfiguration> structureIn, int chunkPosX, int chunkPosZ, BoundingBox bounds, int references, long seed) {
      super(structureIn, chunkPosX, chunkPosZ, bounds, references, seed);
    }

    @Override
    public void func_230364_a_(RegistryAccess registries, ChunkGenerator generator, StructureManager templateManagerIn, int chunkX, int chunkZ, Biome biomeIn, NoneFeatureConfiguration config) {
      // determine orientation
      Rotation rotation = Rotation.values()[this.random.nextInt(Rotation.values().length)];
      // determine coords
      int x = chunkX * 16 + 4 + this.random.nextInt(8);
      int z = chunkZ * 16 + 4 + this.random.nextInt(8);
      int y = getHeight(generator, rotation, x, z, this.random);

      IIslandVariant variant = getVariant(random);
      // fetch the tree now so its consistent on the whole island
      SlimeIslandPiece slimeIslandPiece = new SlimeIslandPiece(templateManagerIn, variant, SIZES[this.random.nextInt(SIZES.length)], new BlockPos(x, y, z), variant.getTreeFeature(random), rotation);
      this.pieces.add(slimeIslandPiece);
      this.calculateBoundingBox();
    }
  }
}
