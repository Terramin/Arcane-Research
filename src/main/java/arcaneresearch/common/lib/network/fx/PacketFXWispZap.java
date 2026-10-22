package arcaneresearch.common.lib.network.fx;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.entities.monster.EntityWisp;
import io.netty.buffer.ByteBuf;
import java.awt.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.handling.IPayloadContext;





public class PacketFXWispZap implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFXWispZap> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "fx_wisp_zap"));

    public static final StreamCodec<ByteBuf, PacketFXWispZap> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketFXWispZap packet) {
            buf.writeInt(packet.sourceEntityId);
            buf.writeInt(packet.targetEntityId);
        }

        @Override
        public PacketFXWispZap decode(ByteBuf buf) {
            return new PacketFXWispZap(buf.readInt(), buf.readInt());
        }
    };


    private final int sourceEntityId;
    private final int targetEntityId;

    public PacketFXWispZap(int sourceEntityId, int targetEntityId) {
        this.sourceEntityId = sourceEntityId;
        this.targetEntityId = targetEntityId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXWispZap msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleClient(msg, ctx.player()));
    }

    private static void handleClient(PacketFXWispZap packet, Player player) {
            if (player == null) return;
            Level level = player.level();
            if (level == null) return;
    
            Entity source = getEntityById(packet.sourceEntityId, player, level);
            Entity target = getEntityById(packet.targetEntityId, player, level);
    
            if (source == null || target == null) return;
    
            float r = 1.0f;
            float g = 1.0f;
            float b = 1.0f;
    
            if (source instanceof EntityWisp wisp) {
                Aspect aspect = wisp.getAspect();
                if (aspect != null) {
                    Color c = new Color(aspect.getColor());
                    r = c.getRed() / 255.0f;
                    g = c.getGreen() / 255.0f;
                    b = c.getBlue() / 255.0f;
                }
            }
    
            FXDispatcher.INSTANCE.arcBolt(
                    source.getX(), source.getY(), source.getZ(),
                    target.getX(), target.getY(), target.getZ(),
                    r, g, b, 0.6f);
        }
    
        private static Entity getEntityById(int entityId, Player player, Level level) {
            if (entityId == player.getId()) {
                return player;
            }
            return level.getEntity(entityId);
        }
}