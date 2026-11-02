package arcaneresearch.common.lib.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BlockUtils {

    private static final Direction[] DIRECTIONS = Direction.values();

    /** @deprecated используйте {@link #getPortableHoleBlackList()} — поле публичное только для обратной совместимости. */
    @Deprecated
    public static final ArrayList<String> portableHoleBlackList = new ArrayList<>();

    public static List<String> getPortableHoleBlackList() {
        return portableHoleBlackList;
    }

    private static boolean removeBlock(Level level, Player player, BlockPos pos, boolean canHarvest) {
        BlockState state = level.getBlockState(pos);
        boolean removed = state.onDestroyedByPlayer(level, pos, player, canHarvest, level.getFluidState(pos));
        if (removed) {
            state.getBlock().destroy(level, pos, state);
        }
        return removed;
    }

    public static boolean harvestBlockSkipCheck(Level level, Player player, BlockPos pos) {
        return harvestBlock(level, player, pos, false, false, 0, true);
    }

    public static boolean harvestBlock(Level level, Player player, BlockPos pos) {
        return harvestBlock(level, player, pos, false, false, 0, false);
    }

    public static boolean harvestBlock(Level level, Player player, BlockPos pos,
                                       boolean alwaysDrop, boolean silkOverride,
                                       int fortuneOverride, boolean skipEvent) {
        if (level == null || pos == null) return false;
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) return false;

        int exp = skipEvent
                ? 0
                : ForgeHooks.onBlockBreakEvent(level, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer, pos);
        if (exp == -1) return false;

        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        Block block = state.getBlock();

        if ((block instanceof CommandBlock || block instanceof StructureBlock)
                && !serverPlayer.canUseGameMasterBlocks()) {
            level.sendBlockUpdated(pos, state, state, 3);
            return false;
        }

        level.levelEvent(null, 2001, pos, Block.getId(state));

        boolean removed;
        if (serverPlayer.isCreative()) {
            removed = removeBlock(level, player, pos, false);
            serverPlayer.connection.send(
                    new net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(level, pos));
        } else {
            ItemStack tool = player.getMainHandItem();
            boolean canHarvest = alwaysDrop || state.canHarvestBlock(level, pos, player);
            removed = removeBlock(level, player, pos, canHarvest);

            if (removed && canHarvest) {
                ItemStack effectiveTool = tool;
                int currentFortune = tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
                boolean hasSilk = tool.getEnchantmentLevel(Enchantments.SILK_TOUCH) > 0;

                if (silkOverride || fortuneOverride > currentFortune) {
                    effectiveTool = tool.copy();
                    if (silkOverride && !hasSilk) {
                        effectiveTool.enchant(Enchantments.SILK_TOUCH, 1);
                    }
                    if (fortuneOverride > currentFortune) {
                        effectiveTool.enchant(Enchantments.BLOCK_FORTUNE, fortuneOverride);
                    }
                }
                block.playerDestroy(level, player, pos, state, blockEntity, effectiveTool);
            }
        }

        if (!serverPlayer.isCreative() && removed && exp > 0 && level instanceof ServerLevel serverLevel) {
            state.getBlock().popExperience(serverLevel, pos, exp);
        }
        return removed;
    }

    public static boolean breakBlock(Level level, BlockPos pos, ItemStack tool, @Nullable Player player) {
        if (level == null || pos == null || level.isClientSide) return false;

        BlockState state = level.getBlockState(pos);
        if (state.isAir() || !isBreakable(level, pos)) return false;

        BlockEntity be = level.getBlockEntity(pos);
        if (level instanceof ServerLevel) {
            Block.dropResources(state, level, pos, be, player, tool);
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        return true;
    }

    public static boolean breakBlockNoDrops(Level level, BlockPos pos) {
        if (level == null || pos == null || level.isClientSide) return false;

        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return false;

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        return true;
    }

    private static final class SearchState {
        BlockPos bestPos;
        double bestDist;
        SearchState(BlockPos pos, double dist) {
            this.bestPos = pos;
            this.bestDist = dist;
        }
    }

    private static void findBlocks(Level level, BlockPos origin, BlockState targetBlock,
                                   int reach, SearchState state) {
        for (int xx = -reach; xx <= reach; ++xx) {
            for (int yy = reach; yy >= -reach; --yy) {
                for (int zz = -reach; zz <= reach; ++zz) {
                    if (Math.abs(state.bestPos.getX() + xx - origin.getX()) > 24) return;
                    if (Math.abs(state.bestPos.getY() + yy - origin.getY()) > 48) return;
                    if (Math.abs(state.bestPos.getZ() + zz - origin.getZ()) > 24) return;

                    BlockPos checkPos = state.bestPos.offset(xx, yy, zz);
                    BlockState bs = level.getBlockState(checkPos);

                    if (bs.is(targetBlock.getBlock())
                            && bs.getDestroySpeed(level, checkPos) >= 0.0f) {
                        double xd = checkPos.getX() - origin.getX();
                        double yd = checkPos.getY() - origin.getY();
                        double zd = checkPos.getZ() - origin.getZ();
                        double d = xd * xd + yd * yd + zd * zd;
                        if (d > state.bestDist) {
                            state.bestDist = d;
                            state.bestPos = checkPos;
                            findBlocks(level, origin, targetBlock, reach, state);
                            return;
                        }
                    }
                }
            }
        }
    }

    public static boolean breakFurthestBlock(Level level, BlockPos pos, BlockState block, Player player) {
        if (level == null || pos == null || block == null || player == null) return false;

        SearchState state = new SearchState(pos, 0.0);
        int reach = isLog(level, pos) ? 2 : 1;
        findBlocks(level, pos, block, reach, state);

        boolean worked = harvestBlockSkipCheck(level, player, state.bestPos);
        level.sendBlockUpdated(pos, block, block, 3);

        if (worked && isLog(level, pos)) {
            for (int xx = -3; xx <= 3; ++xx) {
                for (int yy = -3; yy <= 3; ++yy) {
                    for (int zz = -3; zz <= 3; ++zz) {
                        BlockPos updatePos = state.bestPos.offset(xx, yy, zz);
                        BlockState updateState = level.getBlockState(updatePos);
                        level.scheduleTick(updatePos, updateState.getBlock(),
                                50 + level.random.nextInt(75));
                    }
                }
            }
        }
        return worked;
    }

    public static boolean isExposed(Level level, BlockPos pos) {
        if (level == null || pos == null) return false;
        for (Direction dir : DIRECTIONS) {
            if (level.isEmptyBlock(pos.relative(dir))) return true;
        }
        return false;
    }

    public static boolean isBlockExposed(Level level, BlockPos pos) {
        if (level == null || pos == null) return false;
        for (Direction dir : DIRECTIONS) {
            BlockPos neighbor = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighbor);
            if (!neighborState.isSolidRender(level, neighbor)) return true;
        }
        return false;
    }

    public static int countExposedSides(Level level, BlockPos pos) {
        if (level == null || pos == null) return 0;
        int count = 0;
        for (Direction dir : DIRECTIONS) {
            if (level.isEmptyBlock(pos.relative(dir))) count++;
        }
        return count;
    }

    public static boolean isAdjacentToSolidBlock(Level level, BlockPos pos) {
        if (level == null || pos == null) return false;
        for (Direction face : DIRECTIONS) {
            BlockPos neighbor = pos.relative(face);
            BlockState state = level.getBlockState(neighbor);
            if (state.isFaceSturdy(level, neighbor, face.getOpposite())) return true;
        }
        return false;
    }

    public static boolean isBlockTouching(Level level, BlockPos pos, BlockState targetState) {
        if (level == null || pos == null || targetState == null) return false;
        for (Direction face : DIRECTIONS) {
            if (level.getBlockState(pos.relative(face)).equals(targetState)) return true;
        }
        return false;
    }

    public static boolean isBlockTouching(Level level, BlockPos pos, Block targetBlock) {
        if (level == null || pos == null || targetBlock == null) return false;
        for (Direction face : DIRECTIONS) {
            if (level.getBlockState(pos.relative(face)).is(targetBlock)) return true;
        }
        return false;
    }

    @Nullable
    public static Direction getFaceBlockTouching(Level level, BlockPos pos, Block targetBlock) {
        if (level == null || pos == null || targetBlock == null) return null;
        for (Direction face : DIRECTIONS) {
            if (level.getBlockState(pos.relative(face)).is(targetBlock)) return face;
        }
        return null;
    }

    public static boolean isBreakable(Level level, BlockPos pos) {
        if (level == null || pos == null) return false;
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return false;
        return state.getDestroySpeed(level, pos) >= 0;
    }

    public static boolean isPlant(Level level, BlockPos pos) {
        if (level == null || pos == null) return false;
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        return block instanceof BushBlock
                || block instanceof SaplingBlock
                || block instanceof VineBlock;
    }

    public static boolean isLog(Level level, BlockPos pos) {
        if (level == null || pos == null) return false;
        return level.getBlockState(pos).is(BlockTags.LOGS);
    }

    public static boolean isLeaves(Level level, BlockPos pos) {
        if (level == null || pos == null) return false;
        return level.getBlockState(pos).is(BlockTags.LEAVES);
    }

    public static boolean isOre(Level level, BlockPos pos) {
        if (level == null || pos == null) return false;
        BlockState state = level.getBlockState(pos);
        return state.is(BlockTags.GOLD_ORES)
                || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.COPPER_ORES)
                || state.is(BlockTags.COAL_ORES)
                || state.is(BlockTags.REDSTONE_ORES)
                || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.DIAMOND_ORES)
                || state.is(BlockTags.EMERALD_ORES);
    }

    public static Direction.Axis getBlockAxis(Level level, BlockPos pos) {
        if (level == null || pos == null) return Direction.Axis.Y;
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(BlockStateProperties.AXIS)) {
            return state.getValue(BlockStateProperties.AXIS);
        }
        return Direction.Axis.Y;
    }

    public static boolean isPortableHoleBlackListed(BlockState state) {
        return isBlockListed(state, portableHoleBlackList);
    }

    public static boolean isBlockListed(BlockState state, List<String> list) {
        if (state == null || list == null || list.isEmpty()) return false;

        String blockName = state.getBlock().builtInRegistryHolder().key().location().toString();
        String stateString = state.toString();

        for (String key : list) {
            if (key == null || key.isEmpty()) continue;

            String[] parts = key.split(";");
            if (parts.length == 0) continue;

            if (parts[0].contains(":")) {
                if (!blockName.equals(parts[0])) continue;
                if (parts.length <= 1) return true;

                int matches = 0;
                for (int i = 1; i < parts.length; i++) {
                    if (stateString.contains(parts[i])) matches++;
                }
                if (matches == parts.length - 1) return true;
            } else {
                ResourceLocation rl = ResourceLocation.tryParse(parts[0]);
                if (rl == null) continue;
                if (state.is(BlockTags.create(rl))) return true;
            }
        }
        return false;
    }

    public static ItemStack getSilkTouchDrop(BlockState state, Level level, BlockPos pos) {
        if (state == null) return ItemStack.EMPTY;

        ItemStack dropped = ItemStack.EMPTY;
        try {
            ItemStack clone = state.getBlock().getCloneItemStack(level, pos, state);
            if (clone != null && !clone.isEmpty()) {
                dropped = clone.copy();
            }
        } catch (RuntimeException ignored) {
        }
        if (dropped.isEmpty()) {
            dropped = new ItemStack(state.getBlock());
        }
        return dropped;
    }

    public static double distanceSq(BlockPos b1, BlockPos b2) {
        if (b1 == null || b2 == null) return Double.MAX_VALUE;
        double dx = b1.getX() - b2.getX();
        double dy = b1.getY() - b2.getY();
        double dz = b1.getZ() - b2.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    @Nullable
    public static Direction getDirectionTo(BlockPos from, BlockPos to) {
        if (from == null || to == null) return null;

        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();
        if (dx == 0 && dy == 0 && dz == 0) return null;

        int ax = Math.abs(dx);
        int ay = Math.abs(dy);
        int az = Math.abs(dz);

        if (ay >= ax && ay >= az) return dy > 0 ? Direction.UP : Direction.DOWN;
        if (ax >= az) return dx > 0 ? Direction.EAST : Direction.WEST;
        return dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    public static List<BlockPos> getBlocksInArea(BlockPos center, int radius) {
        if (center == null || radius < 0) return new ArrayList<>(0);

        int side = 2 * radius + 1;
        List<BlockPos> positions = new ArrayList<>(side * side * side);
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    positions.add(center.offset(x, y, z));
                }
            }
        }
        return positions;
    }

    public static List<BlockPos> getBlocksInSphere(BlockPos center, double radius) {
        if (center == null || radius < 0) return new ArrayList<>(0);

        int r = (int) Math.ceil(radius);
        double rSq = radius * radius;
        int approxSize = (int) Math.ceil(4.0 / 3.0 * Math.PI * radius * radius * radius) + 1;
        List<BlockPos> positions = new ArrayList<>(approxSize);

        for (int x = -r; x <= r; x++) {
            int xSq = x * x;
            for (int y = -r; y <= r; y++) {
                int xySq = xSq + y * y;
                if (xySq > rSq) continue;
                for (int z = -r; z <= r; z++) {
                    if (xySq + z * z <= rSq) {
                        positions.add(center.offset(x, y, z));
                    }
                }
            }
        }
        return positions;
    }

    public static AABB getBlockAABB(BlockPos pos) {
        return new AABB(pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

    public static Vec3 getBlockCenter(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static boolean hasLOS(Level level, BlockPos source, BlockPos target) {
        if (level == null || source == null || target == null) return false;

        Vec3 sourceVec = new Vec3(source.getX() + 0.5, source.getY() + 0.5, source.getZ() + 0.5);
        Vec3 targetVec = new Vec3(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
        BlockHitResult result = level.clip(new ClipContext(
                sourceVec, targetVec,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));

        if (result.getType() == HitResult.Type.MISS) return true;
        return result.getBlockPos().equals(target);
    }

    public static BlockHitResult getTargetBlock(Level level, Entity entity, boolean stopOnLiquid) {
        return getTargetBlock(level, entity, stopOnLiquid, stopOnLiquid, 10.0);
    }

    public static BlockHitResult getTargetBlock(Level level, Entity entity,
                                                boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox,
                                                double range) {
        if (level == null || entity == null) return null;

        Vec3 eyePos = entity.getEyePosition();
        Vec3 lookVec = entity.getViewVector(1.0f);
        Vec3 endPos = eyePos.add(lookVec.scale(range));
        ClipContext.Fluid fluidMode = stopOnLiquid ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE;
        ClipContext.Block blockMode = ignoreBlockWithoutBoundingBox
                ? ClipContext.Block.COLLIDER
                : ClipContext.Block.OUTLINE;
        return level.clip(new ClipContext(eyePos, endPos, blockMode, fluidMode, entity));
    }

    public static boolean placeBlock(Level level, BlockPos pos, BlockState state) {
        if (level == null || pos == null || state == null || level.isClientSide) return false;

        BlockState existing = level.getBlockState(pos);
        if (!existing.canBeReplaced()) return false;

        level.setBlock(pos, state, Block.UPDATE_ALL);
        return true;
    }

    public static class BlockPosComparator implements Comparator<BlockPos> {

        private final BlockPos source;

        public BlockPosComparator(BlockPos source) {
            this.source = source;
        }

        @Override
        public int compare(BlockPos a, BlockPos b) {
            if (a == b) return 0;
            if (a == null) return 1;
            if (b == null) return -1;
            if (a.equals(b)) return 0;

            double da = distanceSq(source, a);
            double db = distanceSq(source, b);
            return Double.compare(da, db);
        }
    }
}