package slimeknights.tconstruct.library.modifiers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.logging.log4j.LogManager;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.recipe.tinkerstation.ValidatedResult;
import slimeknights.tconstruct.library.tools.ToolDefinition;
import slimeknights.tconstruct.library.tools.helper.ToolAttackContext;
import slimeknights.tconstruct.library.tools.helper.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.nbt.IModDataReadOnly;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * Interface representing both modifiers and traits.
 * Any behavior special to either one is handled elsewhere.
 */
@RequiredArgsConstructor
public class Modifier implements IForgeRegistryEntry<Modifier> {

  /** Modifier random instance, use for chance based effects */
  protected static Random RANDOM = new Random();

  protected static final String KEY_LEVEL = "enchantment.level.";
  public static final int DEFAULT_PRIORITY = 100;

  /** Display color for all text for this modifier */
  @Getter
  private final int color;

  /** Registry name of this modifier, null before fully registered */
  @Getter @Nullable
  private ModifierId registryName;

  /** Cached key used for translations */
  @Nullable
  private String translationKey;
  /** Cached text component for display names */
  @Nullable
  private Component displayName;
  /** Cached text component for description */
  @Nullable
  private List<Component> descriptionList;
  /** Cached text component for description */
  @Nullable
  private Component description;

  /**
   * Override this method to make your modifier run earlier or later.
   * Higher numbers run earlier, 100 is default
   * @return Priority
   */
  public int getPriority() {
    return DEFAULT_PRIORITY;
  }


  /* Registry methods */

  @Override
  public final Modifier setRegistryName(ResourceLocation name) {
    if (registryName != null) {
      throw new IllegalStateException("Attempted to set registry name with existing registry name! New: " + name + " Old: " + registryName);
    }
    // check mod container, should be the active mod
    // don't want mods registering stuff in Tinkers namespace, or Minecraft
    String activeMod = ModLoadingContext.get().getActiveNamespace();
    if (!name.getNamespace().equals(activeMod)) {
      LogManager.getLogger().info("Potentially Dangerous alternative prefix for name `{}`, expected `{}`. This could be a intended override, but in most cases indicates a broken mod.", name, activeMod);
    }
    this.registryName = new ModifierId(name);
    return this;
  }

  /**
   * Gets the modifier ID. Unlike {@link #getRegistryName()}, this method must be nonnull
   * @return  Modifier ID
   */
  public ModifierId getId() {
    return Objects.requireNonNull(registryName, "Modifier has null registry name");
  }

  @Override
  public Class<Modifier> getRegistryType() {
    return Modifier.class;
  }


  /* Tooltips */

  /**
   * Overridable method to create a translation key. Will be called once and the result cached
   * @return  Translation key
   */
  protected String makeTranslationKey() {
    return Util.makeDescriptionId("modifier", registryName);
  }

  /**
   * Gets the translation key for this modifier
   * @return  Translation key
   */
  public final String getTranslationKey() {
    if (translationKey == null) {
      translationKey = makeTranslationKey();
    }
    return translationKey;
  }

  /**
   * Overridable method to create the display name for this modifier, ideal to modify colors
   * @return  Display name
   */
  protected Component makeDisplayName() {
    return new TranslatableComponent(getTranslationKey());
  }

  /**
   * Applies relevant text styles (typically color) to the modifier text
   * @param component  Component to modifiy
   * @return  Resulting component
   */
  public MutableComponent applyStyle(MutableComponent component) {
      return component.withStyle(style -> style.withColor(TextColor.fromRgb(color)));
  }

  /**
   * Gets the display name for this modifier
   * @return  Display name for this modifier
   */
  public final Component getDisplayName() {
    if (displayName == null) {
      displayName = new TranslatableComponent(getTranslationKey()).withStyle(style -> style.withColor(TextColor.fromRgb(getColor())));
    }
    return displayName;
  }

  /**
   * Gets the display name for the given level of this modifier
   * @param level  Modifier level
   * @return  Display name
   */
  public Component getDisplayName(int level) {
    return applyStyle(new TranslatableComponent(getTranslationKey())
                        .append(" ")
                        .append(new TranslatableComponent(KEY_LEVEL + level)));
  }

