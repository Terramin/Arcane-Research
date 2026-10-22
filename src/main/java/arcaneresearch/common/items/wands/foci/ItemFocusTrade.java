package arcaneresearch.common.items.wands.foci;

import net.minecraft.nbt.NbtOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
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
import arcaneresearch.api.ArcaneresearchInvHelper;
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

public class ItemFocusTrade extends ItemFocusBasic implements IArchitect, IFocusPicker {
    private static final AspectList COST = new AspectList().add(Aspect.ENTROPY, 1).add(Aspect.EARTH, 1)
            .add(Aspect.ORDER, 1);

    ArrayList<BlockPos> checked = new ArrayList<>();

    public ItemFocusTrade(net.minecraft.world.item.Item.Properties props) {
        super("equal_trade", 8747923, props);
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
            
            reach = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_INTERACTION_RANGE);
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
        BlockHitResult mop = hit instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK
                ? blockHit
                : null;
        if (mop == null) {
            return false;
        }
        BlockState clicked = level.getBlockState(mop.getBlockPos());
        if (player.isCrouching()) {
            if (!level.isClientSide() && level.getBlockEntity(mop.getBlockPos()) == null) {
                ItemStack picked = new ItemStack(clicked.getBlock());
                try {
                    if (!clicked.is(Blocks.BEDROCK)) {
                        ItemStack stacked = createStackedBlock(clicked);
                        if (stacked != null && !stacked.isEmpty()) {
                            picked = stacked.copy();
                        }
                    }
                } catch (Exception ignored) {
                }
                storePickedBlock(wandstack, picked);
            }
        } else {
            BlockHitResult target = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
            ItemStack picked = getPickedBlock(wandstack);
            if (!picked.isEmpty() && level.isClientSide()) {
                return true;
            } else if (!picked.isEmpty() && target.getType() == HitResult.Type.BLOCK
                    && level.getBlockEntity(target.getBlockPos()) == null) {
                boolean silk = isUpgradedWith(wand.getFocusStack(wandstack), FocusUpgradeType.silktouch);
                int fortune = wand.getFocusTreasure(wandstack);

                if (isUpgradedWith(wand.getFocusStack(wandstack), FocusUpgradeType.architect)) {
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
        }
        return false;
    }

    
    
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        if (entity.level().isClientSide() || !(entity instanceof Player player)) {
            return false;
        }
        ItemStack picked = getPickedBlock(stack);
        BlockHitResult mop = getMovingObjectPositionFromPlayer(entity.level(), player);
        if (mop != null && !picked.isEmpty() && entity.level().getBlockEntity(mop.getBlockPos()) == null) {
            ServerEvents.addSwapper(entity.level(), mop.getBlockPos(),
                    entity.level().getBlockState(mop.getBlockPos()), picked, true, 0, player,
                    true, false, 8038177, true, false, 0,
                    ServerEvents.DEFAULT_PREDICATE, 0.0F);
        }
        return false;
    }

    public void storePickedBlock(ItemStack stack, ItemStack picked) {
        if (!stack.isEmpty() && picked != null && !picked.isEmpty()) {
            
            
            CompoundTag root = ArcaneresearchInvHelper.getOrCreateCustomTag(stack);
            ItemStack.CODEC.encodeStart(arcaneresearch.common.lib.utils.RegistryNbt.ops(), picked.copy()).result().ifPresent(encoded -> {
                root.put("picked", encoded);
            });
            ArcaneresearchInvHelper.setCustomTag(stack, root);
        }
    }

    @Override
    public ItemStack getPickedBlock(ItemStack stack) {
        CompoundTag root = ArcaneresearchInvHelper.getCustomTag(stack);
        if (root != null && root.contains("picked")) {
            ItemStack out = ItemStack.CODEC.parse(arcaneresearch.common.lib.utils.RegistryNbt.ops(), root.get("picked"))
                    .result().orElse(ItemStack.EMPTY);
            if (!out.isEmpty()) {
                return out;
            }
        }
        return ItemStack.EMPTY;
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
        checked.clear();
        if (side.getAxis() == Direction.Axis.Z) {
            checkNeighbours(level, pos, state, pos, side, WandManager.getAreaZ(stack), WandManager.getAreaY(stack),
                    WandManager.getAreaX(stack), out, player);
        } else {
            checkNeighbours(level, pos, state, pos, side, WandManager.getAreaX(stack), WandManager.getAreaY(stack),
                    WandManager.getAreaZ(stack), out, player);
        }
        return out;
    }

    public void checkNeighbours(Level level, BlockPos origin, BlockState state, BlockPos current, Direction side,
                                 int sizeX, int sizeY, int sizeZ, ArrayList<BlockPos> out, Player player) {
        if (checked.contains(current)) {
            return;
        }
        checked.add(current);
        switch (side.getAxis()) {
            case Y:
                if (Math.abs(current.getX() - origin.getX()) > sizeX) {
                    return;
                }
                if (Math.abs(current.getZ() - origin.getZ()) > sizeZ) {
                    return;
                }
                break;
            case Z:
                if (Math.abs(current.getX() - origin.getX()) > sizeX) {
                    return;
                }
                if (Math.abs(current.getY() - origin.getY()) > sizeZ) {
                    return;
                }
                break;
            case X:
                if (Math.abs(current.getY() - origin.getY()) > sizeX) {
                    return;
                }
                if (Math.abs(current.getZ() - origin.getZ()) > sizeZ) {
                    return;
                }
                break;
            default:
                break;
        }
        if (level.getBlockState(current) == state && BlockUtils.isBlockExposed(level, current)
                && !level.isEmptyBlock(current) && state.getDestroySpeed(level, current) >= 0.0F
                && player.mayUseItemAt(current, side, ItemStack.EMPTY)) {
            out.add(current);
        } else {
            return;
        }
        for (Direction dir : Direction.values()) {
            if (dir != side && dir.getOpposite() != side) {
                checkNeighbours(level, origin, state, current.relative(dir), side, sizeX, sizeY, sizeZ, out, player);
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
        switch (rank) {
            case 1:
            case 2:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge};
            case 3:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge,
                        FocusUpgradeType.treasure, FocusUpgradeType.architect};
            case 4:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge};
            case 5:
                return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge,
                        FocusUpgradeType.silktouch};
            default:
                break;
        }
        return null;
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
