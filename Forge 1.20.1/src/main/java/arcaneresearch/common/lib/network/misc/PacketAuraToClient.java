package arcaneresearch.common.lib.network.misc;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.common.world.aura.AuraChunk;
import java.util.function.Supplier;
public class PacketAuraToClient {
    @OnlyIn(Dist.CLIENT)
    public static AuraChunk currentAura = null;
    private short base;
    private float vis;
    private float flux;
    public PacketAuraToClient() {
    }
    public PacketAuraToClient(AuraChunk auraChunk) {
        this.base = auraChunk.getBase();
        this.vis = auraChunk.getVis();
        this.flux = auraChunk.getFlux();
    }
    public PacketAuraToClient(short base, float vis, float flux) {
        this.base = base;
        this.vis = vis;
        this.flux = flux;
    }
    public static void encode(PacketAuraToClient msg, FriendlyByteBuf buf) {
        buf.writeShort(msg.base);
        buf.writeFloat(msg.vis);
        buf.writeFloat(msg.flux);
    }
    public static PacketAuraToClient decode(FriendlyByteBuf buf) {
        PacketAuraToClient msg = new PacketAuraToClient();
        msg.base = buf.readShort();
        msg.vis = buf.readFloat();
        msg.flux = buf.readFloat();
        return msg;
    }
    public static void handle(PacketAuraToClient msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> handleOnClient(msg));
        ctx.setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleOnClient(PacketAuraToClient msg) {
        currentAura = new AuraChunk(null, msg.base, msg.vis, msg.flux);
        arcaneresearch.client.lib.events.HudHandler.updateAura(currentAura);
    }
}