  /**
   * Stack sensitive version of {@link #getDisplayName(int)}. Useful for displaying persistent data such as overslime or redstone amount
   * @param tool   Tool instance
   * @param level  Tool level
   * @return  Stack sensitive display name
   */
  public Component getDisplayName(IModifierToolStack tool, int level) {
    return getDisplayName(level);
  }

  /**
   * Adds additional information from the modifier to the tooltip. Shown when holding shift on a tool, or in the stats area of the tinker station
   * @param tool      Tool instance
   * @param level     Tool level
   * @param tooltip   Tooltip
   * @param isAdvanced  Tooltip flag type
   * @param detailed  If true, showing detailed view, such as in the tinker station
   */
  public void addInformation(IModifierToolStack tool, int level, List<Component> tooltip, boolean isAdvanced, boolean detailed) {}

  /**
   * Gets the description for this modifier
   * @return  Description for this modifier
   */
  public final List<Component> getDescriptionList() {
    if (descriptionList == null) {
      descriptionList = Arrays.asList(
        new TranslatableComponent(getTranslationKey() + ".flavor").withStyle(ChatFormatting.ITALIC),
        new TranslatableComponent(getTranslationKey() + ".description"));
    }
    return descriptionList;
  }

  /**
   * Gets the description for this modifier
   * @return  Description for this modifier
   */
  public final Component getDescription() {
    if (description == null) {
      description = getDescriptionList().stream()
                                        .reduce((c1, c2) -> new TextComponent("").append(c1).append("\n").append(c2))
                                        .orElse(TextComponent.EMPTY);
    }
    return description;
  }


  /* Tool building hooks */

  /**
   * Adds any relevant volatile data to the tool data. This data is rebuilt every time modifiers rebuild.
   * <br>
   * Alternatives:
   * <ul>
   *   <li>Persistent mod data (accessed via {@link IModifierToolStack}): Can be written to freely, but will not automatically remove if the modifier is removed.</li>
   * </ul>
   * @param toolDefinition  Tool definition, will be empty for non-multitools
   * @param baseStats       Base material stats. Does not take tool definition or other modifiers into account. Not stored, so if you want any data store it in volatile data
   * @param persistentData  Extra modifier NBT. Note that if you rely on a value in persistent data, it is up to you to ensure tool stats refresh if it changes
   * @param level           Modifier level
   * @param volatileData    Mutable mod NBT data, result of this method
   */
  public void addVolatileData(ToolDefinition toolDefinition, StatsNBT baseStats, IModDataReadOnly persistentData, int level, ModDataNBT volatileData) {}

  /**
   * Adds raw stats to the tool. Called whenever tool stats are rebuilt.
   * <br>
   * Alternatives:
   * <ul>
   *   <li>{@link #addAttributes(IModifierToolStack, int, EquipmentSlot, BiConsumer)}: Allows dynamic stats based on any tool stat, but does not support mining speed, mining level, or durability.</li>
   *   <li>{@link #onBreakSpeed(IModifierToolStack, int, BreakSpeed, Direction, boolean, float)}: Allows dynamic mining speed based on the block mined and the entity mining. Will not show in tooltips.</li>
   * </ul>
   * @param toolDefinition  Tool definition, will be empty for non-multitools
   * @param baseStats       Base material stats. Does not take tool definition or other modifiers into account
   * @param persistentData  Extra modifier NBT. Note that if you rely on a value in persistent data, it is up to you to ensure tool stats refresh if it changes
   * @param volatileData    Modifier NBT calculated from modifiers in {@link #addVolatileData(ToolDefinition, StatsNBT, IModDataReadOnly, int, ModDataNBT)}
   * @param level           Modifier level
   * @param builder         Tool stat builder
   */
  public void addToolStats(ToolDefinition toolDefinition, StatsNBT baseStats, IModDataReadOnly persistentData, IModDataReadOnly volatileData, int level, ModifierStatsBuilder builder) {}

