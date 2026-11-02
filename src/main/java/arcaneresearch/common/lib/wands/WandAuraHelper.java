package arcaneresearch.common.lib.wands;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.common.world.aura.AuraHandler;

public class WandAuraHelper {
    public static boolean shouldPreserveAura(Level level, Player player, BlockPos pos, Aspect aspect) {
        try {
            return AuraHandler.shouldPreserveAura(level, player, pos);
        } catch (Exception e) {
            return false;
        }
    }

    public static int drainAuraAvailable(Level level, BlockPos pos, Aspect aspect, int amount) {
        try {
            float drained = AuraHandler.drainVis(level, pos, (float) amount, false);
            return (int) drained;
        } catch (Exception e) {
            return 0;
        }
    }
}
