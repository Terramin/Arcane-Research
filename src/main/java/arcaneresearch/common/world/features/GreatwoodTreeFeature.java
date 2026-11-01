package arcaneresearch.common.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import arcaneresearch.init.ModBlocks;

import java.util.ArrayList;
import java.util.List;

public class GreatwoodTreeFeature extends Feature<NoneFeatureConfiguration> {

    private static final int[] Y_OFFSETS = {-2, 2, -4, 4};
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final byte[] OTHER_COORD_PAIRS = {2, 0, 0, 1, 2, 1};
    private static final int SET_BLOCK_FLAGS = 2;

    private final int    leafDistanceLimit = 4;
    private final boolean forceSpiders;

    public GreatwoodTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        this(codec, false);
    }

    public GreatwoodTreeFeature(Codec<NoneFeatureConfiguration> codec, boolean forceSpiders) {
        super(codec);
        this.forceSpiders = forceSpiders;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();
        boolean spiders = forceSpiders || random.nextInt(8) == 0;

        if (generateTree(level, random, pos, spiders)) return true;
        for (int dy : Y_OFFSETS) {
            if (generateTree(level, random, pos.offset(0, dy, 0), spiders)) return true;
        }
        return false;
    }

    private boolean generateTree(WorldGenLevel level, RandomSource random, BlockPos pos, boolean spiders) {
        GenCtx ctx = new GenCtx(level, random, pos.getX(), pos.getY(), pos.getZ(),
                ModBlocks.GREATWOOD_LEAVES.get(),
                ModBlocks.GREATWOOD_LEAVES.get().defaultBlockState(),
                ModBlocks.GREATWOOD_LOG.get().defaultBlockState());
        int heightLimitLimit = 11;
        ctx.heightLimit = heightLimitLimit + random.nextInt(heightLimitLimit);

        int trunkSize = 2;
        for (int x = 0; x < trunkSize; x++) {
            for (int z = 0; z < trunkSize; z++) {
                if (!isValidTreeLocation(ctx, x, z)) return false;
            }
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), SET_BLOCK_FLAGS);

        ctx.scaleWidth = 1.2;
        generateLeafNodeList(ctx);
        generateLeaves(ctx);
        generateLeafNodeBases(ctx);
        generateTrunk(ctx);

        ctx.scaleWidth = 1.66;
        ctx.baseY = pos.getY() + ctx.height;
        generateLeafNodeList(ctx);
        generateLeaves(ctx);
        generateLeafNodeBases(ctx);
        generateTrunk(ctx);

        if (spiders) generateSpiderNest(ctx, pos);
        return true;
    }

    private void generateLeafNodeList(GenCtx ctx) {
        // === Конфигурация (immutable после регистрации) ===
        double heightAttenuation = 0.618;
        ctx.height = (int) (ctx.heightLimit * heightAttenuation);
        if (ctx.height >= ctx.heightLimit) ctx.height = ctx.heightLimit - 1;

        double leafDensity = 0.9;
        int numLeafGroups = (int) (1.382 + Math.pow(leafDensity * ctx.heightLimit / 13.0, 2.0));
        if (numLeafGroups < 1) numLeafGroups = 1;

        List<FoliageNode> nodes = new ArrayList<>(Math.max(16, numLeafGroups * ctx.heightLimit));

        int leafY = ctx.baseY + ctx.heightLimit - leafDistanceLimit;
        int branchStartY = ctx.baseY + ctx.height;
        int layerIndex = leafY - ctx.baseY;

        nodes.add(new FoliageNode(ctx.baseX, leafY, ctx.baseZ, branchStartY));
        leafY--;

        RandomSource rand = ctx.rand;
        double scaleWidth = ctx.scaleWidth;
        int baseX = ctx.baseX;
        int baseZ = ctx.baseZ;

        while (layerIndex >= 0) {
            float size = layerSize(layerIndex, ctx.heightLimit);
            if (size < 0.0f) {
                leafY--;
                layerIndex--;
                continue;
            }
            for (int i = 0; i < numLeafGroups; i++) {
                double radius = scaleWidth * size * (rand.nextFloat() + 0.328);
                double angle = rand.nextFloat() * 2.0 * Math.PI;
                int nodeX = Mth.floor(radius * Math.sin(angle) + baseX + 0.5);
                int nodeZ = Mth.floor(radius * Math.cos(angle) + baseZ + 0.5);

                if (checkBlockLine(ctx, nodeX, leafY, nodeZ, nodeX, leafY + leafDistanceLimit, nodeZ) != -1) {
                    continue;
                }
                int dx = baseX - nodeX;
                int dz = baseZ - nodeZ;
                double dist = Math.sqrt((double) dx * dx + (double) dz * dz);
                double branchSlope = 0.38;
                double slopeHeight = dist * branchSlope;
                int branchBaseY = (leafY - slopeHeight > branchStartY)
                        ? branchStartY
                        : (int) (leafY - slopeHeight);

                if (checkBlockLine(ctx, baseX, branchBaseY, baseZ, nodeX, leafY, nodeZ) == -1) {
                    nodes.add(new FoliageNode(nodeX, leafY, nodeZ, branchBaseY));
                }
            }
            leafY--;
            layerIndex--;
        }
        ctx.leafNodes = nodes;
    }

    private static float layerSize(int layer, int heightLimit) {
        if (layer < (float) heightLimit * 0.3) return -1.618f;
        float halfHeight = heightLimit / 2.0f;
        float distFromMiddle = halfHeight - layer;
        if (distFromMiddle == 0.0f) return halfHeight * 0.5f;
        if (Math.abs(distFromMiddle) >= halfHeight) return 0.0f;
        return (float) Math.sqrt(halfHeight * halfHeight - distFromMiddle * distFromMiddle) * 0.5f;
    }

    private float leafSize(int layer) {
        if (layer < 0 || layer >= leafDistanceLimit) return -1.0f;
        return (layer != 0 && layer != leafDistanceLimit - 1) ? 3.0f : 2.0f;
    }

    private void generateLeaves(GenCtx ctx) {
        for (FoliageNode node : ctx.leafNodes) {
            generateLeafNode(ctx, node.x(), node.y(), node.z());
        }
    }

    private void generateLeafNode(GenCtx ctx, int x, int y, int z) {
        for (int ly = 0; ly < leafDistanceLimit; ly++) {
            generateLeafLayer(ctx, x, y + ly, z, leafSize(ly));
        }
    }

    private void generateLeafLayer(GenCtx ctx, int x, int y, int z, float radius) {
        if (radius <= 0.0f) return;
        int intRadius = (int) (radius + 0.618);
        double radiusSq = (double) radius * radius;
        WorldGenLevel level = ctx.level;
        BlockState leafState = ctx.leafState;
        Block leafBlock = ctx.leafBlock;

        for (int i = -intRadius; i <= intRadius; i++) {
            double a = Math.abs(i) + 0.5;
            double aiSq = a * a;
            if (aiSq > radiusSq) continue;
            int px = x + i;
            for (int j = -intRadius; j <= intRadius; j++) {
                double b = Math.abs(j) + 0.5;
                if (aiSq + b * b > radiusSq) continue;
                BlockPos p = new BlockPos(px, y, z + j);
                BlockState state = level.getBlockState(p);
                if ((state.isAir() || state.is(leafBlock)) && state.canBeReplaced()) {
                    level.setBlock(p, leafState, SET_BLOCK_FLAGS);
                }
            }
        }
    }

    private void generateTrunk(GenCtx ctx) {
        int x = ctx.baseX, y = ctx.baseY, z = ctx.baseZ;
        int topY = y + ctx.height;
        placeBlockLine(ctx, x, y, z, x, topY, z);
        placeBlockLine(ctx, x + 1, y, z, x + 1, topY, z);
        placeBlockLine(ctx, x + 1, y, z + 1, x + 1, topY, z + 1);
        placeBlockLine(ctx, x,     y, z + 1, x,     topY, z + 1);
    }

    private void generateLeafNodeBases(GenCtx ctx) {
        float minBranchDist = ctx.heightLimit * 0.2f;
        for (FoliageNode node : ctx.leafNodes) {
            int distFromBase = node.branchBase() - ctx.baseY;
            if (distFromBase >= minBranchDist) {
                placeBlockLine(ctx,
                        ctx.baseX, node.branchBase(), ctx.baseZ,
                        node.x(), node.y(), node.z());
            }
        }
    }

    private void placeBlockLine(GenCtx ctx, int fx, int fy, int fz, int tx, int ty, int tz) {
        int dx = tx - fx, dy = ty - fy, dz = tz - fz;
        int ay = Math.abs(dy);
        int az = Math.abs(dz);

        int mainAxis = 0, mainAbs = Math.abs(dx);
        if (ay > mainAbs) { mainAxis = 1; mainAbs = ay; }
        if (az > mainAbs) { mainAxis = 2; mainAbs = az; }

        int mainDelta = (mainAxis == 1) ? dy : (mainAxis == 2) ? dz : dx;
        if (mainDelta == 0) return;

        int[] start = {fx, fy, fz};
        int[] delta = {dx, dy, dz};
        byte coord1 = OTHER_COORD_PAIRS[mainAxis];
        byte coord2 = OTHER_COORD_PAIRS[mainAxis + 3];
        int direction = mainDelta > 0 ? 1 : -1;
        double slope1 = delta[coord1] / (double) mainDelta;
        double slope2 = delta[coord2] / (double) mainDelta;

        WorldGenLevel level = ctx.level;
        BlockState logState = ctx.logState;
        int[] pos = new int[3];
        int end = mainDelta + direction;

        for (int i = 0; i != end; i += direction) {
            pos[mainAxis] = Mth.floor(start[mainAxis] + i + 0.5);
            pos[coord1]   = Mth.floor(start[coord1] + i * slope1 + 0.5);
            pos[coord2]   = Mth.floor(start[coord2] + i * slope2 + 0.5);
            BlockPos p = new BlockPos(pos[0], pos[1], pos[2]);
            if (isReplaceable(level, p)) {
                level.setBlock(p, logState, SET_BLOCK_FLAGS);
            }
        }
    }

    private int checkBlockLine(GenCtx ctx, int fx, int fy, int fz, int tx, int ty, int tz) {
        int dx = tx - fx, dy = ty - fy, dz = tz - fz;
        int ay = Math.abs(dy);
        int az = Math.abs(dz);

        int mainAxis = 0, mainAbs = Math.abs(dx);
        if (ay > mainAbs) { mainAxis = 1; mainAbs = ay; }
        if (az > mainAbs) { mainAxis = 2; mainAbs = az; }

        int mainDelta = (mainAxis == 1) ? dy : (mainAxis == 2) ? dz : dx;
        if (mainDelta == 0) return -1;

        int[] start = {fx, fy, fz};
        int[] delta = {dx, dy, dz};
        byte coord1 = OTHER_COORD_PAIRS[mainAxis];
        byte coord2 = OTHER_COORD_PAIRS[mainAxis + 3];
        int direction = mainDelta > 0 ? 1 : -1;
        double slope1 = delta[coord1] / (double) mainDelta;
        double slope2 = delta[coord2] / (double) mainDelta;

        WorldGenLevel level = ctx.level;
        Block leafBlock = ctx.leafBlock;
        int[] pos = new int[3];
        int end = mainDelta + direction;
        int i = 0;

        for (; i != end; i += direction) {
            pos[mainAxis] = start[mainAxis] + i;
            pos[coord1]   = Mth.floor(start[coord1] + i * slope1);
            pos[coord2]   = Mth.floor(start[coord2] + i * slope2);
            try {
                BlockState state = level.getBlockState(new BlockPos(pos[0], pos[1], pos[2]));
                if (!state.isAir() && !state.is(leafBlock)) break;
            } catch (Exception ignored) {
                //TODO не выгруженный чанк
            }
        }
        return (i == end) ? -1 : Math.abs(i);
    }

    private boolean isValidTreeLocation(GenCtx ctx, int offsetX, int offsetZ) {
        WorldGenLevel level = ctx.level;
        int bx = ctx.baseX + offsetX;
        int by = ctx.baseY;
        int bz = ctx.baseZ + offsetZ;

        BlockState groundState = level.getBlockState(new BlockPos(bx, by - 1, bz));
        if (!groundState.is(BlockTags.DIRT) && !groundState.is(Blocks.GRASS_BLOCK)) return false;

        for (int y = by; y < by + ctx.heightLimit; y++) {
            BlockState state = level.getBlockState(new BlockPos(bx, y, bz));
            if (!state.isAir() && !state.is(BlockTags.LEAVES) && !state.canBeReplaced()) {
                if (y - by < 6) return false;
                ctx.heightLimit = y - by;
                break;
            }
        }
        return true;
    }

    private static boolean isReplaceable(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.is(BlockTags.LEAVES) || state.canBeReplaced();
    }

    private void generateSpiderNest(GenCtx ctx, BlockPos treeBase) {
        WorldGenLevel level = ctx.level;
        RandomSource random = ctx.rand;

        BlockPos spawnerPos = treeBase.below();
        level.setBlock(spawnerPos, Blocks.SPAWNER.defaultBlockState(), SET_BLOCK_FLAGS);
        if (level.getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(EntityType.CAVE_SPIDER, random);
        }

        for (int i = 0; i < 50; i++) {
            int wx = treeBase.getX() - 7 + random.nextInt(14);
            int wy = treeBase.getY() + random.nextInt(10);
            int wz = treeBase.getZ() - 7 + random.nextInt(14);
            BlockPos webPos = new BlockPos(wx, wy, wz);
            if (level.getBlockState(webPos).isAir() && isTouchingTreeBlock(ctx, webPos)) {
                level.setBlock(webPos, Blocks.COBWEB.defaultBlockState(), SET_BLOCK_FLAGS);
            }
        }

        BlockPos chestPos = treeBase.below(2);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), SET_BLOCK_FLAGS);
        if (level.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity chest) {
            chest.setLootTable(BuiltInLootTables.SIMPLE_DUNGEON, random.nextLong());
        }
    }

    private static boolean isTouchingTreeBlock(GenCtx ctx, BlockPos pos) {
        WorldGenLevel level = ctx.level;
        Block leaf = ctx.leafBlock;
        Block log = ModBlocks.GREATWOOD_LOG.get();
        for (Direction dir : DIRECTIONS) {
            BlockState state = level.getBlockState(pos.relative(dir));
            if (state.is(leaf) || state.is(log)) return true;
        }
        return false;
    }

    private record FoliageNode(int x, int y, int z, int branchBase) {}

    private static final class GenCtx {
        final WorldGenLevel level;
        final RandomSource rand;
        final int baseX;
        int baseY;
        final int baseZ;
        final Block leafBlock;
        final BlockState leafState;
        final BlockState logState;

        int heightLimit;
        int height;
        double scaleWidth;
        List<FoliageNode> leafNodes;

        GenCtx(WorldGenLevel level, RandomSource rand, int baseX, int baseY, int baseZ,
               Block leafBlock, BlockState leafState, BlockState logState) {
            this.level = level;
            this.rand = rand;
            this.baseX = baseX;
            this.baseY = baseY;
            this.baseZ = baseZ;
            this.leafBlock = leafBlock;
            this.leafState = leafState;
            this.logState = logState;
        }
    }
}