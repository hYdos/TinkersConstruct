package slimeknights.tconstruct.tools.item.small;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.ToolType;
import slimeknights.tconstruct.library.tools.ToolDefinition;
import slimeknights.tconstruct.library.tools.helper.ToolHarvestLogic;
import slimeknights.tconstruct.library.tools.item.ToolCore;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;

/** Tool with sword harvest and creative block breaking prevention */
public class SwordTool extends ToolCore {
  public static final ToolType TOOL_TYPE = ToolType.get("sword");
  public static final ImmutableSet<Material> EFFECTIVE_MATERIALS = ImmutableSet.of(Material.WEB, Material.REPLACEABLE_PLANT, Material.CORAL, Material.VEGETABLE, Material.LEAVES);
  public static final ToolHarvestLogic HARVEST_LOGIC = new HarvestLogic();

  public SwordTool(Properties properties, ToolDefinition toolDefinition) {
    super(properties, toolDefinition);
  }

  @Override
  public boolean canAttackBlock(BlockState state, Level worldIn, BlockPos pos, Player player) {
    return !player.isCreative();
  }

  @Override
  public ToolHarvestLogic getToolHarvestLogic() {
    return HARVEST_LOGIC;
  }

  /** Harvest logic for swords */
  public static class HarvestLogic extends ToolHarvestLogic {
    @Override
    public boolean isEffectiveAgainst(IModifierToolStack tool, ItemStack stack, BlockState state) {
      // no sword tool type by default, so augment with vanilla list
      return EFFECTIVE_MATERIALS.contains(state.getMaterial()) || super.isEffectiveAgainst(tool, stack, state);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
      // webs are slow
      float speed = super.getDestroySpeed(stack, state);
      if (state.getMaterial() == Material.WEB) {
        speed *= 7.5f;
      }
      return speed;
    }
  }
}
