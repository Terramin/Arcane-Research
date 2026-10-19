package arcaneresearch.common.blocks.devices;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;
import arcaneresearch.api.ArcaneresearchInvHelper;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.common.tiles.devices.TileMirror;
import arcaneresearch.common.tiles.devices.TileMirrorEssentia;
import arcaneresearch.init.ModSounds;

import javax.annotation.Nullable;

public class MirrorBlockItem extends BlockItem {

    public MirrorBlockItem(Block block, net.minecraft.world.item.Item.Properties properties) {
        super(block, properties.rarity(Rarity.UNCOMMON).stacksTo(1));
    }

    private static void restoreLinkFromStack(@Nullable TileMirror mirror,
                                             @Nullable TileMirrorEssentia essentia,
                                             ItemStack stack, Level level) {
        if (!ArcaneresearchInvHelper.hasCustomTag(stack) || level.isClientSide()) {
            return;
        }
        if (ArcaneresearchInvHelper.getCustomTag(stack).contains("linkX")) {
            int lx = ArcaneresearchInvHelper.getCustomTag(stack).getIntOr("linkX", 0);
            int ly = ArcaneresearchInvHelper.getCustomTag(stack).getIntOr("linkY", 0);
            int lz = ArcaneresearchInvHelper.getCustomTag(stack).getIntOr("linkZ", 0);
            ResourceKey<Level> dim = level.dimension();
            if (ArcaneresearchInvHelper.getCustomTag(stack).contains("linkDim")) {
                try {
                    dim = ResourceKey.create(Registries.DIMENSION,
                            Identifier.parse(ArcaneresearchInvHelper.getCustomTag(stack).getStringOr("linkDim", "")));
                } catch (Exception ignored) {
                }
            }
            if (mirror != null) {
                mirror.linkX = lx;
                mirror.linkY = ly;
                mirror.linkZ = lz;
                mirror.linkDimension = dim;
                mirror.restoreLink();
            } else if (essentia != null) {
                essentia.linkX = lx;
                essentia.linkY = ly;
                essentia.linkZ = lz;
                essentia.linkDimension = dim;
                essentia.restoreLink();
            }
        }
    }

    // NEOFORGE_PORT: Item.getRarity(ItemStack) override removed in 26.x ->
    // rarity is set via Properties in the constructor (UNCOMMON, Forge parity).

    // NEOFORGE_PORT: appendHoverText signature changed in 26.x
    // (Level+List) -> (TooltipContext, TooltipDisplay, Consumer<Component>).
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        if (ArcaneresearchInvHelper.hasCustomTag(stack)
                && ArcaneresearchInvHelper.getCustomTag(stack).contains("linkX")) {
            int lx = ArcaneresearchInvHelper.getCustomTag(stack).getIntOr("linkX", 0);
            int ly = ArcaneresearchInvHelper.getCustomTag(stack).getIntOr("linkY", 0);
            int lz = ArcaneresearchInvHelper.getCustomTag(stack).getIntOr("linkZ", 0);
            String dim = ArcaneresearchInvHelper.getCustomTag(stack).contains("linkDim")
                    ? ArcaneresearchInvHelper.getCustomTag(stack).getStringOr("linkDim", "") : "?";
            tooltip.accept(Component.translatable("tc.mirror.linked_to",
                            lx + "," + ly + "," + lz + " in " + dim)
                    .withStyle(style -> style.withColor(0x808080)));
        }
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();

        if (player != null && tryCopyLink(level, pos, player, hand, stack)) {
            if (level.isClientSide()) {
                player.swing(hand, net.minecraft.world.item.component.SwingAnimation.DEFAULT, false);
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        return super.useOn(context);
    }

    private boolean tryCopyLink(Level level, BlockPos pos, Player player,
                                InteractionHand hand, ItemStack held) {
        BlockState clicked = level.getBlockState(pos);
        boolean clickedItemMirror = clicked.getBlock() instanceof BlockMirror mirror
                && mirror.getMirrorType() == BlockMirror.MirrorType.ITEM;
        boolean clickedEssentiaMirror = clicked.getBlock() instanceof BlockMirror mirror2
                && mirror2.getMirrorType() == BlockMirror.MirrorType.PLAYER;

        boolean heldItemMirror = getBlock() instanceof BlockMirror heldBlock
                && heldBlock.getMirrorType() == BlockMirror.MirrorType.ITEM;
        boolean heldEssentiaMirror = getBlock() instanceof BlockMirror heldBlock2
                && heldBlock2.getMirrorType() == BlockMirror.MirrorType.PLAYER;

        if (!((clickedItemMirror && heldItemMirror) || (clickedEssentiaMirror && heldEssentiaMirror))) {
            return false;
        }
        if (level.isClientSide()) {
            return true;
        }
        BlockEntity te = level.getBlockEntity(pos);
        boolean sameType = (te instanceof TileMirror && heldItemMirror)
                || (te instanceof TileMirrorEssentia && heldEssentiaMirror);
        if (!sameType) {
            return true;
        }
        boolean linkValid = (te instanceof TileMirror tm)
                ? tm.isLinkValid()
                : ((TileMirrorEssentia) te).isLinkValid();
        if (!linkValid) {
            ItemStack copy = held.copy();
            copy.setCount(1);
            // NEOFORGE_PORT: stack root tag -> CUSTOM_DATA (same keys).
            net.minecraft.nbt.CompoundTag linkTag =
                    arcaneresearch.api.ArcaneresearchInvHelper.getOrCreateCustomTag(copy);
            linkTag.putInt("linkX", pos.getX());
            linkTag.putInt("linkY", pos.getY());
            linkTag.putInt("linkZ", pos.getZ());
            linkTag.putString("linkDim", level.dimension().identifier().toString());
            arcaneresearch.api.ArcaneresearchInvHelper.setCustomTag(copy, linkTag);
            level.playSound(null, pos, ModSounds.JAR.get(), SoundSource.BLOCKS, 1.0f, 2.0f);
            if (!player.getInventory().add(copy)) {
                ItemEntity entityItem = new ItemEntity(level,
                        player.getX(), player.getY(), player.getZ(), copy);
                level.addFreshEntity(entityItem);
            }
            if (!player.isCreative()) {
                held.shrink(1);
            }
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.inventoryMenu.broadcastChanges();
            }
        } else {
            player.sendSystemMessage(Component.translatable("tc.mirror.already_linked"));
        }
        return true;
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {

        if (!super.placeBlock(context, state)) {
            return false;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.isClientSide()) {
            return true;
        }

        BlockEntity placed = level.getBlockEntity(pos);
        if (placed instanceof TileMirror tile) {
            restoreLinkFromStack(tile, null, context.getItemInHand(), level);
            tile.setChanged();
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
        } else if (placed instanceof TileMirrorEssentia essentia) {
            restoreLinkFromStack(null, essentia, context.getItemInHand(), level);
            essentia.setChanged();
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
        }
        return true;
    }
}
