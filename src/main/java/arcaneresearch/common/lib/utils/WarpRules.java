package arcaneresearch.common.lib.utils;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import arcaneresearch.init.ModItems;

/** Pure warp rules shared by the event handler and the simulation tests. */
public final class WarpRules {

    private WarpRules() {
    }

    /**
     * Drains the warp counter after a successful warp event. The subtraction is
     * cast as a whole, as in the 1.12.2 original; casting only the deducted
     * amount would drain the counter one point too slowly.
     *
     * @param counter current counter value
     * @param gearWarp warp from equipped gear
     * @return the new counter, never negative for a positive counter
     */
    public static int nextCounter(int counter, int gearWarp) {
        return (int) (counter - Math.max(5.0, Math.sqrt(counter) * 2.0 - gearWarp * 2));
    }

    /**
     * Removes milk from an effect's curative items. Hunger is cured only by
     * bread and zombie brain, matching the original warp tables.
     *
     * @param effect effect to modify
     * @param hunger whether to add the hunger-specific cures
     * @return the same effect instance
     */
    public static MobEffectInstance applyWarpCuratives(MobEffectInstance effect, boolean hunger) {
        effect.getCurativeItems().clear();
        if (hunger) {
            effect.addCurativeItem(new ItemStack(Items.BREAD));
            effect.addCurativeItem(new ItemStack(ModItems.ZOMBIE_BRAIN.get()));
        }
        return effect;
    }
}
