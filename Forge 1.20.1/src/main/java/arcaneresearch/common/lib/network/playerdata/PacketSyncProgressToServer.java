package arcaneresearch.common.lib.network.playerdata;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchEntry;
import arcaneresearch.api.research.ResearchStage;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.common.lib.research.ResearchManager;
import java.util.function.Supplier;
public class PacketSyncProgressToServer {
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
    public static void encode(PacketSyncProgressToServer msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.key);
        buf.writeBoolean(msg.first);
        buf.writeBoolean(msg.checks);
        buf.writeBoolean(msg.noFlags);
    }
    public static PacketSyncProgressToServer decode(FriendlyByteBuf buf) {
        PacketSyncProgressToServer msg = new PacketSyncProgressToServer();
        msg.key = buf.readUtf(256);
        msg.first = buf.readBoolean();
        msg.checks = buf.readBoolean();
        msg.noFlags = buf.readBoolean();
        return msg;
    }
    public static void handle(PacketSyncProgressToServer msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
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
        ctx.setPacketHandled(true);
    }
    public static boolean checkRequisites(ServerPlayer player, String key) {
        ResearchEntry research = ResearchCategories.getResearch(key);
        if (research == null || research.getStages() == null) {
            return true;
        }
        IPlayerKnowledge knowledge = ArcaneresearchCapabilities.getKnowledge(player).orElse(null);
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
            return TagKey.create(Registries.ITEM, new ResourceLocation(tagName));
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
            var holders = player.level().registryAccess()
                    .registryOrThrow(Registries.ITEM).getTag(tag);
            if (holders.isPresent() && !holders.get().stream().toList().isEmpty()) {
                return new ItemStack(holders.get().stream().toList().get(0).value());
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
