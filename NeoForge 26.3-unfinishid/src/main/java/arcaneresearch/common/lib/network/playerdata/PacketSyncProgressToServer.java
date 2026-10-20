package arcaneresearch.common.lib.network.playerdata;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchEntry;
import arcaneresearch.api.research.ResearchStage;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.common.lib.research.ResearchManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// NEOFORGE_PORT: 1:1 from Forge .../network/playerdata/PacketSyncProgressToServer.java (PLAY_TO_SERVER).
// Fields/ctors/handler logic preserved verbatim; transport -> CustomPacketPayload
// with manual StreamCodec (wire order identical to old encode/decode).
// Payload id "arcaneresearch:sync_progress_to_server".
public class PacketSyncProgressToServer implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSyncProgressToServer> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "sync_progress_to_server"));

    public static final StreamCodec<ByteBuf, PacketSyncProgressToServer> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(ByteBuf buf, PacketSyncProgressToServer packet) {
            ByteBufCodecs.STRING_UTF8.encode(buf, packet.key);
            buf.writeBoolean(packet.first);
            buf.writeBoolean(packet.checks);
            buf.writeBoolean(packet.noFlags);
        }

        @Override
        public PacketSyncProgressToServer decode(ByteBuf buf) {
            PacketSyncProgressToServer msg = new PacketSyncProgressToServer();
            // NEOFORGE_PORT (26.3): Forge decode used readUtf(256) (server-side length cap).
            msg.key = ByteBufCodecs.stringUtf8(256).decode(buf);
            msg.first = buf.readBoolean();
            msg.checks = buf.readBoolean();
            msg.noFlags = buf.readBoolean();
            return msg;
        }
    };

    private String key;
    private boolean first;
    private boolean checks;
    private boolean noFlags;

    public PacketSyncProgressToServer() {
    }

    public PacketSyncProgressToServer(String key, boolean first) {
        this(key, first, false, true);
    }

    public PacketSyncProgressToServer(String key, boolean first, boolean checks, boolean noFlags) {
        this.key = key;
        this.first = first;
        this.checks = checks;
        this.noFlags = noFlags;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncProgressToServer msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (player == null) return;

            boolean knowsResearch = ArcaneresearchCapabilities.isResearchKnown(player, msg.key);
            if (msg.first != knowsResearch) {

                if (msg.checks && !checkRequisites(player, msg.key)) {
                    return;
                }

                if (msg.noFlags) {
                    ResearchManager.noFlags = true;
                }

                ResearchManager.progressResearch(player, msg.key);
            }
        });
    }

    // NEOFORGE_PORT: package-visible in Forge; public for DiscoveredNotes.tryCompleteFromNote.
    // getKnowledge returns @Nullable IPlayerKnowledge directly in 26.x (no Optional).
    public static boolean checkRequisites(ServerPlayer player, String key) {
        ResearchEntry research = ResearchCategories.getResearch(key);
        if (research == null || research.getStages() == null) {
            return true;
        }

        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge == null) return false;

        int currentStage = knowledge.getResearchStage(key) - 1;
        if (currentStage < 0) {
            return false;
        }
        if (currentStage >= research.getStages().length) {
            return true;
        }

        ResearchStage stage = research.getStages()[currentStage];

        Object[] obtain = stage.getObtain();
        if (obtain != null) {
            for (Object o : obtain) {
                if (o instanceof ItemStack required) {
                    if (!required.isEmpty() && !arcaneresearch.common.lib.utils.InventoryUtils
                    .isPlayerCarryingAmount(player, required, false)) {
                        return false;
                    }
                } else if (o instanceof String tagName) {

                    ItemStack first = firstTagMember(player, tagName);
                    if (first != null && !arcaneresearch.common.lib.utils.InventoryUtils
                    .isPlayerCarryingAmount(player, first, true)) {
                        return false;
                    }
                }
            }

            for (Object o : obtain) {
                if (o instanceof ItemStack required) {
                    arcaneresearch.common.lib.utils.InventoryUtils.consumePlayerItem(player, required, true, false);
                } else if (o instanceof String tagName) {
                    ItemStack first = firstTagMember(player, tagName);
                    if (first != null) {
                        arcaneresearch.common.lib.utils.InventoryUtils.consumePlayerItem(player, first, true, true);
                    }
                }
            }
        }

        Object[] craft = stage.getCraft();
        if (craft != null) {
            int[] craftRef = stage.getCraftReference();
            for (int i = 0; i < craft.length; i++) {

                String refKey = "[#]" + craftRef[i];
                if (!knowledge.isResearchKnown(refKey)) {
                    return false;
                }
            }
        }

        String[] researchReqs = stage.getResearch();
        if (researchReqs != null) {
            for (String req : researchReqs) {
                if (!arcaneresearch.api.capabilities.ArcaneresearchCapabilities.knowsResearchStrict(player, req)) {
                    return false;
                }
            }
        }

        ResearchStage.Knowledge[] knowReqs = stage.getKnow();
        if (knowReqs != null) {

            for (ResearchStage.Knowledge k : knowReqs) {
                int playerKnow = knowledge.getKnowledge(k.type, k.category != null ? k.category.key : null);
                if (playerKnow < k.amount) {
                    return false;
                }
            }

            for (ResearchStage.Knowledge k : knowReqs) {
                ResearchManager.addKnowledge(player, k.type, k.category, -k.amount * k.type.getProgression());
            }
        }

        // NEOFORGE_PORT: required_discovery stages (discovered/* package): the player must
        // have presented each discovery (PKM), consumed on success — Forge 1:1.
        String[] discoveryReqs = stage.getDiscovery();
        if (discoveryReqs != null) {
            for (String req : discoveryReqs) {
                if (!arcaneresearch.discovered.note.DiscoveredNotes.hasDiscovery(player, req)) {
                    return false;
                }
            }
            for (String req : discoveryReqs) {
                arcaneresearch.discovered.note.DiscoveredNotes.consumeDiscovery(player, req);
            }
        }

        return true;
    }

    private static TagKey<Item> parseItemTag(String tagName) {
        if (tagName == null || tagName.isEmpty()) {
            return null;
        }
        try {
            return TagKey.create(Registries.ITEM, Identifier.parse(tagName));
        } catch (Exception e) {
            return null;
        }
    }

    private static ItemStack firstTagMember(ServerPlayer player, String tagName) {
        TagKey<Item> tag = parseItemTag(tagName);
        if (tag == null) {
            return null;
        }
        try {
            // NEOFORGE_PORT: Registry.getTag removed -> getTagOrEmpty (same holders).
            java.util.Iterator<net.minecraft.core.Holder<Item>> it = player.level().registryAccess()
                    .lookupOrThrow(Registries.ITEM).getTagOrEmpty(tag).iterator();
            if (it.hasNext()) {
                return new ItemStack(it.next().value());
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
