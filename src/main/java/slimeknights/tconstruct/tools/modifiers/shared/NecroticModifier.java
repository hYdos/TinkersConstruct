package slimeknights.tconstruct.tools.modifiers.shared;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tools.helper.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;

public class NecroticModifier extends Modifier {
  public NecroticModifier() {
    super(0x4D4D4D);
  }

  @Override
  public int afterEntityHit(IModifierToolStack tool, int level, ToolAttackContext context, float damageDealt) {
    if (context.isFullyCharged() && damageDealt > 0) {
      // heals between 0 and (level) * 5% of damage dealt
      LivingEntity attacker = context.getAttacker();
      float heal = attacker.getRandom().nextFloat() * damageDealt * level * 0.05f;
      attacker.heal(heal);
      if (heal > 2) {
        attacker.level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.ZOMBIE_INFECT, SoundSource.PLAYERS, 1.0f, 1.0f);
      }
    }
    return 0;
  }
}
