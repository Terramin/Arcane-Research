package arcaneresearch.common.lib.network.misc;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.world.aura.AuraChunk;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/misc/PacketAuraToClient.java (PLAY_TO_CLIENT).
// Fields/ctors/handler logic preserved verbatim; transport -> CustomPacketPayload
// with manual StreamCodec (wire order identical to old encode/decode).
// Payload id "arcaneresearch:aura_to_client".
public class PacketAuraToClient implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketAuraToClient> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "aura_to_client"));

    public static final StreamCodec<ByteBuf, PacketAuraToClient> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketAuraToClient packet) {
            buf.writeShort(packet.base);
            buf.writeFloat(packet.vis);
            buf.writeFloat(packet.flux);
        }

        @Override
        public PacketAuraToClient decode(ByteBuf buf) {
            PacketAuraToClient msg = new PacketAuraToClient();
            msg.base = buf.readShort();
            msg.vis = buf.readFloat();
            msg.flux = buf.readFloat();
            return msg;
        }
    };

    public static AuraChunk currentAura = null;
    private short base;
    private float vis;
    private float flux;

    public PacketAuraToClient() {
    }

    public PacketAuraToClient(AuraChunk auraChunk) {
        this.base = auraChunk.getBase();
        this.vis = auraChunk.getVis();
        this.flux = auraChunk.getFlux();
    }

    public PacketAuraToClient(short base, float vis, float flux) {
        this.base = base;
        this.vis = vis;
        this.flux = flux;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketAuraToClient msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleOnClient(msg));
    }

    private static void handleOnClient(PacketAuraToClient msg) {

        currentAura = new AuraChunk(null, msg.base, msg.vis, msg.flux);

        arcaneresearch.client.lib.events.HudHandler.updateAura(currentAura);
    }
}