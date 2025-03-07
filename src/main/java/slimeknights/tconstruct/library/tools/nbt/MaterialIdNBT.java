package slimeknights.tconstruct.library.tools.nbt;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.Constants.NBT;
import slimeknights.tconstruct.library.materials.IMaterial;
import slimeknights.tconstruct.library.materials.MaterialId;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Similar to {@link MaterialNBT}, but does not check materials against the registry.
 * Used for rendering so we can have client side only materials for UIs. Anything logic based should use the regular material NBT
 */
@EqualsAndHashCode
@ToString
public class MaterialIdNBT {
  /** Instance containing no materials, for errors with parsing NBT */
  final static MaterialIdNBT EMPTY = new MaterialIdNBT(ImmutableList.of());

  /** List of materials contained in this NBT */
  @Getter
  private final List<MaterialId> materials;

  /** Creates a new material NBT */
  public MaterialIdNBT(List<MaterialId> materials) {
    this.materials = ImmutableList.copyOf(materials);
  }

  /**
   * Gets the material at the given index
   * @param index  Index
   * @return  Material, or unknown if index is invalid
   */
  public MaterialId getMaterial(int index) {
    if (index >= materials.size() || index < 0) {
      return IMaterial.UNKNOWN_ID;
    }
    return materials.get(index);
  }

  /**
   * Parses the material list from NBT
   * @param nbt  NBT instance
   * @return  MaterialNBT instance
   */
  public static MaterialIdNBT readFromNBT(@Nullable Tag nbt) {
    if (nbt == null || nbt.getId() != NBT.TAG_LIST) {
      return EMPTY;
    }
    ListTag listNBT = (ListTag) nbt;
    if (listNBT.getElementType() != NBT.TAG_STRING) {
      return EMPTY;
    }

    List<MaterialId> materials = listNBT.stream()
      .map(Tag::getAsString)
      .map(MaterialId::tryParse)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
    return new MaterialIdNBT(materials);
  }

  /**
   * Writes this material list to NBT
   * @return  List of materials
   */
  public ListTag serializeToNBT() {
    return materials.stream()
                    .map(MaterialId::toString)
                    .map(StringTag::valueOf)
                    .collect(Collectors.toCollection(ListTag::new));
  }

  /**
   * Parses the material list from a stack
   * @param stack  Tool stack instance
   * @return  MaterialNBT instance
   */
  public static MaterialIdNBT from(ItemStack stack) {
    CompoundTag nbt = stack.getTag();
    if (nbt != null) {
      return readFromNBT(nbt.getList(ToolStack.TAG_MATERIALS, NBT.TAG_STRING));
    }
    return EMPTY;
  }

  /** Writes this material list to the given stack */
  public ItemStack updateStack(ItemStack stack) {
    stack.getOrCreateTag().put(ToolStack.TAG_MATERIALS, serializeToNBT());
    return stack;
  }
}
