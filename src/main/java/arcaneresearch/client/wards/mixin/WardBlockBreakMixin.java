package arcaneresearch.client.wards.mixin;

import arcaneresearch.client.wards.ClientWardOverlay;
import arcaneresearch.common.wards.WardManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Warded blocks must never be broken in survival. Cancelling BreakEvent on the
 * server is not enough: the client predicts the break and removes the block
 * locally, so it visibly disappears and pops back after the server rejects it.
 * Stop the dig before it starts instead. Creative mode still breaks the block
 * (and drops the ward), matching TC4.
 */
@Mixin(MultiPlayerGameMode.class)
public abstract class WardBlockBreakMixin {

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void arcaneresearch$skipWardedDig(BlockPos pos, Direction face,
                                              CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.player.isCreative()
                || !WardManager.isWarded(mc.level, pos)) {
            return;
        }
        // The left-click event is not fired when the dig is cancelled here,
        // so spawn TC4's ward hit FX ourselves.
        ClientWardOverlay.spawnWardHitFx(mc.level, pos, face);
        cir.setReturnValue(false);
    }

    /** Safety net for a dig started before the ward was placed. */
    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void arcaneresearch$skipWardedBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.player.isCreative()
                || !WardManager.isWarded(mc.level, pos)) {
            return;
        }
        cir.setReturnValue(false);
    }
}
