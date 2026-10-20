package arcaneresearch.common.items.misc;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.List;

import arcaneresearch.common.blocks.aura.AuraBlocks;

/**
 * TC4 ItemEldritchObject meta 4 "Eldritch Obelisk Placer" — в 1.7.10 у
 * обелиска свой ПРЕДМЕТ (не BlockItem) со своей иконкой ob_placer и
 * тултипом §oCreative Mode Only. ПКМ по ВЕРХНЕЙ грани: при 6 воздухе
 * сверху (y+1..y+6, TC4 verbatim func_147437_c) ставит алтарь (meta 0)
 * на y+1, базу обелиска (meta 1) на y+3 и 4 тела (meta 2) на y+4..y+7
 * (setBlock(...,3) как world.func_147465_d). Предмет не расходуется.
 * Лежит в arcaneresearch.arcaneresearch.common.items.misc.
 */
public class ItemEldritchObeliskPlacer extends Item {

    public ItemEldritchObeliskPlacer(Item.Properties props) {
        super(props.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    public ItemEldritchObeliskPlacer() {
        this(new Item.Properties());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null || context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }
        // TC4: player.swingItem() ДО проверок — на обеих сторонах.
        // NEOFORGE_PORT (26.x): LivingEntity.swing(InteractionHand) -> swing(hand, SwingAnimation, sendToSwingingEntity).
        player.swing(InteractionHand.MAIN_HAND,
                net.minecraft.world.item.component.SwingAnimation.DEFAULT, false);
        // TC4 verbatim: проверка воздуха только y+1..y+6 (тело на y+7
        // ставится поверх чего угодно, как func_147465_d).
        for (int a = 1; a <= 6; a++) {
            if (!level.getBlockState(pos.above(a)).isAir()) {
                return InteractionResult.FAIL;
            }
        }
        if (!level.isClientSide()) {
            level.setBlock(pos.above(1), AuraBlocks.ELDRITCH_ALTAR.get().defaultBlockState(), 3);
            level.setBlock(pos.above(3), AuraBlocks.ELDRITCH_OBELISK.get().defaultBlockState(), 3);
            Block body = AuraBlocks.ELDRITCH_OBELISK_BODY.get();
            for (int a = 4; a <= 7; a++) {
                level.setBlock(pos.above(a), body.defaultBlockState(), 3);
            }
        }
        // TC4: return !world.field_72995_K.
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    // NEOFORGE_PORT: appendHoverText signature changed in 26.x
    // (TooltipContext/TooltipDisplay/Consumer). Same content as Forge.
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                net.minecraft.world.item.component.TooltipDisplay display,
                                java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("item.arcaneresearch.eldritch_obelisk_placer.tooltip")
                .withStyle(ChatFormatting.ITALIC));
    }
}
