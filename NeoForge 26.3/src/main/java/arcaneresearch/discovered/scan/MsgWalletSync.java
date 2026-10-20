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

/**
 * Сервер -&gt; клиент: снимок хранилища аспектов (кошелёк игрока + бонус
 * открытого тайла). Порт {@code PacketAspectPool}: клиентское зеркало для
 * отображения пула в GUI ({@code ScanWallet.applyView}).
 *
 * NEOFORGE_PORT: SimpleChannel(ScanNetwork) -&gt; CustomPacketPayload
 * (ручной StreamCodec, порядок полей как в encode/decode 1:1).
 * Payload id "arcaneresearch:scan_wallet_sync".
 */
public class MsgWalletSync implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<MsgWalletSync> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "scan_wallet_sync"));

    public static final StreamCodec<ByteBuf, MsgWalletSync> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, MsgWalletSync packet) {
            ByteBufCodecs.VAR_INT.encode(buf, packet.wallet.size());
            for (Map.Entry<String, Integer> e : packet.wallet.entrySet()) {
                ByteBufCodecs.STRING_UTF8.encode(buf, e.getKey());
                ByteBufCodecs.VAR_INT.encode(buf, e.getValue());
            }
            ByteBufCodecs.VAR_INT.encode(buf, packet.bonus.size());
            for (Map.Entry<String, Integer> e : packet.bonus.entrySet()) {
                ByteBufCodecs.STRING_UTF8.encode(buf, e.getKey());
                ByteBufCodecs.VAR_INT.encode(buf, e.getValue());
            }
        }

        @Override
        public MsgWalletSync decode(ByteBuf buf) {
            Map<String, Integer> wallet = new LinkedHashMap<>();
            int n = ByteBufCodecs.VAR_INT.decode(buf);
            for (int i = 0; i < n; i++) {
                wallet.put(ByteBufCodecs.STRING_UTF8.decode(buf), ByteBufCodecs.VAR_INT.decode(buf));
            }
            Map<String, Integer> bonus = new LinkedHashMap<>();
            int m = ByteBufCodecs.VAR_INT.decode(buf);
            for (int i = 0; i < m; i++) {
                bonus.put(ByteBufCodecs.STRING_UTF8.decode(buf), ByteBufCodecs.VAR_INT.decode(buf));
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
            this.wallet.putAll(wallet);
        }
        if (bonus != null) {
            this.bonus.putAll(bonus);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MsgWalletSync msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> applyClient(msg));
    }

    @OnlyIn(Dist.CLIENT)
    private static void applyClient(MsgWalletSync msg) {
        ScanWallet.applyView(msg.wallet, msg.bonus);
    }
}
