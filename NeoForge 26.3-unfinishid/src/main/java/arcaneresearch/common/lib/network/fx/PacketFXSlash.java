package arcaneresearch.common.lib.network.fx;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.fx.FXDispatcher;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/fx/PacketFXSlash.java (PLAY_TO_CLIENT).
// Fields/ctors/handleClient logic preserved verbatim; transport -> CustomPacketPayload
// with manual StreamCodec (wire order identical to old encode/decode).
// Payload id "arcaneresearch:fx_slash".
public class PacketFXSlash implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFXSlash> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "fx_slash"));

    public static final StreamCodec<ByteBuf, PacketFXSlash> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketFXSlash packet) {
            buf.writeInt(packet.sourceId);
            buf.writeInt(packet.targetId);
        }

        @Override
        public PacketFXSlash decode(ByteBuf buf) {
            return new PacketFXSlash(buf.readInt(), buf.readInt());
        }
    };


    private final int sourceId;
    private final int targetId;

    public PacketFXSlash(int sourceId, int targetId) {
        this.sourceId = sourceId;
        this.targetId = targetId;
    }

    public PacketFXSlash(Entity source, Entity target) {
        this.sourceId = source.getId();
        this.targetId = target.getId();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXSlash msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleClient(msg, ctx.player()));
    }

    private static void handleClient(PacketFXSlash packet, Player player) {
            if (player == null) return;
            Level level = player.level();
            if (level == null) return;
    
            Entity source = level.getEntity(packet.sourceId);
            Entity target = level.getEntity(packet.targetId);
    
            if (source != null && target != null) {
                double sourceY = source.getBoundingBox().minY + source.getBbHeight() / 2.0f;
                double targetY = target.getBoundingBox().minY + target.getBbHeight() / 2.0f;
    
                FXDispatcher.INSTANCE.drawSlash(
                        source.getX(), sourceY, source.getZ(),
                        target.getX(), targetY, target.getZ(),
                        8);
            }
        }
}
