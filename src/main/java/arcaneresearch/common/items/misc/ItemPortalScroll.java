package arcaneresearch.common.items.misc;

import arcaneresearch.api.items.IPortalDestination;
import arcaneresearch.api.portals.PortalApi;
import arcaneresearch.api.portals.PortalDestination;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemPortalScroll extends Item implements IPortalDestination {

    public ItemPortalScroll() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public boolean hasSavedPosition(@NotNull ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null
                && tag.contains(PortalApi.TAG_X)
                && tag.contains(PortalApi.TAG_Y)
                && tag.contains(PortalApi.TAG_Z);
    }

    @Override
    public @Nullable PortalDestination getSavedDestination(@NotNull ItemStack stack) {
        return PortalApi.readDestination(stack.getTag());
    }

    @Override
    public boolean bindPosition(@NotNull ItemStack stack, @NotNull Level level, @NotNull BlockPos pos) {
        PortalApi.writeDestination(stack.getOrCreateTag(), pos, level.dimension(),
                PortalDestination.DEFAULT_WIDTH, PortalDestination.DEFAULT_HEIGHT);
        return true;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return hasSavedPosition(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        PortalDestination destination = getSavedDestination(stack);
        if (destination != null) {
            BlockPos pos = destination.pos();
            ResourceKey<Level> dim = destination.dimension();
            tooltip.add(Component.translatable("key.arcaneresearch.scroll.teleport").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(": ").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]")
                            .withStyle(ChatFormatting.WHITE)));
            tooltip.add(Component.translatable("key.arcaneresearch.scroll.dimension").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(": ").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(dim.location().toString()).withStyle(ChatFormatting.AQUA)));
        } else {
            tooltip.add(Component.translatable("key.arcaneresearch.scroll.teleport").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(": ").withStyle(ChatFormatting.GOLD))
                    .append(Component.translatable("key.arcaneresearch.scroll.not_set").withStyle(ChatFormatting.GRAY)));
        }
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();

        if (player == null) return InteractionResult.PASS;

        if (!hasSavedPosition(stack)) {
            if (!level.isClientSide) {
                bindPosition(stack, level, clickedPos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide) {
            PortalDestination destination = getSavedDestination(stack);
            if (destination == null) return InteractionResult.FAIL;

            BlockPos spawnPos = clickedPos.relative(context.getClickedFace());
            float yaw = Mth.wrapDegrees(Math.round(player.getYRot() / 90f) * 90f);

            if (PortalApi.createPortal(level, spawnPos, yaw, destination) == null) {
                return InteractionResult.FAIL;
            }

            stack.shrink(1);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
