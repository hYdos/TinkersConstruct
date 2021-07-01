package slimeknights.tconstruct.shared;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import slimeknights.tconstruct.shared.block.SlimeType;

@SuppressWarnings("WeakerAccess")
public final class TinkerFood {
  private TinkerFood() {}
  /* Bacon. What more is there to say? */
  public static final FoodProperties BACON = (new FoodProperties.Builder()).nutrition(4).saturationMod(0.6F).build();

  /* Cake block is set up to take food as a parameter */
  public static final FoodProperties EARTH_CAKE = new FoodProperties.Builder().nutrition(1).saturationMod(0.1f).alwaysEat().effect(() -> new MobEffectInstance(MobEffects.LUCK, 20 * 15, 0), 1.0f).build();
  public static final FoodProperties SKY_CAKE   = new FoodProperties.Builder().nutrition(1).saturationMod(0.1f).alwaysEat().effect(() -> new MobEffectInstance(MobEffects.JUMP, 20 * 20, 1), 1.0f).build();
  public static final FoodProperties ICHOR_CAKE = new FoodProperties.Builder().nutrition(3).saturationMod(0.1f).alwaysEat().effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 20 * 30, 0), 1.0f).build();
  public static final FoodProperties MAGMA_CAKE = new FoodProperties.Builder().nutrition(1).saturationMod(0.2f).alwaysEat().effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 30, 0), 1.0f).build();
  public static final FoodProperties ENDER_CAKE = new FoodProperties.Builder().nutrition(2).saturationMod(0.2f).alwaysEat().effect(() -> new MobEffectInstance(MobEffects.LEVITATION, 20 * 10, 0), 1.0f).build();

  /**
   * Gets the cake for the given slime type
   * @param slime  Slime type
   * @return  Cake food
   */
  public static FoodProperties getCake(SlimeType slime) {
    switch (slime) {
      case EARTH: default: return EARTH_CAKE;
      case SKY: return SKY_CAKE;
      case ICHOR: return ICHOR_CAKE;
      case BLOOD: return MAGMA_CAKE;
      case ENDER: return ENDER_CAKE;
    }
  }
}
