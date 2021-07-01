package slimeknights.tconstruct.gadgets.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.item.ArmorTooltipItem;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.shared.block.SlimeType;

public class SlimeBootsItem extends ArmorTooltipItem {

  public SlimeBootsItem(SlimeType slimeType, Properties props) {
    super(new SlimeArmorMaterial(slimeType.getSerializedName() + "_slime"), EquipmentSlot.FEET, props);
  }

  @Override
  public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot equipmentSlot) {
    return HashMultimap.create();
  }

  public static class SlimeArmorMaterial implements ArmorMaterial {
    private final Ingredient empty_repair_material = Ingredient.of(Items.AIR);
    private final String name;

    public SlimeArmorMaterial(String slimeName) {
      name = slimeName;
    }

    @Override
    public int getDurabilityForSlot(EquipmentSlot slotIn) {
      return 0;
    }

    @Override
    public int getDefenseForSlot(EquipmentSlot slotIn) {
      return 0;
    }

    @Override
    public int getEnchantmentValue() {
      return 0;
    }

    @Override
    public SoundEvent getEquipSound() {
      return SoundEvents.SLIME_BLOCK_PLACE;
    }

    @Override
    public Ingredient getRepairIngredient() {
      return this.empty_repair_material;
    }

    @Override
    public String getName() {
      return Util.resource(name);
    }

    @Override
    public float getToughness() {
      return 0;
    }

    @Override
    public float getKnockbackResistance() {
      return 0;
    }
  }
}
