package slimeknights.tconstruct.library.utils;

import lombok.AllArgsConstructor;
import net.minecraft.world.inventory.DataSlot;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

@AllArgsConstructor
public class LambdaIntReference extends DataSlot {
  private final IntSupplier getter;
  private final IntConsumer setter;

  /** Constructor to let you start from a value other than 0 */
  public LambdaIntReference(int startingValue, IntSupplier getter, IntConsumer setter) {
    this(getter, setter);
    this.prevValue = startingValue;
  }

  @Override
  public int get() {
    return getter.getAsInt();
  }

  @Override
  public void set(int value) {
    setter.accept(value);
  }
}
