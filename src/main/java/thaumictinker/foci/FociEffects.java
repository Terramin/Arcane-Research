package thaumictinker.foci;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import thaumictinker.net.TtNet;

 


 
public final class FociEffects {

    private FociEffects() {
    }

    public static void send(Player player, int kind, BlockPos pos) {
        if (player instanceof net.minecraft.server.level.ServerPlayer) {
            TtNet.sendToTrackingAndSelf(player, new TtNet.FociEffectPayload(kind, player.getId(),
                    player.getX(), player.getY(), player.getZ(), pos.getX(), pos.getY(), pos.getZ()));
        }
    }

    public static final int PICK_BURST = 0;
    public static final int PLACE_BURST = 1;
    public static final int SMELT_BEAM = 2;
    public static final int SMELT_PROGRESS = 3;
    public static final int SMELT_DONE = 4;
}
