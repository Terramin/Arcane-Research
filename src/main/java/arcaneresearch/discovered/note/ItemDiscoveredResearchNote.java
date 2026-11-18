package arcaneresearch.discovered.note;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.research.notes.NoteData;
import arcaneresearch.api.research.notes.Notes;
import arcaneresearch.proxies.Proxies;
import org.jetbrains.annotations.NotNull;

public class ItemDiscoveredResearchNote extends Item {
    public ItemDiscoveredResearchNote() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean client = level.isClientSide;
        NoteData data = NoteData.read(stack);
        Item discoveryItem = DiscoveredNoteSetup.getDiscoveryItem();
        if (data.key != null && discoveryItem != null && stack.getItem() == discoveryItem) {
            if (!client) {
                DiscoveredNotes.presentDiscovery(player, data.key);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.BOOK_PAGE_TURN,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            return InteractionResultHolder.sidedSuccess(stack, client);
        }
        return DiscoveredNotes.tryCompleteFromNote(level, player, stack);
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        NoteData data = NoteData.read(stack);
        float progress = data.getTotalProgress();
        Item discoveryItem = DiscoveredNoteSetup.getDiscoveryItem();
        if (discoveryItem != null && stack.getItem() == discoveryItem) {
            progress = 1.0F;
        }
        if (data.key != null && (progress >= 0.2F || data.revealed)) {
            Component name = Notes.researchName(data.key);
            if (name != null) {
                return name;
            }
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (stack.isEmpty()) {
            return;
        }
        NoteData data = NoteData.read(stack);
        if (!NoteData.hasNote(stack) || data.key == null) {
            return;
        }
        Item discoveryItem = DiscoveredNoteSetup.getDiscoveryItem();
        Item noteItem = DiscoveredNoteSetup.getNoteItem();
        Item stackItem = stack.getItem();
        float progress = data.getTotalProgress();
        boolean isDiscovery = discoveryItem != null && stackItem == discoveryItem;
        if (isDiscovery) {
            progress = 1.0F;
        }
        boolean isNote = noteItem != null && stackItem == noteItem;
        boolean known = progress >= 0.2F || data.revealed;
        boolean complete = data.isComplete();

        if (known) {
            Component name = Notes.researchName(data.key);
            tooltip.add(name == null ? Component.literal(data.key) : name);
            Component popup = Notes.researchPopup(data.key);
            if (popup != null) {
                tooltip.add(popup.copy().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        } else {
            tooltip.add(Component.translatable("tc.discoveryunknown").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }

        if (isNote && !complete) {
            int pct = (int) (progress * 100.0F);
            tooltip.add(Component.translatable("tooltip.arcaneresearch.stage1", pct));
            boolean showStudy = NoteData.requiresHex(data.key);
            if (showStudy) {
                int pct2 = data.stage2 ? 100 : 0;
                tooltip.add(Component.translatable("tooltip.arcaneresearch.stage2", pct2));
            }
        }

        if (complete) {
            tooltip.add(Component.translatable("tooltip.arcaneresearch.note.complete").withStyle(ChatFormatting.GREEN));
        } else if (known) {
            tooltip.add(Component.translatable("tooltip.arcaneresearch.note.in_progress").withStyle(ChatFormatting.YELLOW));
        }

        var proxy = Proxies.get();
        boolean shift = proxy != null && proxy.isShiftKeyDown();
        if (shift && known && !complete) {
            Aspect[] tags = data.tags;
            int[] need = data.need;
            int[] have = data.have;
            int needLen = need == null ? 0 : need.length;
            int haveLen = have == null ? 0 : have.length;
            if (tags != null && tags.length > 0) {
                tooltip.add(Component.translatable("tooltip.arcaneresearch.note.aspects_header").withStyle(ChatFormatting.GRAY));
                for (int i = 0; i < tags.length; i++) {
                    Aspect aspect = tags[i];
                    if (aspect == null) {
                        continue;
                    }
                    String tag = aspect.getTag();
                    if (tag == null) {
                        continue;
                    }
                    int want = i < needLen ? need[i] : 0;
                    int got = i < haveLen ? Math.min(have[i], want) : 0;
                    boolean done = want > 0 && got >= want;
                    ChatFormatting color = done ? ChatFormatting.GREEN : ChatFormatting.GRAY;
                    MutableComponent line = Component.literal("  ")
                            .append(Component.literal(done ? "\u2713 " : "\u2717 ").withStyle(color))
                            .append(Component.translatable("tc.aspect." + tag).withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(": " + got + "/" + want).withStyle(ChatFormatting.GRAY));
                    byte failed = data.getFailed(tag);
                    if (failed > 0) {
                        line = line.append(Component.literal("  (\u00d7" + failed + ")").withStyle(ChatFormatting.RED));
                    }
                    tooltip.add(line);
                }
            }
        } else if (known && !complete) {
            int gathered = 0, total = 0;
            if (data.tags != null) {
                total = data.tags.length;
                int[] need = data.need, have = data.have;
                int nL = need == null ? 0 : need.length;
                int hL = have == null ? 0 : have.length;
                for (int i = 0; i < data.tags.length; i++) {
                    int want = i < nL ? need[i] : 0;
                    int got = i < hL ? Math.min(have[i], want) : 0;
                    if (want > 0 && got >= want) {
                        gathered++;
                    }
                }
            }
            if (total > 0) {
                tooltip.add(Component.translatable("tooltip.arcaneresearch.note.aspects_summary", gathered, total).withStyle(ChatFormatting.GRAY));
            }
            tooltip.add(Component.translatable("tooltip.arcaneresearch.note.shift_hint").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return NoteData.read(stack).isComplete();
    }

    @Override
    public @NotNull Rarity getRarity(@NotNull ItemStack stack) {
        if (stack.isEmpty()) {
            return Rarity.UNCOMMON;
        }
        return NoteData.read(stack).isComplete() ? Rarity.RARE : Rarity.UNCOMMON;
    }
}