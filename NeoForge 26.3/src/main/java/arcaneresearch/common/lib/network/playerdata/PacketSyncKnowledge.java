package arcaneresearch.common.lib.network.playerdata;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchEntry;

// NEOFORGE_PORT: 1:1 from Forge .../playerdata/PacketSyncKnowledge.java (PLAY_TO_CLIENT).
// Payload (CompoundTag NBT) + client logic preserved verbatim.
// REQUIRED transport changes only:
//   - encode/decode/handle(Supplier<NetworkEvent.Context>) triple (+NetworkEvent import)
//     -> CustomPacketPayload { TYPE, STREAM_CODEC, type() } + handle(msg, IPayloadContext).
//   - STREAM_CODEC maps the same NBT via ByteBufCodecs.COMPOUND_TAG (no FriendlyByteBuf methods).
//   - Payload id "arcaneresearch:sync_knowledge" (Forge used one "main" channel + numeric ids;
//     see PacketHandler order table for the full id mapping).
public class PacketSyncKnowledge implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSyncKnowledge> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "sync_knowledge"));

    public static final StreamCodec<ByteBuf, PacketSyncKnowledge> STREAM_CODEC =
            ByteBufCodecs.COMPOUND_TAG.map(PacketSyncKnowledge::new, msg -> msg.data);

    private CompoundTag data;

    public PacketSyncKnowledge() {
        this.data = new CompoundTag();
    }

    private PacketSyncKnowledge(CompoundTag data) {
        this.data = data;
    }

    public PacketSyncKnowledge(Player player) {
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge != null) {
            this.data = knowledge.serializeNBT();

            for (String key : knowledge.getResearchList()) {
                knowledge.clearResearchFlag(key, IPlayerKnowledge.EnumResearchFlag.POPUP);
                // NEOFORGE_PORT (discovered/*, Forge 1:1): флаг DISCOVERY тоже
                // одноразовый — чистится при отправке.
                knowledge.clearResearchFlag(key, IPlayerKnowledge.EnumResearchFlag.DISCOVERY);
            }
        } else {
            this.data = new CompoundTag();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncKnowledge msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> handleOnClient(msg, ctx.player()));
    }

    private static void handleOnClient(PacketSyncKnowledge msg, Player player) {
        if (player == null) return;

        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge != null && msg.data != null) {
            knowledge.deserializeNBT(msg.data);

            for (String key : knowledge.getResearchList()) {
                if (knowledge.hasResearchFlag(key, IPlayerKnowledge.EnumResearchFlag.POPUP)) {
                    ResearchEntry entry = ResearchCategories.getResearch(key);
                    if (entry != null) {
                        showResearchToast(entry);
                    }
                    knowledge.clearResearchFlag(key, IPlayerKnowledge.EnumResearchFlag.POPUP);
                }
                // NEOFORGE_PORT (discovered/*, Forge 1:1): тост предъявленного открытия.
                if (knowledge.hasResearchFlag(key, IPlayerKnowledge.EnumResearchFlag.DISCOVERY)) {
                    ResearchEntry entry = ResearchCategories.getResearch(key);
                    if (entry != null) {
                        showDiscoveryToast(entry);
                    }
                    knowledge.clearResearchFlag(key, IPlayerKnowledge.EnumResearchFlag.DISCOVERY);
                }
            }
        }
    }

    // NEOFORGE_PORT: toast construction lives in the client class (ResearchToast.show*);
    // a Toast-typed expression here made the dedicated server fail to link this packet.
    private static void showResearchToast(ResearchEntry entry) {
        arcaneresearch.client.gui.ResearchToast.show(entry);
    }

    private static void showDiscoveryToast(ResearchEntry entry) {
        arcaneresearch.client.gui.ResearchToast.showDiscovered(entry);
    }
}
