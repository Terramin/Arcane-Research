package arcaneresearch.common.items.misc;

import arcaneresearch.common.blocks.aura.AuraBlocks;
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
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemEldritchObeliskPlacer extends Item {

    public ItemEldritchObeliskPlacer() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        BlockPos.MutableBlockPos cursor = pos.mutable().move(Direction.UP);
        for (int i = 0; i < 7; i++) {
            if (!level.isEmptyBlock(cursor)) {
                return InteractionResult.FAIL;
            }
            cursor.move(Direction.UP);
        }

        player.swing(InteractionHand.MAIN_HAND);

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Block altar = AuraBlocks.ELDRITCH_ALTAR.get();
        Block obelisk = AuraBlocks.ELDRITCH_OBELISK.get();
        Block body = AuraBlocks.ELDRITCH_OBELISK_BODY.get();

        level.setBlock(pos.above(1), altar.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(pos.above(3), obelisk.defaultBlockState(), Block.UPDATE_CLIENTS);
        for (int a = 4; a <= 7; a++) {
            level.setBlock(pos.above(a), body.defaultBlockState(), Block.UPDATE_CLIENTS);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("item.arcaneresearch.eldritch_obelisk_placer.tooltip")
                .withStyle(ChatFormatting.ITALIC));
    }
}