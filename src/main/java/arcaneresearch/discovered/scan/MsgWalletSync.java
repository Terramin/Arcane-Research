package arcaneresearch.discovered.scan;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

public class MsgWalletSync {
    private static final int MAX_ENTRIES = 4096;
    private static final int MAX_KEY_LEN = 64;

    private final Map<String, Integer> wallet = new LinkedHashMap<>();
    private final Map<String, Integer> bonus = new LinkedHashMap<>();

    public MsgWalletSync() {
    }

    public MsgWalletSync(Map<String, Integer> wallet, Map<String, Integer> bonus) {
        if (wallet != null) {
            for (Map.Entry<String, Integer> e : wallet.entrySet()) {
                String k = e.getKey();
                Integer v = e.getValue();
                if (k != null && v != null && v > 0) {
                    this.wallet.put(k, v);
                }
            }
        }
        if (bonus != null) {
            for (Map.Entry<String, Integer> e : bonus.entrySet()) {
                String k = e.getKey();
                Integer v = e.getValue();
                if (k != null && v != null) {
                    this.bonus.put(k, v);
                }
            }
        }
    }

    public void encode(FriendlyByteBuf buf) {
        if (buf == null) {
            return;
        }
        buf.writeVarInt(wallet.size());
        for (Map.Entry<String, Integer> e : wallet.entrySet()) {
            String k = e.getKey();
            Integer v = e.getValue();
            if (k == null || v == null) {
                continue;
            }
            buf.writeUtf(k, MAX_KEY_LEN);
            buf.writeVarInt(v);
        }
        buf.writeVarInt(bonus.size());
        for (Map.Entry<String, Integer> e : bonus.entrySet()) {
            String k = e.getKey();
            Integer v = e.getValue();
            if (k == null || v == null) {
                continue;
            }
            buf.writeUtf(k, MAX_KEY_LEN);
            buf.writeVarInt(v);
        }
    }

    public MsgWalletSync(FriendlyByteBuf buf) {
        if (buf == null) {
            return;
        }
        int n = buf.readVarInt();
        if (n < 0 || n > MAX_ENTRIES) {
            n = 0;
        }
        for (int i = 0; i < n; i++) {
            String k = buf.readUtf(MAX_KEY_LEN);
            int v = buf.readVarInt();
            wallet.put(k, v);
        }
        int m = buf.readVarInt();
        if (m < 0 || m > MAX_ENTRIES) {
            m = 0;
        }
        for (int i = 0; i < m; i++) {
            String k = buf.readUtf(MAX_KEY_LEN);
            int v = buf.readVarInt();
            bonus.put(k, v);
        }
    }

    public static void handle(MsgWalletSync msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx == null ? null : ctx.get();
        if (context == null) {
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
    private static void applyClient(MsgWalletSync msg) {
        if (msg == null) {
            return;
        }
        ScanWallet.applyView(msg.wallet, msg.bonus);
    }
}