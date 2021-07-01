package slimeknights.tconstruct.tools.item.small;

import com.google.common.collect.Sets;
import slimeknights.tconstruct.library.tools.ToolDefinition;
import slimeknights.tconstruct.library.tools.helper.ToolAttackContext;
import slimeknights.tconstruct.library.tools.helper.ToolAttackUtil;
import slimeknights.tconstruct.library.tools.helper.ToolHarvestLogic;
import slimeknights.tconstruct.library.tools.helper.aoe.CircleAOEHarvestLogic;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.Set;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class HandAxeTool extends HarvestTool {
  private static final Set<Material> EXTRA_MATERIALS = Sets.newHashSet(Material.WOOD, Material.NETHER_WOOD, Material.PLANT, Material.REPLACEABLE_PLANT, Material.BAMBOO, Material.VEGETABLE, Material.LEAVES);
  public static final CircleAOEHarvestLogic HARVEST_LOGIC = new CircleAOEHarvestLogic(1, false) {
    @Override
    public boolean isEffectiveAgainst(IModifierToolStack tool, ItemStack stack, BlockState state) {
      return EXTRA_MATERIALS.contains(state.getMaterial()) || super.isEffectiveAgainst(tool, stack, state);
    }
  };
  public HandAxeTool(Properties properties, ToolDefinition toolDefinition) {
    super(properties, toolDefinition);
  }

  @Override
  public ToolHarvestLogic getToolHarvestLogic() {
    return HARVEST_LOGIC;
  }

  @Override
  public boolean dealDamage(IModifierToolStack tool, ToolAttackContext context, float damage) {
    boolean hit = super.dealDamage(tool, context, damage);
    if (hit && context.isFullyCharged()) {
      ToolAttackUtil.spawnAttackParticle(TinkerTools.axeAttackParticle.get(), context.getAttacker(), 0.8d);
    }
    return hit;
  }

  @Override
  public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
    return true;
  }
}
