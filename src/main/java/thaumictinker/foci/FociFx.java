package thaumictinker.foci;

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import arcaneresearch.client.fx.FXDispatcher;

 

 
final class FociFx {

    private FociFx() {
    }

    static void sparkle(@Nullable Level level, Player player, double x, double y, double z, float r, float g, float b) {
        if (level != null && level.isClientSide) {
            FXDispatcher.INSTANCE.sparkle((float) x, (float) y, (float) z, r, g, b);
        }
    }
}
