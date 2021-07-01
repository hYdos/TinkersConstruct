package slimeknights.tconstruct.tools.modifiers.internal;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.modifiers.SingleUseModifier;
import slimeknights.tconstruct.library.tools.OffhandCooldownTracker;
import slimeknights.tconstruct.library.tools.ToolDefinition;
import slimeknights.tconstruct.library.tools.helper.ToolAttackUtil;
import slimeknights.tconstruct.library.tools.item.IModifiableWeapon;
import slimeknights.tconstruct.library.tools.nbt.IModDataReadOnly;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;

public class OffhandAttackModifier extends SingleUseModifier {
  public static final ResourceLocation DUEL_WIELDING = Util.getResource("duel_wielding");
  private final int cooldownTime;
  public OffhandAttackModifier(int color, int cooldownTime) {
    super(color);
    // vanilla is 20 / attackSpeed, making it 25 / attackSpeed makes the offhand only 80% of the speed
    this.cooldownTime = cooldownTime;
  }

  @Override
  public boolean shouldDisplay(boolean advanced) {
    return false;
  }

  @Override
  public void addVolatileData(ToolDefinition toolDefinition, StatsNBT baseStats, IModDataReadOnly persistentData, int level, ModDataNBT volatileData) {
    volatileData.putBoolean(DUEL_WIELDING, true);
  }

  /** If true, we can use the attack */
  protected boolean canAttack(IModifierToolStack tool, Player player, InteractionHand hand) {
    return hand == InteractionHand.OFF_HAND && OffhandCooldownTracker.isAttackReady(player) && tool.getItem() instanceof IModifiableWeapon;
  }

  @Override
  public InteractionResult onEntityUseFirst(IModifierToolStack tool, int level, Player player, Entity target, InteractionHand hand) {
    if (canAttack(tool, player, hand)) {
      if (!player.level.isClientSide()) {
        ToolAttackUtil.attackEntity((IModifiableWeapon)tool.getItem(), tool, player, InteractionHand.OFF_HAND, target, ToolAttackUtil.getCooldownFunction(player, InteractionHand.OFF_HAND), false);
      }
      OffhandCooldownTracker.applyCooldown(player, tool, cooldownTime);
      // we handle swinging the arm, return consume to prevent resetting cooldown
      ToolAttackUtil.swingHand(player, InteractionHand.OFF_HAND, false);
      return InteractionResult.CONSUME;
    }
    return InteractionResult.PASS;
  }

  @Override
  public InteractionResult onToolUse(IModifierToolStack tool, int level, Level world, Player player, InteractionHand hand) {
    if (canAttack(tool, player, hand)) {
      // target done in onEntityInteract, this is just for cooldown cause you missed
      OffhandCooldownTracker.applyCooldown(player, tool, cooldownTime);
      // we handle swinging the arm, return consume to prevent resetting cooldown
      ToolAttackUtil.swingHand(player, InteractionHand.OFF_HAND, false);
      return InteractionResult.CONSUME;
    }
    return InteractionResult.PASS;
  }
}
