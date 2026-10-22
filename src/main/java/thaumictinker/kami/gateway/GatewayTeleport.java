package thaumictinker.kami.gateway;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import arcaneresearch.init.ModSounds;

 



 
public final class GatewayTeleport {

    public static final double LANDING_HEIGHT = 1.6;

    private GatewayTeleport() {
    }

    public static boolean teleport(ServerPlayer player, PearlLocation location) {
        ServerLevel level = (ServerLevel) player.level();
        if (!level.dimension().equals(location.dimension())) {
            player.sendSystemMessage(Component.translatable("ttinkerer.ttmisc.noDest"));
            return false;
        }
        BlockPos pos = location.pos();
        if (!(level.getBlockEntity(pos) instanceof WarpGateBlockEntity destination)) {
            player.sendSystemMessage(Component.translatable("ttinkerer.ttmisc.noDest"));
            return false;
        }
        if (destination.isLocked()) {
            player.sendSystemMessage(Component.translatable("ttinkerer.ttmisc.noTeleport"));
            return false;
        }
        Vec3 from = player.position();
        float yaw = player.getYRot();
        float pitch = player.getXRot();
        level.playSound(null, from.x, from.y, from.z, ModSounds.WAND.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.stopRiding();
        player.teleportTo(level, pos.getX() + 0.5, pos.getY() + LANDING_HEIGHT, pos.getZ() + 0.5, yaw, pitch);
        level.playSound(null, pos.getX() + 0.5, pos.getY() + LANDING_HEIGHT, pos.getZ() + 0.5,
                ModSounds.WAND.get(), SoundSource.PLAYERS, 1.0F, 0.1F);
        
        thaumictinker.net.TtNet.sendToNear(level, from.x, from.y, from.z, FX_RANGE,
                thaumictinker.net.TtNet.elementFx(from.x, from.y, from.z,
                        thaumictinker.net.TtNet.ElementsFxPayload.WISPS, 1.0F));
        thaumictinker.net.TtNet.sendToNear(level, player.getX(), player.getY(), player.getZ(), FX_RANGE,
                thaumictinker.net.TtNet.elementFx(player.getX(), player.getY(), player.getZ(),
                        thaumictinker.net.TtNet.ElementsFxPayload.WISPS, 1.0F));
        return true;
    }

    private static final double FX_RANGE = 32.0;
}

