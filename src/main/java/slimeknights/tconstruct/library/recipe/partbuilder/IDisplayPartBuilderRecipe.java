package slimeknights.tconstruct.library.recipe.partbuilder;

import slimeknights.tconstruct.library.materials.IMaterial;
import slimeknights.tconstruct.library.materials.MaterialId;
import slimeknights.tconstruct.tables.TinkerTables;

import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/**
 * Part builder recipes that can show in JEI
 */
public interface IDisplayPartBuilderRecipe extends IPartBuilderRecipe {
  /** Gets the ID of the recipe's material, safer to call than getMaterial as it does not depend on another registry */
  MaterialId getMaterialId();

  /** Gets the material needed to craft this recipe */
  IMaterial getMaterial();

  /**
   * Gets a list of pattern items to display in the pattern slot
   * @return  Pattern items
   */
  default List<ItemStack> getPatternItems() {
    return Collections.singletonList(new ItemStack(TinkerTables.pattern));
  }
}
