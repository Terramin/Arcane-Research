package arcaneresearch.discovered.scan;

import java.util.LinkedHashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import arcaneresearch.Arcaneresearch;
import org.jspecify.annotations.NonNull;

public class MsgWalletSync implements CustomPacketPayload {

    private static final int MAX_ENTRIES = 4096;
    private static final int MAX_KEY_LEN = 64;

    public static final CustomPacketPayload.Type<MsgWalletSync> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "scan_wallet_sync"));

    public static final StreamCodec<ByteBuf, MsgWalletSync> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(@NonNull ByteBuf buf, @NonNull MsgWalletSync packet) {
            java.util.List<Map.Entry<String, Integer>> walletEntries = new java.util.ArrayList<>();
            for (Map.Entry<String, Integer> e : packet.wallet.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    walletEntries.add(e);
                }
            }
            ByteBufCodecs.VAR_INT.encode(buf, walletEntries.size());
            for (Map.Entry<String, Integer> e : walletEntries) {
                ByteBufCodecs.STRING_UTF8.encode(buf, e.getKey());
                ByteBufCodecs.VAR_INT.encode(buf, e.getValue());
            }
            java.util.List<Map.Entry<String, Integer>> bonusEntries = new java.util.ArrayList<>();
            for (Map.Entry<String, Integer> e : packet.bonus.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    bonusEntries.add(e);
                }
            }
            ByteBufCodecs.VAR_INT.encode(buf, bonusEntries.size());
            for (Map.Entry<String, Integer> e : bonusEntries) {
                ByteBufCodecs.STRING_UTF8.encode(buf, e.getKey());
                ByteBufCodecs.VAR_INT.encode(buf, e.getValue());
            }
        }

        @Override
        public @NonNull MsgWalletSync decode(@NonNull ByteBuf buf) {
            Map<String, Integer> wallet = new LinkedHashMap<>();
            Map<String, Integer> bonus = new LinkedHashMap<>();
            int n = ByteBufCodecs.VAR_INT.decode(buf);
            if (n < 0 || n > MAX_ENTRIES) {
                n = 0;
            }
            for (int i = 0; i < n; i++) {
                String k = ByteBufCodecs.STRING_UTF8.decode(buf);
                int v = ByteBufCodecs.VAR_INT.decode(buf);
                wallet.put(k, v);
            }
            int m = ByteBufCodecs.VAR_INT.decode(buf);
            if (m < 0 || m > MAX_ENTRIES) {
                m = 0;
            }
            for (int i = 0; i < m; i++) {
                String k = ByteBufCodecs.STRING_UTF8.decode(buf);
                int v = ByteBufCodecs.VAR_INT.decode(buf);
                bonus.put(k, v);
            }
            return new MsgWalletSync(wallet, bonus);
        }
    };

    private final Map<String, Integer> wallet = new LinkedHashMap<>();
    private final Map<String, Integer> bonus = new LinkedHashMap<>();

    public MsgWalletSync() {
    }

    public MsgWalletSync(Map<String, Integer> wallet, Map<String, Integer> bonus) {
        if (wallet != null) {
            for (Map.Entry<String, Integer> e : wallet.entrySet()) {
                String k = e.getKey();
                Integer v = e.getValue();
                if (k != null && v != null) {
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

    @Override
    public CustomPacketPayload.@NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MsgWalletSync msg, IPayloadContext ctx) {
        if (ctx == null) {
            return;
        }
        ctx.enqueueWork(() -> applyClient(msg));
    }

    @OnlyIn(Dist.CLIENT)
    private static void applyClient(MsgWalletSync msg) {
        if (msg == null) {
            return;
        }
        ScanWallet.applyView(msg.wallet, msg.bonus);
    }
}