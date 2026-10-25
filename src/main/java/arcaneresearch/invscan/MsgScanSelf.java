package arcaneresearch.invscan;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
public class MsgScanSelf {
    public MsgScanSelf() {
    }
    public void encode(FriendlyByteBuf buf) {
    }
    public MsgScanSelf(FriendlyByteBuf buf) {
    }
    public static void handle(MsgScanSelf msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (!ctx.get().getDirection().getReceptionSide().isServer()) {
                return;
            }
            InvScanServer.handleSelf(ctx.get().getSender());
        });
        ctx.get().setPacketHandled(true);
    }
}
