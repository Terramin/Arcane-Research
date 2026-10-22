package arcaneresearch.common.lib.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BlockUtils {

    public static ArrayList<String> portableHoleBlackList = new ArrayList<>();
    private static BlockPos lastPos = BlockPos.ZERO;
    private static double lastDistance = 0.0;

    private static boolean removeBlock(Level level, Player player, BlockPos pos, boolean canHarvest) {
        BlockState state = level.getBlockState(pos);
        
        
        
        
        
        state.getBlock().playerWillDestroy(level, pos, state, player);
        boolean removed = state.onDestroyedByPlayer(level, pos, player, player.getMainHandItem().copy(),
                canHarvest, level.getFluidState(pos));
        if (removed) {
            state.getBlock().destroy(level, pos, state);
        }
        return removed;
    }

    
    
    
    
    private static boolean suppressDropXp = false;

    @net.neoforged.fml.common.EventBusSubscriber(modid = arcaneresearch.Arcaneresearch.MODID)
    public static final class HarvestDropXpSuppressor {
        private HarvestDropXpSuppressor() {
        }

        @net.neoforged.bus.api.SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST)
        public static void onBlockDrops(net.neoforged.neoforge.event.level.BlockDropsEvent event) {
            if (suppressDropXp) {
                event.setDroppedExperience(0);
            }
        }
    }

    public static boolean harvestBlockSkipCheck(Level level, Player player, BlockPos pos) {
        return harvestBlock(level, player, pos, false, false, 0, true);
    }

    public static boolean harvestBlock(Level level, Player player, BlockPos pos) {
        return harvestBlock(level, player, pos, false, false, 0, false);
    }

    public static boolean harvestBlock(Level level, Player player, BlockPos pos,
                                       boolean alwaysDrop, boolean silkOverride, int fortuneOverride, boolean skipEvent) {

        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)
                || !(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        
        
        
        
        
        int exp = 0;
        if (!skipEvent) {
            BlockState eventState = level.getBlockState(pos);
            var breakEvent = net.neoforged.neoforge.common.CommonHooks.fireBlockBreak(
                    level, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer, pos, eventState);
            if (breakEvent.isCanceled()) {
                return false;
            }
            if (eventState.canHarvestBlock(level, pos, player)) {
                ItemStack held = player.getMainHandItem();
                exp = net.minecraft.world.item.enchantment.EnchantmentHelper.processBlockExperience(
                        serverLevel, held,
                        eventState.getExpDrop(level, pos, level.getBlockEntity(pos), player, held));
            }
        }

        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        Block block = state.getBlock();

        if ((block instanceof CommandBlock || block instanceof StructureBlock) && !serverPlayer.canUseGameMasterBlocks()) {
            level.sendBlockUpdated(pos, state, state, 3);
            return false;
        }

        level.levelEvent(null, 2001, pos, Block.getId(state));

        boolean removed;
        if (serverPlayer.isCreative()) {
            removed = removeBlock(level, player, pos, false);
            serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(level, pos));
        } else {
            ItemStack tool = player.getMainHandItem();
            boolean canHarvest = alwaysDrop || state.canHarvestBlock(level, pos, player);

            removed = removeBlock(level, player, pos, canHarvest);

            if (removed && canHarvest) {

                ItemStack effectiveTool = tool;

                
                var enchantments = level.registryAccess()
                        .lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
                var fortuneHolder = enchantments.getOrThrow(
                        net.minecraft.world.item.enchantment.Enchantments.FORTUNE);
                var silkHolder = enchantments.getOrThrow(
                        net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH);
                int currentFortune = net.minecraft.world.item.enchantment.EnchantmentHelper
                        .getItemEnchantmentLevel(fortuneHolder, tool);
                boolean hasSilk = net.minecraft.world.item.enchantment.EnchantmentHelper
                        .getItemEnchantmentLevel(silkHolder, tool) > 0;

                if (silkOverride || fortuneOverride > currentFortune) {
                    effectiveTool = tool.copy();

                    if (silkOverride && !hasSilk) {
                        effectiveTool.enchant(silkHolder, 1);
                    }

                    if (fortuneOverride > currentFortune) {

                        effectiveTool.enchant(fortuneHolder, fortuneOverride);
                    }
                }

                
                
                
                suppressDropXp = true;
                try {
                    block.playerDestroy(serverLevel, serverPlayer, pos, state, blockEntity, effectiveTool);
                } finally {
                    suppressDropXp = false;
                }
            }
        }

        if (!serverPlayer.isCreative() && removed && exp > 0) {
            state.getBlock().popExperience(serverLevel, pos, exp);
        }

        return removed;
    }

    public static boolean breakBlock(Level level, BlockPos pos, ItemStack tool, @Nullable Player player) {
        if (level.isClientSide()) return false;

        BlockState state = level.getBlockState(pos);
        if (state.isAir() || !isBreakable(level, pos)) {
            return false;
        }

        BlockEntity be = level.getBlockEntity(pos);

        if (level instanceof ServerLevel serverLevel) {
            Block.dropResources(state, level, pos, be, player, tool);
        }

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        return true;
    }

    public static boolean breakBlockNoDrops(Level level, BlockPos pos) {
        if (level.isClientSide()) return false;

        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return false;

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        return true;
    }

    private static void findBlocks(Level level, BlockPos origin, BlockState targetBlock, int reach) {
        for (int xx = -reach; xx <= reach; ++xx) {
            for (int yy = reach; yy >= -reach; --yy) {
                for (int zz = -reach; zz <= reach; ++zz) {

                    if (Math.abs(lastPos.getX() + xx - origin.getX()) > 24) return;
                    if (Math.abs(lastPos.getY() + yy - origin.getY()) > 48) return;
                    if (Math.abs(lastPos.getZ() + zz - origin.getZ()) > 24) return;

                    BlockPos checkPos = lastPos.offset(xx, yy, zz);
                    BlockState bs = level.getBlockState(checkPos);

                    boolean same = bs.is(targetBlock.getBlock());

                    if (same && bs.getDestroySpeed(level, checkPos) >= 0.0f) {
                        double xd = checkPos.getX() - origin.getX();
                        double yd = checkPos.getY() - origin.getY();
                        double zd = checkPos.getZ() - origin.getZ();
                        double d = xd * xd + yd * yd + zd * zd;

                        if (d > lastDistance) {
                            lastDistance = d;
                            lastPos = checkPos;
                            findBlocks(level, origin, targetBlock, reach);
                            return;
                        }
                    }
                }
            }
        }
    }

    public static boolean breakFurthestBlock(Level level, BlockPos pos, BlockState block, Player player) {
        lastPos = pos;
        lastDistance = 0.0;

        int reach = isLog(level, pos) ? 2 : 1;
        findBlocks(level, pos, block, reach);

        boolean worked = harvestBlockSkipCheck(level, player, lastPos);

        level.sendBlockUpdated(pos, block, block, 3);

        if (worked && isLog(level, pos)) {
            for (int xx = -3; xx <= 3; ++xx) {
                for (int yy = -3; yy <= 3; ++yy) {
                    for (int zz = -3; zz <= 3; ++zz) {
                        BlockPos updatePos = lastPos.offset(xx, yy, zz);
                        BlockState updateState = level.getBlockState(updatePos);
                        level.scheduleTick(updatePos, updateState.getBlock(), 50 + level.getRandom().nextInt(75));
                    }
                }
            }
        }

        return worked;
    }

    public static boolean isExposed(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (level.isEmptyBlock(pos.relative(dir))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBlockExposed(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighbor);
            if (!neighborState.isSolidRender()) {
                return true;
            }
        }
        return false;
    }

    public static int countExposedSides(Level level, BlockPos pos) {
        int count = 0;
        for (Direction dir : Direction.values()) {
            if (level.isEmptyBlock(pos.relative(dir))) {
                count++;
            }
        }
        return count;
    }

    public static boolean isAdjacentToSolidBlock(Level level, BlockPos pos) {
        for (Direction face : Direction.values()) {
            BlockPos neighbor = pos.relative(face);
            BlockState state = level.getBlockState(neighbor);
            if (state.isFaceSturdy(level, neighbor, face.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBlockTouching(Level level, BlockPos pos, BlockState targetState) {
        for (Direction face : Direction.values()) {
            if (level.getBlockState(pos.relative(face)) == targetState) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBlockTouching(Level level, BlockPos pos, Block targetBlock) {
        for (Direction face : Direction.values()) {
            if (level.getBlockState(pos.relative(face)).is(targetBlock)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static Direction getFaceBlockTouching(Level level, BlockPos pos, Block targetBlock) {
        for (Direction face : Direction.values()) {
            if (level.getBlockState(pos.relative(face)).is(targetBlock)) {
                return face;
            }
        }
        return null;
    }

    public static boolean isBreakable(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return false;
        if (state.getDestroySpeed(level, pos) < 0) return false;
        return true;
    }

    public static boolean isPlant(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        
        
        return block instanceof net.minecraft.world.level.block.VegetationBlock ||
                block instanceof net.minecraft.world.level.block.CropBlock ||
                block instanceof net.minecraft.world.level.block.StemBlock ||
                block instanceof net.minecraft.world.level.block.VineBlock ||
                block instanceof net.minecraft.world.level.block.SaplingBlock;
    }

    public static boolean isLog(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(BlockTags.LOGS);
    }

    public static boolean isLeaves(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(BlockTags.LEAVES);
    }

    public static boolean isOre(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(BlockTags.GOLD_ORES) ||
                state.is(BlockTags.IRON_ORES) ||
                state.is(BlockTags.COPPER_ORES) ||
                state.is(BlockItemTags.COAL_ORES.block()) ||
                state.is(BlockItemTags.REDSTONE_ORES.block()) ||
                state.is(BlockItemTags.LAPIS_ORES.block()) ||
                state.is(BlockItemTags.DIAMOND_ORES.block()) ||
                state.is(BlockItemTags.EMERALD_ORES.block());
    }

    public static Direction.Axis getBlockAxis(Level level, BlockPos pos) {
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
        String blockName = state.getBlock().builtInRegistryHolder().key().identifier().toString();
        String stateString = state.toString();

        for (String key : list) {
            String[] parts = key.split(";");

            if (parts[0].contains(":")) {

                if (!blockName.equals(parts[0])) {
                    continue;
                }

                if (parts.length <= 1) {
                    return true;
                }

                int matches = 0;
                for (int i = 1; i < parts.length; i++) {
                    if (stateString.contains(parts[i])) {
                        matches++;
                    }
                }

                if (matches == parts.length - 1) {
                    return true;
                }
            } else {

                var tag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, net.minecraft.resources.Identifier.parse(parts[0]));
                if (state.is(tag)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static ItemStack getSilkTouchDrop(net.minecraft.world.level.block.state.BlockState state,
                                             Level level, BlockPos pos) {
        ItemStack dropped = ItemStack.EMPTY;
        try {
            
            ItemStack clone = state.getBlock().getCloneItemStack(level, pos, state, false, null);
            if (clone != null && !clone.isEmpty()) {
                dropped = clone.copy();
            }
        } catch (Exception e) {

        }
        if (dropped.isEmpty()) {
            dropped = new ItemStack(state.getBlock());
        }
        return dropped;
    }

    public static double distanceSq(BlockPos b1, BlockPos b2) {
        double dx = b1.getX() - b2.getX();
        double dy = b1.getY() - b2.getY();
        double dz = b1.getZ() - b2.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    public static Direction getDirectionTo(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();

        int ax = Math.abs(dx);
        int ay = Math.abs(dy);
        int az = Math.abs(dz);

        if (ay >= ax && ay >= az) {
            return dy > 0 ? Direction.UP : Direction.DOWN;
        } else if (ax >= az) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        } else {
            return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }

    public static List<BlockPos> getBlocksInArea(BlockPos center, int radius) {
        List<BlockPos> positions = new ArrayList<>();
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
        List<BlockPos> positions = new ArrayList<>();
        int r = (int) Math.ceil(radius);
        double rSq = radius * radius;

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    if (x * x + y * y + z * z <= rSq) {
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
        Vec3 sourceVec = new Vec3(source.getX() + 0.5, source.getY() + 0.5, source.getZ() + 0.5);
        Vec3 targetVec = new Vec3(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);

        BlockHitResult result = level.clip(new ClipContext(
                sourceVec, targetVec,
                
                
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                net.minecraft.world.phys.shapes.CollisionContext.empty()));

        if (result.getType() == HitResult.Type.MISS) {
            return true;
        }

        return result.getBlockPos().equals(target);
    }

    public static BlockHitResult getTargetBlock(Level level, Entity entity, boolean stopOnLiquid) {
        return getTargetBlock(level, entity, stopOnLiquid, stopOnLiquid, 10.0);
    }

    public static BlockHitResult getTargetBlock(Level level, Entity entity,
                                                boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, double range) {

        Vec3 eyePos = entity.getEyePosition();
        Vec3 lookVec = entity.getViewVector(1.0f);
        Vec3 endPos = eyePos.add(lookVec.scale(range));

        ClipContext.Fluid fluidMode = stopOnLiquid ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE;
        ClipContext.Block blockMode = ignoreBlockWithoutBoundingBox ?
                ClipContext.Block.COLLIDER : ClipContext.Block.OUTLINE;

        return level.clip(new ClipContext(eyePos, endPos, blockMode, fluidMode, entity));
    }

    public static boolean placeBlock(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return false;

        BlockState existing = level.getBlockState(pos);
        if (!existing.canBeReplaced()) {
            return false;
        }

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
            if (a.equals(b)) {
                return 0;
            }
            double da = distanceSq(source, a);
            double db = distanceSq(source, b);
            return Double.compare(da, db);
        }
    }
}
