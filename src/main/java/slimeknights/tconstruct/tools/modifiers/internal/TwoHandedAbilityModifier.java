package slimeknights.tconstruct.tools.modifiers.internal;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.library.modifiers.SingleUseModifier;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;

public class TwoHandedAbilityModifier extends SingleUseModifier {
	public TwoHandedAbilityModifier() {
		super(0xcef1f5);
  }

	@Override
	public boolean shouldDisplay(boolean advanced) {
		return false;
	}
	
  @Override
  public int getPriority() {
    return Integer.MIN_VALUE;
  }

  @Override
  public InteractionResult onToolUse(IModifierToolStack tool, int level, Level world, Player player, InteractionHand hand) {
    return InteractionResult.CONSUME;
  }
}
