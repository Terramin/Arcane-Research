package arcaneresearch.discovered.note;

import arcaneresearch.helper.StackNbtHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.items.IScribeTools;
import arcaneresearch.api.research.ResearchCategories;
import arcaneresearch.api.research.ResearchEntry;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.common.lib.research.ResearchManager;
import arcaneresearch.init.ModSounds;

public final class DiscoveredNotes {
    private DiscoveredNotes() {
    }

    public static ItemStack createNote(String key, AspectList required) {
        if (DiscoveredNoteSetup.getNoteItem() == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(DiscoveredNoteSetup.getNoteItem());
        DiscoveredNoteData.write(stack, DiscoveredNoteData.create(key, required));
        return stack;
    }

    public static boolean isNoteFor(ItemStack stack, String key) {
        if (key == null || stack == null || stack.isEmpty()) {
            return false;
        }
        if (!(stack.getItem() instanceof ItemDiscoveredResearchNote)) {
            return false;
        }
        return key.equals(DiscoveredNoteData.read(stack).key);
    }

    @Deprecated
    public static int findNoteSlot(Player player, String key) {
        if (player == null || key == null) {
            return -1;
        }
        var items = player.getInventory().items;
        int size = items.size();
        for (int slot = 0; slot < size; slot++) {
            if (isNoteFor(items.get(slot), key)) {
                return slot;
            }
        }
        return -1;
    }

    public static InteractionResultHolder<ItemStack> tryCompleteFromNote(Level level, Player player, ItemStack stack) {
        if (level == null || player == null || stack == null) {
            return InteractionResultHolder.pass(stack == null ? ItemStack.EMPTY : stack);
        }
        DiscoveredNoteData data = DiscoveredNoteData.read(stack);
        if (data.key == null || !data.isComplete()
                || ArcaneresearchCapabilities.isResearchComplete(player, data.key)) {
            return InteractionResultHolder.pass(stack);
        }
        boolean client = level.isClientSide;
        if (!client) {
            presentDiscovery(player, data.key);
            boolean ok = player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                    && ResearchManager.doesPlayerHaveRequisites(player, data.key)
                    && arcaneresearch.common.lib.network.playerdata.PacketSyncProgressToServer
                    .checkRequisites(serverPlayer, data.key)
                    && ResearchManager.progressResearch(player, data.key);
            if (ok) {
                stack.shrink(1);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.LEARN.get(), SoundSource.PLAYERS, 0.75F, 1.0F);
            } else {
                player.sendSystemMessage(Component.translatable("tc.discoveryerror", data.key));
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, client);
    }

    public static Component researchName(String key) {
        if (key == null) {
            return null;
        }
        ResearchEntry entry = ResearchCategories.getResearch(key);
        if (entry != null) {
            return entry.getLocalizedName();
        }
        return Component.translatable("tempnote." + key + ".name");
    }

    public static Component researchText(String key) {
        if (key == null) {
            return null;
        }
        ResearchEntry entry = ResearchCategories.getResearch(key);
        if (entry != null && entry.getStages() != null) {
            List<Component> parts = new ArrayList<>();
            for (arcaneresearch.api.research.ResearchStage stage : entry.getStages()) {
                if (stage != null && stage.getText() != null && !stage.getText().isEmpty()) {
                    parts.add(Component.translatable(stage.getText()));
                }
            }
            if (!parts.isEmpty()) {
                net.minecraft.network.chat.MutableComponent out = Component.empty();
                for (int i = 0; i < parts.size(); i++) {
                    if (i > 0) {
                        out.append(" ");
                    }
                    out.append(parts.get(i));
                }
                return out;
            }
        }
        return Component.translatable("tempnote." + key + ".text");
    }

    public static Component researchPopup(String key) {
        if (key == null) {
            return null;
        }
        String langKey = "tempnote." + key + ".popup";
        String resolved = Component.translatable(langKey).getString();
        if (resolved.isEmpty() || resolved.equals(langKey)) {
            return null;
        }
        return Component.translatable(langKey);
    }

    public static ItemStack toDiscovery(ItemStack note) {
        if (note == null || DiscoveredNoteSetup.getDiscoveryItem() == null) {
            return note == null ? ItemStack.EMPTY : note;
        }
        ItemStack out = new ItemStack(DiscoveredNoteSetup.getDiscoveryItem(), note.getCount());
        StackNbtHelper.copyStackTag(note, out);
        return out;
    }

    public static boolean isDiscoveryFor(ItemStack stack, String key) {
        if (stack == null || stack.isEmpty() || key == null
                || DiscoveredNoteSetup.getDiscoveryItem() == null) {
            return false;
        }
        if (stack.getItem() != DiscoveredNoteSetup.getDiscoveryItem()) {
            return false;
        }
        return key.equals(DiscoveredNoteData.read(stack).key);
    }

    public static boolean hasDiscovery(Player player, String key) {
        if (player == null || key == null) {
            return false;
        }
        return arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities.getKnowledge(player)
                .map(k -> k.hasPresentedDiscovery(key)).orElse(false);
    }

    public static boolean presentDiscovery(Player player, String key) {
        if (player == null || key == null) {
            return false;
        }
        var knowledgeOpt = arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities
                .getKnowledge(player);
        boolean added = knowledgeOpt.map(k -> k.addPresentedDiscovery(key)).orElse(false);
        if (added && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            knowledgeOpt.ifPresent(knowledge -> {
                String target = arcaneresearch.api.research.ResearchCategories.getResearch(key) != null
                        ? key
                        : null;
                if (target == null) {
                    arcaneresearch.api.research.ResearchEntry req =
                            arcaneresearch.discovered.table.DiscoveredTableResearch.requiringResearch(key);
                    target = req == null ? null : req.getKey();
                }
                if (target != null && knowledge.isResearchKnown(target)) {
                    knowledge.setResearchFlag(target,
                            arcaneresearch.api.capabilities.IPlayerKnowledge.EnumResearchFlag.DISCOVERY);
                }
                knowledge.sync(serverPlayer);
            });
        }
        return added;
    }

    @Deprecated
    public static boolean isCompleteNoteFor(ItemStack stack, String key) {
        if (stack == null || stack.isEmpty() || key == null
                || DiscoveredNoteSetup.getNoteItem() == null) {
            return false;
        }
        if (stack.getItem() != DiscoveredNoteSetup.getNoteItem()) {
            return false;
        }
        DiscoveredNoteData data = DiscoveredNoteData.read(stack);
        return key.equals(data.key) && data.isComplete();
    }

    public static boolean consumeDiscovery(Player player, String key) {
        if (player == null || key == null) {
            return false;
        }
        return arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities.getKnowledge(player)
                .map(k -> k.consumePresentedDiscovery(key)).orElse(false);
    }

    public static ItemStack discoveryStack(String key) {
        if (DiscoveredNoteSetup.getDiscoveryItem() == null || key == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(DiscoveredNoteSetup.getDiscoveryItem());
        DiscoveredNoteData.write(stack, DiscoveredNoteData.create(key, new AspectList()));
        return stack;
    }

    @Deprecated
    public static int clearNotes(Player player) {
        if (player == null) {
            return 0;
        }
        Inventory inv = player.getInventory();
        int removed = 0;
        int size = inv.items.size();
        for (int i = 0; i < size; i++) {
            ItemStack stack = inv.items.get(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemDiscoveredResearchNote) {
                inv.items.set(i, ItemStack.EMPTY);
                removed++;
            }
        }
        ItemStack off = player.getOffhandItem();
        if (!off.isEmpty() && off.getItem() instanceof ItemDiscoveredResearchNote) {
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            removed++;
        }
        return removed;
    }

    @Deprecated
    public static boolean consumeInk(Player player, boolean doIt) {
        if (player == null) {
            return false;
        }
        Inventory inv = player.getInventory();
        for (ItemStack stack : inv.items) {
            if (stack != null && stack.getItem() instanceof IScribeTools
                    && stack.getDamageValue() < stack.getMaxDamage()) {
                if (doIt && !player.getAbilities().instabuild) {
                    stack.setDamageValue(stack.getDamageValue() + 1);
                }
                return true;
            }
        }
        return false;
    }

    @Deprecated
    public static boolean consumePaper(Player player) {
        if (player == null) {
            return false;
        }
        Inventory inv = player.getInventory();
        for (ItemStack stack : inv.items) {
            if (stack != null && stack.is(Items.PAPER)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    @Deprecated
    public static ItemStack createNoteFromSupplies(Player player, String key, AspectList required) {
        if (player == null) {
            return null;
        }
        if (consumeInk(player, false) && consumePaper(player)) {
            consumeInk(player, true);
            ItemStack out = createNote(key, required);
            return out.isEmpty() ? null : out;
        }
        return null;
    }
}