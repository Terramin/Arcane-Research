package arcaneresearch.common.items.tools;

import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.api.items.IWarpingGear;
import arcaneresearch.init.ModItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemVoidAxe extends AxeItem implements IWarpingGear {
    public ItemVoidAxe() {
        super(ArcaneresearchMaterials.TOOLMAT_VOID, 5.0F, -3.0F,
                new Item.Properties());
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, ItemStack repair) {
        return repair.is(ModItems.VOID_METAL_INGOT.get()) || super.isValidRepairItem(toRepair, repair);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (stack.isDamaged() && entity.tickCount % 20 == 0 && entity instanceof LivingEntity) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide()
                && (!(target instanceof Player) || isPvPEnabled(attacker.level()))) {
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 0));
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    private boolean isPvPEnabled(Level level) {
        if (level.getServer() != null) {
            return level.getServer().isPvpAllowed();
        }
        return true;
    }

    @Override
    public int getWarp(ItemStack itemstack, Player player) {
        return 1;
    }
}
