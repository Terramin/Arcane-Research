package arcaneresearch.invscan;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
public class MsgScanSlot {
    private int containerId;
    private int slotIndex;
    public MsgScanSlot() {
    }
    public MsgScanSlot(int containerId, int slotIndex) {
        this.containerId = containerId;
        this.slotIndex = slotIndex;
    }
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(containerId);
        buf.writeVarInt(slotIndex);
    }
    public MsgScanSlot(FriendlyByteBuf buf) {
        containerId = buf.readVarInt();
        slotIndex = buf.readVarInt();
    }
    public static void handle(MsgScanSlot msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (!ctx.get().getDirection().getReceptionSide().isServer()) {
                return;
            }
            InvScanServer.handleSlot(ctx.get().getSender(), msg.containerId, msg.slotIndex);
        });
        ctx.get().setPacketHandled(true);
    }
}
