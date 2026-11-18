package arcaneresearch.common.items.tools.voidmetal;

import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.api.items.IWarpingGear;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VoidSwordItem extends SwordItem implements IWarpingGear, VoidTool {
    public VoidSwordItem() {
        super(ArcaneresearchMaterials.TOOLMAT_VOID, 3, -2.4F,
                VoidTool.createProperties());
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, ItemStack repair) {
        return VoidTool.canRepair(repair) || super.isValidRepairItem(toRepair, repair);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        VoidTool.repairOverTime(stack, entity);
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide()
                && (!(target instanceof Player) || !(attacker instanceof Player) || VoidTool.isPvPEnabled(attacker.level()))) {
            try {
                target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 0));
            } catch (Exception ignored) {
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public int getWarp(ItemStack itemstack, Player player) {
        return 1;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("enchantment.special.sapless").withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
