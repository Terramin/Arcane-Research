package arcaneresearch.api.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.fx.PacketFXPollute;
import arcaneresearch.common.world.aura.AuraHandler;

public class AuraHelper {

    public static float drainVis(Level level, BlockPos pos, float amount, boolean simulate) {
        return AuraHandler.drainVis(level, pos, amount, simulate);
    }

    public static float drainFlux(Level level, BlockPos pos, float amount, boolean simulate) {
        return AuraHandler.drainFlux(level, pos, amount, simulate);
    }

    public static void addVis(Level level, BlockPos pos, float amount) {
        AuraHandler.addVis(level, pos, amount);
    }

    public static float getVis(Level level, BlockPos pos) {
        return AuraHandler.getVis(level, pos);
    }

    public static void polluteAura(Level level, BlockPos pos, float amount, boolean showEffect) {
        AuraHandler.addFlux(level, pos, amount);

        if (showEffect && level instanceof ServerLevel serverLevel) {

            PacketHandler.sendToAllTrackingChunk(new PacketFXPollute(pos, amount), serverLevel, pos);

            level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS,
                    0.2f + level.getRandom().nextFloat() * 0.1f,
                    0.9f + level.getRandom().nextFloat() * 0.2f);
        }
    }

    public static float getFlux(Level level, BlockPos pos) {
        return AuraHandler.getFlux(level, pos);
    }

    public static int getAuraBase(Level level, BlockPos pos) {
        return AuraHandler.getAuraBase(level, pos);
    }

    public static boolean shouldPreserveAura(Level level, Player player, BlockPos pos) {
        return AuraHandler.shouldPreserveAura(level, player, pos);
    }

    public static float getTotalAura(Level level, BlockPos pos) {
        return AuraHandler.getTotalAura(level, pos);
    }

    public static float getFluxSaturation(Level level, BlockPos pos) {
        return AuraHandler.getFluxSaturation(level, pos);
    }
}
