package arcaneresearch.common.lib.network.playerdata;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/playerdata/PacketSyncResearchFlagsToServer.java (PLAY_TO_SERVER).
// Fields/ctors/handler logic preserved verbatim; transport -> CustomPacketPayload
// with manual StreamCodec (wire order identical to old encode/decode).
// Payload id "arcaneresearch:sync_research_flags_to_server".
public class PacketSyncResearchFlagsToServer implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSyncResearchFlagsToServer> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "sync_research_flags_to_server"));

    public static final StreamCodec<ByteBuf, PacketSyncResearchFlagsToServer> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketSyncResearchFlagsToServer packet) {
            ByteBufCodecs.STRING_UTF8.encode(buf, packet.key);
            buf.writeByte(packet.flags);
        }

        @Override
        public PacketSyncResearchFlagsToServer decode(ByteBuf buf) {
            PacketSyncResearchFlagsToServer msg = new PacketSyncResearchFlagsToServer();
            // NEOFORGE_PORT (26.3): Forge decode used readUtf(256) (server-side length cap).
            msg.key = ByteBufCodecs.stringUtf8(256).decode(buf);
            msg.flags = buf.readByte();
            return msg;
        }
    };

    private String key;
    private byte flags;

    public PacketSyncResearchFlagsToServer() {
    }

    public PacketSyncResearchFlagsToServer(ServerPlayer player, String key) {
        this.key = key;

        // NEOFORGE_PORT: getKnowledge returns @Nullable directly (no Optional).
        arcaneresearch.api.capabilities.IPlayerKnowledge knowledge =
                ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge != null) {
            boolean hasPage = knowledge.hasResearchFlag(key, IPlayerKnowledge.EnumResearchFlag.PAGE);
            boolean hasPopup = knowledge.hasResearchFlag(key, IPlayerKnowledge.EnumResearchFlag.POPUP);
            boolean hasResearch = knowledge.hasResearchFlag(key, IPlayerKnowledge.EnumResearchFlag.RESEARCH);
            this.flags = pack(hasPage, hasPopup, hasResearch);
        }
    }

    public PacketSyncResearchFlagsToServer(String key, boolean hasPage, boolean hasPopup, boolean hasResearch) {
        this.key = key;
        this.flags = pack(hasPage, hasPopup, hasResearch);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncResearchFlagsToServer msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player == null) return;

            boolean[] unpacked = unpack(msg.flags);
            boolean hasPage = unpacked[0];
            boolean hasPopup = unpacked[1];
            boolean hasResearch = unpacked[2];

            // NEOFORGE_PORT: getKnowledge returns @Nullable directly (no Optional).
            arcaneresearch.api.capabilities.IPlayerKnowledge knowledge =
                    ArcaneresearchCapabilities.getKnowledge(player);
            if (knowledge != null) {

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
            }
        });
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
