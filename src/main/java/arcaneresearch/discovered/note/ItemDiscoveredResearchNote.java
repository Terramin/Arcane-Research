package arcaneresearch.discovered.note;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.proxies.Proxies;

 








 
public class ItemDiscoveredResearchNote extends Item {

    public ItemDiscoveredResearchNote(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        DiscoveredNoteData data = DiscoveredNoteData.read(stack);
        
        
        if (data.key != null && stack.getItem() == DiscoveredNoteSetup.getDiscoveryItem()) {
            if (!level.isClientSide()) {
                DiscoveredNotes.presentDiscovery(player, data.key);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.BOOK_PAGE_TURN,
                        net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        }
        return DiscoveredNotes.tryCompleteFromNote(level, player, stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        DiscoveredNoteData data = DiscoveredNoteData.read(stack);
        
        float progress = data.getTotalProgress();
        if (stack.getItem() == DiscoveredNoteSetup.getDiscoveryItem()) {
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
            Consumer<Component> tooltip, TooltipFlag flag) {
        DiscoveredNoteData data = DiscoveredNoteData.read(stack);
        if (!DiscoveredNoteData.hasNote(stack) || data.key == null) {
            return;
        }
        float progress = data.getTotalProgress();
        if (stack.getItem() == DiscoveredNoteSetup.getDiscoveryItem()) {
            progress = 1.0F;
        }
        if (progress >= 0.2F) {
            Component name = DiscoveredNotes.researchName(data.key);
            tooltip.accept(name == null ? Component.literal(data.key) : name);
            Component popup = DiscoveredNotes.researchPopup(data.key);
            if (popup != null) {
                tooltip.accept(popup.copy().withStyle(ChatFormatting.GRAY));
            }
        } else {
            tooltip.accept(Component.translatable("tc.discoveryunknown"));
        }
        
        
        boolean showStudy = false;
        if (stack.getItem() == DiscoveredNoteSetup.getNoteItem()) {
            int pct1 = (int) (progress * 100.0F);
            tooltip.accept(Component.translatable("tooltip.arcaneresearch.stage1", pct1));
            showStudy = DiscoveredNoteData.requiresHex(data.key);
        }
        
        
        if (Proxies.get().isShiftKeyDown()) {
            for (int i = 0; i < data.tags.length; i++) {
                Aspect aspect = data.tags[i];
                if (aspect == null) {
                    continue;
                }
                int want = i < data.need.length ? data.need[i] : 0;
                int got = i < data.have.length ? Math.min(data.have[i], want) : 0;
                if (got <= 0) {
                    continue;
                }
                tooltip.accept(Component.translatable("tc.aspect." + aspect.getTag())
                        .append(Component.literal(": " + got + "/" + want))
                        .withStyle(ChatFormatting.GRAY));
            }
        }
        if (showStudy) {
            int pct2 = data.stage2 ? 100 : 0;
            tooltip.accept(Component.translatable("tooltip.arcaneresearch.stage2", pct2));
        }
    }

      
    @Override
    public boolean isFoil(ItemStack stack) {
        return DiscoveredNoteData.read(stack).isComplete();
    }

    
    
}
