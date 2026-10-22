package arcaneresearch.common.items.misc;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.common.entities.EntityFluxRift;
import arcaneresearch.common.lib.utils.EntityUtils;
import arcaneresearch.init.ModSounds;

import java.util.function.Consumer;

public class ItemCreativeFluxSponge extends Item {

    public ItemCreativeFluxSponge(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC));
    }

    
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.literal("Right-click to drain all").withStyle(ChatFormatting.GREEN));
        tooltip.accept(Component.literal("flux from 9x9 chunk area").withStyle(ChatFormatting.GREEN));
        tooltip.accept(Component.literal("Also removes flux rifts").withStyle(ChatFormatting.DARK_AQUA));
        tooltip.accept(Component.literal("if used while sneaking.").withStyle(ChatFormatting.DARK_AQUA));
        tooltip.accept(Component.literal("Creative only").withStyle(ChatFormatting.DARK_PURPLE));
    }

    
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            
            player.swing(hand, SwingAnimation.DEFAULT, false);
            level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                    ModSounds.CRAFT_START.get(), SoundSource.PLAYERS, 0.15f, 1.0f, false);
        } else {

            int totalDrained = 0;
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    totalDrained += (int) AuraHelper.drainFlux(level,
                            player.blockPosition().offset(16 * x, 0, 16 * z),
                            500.0f, false);
                }
            }

            player.sendSystemMessage(Component.literal(totalDrained + " flux drained from 81 chunks.")
                    .withStyle(ChatFormatting.GREEN));

            if (player.isShiftKeyDown()) {
                java.util.List<EntityFluxRift> rifts = EntityUtils.getEntitiesInRange(level,
                        player.blockPosition(), 32.0, EntityFluxRift.class);

                for (EntityFluxRift rift : rifts) {
                    rift.discard();
                }

                player.sendSystemMessage(Component.literal(rifts.size() + " flux rifts removed.")
                        .withStyle(ChatFormatting.DARK_AQUA));
            }
        }

        
        
        return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
    }
}
