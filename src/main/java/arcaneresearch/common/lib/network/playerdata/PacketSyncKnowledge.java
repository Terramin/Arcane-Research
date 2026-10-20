package arcaneresearch.common.lib.network.playerdata;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchEntry;
import arcaneresearch.client.gui.ResearchToast;
import java.util.function.Supplier;
public class PacketSyncKnowledge {
    private CompoundTag data;
    public PacketSyncKnowledge() {
        this.data = new CompoundTag();
    }
    public PacketSyncKnowledge(Player player) {
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge != null) {
            this.data = knowledge.serializeNBT();
            for (String key : knowledge.getResearchList()) {
                knowledge.clearResearchFlag(key, IPlayerKnowledge.EnumResearchFlag.POPUP);
                knowledge.clearResearchFlag(key, IPlayerKnowledge.EnumResearchFlag.DISCOVERY);
            }
        } else {
            this.data = new CompoundTag();
        }
    }
    public static void encode(PacketSyncKnowledge msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.data);
    }
    public static PacketSyncKnowledge decode(FriendlyByteBuf buf) {
        PacketSyncKnowledge msg = new PacketSyncKnowledge();
        msg.data = buf.readNbt();
        return msg;
    }
    public static void handle(PacketSyncKnowledge msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> handleOnClient(msg));
        ctx.setPacketHandled(true);
    }
    @OnlyIn(Dist.CLIENT)
    private static void handleOnClient(PacketSyncKnowledge msg) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge != null && msg.data != null) {
            knowledge.deserializeNBT(msg.data);
            for (String key : knowledge.getResearchList()) {
                if (knowledge.hasResearchFlag(key, IPlayerKnowledge.EnumResearchFlag.POPUP)) {
                    ResearchEntry entry = ResearchCategories.getResearch(key);
                    if (entry != null) {
                        Minecraft.getInstance().getToasts().addToast(new ResearchToast(entry));
                    }
                    knowledge.clearResearchFlag(key, IPlayerKnowledge.EnumResearchFlag.POPUP);
                }
                if (knowledge.hasResearchFlag(key, IPlayerKnowledge.EnumResearchFlag.DISCOVERY)) {
                    ResearchEntry entry = ResearchCategories.getResearch(key);
                    if (entry != null) {
                        Minecraft.getInstance().getToasts().addToast(ResearchToast.discovered(entry));
                    }
                    knowledge.clearResearchFlag(key, IPlayerKnowledge.EnumResearchFlag.DISCOVERY);
                }
            }
        }
    }
}