  /**
   * Adds attributes from this modifier's effect. Called whenever the item stack refreshes capabilities.
   * <br>
   * Alternatives:
   * <ul>
   *   <li>{@link #addToolStats(ToolDefinition, StatsNBT, IModDataReadOnly, IModDataReadOnly, int, ModifierStatsBuilder)}: Limited context, but can affect durability, mining level, and mining speed.</li>
   * </ul>
   * @param tool      Current tool instance
   * @param level     Modifier level
   * @param slot      Slot for the attributes
   * @param consumer  Attribute consumer
   */
  public void addAttributes(IModifierToolStack tool, int level, EquipmentSlot slot, BiConsumer<Attribute,AttributeModifier> consumer) {}

  /**
   * Called when modifiers or tool materials change to validate the tool. You are free to modify persistent data in this hook if needed.
   * Do not validate max level here, simply ignore levels over max if needed.
   * @param tool   Current tool instance
   * @param level  Modifier level, may be 0 if the modifier is removed.
   * @return  PASS result if success, failure if there was an error.
   */
  public ValidatedResult validate(IModifierToolStack tool, int level) {
    return ValidatedResult.PASS;
  }

  /* Hooks */

  /**
   * Called when the tool is damaged. Can be used to cancel, decrease, or increase the damage.
   * @param toolStack  Tool stack
   * @param level      Tool level
   * @param amount     Amount of damage to deal
   * @return  Replacement damage. Returning 0 cancels the damage and stops other modifiers from processing.
   */
  public int onDamageTool(IModifierToolStack toolStack, int level, int amount) {
    return amount;
  }

  /**
   * Called when the tool is repair. Can be used to decrease, increase, or cancel the repair.
   * @param toolStack  Tool stack
   * @param level      Tool level
   * @param factor     Original factor
   * @return  Replacement factor. Returning 0 prevents repair
   */
  public float getRepairFactor(IModifierToolStack toolStack, int level, float factor) {
    return factor;
  }

