package arcaneresearch.discovered.scan;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
public class MsgWalletSync {
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
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(wallet.size());
        for (Map.Entry<String, Integer> e : wallet.entrySet()) {
            buf.writeUtf(e.getKey(), 64);
            buf.writeVarInt(e.getValue());
        }
        buf.writeVarInt(bonus.size());
        for (Map.Entry<String, Integer> e : bonus.entrySet()) {
            buf.writeUtf(e.getKey(), 64);
            buf.writeVarInt(e.getValue());
        }
    }
    public MsgWalletSync(FriendlyByteBuf buf) {
        int n = buf.readVarInt();
        for (int i = 0; i < n; i++) {
            wallet.put(buf.readUtf(64), buf.readVarInt());
        }
        int m = buf.readVarInt();
        for (int i = 0; i < m; i++) {
            bonus.put(buf.readUtf(64), buf.readVarInt());
        }
    }
    public static void handle(MsgWalletSync msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isServer()) {
                return;
            }
            applyClient(msg);
        });
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void applyClient(MsgWalletSync msg) {
        ScanWallet.applyView(msg.wallet, msg.bonus);
    }
}
