package slimeknights.tconstruct.library.tinkering;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public interface ITinkerStationDisplay {

  /**
   * The "title" displayed in the GUI
   */
  Component getLocalizedName();

  /**
   * Returns an List of ITextComponent, where each Text Component represents an information about the tool. Used to display
   * Information about the item in the GUI
   */
  List<Component> getInformation(ItemStack stack);
}
