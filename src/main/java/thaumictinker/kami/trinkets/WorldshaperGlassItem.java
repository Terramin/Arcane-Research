package thaumictinker.kami.trinkets;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import arcaneresearch.init.ModSounds;

 



 
public class WorldshaperGlassItem extends Item {

    public static final int REACH = 5;
    public static final int DEFAULT_SIZE = 3;
    public static final int MAX_SIZE = 11;

    public WorldshaperGlassItem(Properties properties) {
        super(properties);
    }

    public static Item getItem(ItemStack stack) {
        return BlockStorage.resolve(TrinketsFeature.mirrorBlock(stack));
    }

    public static int getSize(ItemStack stack) {
        return TrinketsFeature.mirrorSize(stack, DEFAULT_SIZE) | 1;
    }

    private static void setSize(ItemStack stack, int size) {
        TrinketsFeature.setMirrorSize(stack, size | 1);
    }

    public static List<BlockPos> getBlocksToPlace(ItemStack stack, Player player) {
        List<BlockPos> coordinates = new java.util.ArrayList<>();
        Level level = player.level();
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(REACH));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE,
                ClipContext.Fluid.SOURCE_ONLY, player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return coordinates;
        }
        BlockPos base = hit.getBlockPos();
        if (level.getBlockState(base).canBeReplaced()) {
            base = base.below();
        }
        Direction direction = hit.getDirection();
        int rotation = Mth.floor(player.getYRot() * 4.0F / 360.0F + 0.5D) & 3;
        int range = (getSize(stack) ^ 1) / 2;
        float pitch = player.getXRot();

        boolean topOrBottom = direction == Direction.UP || direction == Direction.DOWN;
        boolean lookingDown = pitch > 75;

        int xOffset = direction.getAxis() != Direction.Axis.X
                ? topOrBottom ? lookingDown || (rotation & 1) == 0 ? range : 0 : range : 0;
        int yOffset = topOrBottom ? lookingDown ? 0 : range : range;
        int zOffset = direction.getAxis() != Direction.Axis.Z
                ? topOrBottom ? lookingDown || (rotation & 1) == 1 ? range : 0 : range : 0;

        for (int x = -xOffset; x < xOffset + 1; x++) {
            for (int y = 0; y < yOffset * 2 + 1; y++) {
                for (int z = -zOffset; z < zOffset + 1; z++) {
                    BlockPos target = base.offset(x + direction.getStepX(),
                            y + direction.getStepY(), z + direction.getStepZ());
                    BlockState state = level.getBlockState(target);
                    if (state.isAir() || state.canBeReplaced()) {
                        coordinates.add(target);
                    }
                }
            }
        }
        return coordinates;
    }

    public static boolean hasBlocks(ItemStack stack, Player player, int required) {
        Item item = getItem(stack);
        if (item == Items.AIR) {
            return false;
        }
        if (player.isCreative()) {
            return true;
        }
        return BlockStorage.count(player, item) >= required;
    }

    public static boolean hasBlocks(ItemStack stack, Player player, List<BlockPos> blocks) {
        return hasBlocks(stack, player, blocks.size());
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            BlockState state = level.getBlockState(context.getClickedPos());
            Item blockItem = state.getBlock().asItem();
            if (state.getRenderShape() == RenderShape.MODEL && !state.hasBlockEntity() && blockItem != Items.AIR) {
                TrinketsFeature.setMirrorBlock(stack, BlockStorage.idOf(blockItem));
            }
        } else {
            placeAllBlocks(stack, player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                int size = getSize(stack);
                int newSize = size == MAX_SIZE ? DEFAULT_SIZE : size + 2;
                setSize(stack, newSize);
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.displayClientMessage(Component.literal(newSize + " x " + newSize), true);
                }
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F, 0.1F);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    public static int placeAllBlocks(ItemStack stack, Player player) {
        Item item = getItem(stack);
        if (!(item instanceof BlockItem blockItem)) {
            return 0;
        }
        List<BlockPos> targets = getBlocksToPlace(stack, player);
        if (targets.isEmpty() || !hasBlocks(stack, player, targets)) {
            return 0;
        }
        Level level = player.level();
        BlockState state = blockItem.getBlock().defaultBlockState();
        int placed = 0;
        for (BlockPos target : targets) {
            if (!level.setBlock(target, state, 3)) {
                continue;
            }
            placed++;
            if (!player.isCreative()) {
                BlockStorage.consume(player, item, 1);
            }
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.WAND.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        return placed;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        int size = getSize(stack);
        tooltip.add(Component.literal(size + " x " + size));
        Item item = getItem(stack);
        if (item != Items.AIR) {
            tooltip.add(new ItemStack(item).getHoverName());
        }
    }
}