  /**
   * Called when the stack updates in the player inventory
   * @param tool           Current tool instance
   * @param level          Modifier level
   * @param world          World containing tool
   * @param holder         Entity holding tool
   * @param itemSlot       Slot containing this tool
   * @param isSelected     If true, this item is currently in the player's main hand
   * @param isCorrectSlot  If true, this item is in the proper slot. For tools, that is main hand or off hand. For armor, this means its in the correct armor slot
   * @param stack          Item stack instance to check other slots for the tool. Do not modify
   */
  public void onInventoryTick(IModifierToolStack tool, int level, Level world, LivingEntity holder, int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {}

  /**
   * Called on entity or block loot to allow modifying loot
   * @param tool           Current tool instance
   * @param level          Modifier level
   * @param generatedLoot  Current loot list before this modifier
   * @param context        Full loot context
   * @return  Loot replacement
   */
  public List<ItemStack> processLoot(IModifierToolStack tool, int level, List<ItemStack> generatedLoot, LootContext context) {
    return generatedLoot;
  }


  /* Interaction hooks */

  /**
   * Called when this item is used when targeting a block, <i>before</i> the block is activated.
   * In general it is better to use {@link #afterBlockUse(IModifierToolStack, int, UseOnContext)} for consistency with vanilla items.
   * <br>
   * Alternatives:
   * <ul>
   *   <li>{@link #onEntityUse(IModifierToolStack, int, Player, LivingEntity, InteractionHand)}: Processes use actions on entities.</li>
   *   <li>{@link #afterBlockUse(IModifierToolStack, int, UseOnContext)}: Runs after the block is activated, preferred hook. </li>
   *   <li>{@link #onToolUse(IModifierToolStack, int, Level, Player, InteractionHand)}: Processes any use actions, but runs later than onBlockUse or onEntityUse.</li>
   * </ul>
   * @param tool           Current tool instance
   * @param level          Modifier level
   * @param context        Full item use context
   * @return  Return PASS or FAIL to allow vanilla handling, any other to stop later modifiers from running.
   */
  public InteractionResult beforeBlockUse(IModifierToolStack tool, int level, UseOnContext context) {
    return InteractionResult.PASS;
  }


  /**
   * Called when this item is used when targeting a block, <i>after</i> the block is activated. This is the perferred hook for block based tool interactions
   * <br>
   * Alternatives:
   * <ul>
   *   <li>{@link #onEntityUse(IModifierToolStack, int, Player, LivingEntity, InteractionHand)}: Processes use actions on entities.</li>
   *   <li>{@link #beforeBlockUse(IModifierToolStack, int, UseOnContext)}: Runs before the block is activated, can be used to prevent block interaction entirely but less consistent with vanilla </li>
   *   <li>{@link #onToolUse(IModifierToolStack, int, Level, Player, InteractionHand)}: Processes any use actions, but runs later than onBlockUse or onEntityUse.</li>
   * </ul>
   * @param tool           Current tool instance
   * @param level          Modifier level
   * @param context        Full item use context
   * @return  Return PASS or FAIL to allow vanilla handling, any other to stop later modifiers from running.
   */
  public InteractionResult afterBlockUse(IModifierToolStack tool, int level, UseOnContext context) {
    return InteractionResult.PASS;
  }

  /**
    * Called when this item is used when targeting an entity. Runs before the native entity interaction hooks and on all entities instead of just living
   * <br>
   * Alternatives:
   * <ul>
   *   <li>{@link #onEntityUse(IModifierToolStack, int, Player, LivingEntity, InteractionHand)}: Standard interaction hook, generally preferred over this one</li>
   *   <li>{@link #afterBlockUse(IModifierToolStack, int, UseOnContext)}: Processes use actions on blocks.</li>
   *   <li>{@link #onToolUse(IModifierToolStack, int, Level, Player, InteractionHand)}: Processes any use actions, but runs later than onBlockUse or onEntityUse.</li>
   * </ul>
   * @param tool           Current tool instance
   * @param level          Modifier level
   * @param player         Player holding tool
   * @param target         Target
   * @param hand           Current hand
   * @return  Return PASS or FAIL to allow vanilla handling, any other to stop later modifiers from running.
   */
  public InteractionResult onEntityUseFirst(IModifierToolStack tool, int level, Player player, Entity target, InteractionHand hand) {
    return InteractionResult.PASS;
  }

  /**
   * Called when this item is used when targeting an entity.
   * <br>
   * Alternatives:
   * <ul>
   *   <li>{@link #onEntityUseFirst(IModifierToolStack, int, Player, Entity, InteractionHand)}: Runs on all entities instead of just living, and runs before normal entity interaction</li>
   *   <li>{@link #afterBlockUse(IModifierToolStack, int, UseOnContext)}: Processes use actions on blocks.</li>
   *   <li>{@link #onToolUse(IModifierToolStack, int, Level, Player, InteractionHand)}: Processes any use actions, but runs later than onBlockUse or onEntityUse.</li>
   * </ul>
   * @param tool           Current tool instance
   * @param level          Modifier level
   * @param player         Player holding tool
   * @param target         Target
   * @param hand           Current hand
   * @return  Return PASS or FAIL to allow vanilla handling, any other to stop later modifiers from running.
   */
  public InteractionResult onEntityUse(IModifierToolStack tool, int level, Player player, LivingEntity target, InteractionHand hand) {
    return InteractionResult.PASS;
  }

  /**
    * Called when this item is used, after all other hooks PASS.
   * <br>
   * Alternatives:
   * <ul>
   *   <li>{@link #afterBlockUse(IModifierToolStack, int, UseOnContext)}: Processes use actions on blocks.</li>
   *   <li>{@link #onEntityUse(IModifierToolStack, int, Player, LivingEntity, InteractionHand)}: Processes use actions on entities.</li>
   * </ul>
   * @param tool           Current tool instance
   * @param level          Modifier level
   * @param world          World containing tool
   * @param player         Player holding tool
   * @param hand           Current hand
   * @return  Return PASS or FAIL to allow vanilla handling, any other to stop later modifiers from running.
   */
  public InteractionResult onToolUse(IModifierToolStack tool, int level, Level world, Player player, InteractionHand hand) {
    return InteractionResult.PASS;
  }

  /**
   * Called when the player stops using the tool.
   * To setup, use {@link LivingEntity#startUsingItem(InteractionHand)} in {@link #onToolUse(IModifierToolStack, int, Level, Player, InteractionHand)}.
   * <br>
   * Alternatives:
   * <ul>
   *   <li>{@link #onFinishUsing(IModifierToolStack, int, Level, LivingEntity)}: Called when the duration timer reaches the end, even if still held
   *  </ul>
   * @param tool           Current tool instance
   * @param level          Modifier level
   * @param world          World containing tool
   * @param entity         Entity holding tool
   * @param timeLeft       How many ticks of use duration was left
  * @return  Whether the modifier should block any incoming ones from firing
  */
  public boolean onStoppedUsing(IModifierToolStack tool, int level, Level world, LivingEntity entity, int timeLeft) {
    return false;
  }

  /**
   * Called when the use duration on this tool reaches the end.
   * To setup, use {@link LivingEntity#startUsingItem(InteractionHand)} in {@link #onToolUse(IModifierToolStack, int, Level, Player, InteractionHand)} and set the duration in {@link #getUseDuration(IModifierToolStack, int)}
   * <br>
   * Alternatives:
   * <ul>
   *   <li>{@link #onStoppedUsing(IModifierToolStack, int, Level, LivingEntity, int)}: Called when the player lets go before the duration reaches the end
   * </ul>
   * @param tool           Current tool instance
   * @param level          Modifier level
   * @param world          World containing tool
   * @param entity         Entity holding tool
   * @return  Whether the modifier should block any incoming ones from firing
   */
  public boolean onFinishUsing(IModifierToolStack tool, int level, Level world, LivingEntity entity) {
    return false;
  }

  /**
   * @param tool           Current tool instance
   * @param level          Modifier level
  * @return  For how many ticks the modifier should run its use action
  */
  public int getUseDuration(IModifierToolStack tool, int level) {
     return 0;
  }

  /**
   * @param tool           Current tool instance
   * @param level          Modifier level
  * @return  Use action to be performed
  */
  public UseAnim getUseAction(IModifierToolStack tool, int level) {
     return UseAnim.NONE;
  }


  /* Harvest hooks */

  /**
   * Called when break speed is being calculated to affect mining speed conditionally.
   * <br>
   * Alternatives:
   * <ul>
   *   <li>{@link #addToolStats(ToolDefinition, StatsNBT, IModDataReadOnly, IModDataReadOnly, int, ModifierStatsBuilder)}: Limited context, but effect shows in the tooltip.</li>
   * </ul>
   * @param tool                 Current tool instance
   * @param level                Modifier level
   * @param event                Event instance
   * @param sideHit              Side of the block that was hit
   * @param isEffective          If true, the tool is effective against this block type
   * @param miningSpeedModifier  Calculated modifier from potion effects such as haste and environment such as water, use for additive bonuses to ensure consistency with the mining speed stat
   */
  public void onBreakSpeed(IModifierToolStack tool, int level, BreakSpeed event, Direction sideHit, boolean isEffective, float miningSpeedModifier) {}

  /**
   * Adds harvest loot table related enchantments from this modifier's effect, called before breaking a block.
   * Needed to add enchantments for silk touch and fortune. Can add conditionally if needed.
   * For looting, see {@link #getLootingValue(IModifierToolStack, int, LivingEntity, LivingEntity, DamageSource, int)}
   * @param tool      Tool used
   * @param level     Modifier level
   * @param context   Harvest context
   * @param consumer  Consumer accepting any enchantments
   */
  public void applyHarvestEnchantments(IModifierToolStack tool, int level, ToolHarvestContext context, BiConsumer<Enchantment,Integer> consumer) {}

  /**
   * Gets the amount of luck contained in this tool
   * @param tool          Tool instance
   * @param level         Modifier level
   * @param holder        Entity holding the tool
   * @param target        Entity being looted
   * @param damageSource  Damage source that killed the entity. May be null if this hook is called without attacking anything (e.g. shearing)
   * @param looting          Luck value set from previous modifiers
   * @return New luck value
   */
  public int getLootingValue(IModifierToolStack tool, int level, LivingEntity holder, Entity target, @Nullable DamageSource damageSource, int looting) {
    return looting;
  }

  /**
   * Removes the block from the world
   * <br>
   * Alternatives:
   * <ul>
   *   <li>{@link #afterBlockBreak(IModifierToolStack, int, ToolHarvestContext)}: Called after the block is successfully removed.</li>
   * </ul>
   * @param tool      Tool used
   * @param level     Modifier level
   * @param context   Harvest context
   * @return  True to override the default block removing logic and stop all later modifiers from running. False to override default without breaking the block. Null to let default logic run
   */
  @Nullable
  public Boolean removeBlock(IModifierToolStack tool, int level, ToolHarvestContext context) {
    return null;
  }

  /**
   * Called after a block is broken to apply special effects
   * <br>
   * Alternatives:
   * <ul>
   *   <li>{@link #removeBlock(IModifierToolStack, int, ToolHarvestContext)}: Called before the block is set to air.</li>
   * </ul>
   * @param tool      Tool used
   * @param level     Modifier level
   * @param context   Harvest context
   */
  public void afterBlockBreak(IModifierToolStack tool, int level, ToolHarvestContext context) {}


  /* Attack hooks */

  /**
   * Called when an entity is attacked, before critical hit damage is calculated. Allows modifying the damage dealt.
   * Do not modify the entity here, its possible the attack will still be canceled without calling further hooks due to 0 damage being dealt.
   * <br>
   * Alternatives:
   * <ul>
   *   <li>{@link #addToolStats(ToolDefinition, StatsNBT, IModDataReadOnly, IModDataReadOnly, int, ModifierStatsBuilder)}: Adjusts the base tool stats that show in the tooltip, but has less context for modification</li>
   *   <li>{@link #beforeEntityHit(IModifierToolStack, int, ToolAttackContext, float, float, float)}: If you need to modify the entity before attacking, use this hook</li>
   *   <li>{@link #afterEntityHit(IModifierToolStack, int, ToolAttackContext, float)}: Perform special attacks on entity hit beyond damage boosts</li>
   * </ul>
   * @param tool          Tool used to attack
   * @param level         Modifier level
   * @param context       Attack context
   * @param baseDamage    Base damage dealt before modifiers
   * @param damage        Computed damage from all prior modifiers
   * @return  New damage to deal
   */
  public float getEntityDamage(IModifierToolStack tool, int level, ToolAttackContext context, float baseDamage, float damage) {
    return damage;
  }

  /**
   * Called right before an entity is hit, used to modify knockback applied or to apply special effects that need to run before damage. Damage is final damage including critical damage.
   * Note there is still a chance this attack won't deal damage, if that happens {@link #failedEntityHit(IModifierToolStack, int, ToolAttackContext)} will run.
   * <br>
   * Alternatives:
   * <ul>
   *   <li>{@link #afterEntityHit(IModifierToolStack, int, ToolAttackContext, float)}: Perform special attacks on entity hit beyond knockback boosts</li>
   * </ul>
   * @param tool           Tool used to attack
   * @param level          Modifier level
   * @param context        Attack context
   * @param damage         Damage to deal to the attacker
   * @param baseKnockback  Base knockback before modifiers
   * @param knockback      Computed knockback from all prior modifiers
   * @return  New knockback to apply. 0.5 is equivelent to 1 level of the vanilla enchant
   */
  public float beforeEntityHit(IModifierToolStack tool, int level, ToolAttackContext context, float damage, float baseKnockback, float knockback) {
    return knockback;
  }

  /**
   * Called after a living entity is successfully attacked. Used to apply special effects on hit.
   * <br>
   * Alternatives:
   * <ul>
   *   <li>{@link #addToolStats(ToolDefinition, StatsNBT, IModDataReadOnly, IModDataReadOnly, int, ModifierStatsBuilder)}: Adjusts the base tool stats that affect damage</li>
   *   <li>{@link #getEntityDamage(IModifierToolStack, int, ToolAttackContext, float, float)}: Change the amount of damage dealt with attacker context</li>
   *   <li>{@link #beforeEntityHit(IModifierToolStack, int, ToolAttackContext, float, float, float)}: Change the amount of knockback dealt</li>
   *   <li>{@link #failedEntityHit(IModifierToolStack, int, ToolAttackContext)}: Called after living hit when damage was not dealt</li>
   * </ul>
   * @param tool          Tool used to attack
   * @param level         Modifier level
   * @param context       Attack context
   * @param damageDealt   Amount of damage successfully dealt
   * @return  Extra damage to deal to the tool
   */
  public int afterEntityHit(IModifierToolStack tool, int level, ToolAttackContext context, float damageDealt) {
    return 0;
  }

  /**
   * Called after attacking an entity when no damage was dealt
   * @param tool          Tool used to attack
   * @param level         Modifier level
   * @param context       Attack context
   */
  public void failedEntityHit(IModifierToolStack tool, int level, ToolAttackContext context) {}


  /* Display */

  /**
   * Determines if the modifier should display
   * @param advanced  If true, in an advanced view such as the tinker station. False for tooltips
   * @return  True if the modifier should show
   */
  public boolean shouldDisplay(boolean advanced) {
    return true;
  }

  /**
   * Gets the damage percentage for display.  First tool returning something other than NaN will determine display durability
   * @param tool   Tool instance
   * @param level  Modifier level
   * @return  Damage percentage. 0 is undamaged, 1 is fully damaged.
   */
  public double getDamagePercentage(IModifierToolStack tool, int level) {
    return Double.NaN;
  }

  /**
   * Override the default tool logic for showing the durability bar
   * @param tool   Tool instance
   * @param level  Modifier level
   * @return  True forces the bar to show, false forces it to hide. Return null to allow default behavior
   */
  @Nullable
  public Boolean showDurabilityBar(IModifierToolStack tool, int level) {
    return null;
  }

  /**
   * Gets the RGB for the durability bar
   * @param tool   Tool instance
   * @param level  Modifier level
   * @return  RGB, or -1 to not handle it
   */
  public int getDurabilityRGB(IModifierToolStack tool, int level) {
    return -1;
  }


  /* Modules */

  /**
   * Gets a submodule of this modifier.
   *
   * Submodules will contain tool stack sensitive hooks, and do not contain storage. Generally returning the same instance each time is preferred.
   * @param type  Module type to fetch
   * @param <T>   Module return type
   * @return  Module, or null if the module is not contained
   */
  @Nullable
  public <T> T getModule(Class<T> type) {
    return null;
  }


  /* Utils */

  /**
   * Gets the tool stack from the given entities mainhand. Useful for specialized event handling in modifiers
   * @param living  Entity instance
   * @return  Tool stack
   */
  @Nullable
  public static ToolStack getHeldTool(@Nullable LivingEntity living, InteractionHand hand) {
    if (living == null) {
      return null;
    }
    ItemStack stack = living.getItemInHand(hand);
    if (stack.isEmpty() || !stack.getItem().is(TinkerTags.Items.MODIFIABLE)) {
      return null;
    }
    ToolStack tool = ToolStack.from(stack);
    return tool.isBroken() ? null : tool;
  }

  /**
   * Gets the mining speed modifier for the current conditions, notably potions and armor enchants
   * @param entity  Entity to check
   * @return  Mining speed modifier
   */
  public static float getMiningModifier(LivingEntity entity) {
    float modifier = 1.0f;
    // haste effect
    if (MobEffectUtil.hasDigSpeed(entity)) {
      modifier *= 1.0F + (MobEffectUtil.getDigSpeedAmplification(entity) + 1) * 0.2f;
    }
    // mining fatigue
    MobEffectInstance miningFatigue = entity.getEffect(MobEffects.DIG_SLOWDOWN);
    if (miningFatigue != null) {
      switch(miningFatigue.getAmplifier()) {
        case 0:
          modifier *= 0.3F;
          break;
        case 1:
          modifier *= 0.09F;
          break;
        case 2:
          modifier *= 0.0027F;
          break;
        case 3:
        default:
          modifier *= 8.1E-4F;
      }
    }
    // water
    if (entity.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(entity)) {
      modifier /= 5.0F;
    }
    if (!entity.isOnGround()) {
      modifier /= 5.0F;
    }
    return modifier;
  }

  @Override
  public String toString() {
    return "Modifier{" + registryName + '}';
  }
}
