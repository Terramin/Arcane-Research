package arcaneresearch.common.lib.network.fx;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.fx.FXDispatcher;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;





public class PacketFXSonic implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFXSonic> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "fx_sonic"));

    public static final StreamCodec<ByteBuf, PacketFXSonic> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketFXSonic packet) {
            buf.writeInt(packet.sourceId);
        }

        @Override
        public PacketFXSonic decode(ByteBuf buf) {
            int sourceId = buf.readInt();
            return new PacketFXSonic(sourceId);
        }
    };


    private final int sourceId;

    public PacketFXSonic(Entity source) {
        this.sourceId = source.getId();
    }

    public PacketFXSonic(int sourceId) {
        this.sourceId = sourceId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXSonic msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleClient(msg, ctx.player()));
    }

    private static void handleClient(PacketFXSonic packet, Player player) {
            if (player == null) return;
            Level level = player.level();
            if (level == null) return;
    
            Entity source = level.getEntity(packet.sourceId);
            if (source != null) {
    
                FXDispatcher.INSTANCE.sonicBoom(source.getX(), source.getY(), source.getZ(), source, 10);
            }
        }
}
