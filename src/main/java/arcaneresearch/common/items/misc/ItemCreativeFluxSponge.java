package arcaneresearch.common.items.misc;

import arcaneresearch.api.aura.AuraHelper;
import arcaneresearch.common.entities.EntityFluxRift;
import arcaneresearch.common.lib.utils.EntityUtils;
import arcaneresearch.init.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCreativeFluxSponge extends Item {

    public ItemCreativeFluxSponge(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("Right-click to drain all").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("flux from 9x9 chunk area").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("Also removes flux rifts").withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.literal("if used while sneaking.").withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.literal("Creative only").withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        boolean clientSide = level.isClientSide;

        if (clientSide) {
            player.swing(hand);
            SoundEvent sound = ModSounds.CRAFT_START.get();
            level.playLocalSound(
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    sound,
                    SoundSource.PLAYERS,
                    0.15f,
                    1.0f,
                    false
            );
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        BlockPos center = player.blockPosition();
        int totalDrained = 0;

        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                totalDrained += (int) AuraHelper.drainFlux(
                        level,
                        center.offset(16 * x, 0, 16 * z),
                        500.0f,
                        false
                );
            }
        }

        player.sendSystemMessage(
                Component.literal(totalDrained + " flux drained from 81 chunks.")
                        .withStyle(ChatFormatting.GREEN)
        );

        if (player.isShiftKeyDown()) {
            List<EntityFluxRift> rifts = EntityUtils.getEntitiesInRange(
                    level,
                    center,
                    32.0,
                    EntityFluxRift.class
            );

            int removed = 0;
            for (EntityFluxRift rift : rifts) {
                if (rift != null) {
                    rift.discard();
                    removed++;
                }
            }

            player.sendSystemMessage(
                    Component.literal(removed + " flux rifts removed.")
                            .withStyle(ChatFormatting.DARK_AQUA)
            );
        }

        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}