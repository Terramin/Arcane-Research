package arcaneresearch.invscan;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import arcaneresearch.Arcaneresearch;

public class MsgScannedSync implements CustomPacketPayload {

    private static final int MAX_KEYS = 8192;
    private static final int MAX_KEY_LEN = 128;

    public static final CustomPacketPayload.Type<MsgScannedSync> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "invscan_scanned_sync"));

    public static final StreamCodec<ByteBuf, MsgScannedSync> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, MsgScannedSync packet) {
            ByteBufCodecs.BOOL.encode(buf, packet.replace);
            ByteBufCodecs.VAR_INT.encode(buf, packet.keys.size());
            for (String key : packet.keys) {
                ByteBufCodecs.STRING_UTF8.encode(buf, key == null ? "" : key);
            }
        }

        @Override
        public MsgScannedSync decode(ByteBuf buf) {
            boolean replace = ByteBufCodecs.BOOL.decode(buf);
            int n = ByteBufCodecs.VAR_INT.decode(buf);
            if (n < 0 || n > MAX_KEYS) {
                n = 0;
            }
            List<String> keys = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                String key = ByteBufCodecs.STRING_UTF8.decode(buf);
                if (key != null && !key.isEmpty()) {
                    keys.add(key);
                }
            }
            return new MsgScannedSync(keys, replace);
        }
    };

    public final List<String> keys;
    public final boolean replace;

    public MsgScannedSync(List<String> keys, boolean replace) {
        this.keys = keys == null ? List.of() : keys;
        this.replace = replace;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MsgScannedSync msg, IPayloadContext ctx) {
        if (msg == null || ctx == null) {
            return;
        }
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof net.minecraft.server.level.ServerPlayer) {
                return;
            }
            try {
                InvScanClient.applyScannedSync(msg.keys, msg.replace);
            } catch (Exception e) {
                Arcaneresearch.LOGGER.warn("InvScan: failed to apply scanned sync", e);
            }
        });
    }
}
