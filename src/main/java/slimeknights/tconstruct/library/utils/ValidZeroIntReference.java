package slimeknights.tconstruct.library.utils;

import java.util.function.Consumer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.DataSlot;

/**
 * Int reference holder that starts the "lastKnownValue" at an invalid value.
 * Fixes a bug where a non-zero value on the client is not updated on UI open as the new value is 0
 * TODO: move to mantle
 */
public class ValidZeroIntReference extends DataSlot {
  private final ContainerData data;
  private final int idx;

  public ValidZeroIntReference(ContainerData data, int idx) {
    this.prevValue = Integer.MIN_VALUE;
    this.data = data;
    this.idx = idx;
  }

  @Override
  public int get() {
    return data.get(idx);
  }

  @Override
  public void set(int value) {
    data.set(idx, value);
  }

  /**
   * Creates smart int reference holders and adds them to the given consumer
   * @param consumer  Consumer for reference holders
   * @param array     Array source
   */
  public static void trackIntArray(Consumer<DataSlot> consumer, ContainerData array) {
    for(int i = 0; i < array.getCount(); ++i) {
      consumer.accept(new ValidZeroIntReference(array, i));
    }
  }
}
