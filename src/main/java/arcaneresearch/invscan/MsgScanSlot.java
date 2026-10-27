package arcaneresearch.invscan;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
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

    public static void handle(MsgScanSlot msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context context = ctxSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender != null) {
                InvScanServer.handleSlot(sender, msg.containerId, msg.slotIndex);
            }
        });
        context.setPacketHandled(true);
    }
}