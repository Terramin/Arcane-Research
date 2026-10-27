package arcaneresearch.discovered.note;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import arcaneresearch.api.aspects.Aspect;
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
        DiscoveredNoteData data = DiscoveredNoteData.read(stack);
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
        DiscoveredNoteData data = DiscoveredNoteData.read(stack);
        float progress = data.getTotalProgress();
        Item discoveryItem = DiscoveredNoteSetup.getDiscoveryItem();
        if (discoveryItem != null && stack.getItem() == discoveryItem) {
            progress = 1.0F;
        }
        if (data.key != null && progress >= 0.2F) {
            Component name = DiscoveredNotes.researchName(data.key);
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
        DiscoveredNoteData data = DiscoveredNoteData.read(stack);
        if (!DiscoveredNoteData.hasNote(stack) || data.key == null) {
            return;
        }
        Item discoveryItem = DiscoveredNoteSetup.getDiscoveryItem();
        Item noteItem = DiscoveredNoteSetup.getNoteItem();
        Item stackItem = stack.getItem();
        float progress = data.getTotalProgress();
        if (discoveryItem != null && stackItem == discoveryItem) {
            progress = 1.0F;
        }
        if (progress >= 0.2F) {
            Component name = DiscoveredNotes.researchName(data.key);
            tooltip.add(name == null ? Component.literal(data.key) : name);
            Component popup = DiscoveredNotes.researchPopup(data.key);
            if (popup != null) {
                tooltip.add(popup.copy().withStyle(ChatFormatting.GRAY));
            }
        } else {
            tooltip.add(Component.translatable("tc.discoveryunknown"));
        }
        boolean showStudy = false;
        if (noteItem != null && stackItem == noteItem) {
            int pct1 = (int) (progress * 100.0F);
            tooltip.add(Component.translatable("tooltip.arcaneresearch.stage1", pct1));
            showStudy = DiscoveredNoteData.requiresHex(data.key);
        }
        var proxy = Proxies.get();
        if (proxy != null && proxy.isShiftKeyDown()) {
            Aspect[] tags = data.tags;
            int[] need = data.need;
            int[] have = data.have;
            if (tags != null) {
                int needLen = need == null ? 0 : need.length;
                int haveLen = have == null ? 0 : have.length;
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
                    if (got <= 0) {
                        continue;
                    }
                    tooltip.add(Component.translatable("tc.aspect." + tag)
                            .append(Component.literal(": " + got + "/" + want))
                            .withStyle(ChatFormatting.GRAY));
                }
            }
        }
        if (showStudy) {
            int pct2 = data.stage2 ? 100 : 0;
            tooltip.add(Component.translatable("tooltip.arcaneresearch.stage2", pct2));
        }
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return DiscoveredNoteData.read(stack).isComplete();
    }

    @Override
    public @NotNull Rarity getRarity(@NotNull ItemStack stack) {
        if (stack.isEmpty()) {
            return Rarity.UNCOMMON;
        }
        return DiscoveredNoteData.read(stack).isComplete() ? Rarity.RARE : Rarity.UNCOMMON;
    }
}