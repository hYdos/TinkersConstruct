package slimeknights.tconstruct.gadgets.item;

import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import slimeknights.mantle.util.TranslationHelper;
import slimeknights.tconstruct.gadgets.entity.shuriken.ShurikenEntityBase;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;

public class ShurikenItem extends SnowballItem {

  private final BiFunction<Level, Player, ShurikenEntityBase> entity;

  public ShurikenItem(Properties properties, BiFunction<Level, Player, ShurikenEntityBase> entity) {
    super(properties);
    this.entity = entity;
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
    ItemStack itemStack = playerIn.getItemInHand(handIn);
    if (!playerIn.abilities.instabuild) {
      itemStack.shrink(1);
    }

    playerIn.getCooldowns().addCooldown(itemStack.getItem(), 4);

    if(!worldIn.isClientSide) {
      ShurikenEntityBase entity = this.entity.apply(worldIn, playerIn);
      entity.setItem(itemStack);
      entity.shootFromRotation(playerIn, playerIn.xRot, playerIn.yRot, 0.0F, 1.5F, 1.0F);
      worldIn.addFreshEntity(entity);
    }

    playerIn.awardStat(Stats.ITEM_USED.get(this));
    return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
    TranslationHelper.addOptionalTooltip(stack, tooltip);
    super.appendHoverText(stack, worldIn, tooltip, flagIn);
  }
}
