package arcaneresearch.common.lib.network.fx;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.fx.FXDispatcher;

// NEOFORGE_PORT: 1:1 from Forge .../network/fx/PacketFXPollute.java (PLAY_TO_CLIENT).
// Fields/ctor/clamp logic preserved verbatim; transport -> CustomPacketPayload
// (same pattern as PacketSyncKnowledge; see its javadoc).
// Payload id "arcaneresearch:fx_pollute".
public class PacketFXPollute implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFXPollute> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "fx_pollute"));

    public static final StreamCodec<ByteBuf, PacketFXPollute> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, p -> p.x,
            ByteBufCodecs.INT, p -> p.y,
            ByteBufCodecs.INT, p -> p.z,
            ByteBufCodecs.BYTE, p -> p.amount,
            PacketFXPollute::new);

    private final int x;
    private final int y;
    private final int z;
    private final byte amount;

    public PacketFXPollute(BlockPos pos, float amt) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();

        if (amt < 1.0f && amt > 0.0f) {
            amt = 1.0f;
        }
        this.amount = (byte) Math.min(amt, 127);
    }

    private PacketFXPollute(int x, int y, int z, byte amount) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.amount = amount;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFXPollute packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleClient(packet));
    }

    private static void handleClient(PacketFXPollute packet) {
        BlockPos pos = new BlockPos(packet.x, packet.y, packet.z);

        int particleCount = Math.min(40, Math.abs(packet.amount));
        for (int a = 0; a < particleCount; a++) {
            FXDispatcher.INSTANCE.drawPollutionParticles(pos);
        }
    }
}