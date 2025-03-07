package slimeknights.tconstruct.tables.client.inventory.library.slots;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import slimeknights.tconstruct.library.tools.item.ToolCore;

import java.util.Collections;
import java.util.List;

/** Slot information to show in the tool station */
@RequiredArgsConstructor
public class SlotInformation {
  public static final SlotInformation EMPTY = new SlotInformation(Collections.emptyList(), SlotPosition.EMPTY, Items.AIR, -1, false);

  @Getter
  private final List<SlotPosition> points;
  @Getter
  private final SlotPosition toolSlot;
  @Getter
  private final Item item;
  @Getter
  private final int sortIndex;
  @Getter
  private final boolean strictSlots;

  /** Cache of the tool rendering stack */
  private ItemStack toolForRendering;

  /**
   * Creates a new instance of SlotInformation from a json
   *
   * @param json the json object
   * @return a instance of SlotInformation that contains all the points, sort index and tool
   */
  public static SlotInformation fromJson(JsonObject json) {
    List<SlotPosition> slots = SlotPosition.listFromJson(json, "slots");
    Item item = Items.AIR;
    if (json.has("item")) {
      item = GsonHelper.getAsItem(json, "item");
    }

    SlotPosition slotPosition = new SlotPosition(-1, -1);
    if (json.has("tool")) {
      slotPosition = SlotPosition.fromJson(GsonHelper.getAsJsonObject(json, "tool"));
    }

    int sortIndex = GsonHelper.getAsInt(json, "sortIndex");
    // strict defaults to true if its a tool core as it has a definition to be strict
    boolean strictSlots = GsonHelper.getAsBoolean(json, "strictSlots", item instanceof ToolCore);

    return new SlotInformation(slots, slotPosition, item, sortIndex, strictSlots);
  }

  /**
   * Gets the item to use for rendering in the client's screen
   *
   * @return the itemstack to use for rendering
   */
  public ItemStack getToolForRendering() {
    if (this.toolForRendering == null || this.toolForRendering.isEmpty()) {
      if (this.item instanceof ToolCore) {
        this.toolForRendering = ((ToolCore) this.item).buildToolForRendering();
      }
      else {
        this.toolForRendering = new ItemStack(this.item);
      }
    }

    return this.toolForRendering;
  }

  /** Checks if this slot information is the repair button */
  public boolean isRepair() {
    return item == Items.AIR;
  }
}
