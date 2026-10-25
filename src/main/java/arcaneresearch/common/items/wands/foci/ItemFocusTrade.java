package arcaneresearch.common.items.wands.foci;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.items.IArchitect;
import arcaneresearch.api.wands.FocusUpgradeType;
import arcaneresearch.api.wands.IFocusPicker;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.api.wands.ItemFocusBasic;
import arcaneresearch.common.items.wands.WandManager;
import arcaneresearch.common.lib.events.ServerEvents;
import arcaneresearch.common.lib.utils.BlockUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemFocusTrade extends ItemFocusBasic implements IArchitect, IFocusPicker {

    private static final AspectList COST = new AspectList()
            .add(Aspect.ENTROPY, 1).add(Aspect.EARTH, 1).add(Aspect.ORDER, 1);
    private static final FocusUpgradeType[] RANK_1_2 = {
            FocusUpgradeType.frugal, FocusUpgradeType.enlarge};
    private static final FocusUpgradeType[] RANK_3 = {
            FocusUpgradeType.frugal, FocusUpgradeType.enlarge,
            FocusUpgradeType.treasure, FocusUpgradeType.architect};
    private static final FocusUpgradeType[] RANK_4 = {
            FocusUpgradeType.frugal, FocusUpgradeType.enlarge};
    private static final FocusUpgradeType[] RANK_5 = {
            FocusUpgradeType.frugal, FocusUpgradeType.enlarge, FocusUpgradeType.silktouch};

    public ItemFocusTrade() {
        super("equal_trade", 8747923);
    }

    private static ItemStack createStackedBlock(BlockState bs) {
        if (bs == null || bs.isAir()) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(bs.getBlock());
    }

    protected BlockHitResult getMovingObjectPositionFromPlayer(Level level, Player player) {
        double reach = 5.0D;
        try {
            reach = player.getAttributeValue(net.minecraftforge.common.ForgeMod.BLOCK_REACH.get());
        } catch (Exception ignored) {
        }
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.x * reach, look.y * reach, look.z * reach);
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.BLOCK ? hit : null;
    }

    @Override
    public AspectList getVisCost(ItemStack focusstack) {
        if (isUpgradedWith(focusstack, FocusUpgradeType.silktouch)) {
            AspectList silk = new AspectList().add(Aspect.AIR, 1).add(Aspect.FIRE, 1).add(Aspect.EARTH, 1)
                    .add(Aspect.WATER, 1).add(Aspect.ORDER, 1);
            silk.add(COST);
            return silk;
        }
        return COST.copy();
    }

    @Override
    public int getMaxAreaSize(ItemStack focusstack) {
        return 3 + getUpgradeLevel(focusstack, FocusUpgradeType.enlarge) * 2;
    }

    @Override
    public boolean onFocusActivation(ItemStack wandstack, Level level, LivingEntity entity, HitResult hit, int useCount) {
        if (!(wandstack.getItem() instanceof IWand wand) || !(entity instanceof Player player)) {
            return false;
        }
        if (!(hit instanceof BlockHitResult mop) || mop.getType() != HitResult.Type.BLOCK) {
            return false;
        }

        if (player.isCrouching()) {
            if (!level.isClientSide && level.getBlockEntity(mop.getBlockPos()) == null) {
                BlockState clicked = level.getBlockState(mop.getBlockPos());
                ItemStack picked = new ItemStack(clicked.getBlock());
                try {
                    if (!clicked.is(Blocks.BEDROCK)) {
                        ItemStack stacked = createStackedBlock(clicked);
                        if (!stacked.isEmpty()) {
                            picked = stacked.copy();
                        }
                    }
                } catch (Exception ignored) {
                }
                storePickedBlock(wandstack, picked);
            }
            return false;
        }

        BlockHitResult target = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        ItemStack picked = getPickedBlock(wandstack);
        if (picked.isEmpty()) {
            return false;
        }
        if (level.isClientSide) {
            return true;
        }
        if (target.getType() != HitResult.Type.BLOCK
                || level.getBlockEntity(target.getBlockPos()) != null) {
            return false;
        }

        ItemStack focusStack = wand.getFocusStack(wandstack);
        boolean silk = isUpgradedWith(focusStack, FocusUpgradeType.silktouch);
        int fortune = wand.getFocusTreasure(wandstack);

        if (isUpgradedWith(focusStack, FocusUpgradeType.architect)) {
            ArrayList<BlockPos> blocks = getArchitectBlocks(wandstack, level, target.getBlockPos(),
                    target.getDirection(), player);
            for (BlockPos targetPos : blocks) {
                ServerEvents.addSwapper(level, targetPos, level.getBlockState(targetPos), picked, true, 0,
                        player, true, false, 8038177, true, silk, fortune,
                        ServerEvents.DEFAULT_PREDICATE, 0.0F);
            }
        } else {
            ServerEvents.addSwapper(level, target.getBlockPos(),
                    level.getBlockState(target.getBlockPos()), picked, true,
                    3 + wand.getFocusEnlarge(wandstack), player, true, false, 8038177, true, silk, fortune,
                    ServerEvents.DEFAULT_PREDICATE, 0.0F);
        }
        return true;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        Level level = entity.level();
        if (level.isClientSide || !(entity instanceof Player player)) {
            return super.onEntitySwing(stack, entity);
        }
        ItemStack picked = getPickedBlock(stack);
        BlockHitResult mop = getMovingObjectPositionFromPlayer(level, player);
        if (mop != null && !picked.isEmpty() && level.getBlockEntity(mop.getBlockPos()) == null) {
            ServerEvents.addSwapper(level, mop.getBlockPos(),
                    level.getBlockState(mop.getBlockPos()), picked, true, 0, player,
                    true, false, 8038177, true, false, 0,
                    ServerEvents.DEFAULT_PREDICATE, 0.0F);
        }
        return super.onEntitySwing(stack, entity);
    }

    public void storePickedBlock(ItemStack stack, ItemStack picked) {
        if (stack.isEmpty()) {
            return;
        }
        if (picked == null || picked.isEmpty()) {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                tag.remove("picked");
            }
            return;
        }
        stack.getOrCreateTag().put("picked", picked.save(new CompoundTag()));
    }

    @Override
    public ItemStack getPickedBlock(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("picked")) {
            return ItemStack.EMPTY;
        }
        ItemStack out = ItemStack.of(tag.getCompound("picked"));
        return out.isEmpty() ? ItemStack.EMPTY : out;
    }

    @Override
    public ArrayList<BlockPos> getArchitectBlocks(ItemStack stack, Level level, BlockPos pos, Direction side,
                                                  Player player) {
        ArrayList<BlockPos> out = new ArrayList<>();
        if (!(stack.getItem() instanceof IWand wand)) {
            return out;
        }
        if (!isUpgradedWith(wand.getFocusStack(stack), FocusUpgradeType.architect)) {
            return out;
        }
        BlockState state = level.getBlockState(pos);
        int areaX = WandManager.getAreaX(stack);
        int areaY = WandManager.getAreaY(stack);
        int areaZ = WandManager.getAreaZ(stack);
        Set<BlockPos> checked = new HashSet<>();
        checkNeighbours(level, pos, state, pos, side, areaX, areaY, areaZ, out, checked, player);
        return out;
    }

    private void checkNeighbours(Level level, BlockPos origin, BlockState state, BlockPos current, Direction side,
                                 int areaX, int areaY, int areaZ,
                                 List<BlockPos> out, Set<BlockPos> checked, Player player) {
        if (!checked.add(current)) {
            return;
        }

        switch (side.getAxis()) {
            case Y:
                if (Math.abs(current.getX() - origin.getX()) > areaX) return;
                if (Math.abs(current.getZ() - origin.getZ()) > areaZ) return;
                break;
            case Z:
                if (Math.abs(current.getX() - origin.getX()) > areaX) return;
                if (Math.abs(current.getY() - origin.getY()) > areaY) return;
                break;
            case X:
                if (Math.abs(current.getY() - origin.getY()) > areaY) return;
                if (Math.abs(current.getZ() - origin.getZ()) > areaZ) return;
                break;
            default:
                break;
        }

        if (level.getBlockState(current) == state
                && BlockUtils.isBlockExposed(level, current)
                && !level.isEmptyBlock(current)
                && state.getDestroySpeed(level, current) >= 0.0F
                && player.mayUseItemAt(current, side, ItemStack.EMPTY)) {
            out.add(current);
        } else {
            return;
        }

        for (Direction dir : Direction.values()) {
            if (dir != side && dir.getOpposite() != side) {
                checkNeighbours(level, origin, state, current.relative(dir), side,
                        areaX, areaY, areaZ, out, checked, player);
            }
        }
    }

    @Override
    public boolean showAxis(ItemStack stack, Level level, Player player, Direction side, IArchitect.EnumAxis axis) {
        int dim = WandManager.getAreaDim(stack);
        switch (side.getAxis()) {
            case Y:
                if ((axis == IArchitect.EnumAxis.X && (dim == 0 || dim == 1))
                        || (axis == IArchitect.EnumAxis.Z && (dim == 0 || dim == 2))) {
                    return true;
                }
                break;
            case Z:
                if ((axis == IArchitect.EnumAxis.Y && (dim == 0 || dim == 1))
                        || (axis == IArchitect.EnumAxis.X && (dim == 0 || dim == 2))) {
                    return true;
                }
                break;
            case X:
                if ((axis == IArchitect.EnumAxis.Y && (dim == 0 || dim == 1))
                        || (axis == IArchitect.EnumAxis.Z && (dim == 0 || dim == 2))) {
                    return true;
                }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focusstack, int rank) {
        return switch (rank) {
            case 1, 2 -> RANK_1_2;
            case 3 -> RANK_3;
            case 4 -> RANK_4;
            case 5 -> RANK_5;
            default -> null;
        };
    }

    @Override
    public boolean onFocusBlockStartBreak(ItemStack wandstack, BlockPos pos, Player player) {
        return false;
    }

    @Override
    public BlockHitResult getArchitectMOP(ItemStack stack, Level level, LivingEntity entity) {
        return BlockUtils.getTargetBlock(level, entity, false);
    }

    @Override
    public boolean useBlockHighlight(ItemStack stack) {
        return true;
    }
}