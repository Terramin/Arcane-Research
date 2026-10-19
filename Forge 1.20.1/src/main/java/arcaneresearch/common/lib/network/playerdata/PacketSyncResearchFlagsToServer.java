package arcaneresearch.common.lib.network.playerdata;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import java.util.function.Supplier;
public class PacketSyncResearchFlagsToServer {
    private String key;
    private byte flags;
    public PacketSyncResearchFlagsToServer() {
    }
    public PacketSyncResearchFlagsToServer(ServerPlayer player, String key) {
        this.key = key;
        ArcaneresearchCapabilities.getKnowledge(player).ifPresent(knowledge -> {
            boolean hasPage = knowledge.hasResearchFlag(key, IPlayerKnowledge.EnumResearchFlag.PAGE);
            boolean hasPopup = knowledge.hasResearchFlag(key, IPlayerKnowledge.EnumResearchFlag.POPUP);
            boolean hasResearch = knowledge.hasResearchFlag(key, IPlayerKnowledge.EnumResearchFlag.RESEARCH);
            this.flags = pack(hasPage, hasPopup, hasResearch);
        });
    }
    public PacketSyncResearchFlagsToServer(String key, boolean hasPage, boolean hasPopup, boolean hasResearch) {
        this.key = key;
        this.flags = pack(hasPage, hasPopup, hasResearch);
    }
    public static void encode(PacketSyncResearchFlagsToServer msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.key);
        buf.writeByte(msg.flags);
    }
    public static PacketSyncResearchFlagsToServer decode(FriendlyByteBuf buf) {
        PacketSyncResearchFlagsToServer msg = new PacketSyncResearchFlagsToServer();
        msg.key = buf.readUtf(256);
        msg.flags = buf.readByte();
        return msg;
    }
    public static void handle(PacketSyncResearchFlagsToServer msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            boolean[] unpacked = unpack(msg.flags);
            boolean hasPage = unpacked[0];
            boolean hasPopup = unpacked[1];
            boolean hasResearch = unpacked[2];
            ArcaneresearchCapabilities.getKnowledge(player).ifPresent(knowledge -> {
                if (hasPage) {
                    knowledge.setResearchFlag(msg.key, IPlayerKnowledge.EnumResearchFlag.PAGE);
                } else {
                    knowledge.clearResearchFlag(msg.key, IPlayerKnowledge.EnumResearchFlag.PAGE);
                }
                if (hasPopup) {
                    knowledge.setResearchFlag(msg.key, IPlayerKnowledge.EnumResearchFlag.POPUP);
                } else {
                    knowledge.clearResearchFlag(msg.key, IPlayerKnowledge.EnumResearchFlag.POPUP);
                }
                if (hasResearch) {
                    knowledge.setResearchFlag(msg.key, IPlayerKnowledge.EnumResearchFlag.RESEARCH);
                } else {
                    knowledge.clearResearchFlag(msg.key, IPlayerKnowledge.EnumResearchFlag.RESEARCH);
                }
            });
        });
        ctx.setPacketHandled(true);
    }
    private static byte pack(boolean b0, boolean b1, boolean b2) {
        byte result = 0;
        if (b0) result |= 1;
        if (b1) result |= 2;
        if (b2) result |= 4;
        return result;
    }
    private static boolean[] unpack(byte packed) {
        return new boolean[]{
                (packed & 1) != 0,
                (packed & 2) != 0,
                (packed & 4) != 0
        };
    }
}
