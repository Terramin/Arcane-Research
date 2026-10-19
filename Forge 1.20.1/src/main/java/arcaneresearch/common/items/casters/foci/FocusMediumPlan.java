package arcaneresearch.common.items.casters.foci;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.casters.FocusMedium;
import arcaneresearch.api.casters.ICaster;
import arcaneresearch.api.casters.NodeSetting;
import arcaneresearch.api.casters.Trajectory;
import arcaneresearch.api.items.IArchitect;
import arcaneresearch.common.items.casters.CasterManager;
import java.util.*;
public class FocusMediumPlan extends FocusMedium implements IArchitect {
    public static final int METHOD_FULL = 0;
    public static final int METHOD_SURFACE = 1;
    private final Set<BlockPos> checked = new HashSet<>();
    @Override
    public String getResearch() {
        return "FOCUSPLAN";
    }
    @Override
    public String getKey() {
        return "arcaneresearch.PLAN";
    }
    @Override
    public int getComplexity() {
        return 4;
    }
    @Override
    public Aspect getAspect() {
        return Aspect.CRAFT;
    }
    @Override
    public EnumSupplyType[] mustBeSupplied() {
        return new EnumSupplyType[]{EnumSupplyType.TRAJECTORY};
    }
    @Override
    public EnumSupplyType[] willSupply() {
        return new EnumSupplyType[]{EnumSupplyType.TARGET};
    }
    @Override
    public HitResult[] supplyTargets() {
        if (getParent() == null || getPackage() == null || getPackage().world == null) {
            return new HitResult[0];
        }
        Player player = getCasterPlayer();
        if (player == null) {
            return new HitResult[0];
        }
        Level world = getPackage().world;
        List<HitResult> targets = new ArrayList<>();
        Trajectory[] parentTrajectories = getParent().supplyTrajectories();
        if (parentTrajectories == null) {
            return new HitResult[0];
        }
        for (Trajectory trajectory : parentTrajectories) {
            Vec3 start = trajectory.source;
            Vec3 direction = trajectory.direction.normalize();
            Vec3 end = start.add(direction.scale(16.0));
            BlockHitResult blockHit = world.clip(new ClipContext(
                    start, end,
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    player));
            if (blockHit.getType() == HitResult.Type.BLOCK) {
                List<BlockPos> affectedBlocks = getAffectedBlocks(world, blockHit.getBlockPos(),
                        blockHit.getDirection(), player);
                BlockPos hitPos = blockHit.getBlockPos();
                affectedBlocks.sort(Comparator.comparingDouble(pos -> pos.distSqr(hitPos)));
                for (BlockPos pos : affectedBlocks) {
                    targets.add(new BlockHitResult(
                            Vec3.atCenterOf(pos),
                            blockHit.getDirection(),
                            pos,
                            false));
                }
            }
        }
        return targets.toArray(new HitResult[0]);
    }
    @Override
    public NodeSetting[] createSettings() {
        int[] method = {METHOD_FULL, METHOD_SURFACE};
        String[] methodDesc = {"focus.plan.full", "focus.plan.surface"};
        return new NodeSetting[]{
                new NodeSetting("method", "focus.plan.method",
                        new NodeSetting.NodeSettingIntList(method, methodDesc))
        };
    }
    @Override
    public boolean isExclusive() {
        return true;
    }
    private List<BlockPos> getAffectedBlocks(Level world, BlockPos hitPos, Direction side, Player player) {
        return getAffectedBlocks(world, hitPos, side, getCasterStack(player));
    }
    private List<BlockPos> getAffectedBlocks(Level world, BlockPos hitPos, Direction side, ItemStack casterStack) {
        List<BlockPos> result = new ArrayList<>();
        checked.clear();
        if (casterStack.isEmpty()) {
            return result;
        }
        int sizeX = CasterManager.getAreaX(casterStack);
        int sizeY = CasterManager.getAreaY(casterStack);
        int sizeZ = CasterManager.getAreaZ(casterStack);
        if (getSettingValue("method") == METHOD_FULL) {
            checkNeighboursFull(world, hitPos, hitPos, side, sizeX, sizeY, sizeZ, result);
        } else {
            BlockState targetState = world.getBlockState(hitPos);
            if (side.getAxis() == Direction.Axis.Z) {
                checkNeighboursSurface(world, hitPos, targetState, hitPos, side, sizeZ, sizeY, sizeX, result);
            } else {
                checkNeighboursSurface(world, hitPos, targetState, hitPos, side, sizeX, sizeY, sizeZ, result);
            }
        }
        return result;
    }
    private void checkNeighboursFull(Level world, BlockPos origin, BlockPos current, Direction side,
                                     int sizeX, int sizeY, int sizeZ, List<BlockPos> result) {
        if (checked.contains(current)) {
            return;
        }
        checked.add(current);
        if (!world.getBlockState(current).isAir()) {
            result.add(current);
        }
        int xs = origin.getX() - sizeX - sizeX * side.getStepX();
        int xe = origin.getX() + sizeX - sizeX * side.getStepX();
        int ys = origin.getY() - sizeY - sizeY * side.getStepY();
        int ye = origin.getY() + sizeY - sizeY * side.getStepY();
        int zs = origin.getZ() - sizeZ - sizeZ * side.getStepZ();
        int ze = origin.getZ() + sizeZ - sizeZ * side.getStepZ();
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = current.relative(dir);
            if (neighbor.getX() >= xs && neighbor.getX() <= xe &&
                    neighbor.getY() >= ys && neighbor.getY() <= ye &&
                    neighbor.getZ() >= zs && neighbor.getZ() <= ze) {
                checkNeighboursFull(world, origin, neighbor, side, sizeX, sizeY, sizeZ, result);
            }
        }
    }
    private void checkNeighboursSurface(Level world, BlockPos origin, BlockState targetState, BlockPos current,
                                        Direction side, int sizeX, int sizeY, int sizeZ, List<BlockPos> result) {
        if (checked.contains(current)) {
            return;
        }
        checked.add(current);
        switch (side.getAxis()) {
            case Y:
                if (Math.abs(current.getX() - origin.getX()) > sizeX) return;
                if (Math.abs(current.getZ() - origin.getZ()) > sizeZ) return;
                break;
            case Z:
                if (Math.abs(current.getX() - origin.getX()) > sizeX) return;
                if (Math.abs(current.getY() - origin.getY()) > sizeZ) return;
                break;
            case X:
                if (Math.abs(current.getY() - origin.getY()) > sizeX) return;
                if (Math.abs(current.getZ() - origin.getZ()) > sizeZ) return;
                break;
        }
        BlockState currentState = world.getBlockState(current);
        if (currentState == targetState &&
                !currentState.isAir() &&
                arcaneresearch.common.lib.utils.BlockUtils.isBlockExposed(world, current)) {
            result.add(current);
            for (Direction dir : Direction.values()) {
                if (dir != side && dir != side.getOpposite()) {
                    checkNeighboursSurface(world, origin, targetState, current.relative(dir),
                            side, sizeX, sizeY, sizeZ, result);
                }
            }
        }
    }
    private ItemStack getCasterStack(Player player) {
        if (player == null) {
            return ItemStack.EMPTY;
        }
        ItemStack mainhand = player.getMainHandItem();
        if (!mainhand.isEmpty() && mainhand.getItem() instanceof ICaster) {
            return mainhand;
        }
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && offhand.getItem() instanceof ICaster) {
            return offhand;
        }
        return ItemStack.EMPTY;
    }
    private Player getCasterPlayer() {
        if (getPackage() == null || getPackage().getCasterUUID() == null) {
            return null;
        }
        if (getPackage().world != null) {
            for (Player player : getPackage().world.players()) {
                if (player.getUUID().equals(getPackage().getCasterUUID())) {
                    return player;
                }
            }
        }
        return null;
    }
    @Override
    public BlockHitResult getArchitectMOP(ItemStack stack, Level world, LivingEntity player) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(16.0));
        return world.clip(new ClipContext(start, end,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }
    @Override
    public boolean useBlockHighlight(ItemStack stack) {
        return false;
    }
    @Override
    public boolean showAxis(ItemStack stack, Level world, Player player, Direction side,
                            IArchitect.EnumAxis axis) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        int dim = CasterManager.getAreaDim(stack);
        if (getSettingValue("method") == METHOD_FULL) {
            switch (axis) {
                case Y:
                    return dim == 0 || dim == 3;
                case Z:
                    return dim == 0 || dim == 2;
                case X:
                    return dim == 0 || dim == 1;
                default:
                    return false;
            }
        } else {
            switch (side.getAxis()) {
                case Y:
                    return (axis == IArchitect.EnumAxis.X && (dim == 0 || dim == 1))
                            || (axis == IArchitect.EnumAxis.Z && (dim == 0 || dim == 2));
                case Z:
                    return (axis == IArchitect.EnumAxis.Y && (dim == 0 || dim == 1))
                            || (axis == IArchitect.EnumAxis.X && (dim == 0 || dim == 2));
                case X:
                    return (axis == IArchitect.EnumAxis.Y && (dim == 0 || dim == 1))
                            || (axis == IArchitect.EnumAxis.Z && (dim == 0 || dim == 2));
                default:
                    return false;
            }
        }
    }
    @Override
    public ArrayList<BlockPos> getArchitectBlocks(ItemStack stack, Level world, BlockPos pos,
                                                  Direction side, Player player) {
        return new ArrayList<>(getAffectedBlocks(world, pos, side, stack));
    }
}
