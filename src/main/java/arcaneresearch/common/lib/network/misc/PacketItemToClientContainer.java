package arcaneresearch.common.lib.network.misc;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;
public class PacketItemToClientContainer {
    private int windowId;
    private int slot;
    private ItemStack item;
    public PacketItemToClientContainer() {
    }
    public PacketItemToClientContainer(int windowIdIn, int slotIn, ItemStack itemIn) {
        windowId = windowIdIn;
        slot = slotIn;
        item = itemIn == null ? ItemStack.EMPTY : itemIn.copy();
    }
    public static void encode(PacketItemToClientContainer msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.windowId);
        buf.writeInt(msg.slot);
        buf.writeItem(msg.item == null ? ItemStack.EMPTY : msg.item);
    }
    public static PacketItemToClientContainer decode(FriendlyByteBuf buf) {
        PacketItemToClientContainer msg = new PacketItemToClientContainer();
        msg.windowId = buf.readInt();
        msg.slot = buf.readInt();
        msg.item = buf.readItem();
        return msg;
    }
    public static void handle(PacketItemToClientContainer msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> handleOnClient(msg));
        ctx.get().setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleOnClient(PacketItemToClientContainer message) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.containerMenu != null
                    && mc.player.containerMenu.containerId == message.windowId) {
                mc.player.containerMenu.setItem(message.slot, 0, message.item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
