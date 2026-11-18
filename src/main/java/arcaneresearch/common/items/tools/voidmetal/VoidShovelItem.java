package arcaneresearch.common.items.tools.voidmetal;

import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.api.items.IWarpingGear;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class VoidShovelItem extends ShovelItem implements IWarpingGear, VoidTool {
    public VoidShovelItem() {
        super(ArcaneresearchMaterials.TOOLMAT_VOID, 1.5F, -3.0F,
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
                && (!(target instanceof Player) || VoidTool.isPvPEnabled(attacker.level()))) {
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 0));
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public int getWarp(ItemStack itemstack, Player player) {
        return 1;
    }
}
