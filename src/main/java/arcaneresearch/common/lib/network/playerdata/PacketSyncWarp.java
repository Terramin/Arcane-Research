package arcaneresearch.common.lib.network.playerdata;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import java.util.function.Supplier;
public class PacketSyncWarp {
    private CompoundTag data;
    public PacketSyncWarp() {
        this.data = new CompoundTag();
    }
    public PacketSyncWarp(Player player) {
        IPlayerWarp warp = ArcaneresearchCapabilities.getWarp(player);
        if (warp != null) {
            this.data = warp.serializeNBT();
        } else {
            this.data = new CompoundTag();
        }
    }
    public static void encode(PacketSyncWarp msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.data);
    }
    public static PacketSyncWarp decode(FriendlyByteBuf buf) {
        PacketSyncWarp msg = new PacketSyncWarp();
        msg.data = buf.readNbt();
        return msg;
    }
    public static void handle(PacketSyncWarp msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> handleOnClient(msg));
        ctx.setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleOnClient(PacketSyncWarp msg) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        IPlayerWarp warp = ArcaneresearchCapabilities.getWarp(player);
        if (warp != null && msg.data != null) {
            warp.deserializeNBT(msg.data);
        }
    }
}
