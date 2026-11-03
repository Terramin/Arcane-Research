package arcaneresearch.invscan;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import arcaneresearch.Arcaneresearch;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class MsgScannedSync {
    private static final int MAX_KEYS = 8192;
    private static final int MAX_KEY_LEN = 128;

    private final List<String> keys = new ArrayList<>();
    private boolean replace;

    public MsgScannedSync() {
    }

    public MsgScannedSync(List<String> keys, boolean replace) {
        this.replace = replace;
        if (keys != null) {
            for (String k : keys) {
                if (k != null && !k.isEmpty()) {
                    this.keys.add(k);
                }
            }
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(replace);
        buf.writeVarInt(keys.size());
        for (String k : keys) {
            buf.writeUtf(k, MAX_KEY_LEN);
        }
    }

    public MsgScannedSync(FriendlyByteBuf buf) {
        replace = buf.readBoolean();
        int n = buf.readVarInt();
        if (n < 0 || n > MAX_KEYS) {
            n = 0;
        }
        for (int i = 0; i < n; i++) {
            keys.add(buf.readUtf(MAX_KEY_LEN));
        }
    }

    public static void handle(MsgScannedSync msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx == null ? null : ctx.get();
        if (context == null || msg == null) {
            return;
        }
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isServer()) {
                return;
            }
            applyClient(msg);
        });
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void applyClient(MsgScannedSync msg) {
        try {
            InvScanClient.applyScannedSync(msg.keys, msg.replace);
        } catch (Exception e) {
            Arcaneresearch.LOGGER.warn("InvScan: failed to apply scanned sync", e);
        }
    }
}
