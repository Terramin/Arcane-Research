package arcaneresearch.common.lib.network.playerdata;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public class PacketRunicCharge {
    private final int playerId;
    private final int charge;
    private final int max;
    public PacketRunicCharge() {
        this.playerId = 0;
        this.charge = 0;
        this.max = 0;
    }
    public PacketRunicCharge(int playerId, int charge, int max) {
        this.playerId = playerId;
        this.charge = charge;
        this.max = max;
    }
    public static void encode(PacketRunicCharge msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.playerId);
        buf.writeInt(msg.charge);
        buf.writeInt(msg.max);
    }
    public static PacketRunicCharge decode(FriendlyByteBuf buf) {
        return new PacketRunicCharge(buf.readInt(), buf.readInt(), buf.readInt());
    }
    public static void handle(PacketRunicCharge msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleOnClient(msg));
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleOnClient(PacketRunicCharge msg) {
        arcaneresearch.client.lib.events.RunicShieldHud.applySync(
                msg.playerId, msg.charge, msg.max);
    }
}
