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
        
        
        player.swing(InteractionHand.MAIN_HAND,
                net.minecraft.world.item.component.SwingAnimation.DEFAULT, false);
        
        
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
        
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                net.minecraft.world.item.component.TooltipDisplay display,
                                java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("item.arcaneresearch.eldritch_obelisk_placer.tooltip")
                .withStyle(ChatFormatting.ITALIC));
    }
}
