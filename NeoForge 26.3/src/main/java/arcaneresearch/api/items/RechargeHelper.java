package arcaneresearch.api.items;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import arcaneresearch.api.aura.AuraHelper;

public class RechargeHelper {

    public static final String NBT_TAG = "tc.charge";
    private static final String CHARGE_KEY = NBT_TAG;

    public static int getCharge(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IRechargable)) {
            return -1;
        }
        // NEOFORGE_PORT: stack root tag -> DataComponents.CUSTOM_DATA (same NBT shape).
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag().getIntOr(CHARGE_KEY, 0) : 0;
    }

    public static float getChargePercentage(ItemStack stack, LivingEntity entity) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IRechargable rechargable)) {
            return -1.0f;
        }
        float c = getCharge(stack);
        float m = rechargable.getMaxCharge(stack, entity);
        return c / m;
    }

    public static float getChargeRatio(ItemStack stack, LivingEntity entity) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IRechargable rechargable)) {
            return 0.0f;
        }
        int max = rechargable.getMaxCharge(stack, entity);
        if (max <= 0) return 0.0f;
        return (float) getCharge(stack) / max;
    }

    public static void setCharge(ItemStack stack, int charge, LivingEntity entity) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IRechargable rechargable)) {
            return;
        }
        int max = rechargable.getMaxCharge(stack, entity);
        final int clamped = Math.max(0, Math.min(charge, max));
        // NEOFORGE_PORT: stack root tag -> DataComponents.CUSTOM_DATA (same NBT shape).
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(CHARGE_KEY, clamped));
    }

    public static int addCharge(ItemStack stack, int amount, LivingEntity entity) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IRechargable rechargable)) {
            return 0;
        }
        int current = getCharge(stack);
        int max = rechargable.getMaxCharge(stack, entity);
        int newCharge = Math.min(current + amount, max);
        int added = newCharge - current;

        if (added > 0) {
            setCharge(stack, newCharge, entity);
        }
        return added;
    }

    public static boolean consumeCharge(ItemStack stack, LivingEntity entity, int amount) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IRechargable)) {
            return false;
        }

        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            return false;
        }
        int current = getCharge(stack);
        if (current < amount) {
            return false;
        }
        setCharge(stack, current - amount, entity);
        return true;
    }

    public static boolean hasCharge(ItemStack stack, int amount) {
        return getCharge(stack) >= amount;
    }

    public static void fullRecharge(ItemStack stack, LivingEntity entity) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IRechargable rechargable)) {
            return;
        }
        setCharge(stack, rechargable.getMaxCharge(stack, entity), entity);
    }

    public static float rechargeItem(Level level, ItemStack stack, BlockPos pos, LivingEntity entity, int maxAmount) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IRechargable rechargable)) {
            return 0.0f;
        }
        if (entity instanceof Player player && AuraHelper.shouldPreserveAura(level, player, pos)) {
            return 0.0f;
        }
        int amt = Math.min(maxAmount, rechargable.getMaxCharge(stack, entity) - getCharge(stack));
        int drained = (int) AuraHelper.drainVis(level, pos, amt, false);
        if (drained > 0) {
            addCharge(stack, drained, entity);
            return drained;
        }
        return 0.0f;
    }

    public static float rechargeItemBlindly(ItemStack stack, LivingEntity entity, int maxAmount) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IRechargable rechargable)) {
            return 0.0f;
        }
        int amt = Math.min(maxAmount, rechargable.getMaxCharge(stack, entity) - getCharge(stack));
        if (amt > 0) {
            addCharge(stack, amt, entity);
        }
        return amt;
    }
}
