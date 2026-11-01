package arcaneresearch.common.world.features;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import arcaneresearch.init.ModBlocks;

import java.util.ArrayList;
import java.util.List;

public class BigMagicTreeFeature extends Feature<NoneFeatureConfiguration> {

    private static final int[] Y_OFFSETS = {-2, 2, -4, 4};
    private static final int SET_BLOCK_FLAGS = 2;

    private final double heightAttenuation = 0.6618;
    private final double branchSlope = 0.381;
    private final double scaleWidth = 1.25;
    private final double leafDensity = 0.9;
    private final int trunkSize = 1;
    private final int heightLimitLimit = 11;
    private final int leafDistanceLimit = 4;
    private final TreeType treeType;

    public BigMagicTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
        this.treeType = TreeType.GREATWOOD;
    }

    public BigMagicTreeFeature(Codec<NoneFeatureConfiguration> codec, TreeType type) {
        super(codec);
        this.treeType = type;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();
        if (generate(level, random, pos)) return true;
        for (int dy : Y_OFFSETS) {
            if (generate(level, random, pos.offset(0, dy, 0))) return true;
        }
        return false;
    }

    public boolean generate(WorldGenLevel worldIn, RandomSource randomIn, BlockPos position) {
        GenCtx ctx = new GenCtx(worldIn, randomIn, position,
                getLeafBlock(), getLogBlock(Direction.Axis.X),
                getLogBlock(Direction.Axis.Y), getLogBlock(Direction.Axis.Z));
        ctx.heightLimit = heightLimitLimit + randomIn.nextInt(heightLimitLimit);
        if (!validTreeLocation(ctx)) return false;
        generateLeafNodeList(ctx);
        generateLeaves(ctx);
        generateTrunk(ctx);
        generateLeafNodeBases(ctx);
        return true;
    }

    private void generateLeafNodeList(GenCtx ctx) {
        int heightLimit = ctx.heightLimit;
        double heightAttenuation = 0.6618;
        int height = (int) (heightLimit * heightAttenuation);
        if (height >= heightLimit) height = heightLimit - 1;
        ctx.height = height;

        double leafDensity = 0.9;
        int numLeafGroups = (int) (1.382 + Math.pow(leafDensity * heightLimit / 13.0, 2.0));
        if (numLeafGroups < 1) numLeafGroups = 1;

        BlockPos basePos = ctx.basePos;
        RandomSource rand = ctx.rand;
        int bx = basePos.getX();
        int by = basePos.getY();
        int bz = basePos.getZ();
        int branchStartY = by + height;

        List<FoliageCoordinates> foliage = new ArrayList<>(Math.max(16, numLeafGroups * heightLimit / 2));
        foliage.add(new FoliageCoordinates(basePos.above(heightLimit - leafDistanceLimit), branchStartY));

        for (int layerIndex = heightLimit - leafDistanceLimit; layerIndex >= 0; layerIndex--) {
            float layerSize = layerSize(layerIndex, heightLimit);
            if (layerSize < 0.0f) continue;

            for (int i = 0; i < numLeafGroups; i++) {
                double scaleWidth = 1.25;
                double radius   = scaleWidth * layerSize * (rand.nextFloat() + 0.328);
                double angle    = rand.nextFloat() * 2.0 * Math.PI;
                double offsetX  = radius * Math.sin(angle) + 0.5;
                double offsetZ  = radius * Math.cos(angle) + 0.5;
                BlockPos nodePos = basePos.offset(Mth.floor(offsetX), layerIndex - 1, Mth.floor(offsetZ));

                if (checkBlockLine(ctx, nodePos, nodePos.above(leafDistanceLimit)) != -1) continue;

                int distX = bx - nodePos.getX();
                int distZ = bz - nodePos.getZ();
                double dist        = Math.sqrt(distX * distX + distZ * distZ);
                double branchSlope = 0.381;
                double slopeHeight = nodePos.getY() - dist * branchSlope;
                int branchBaseY    = (slopeHeight > branchStartY) ? branchStartY : (int) slopeHeight;

                BlockPos branchBase = new BlockPos(bx, branchBaseY, bz);
                if (checkBlockLine(ctx, branchBase, nodePos) == -1) {
                    foliage.add(new FoliageCoordinates(nodePos, branchBaseY));
                }
            }
        }
        ctx.foliageCoords = foliage;
    }

    private void crossSection(GenCtx ctx, BlockPos center, float radius) {
        if (radius <= 0.0f) return;
        int intRadius = (int) (radius + 0.618);
        double radiusSq = (double) radius * radius;
        BlockState leaf = ctx.leafBlock;
        WorldGenLevel lvl = ctx.level;

        for (int dx = -intRadius; dx <= intRadius; dx++) {
            double a = Math.abs(dx) + 0.5;
            double dxSq = a * a;
            for (int dz = -intRadius; dz <= intRadius; dz++) {
                double b = Math.abs(dz) + 0.5;
                if (dxSq + b * b > radiusSq) continue;

                BlockPos leafPos = center.offset(dx, 0, dz);
                BlockState state = lvl.getBlockState(leafPos);
                if (state.isAir() || state.is(BlockTags.LEAVES)) {
                    lvl.setBlock(leafPos, leaf, SET_BLOCK_FLAGS);
                }
            }
        }
    }

    private static float layerSize(int layer, int heightLimit) {
        if (layer < (float) heightLimit * 0.3f) return -1.0f;
        float halfHeight = heightLimit / 2.0f;
        float distFromMiddle = halfHeight - layer;
        if (distFromMiddle == 0.0f) return halfHeight * 0.5f;
        if (Math.abs(distFromMiddle) >= halfHeight) return 0.0f;
        return (float) Math.sqrt(halfHeight * halfHeight - distFromMiddle * distFromMiddle) * 0.5f;
    }

    private static float leafSize(int layer) {
        if (layer < 0 || layer >= 4) return -1.0f;
        return (layer != 0 && layer != 3) ? 3.0f : 2.0f;
    }

    private void generateLeafNode(GenCtx ctx, BlockPos pos) {
        for (int i = 0; i < leafDistanceLimit; i++) {
            crossSection(ctx, pos.above(i), leafSize(i));
        }
    }

    private void limb(GenCtx ctx, BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();
        int maxDist = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
        if (maxDist == 0) return;

        float stepX = dx / (float) maxDist;
        float stepY = dy / (float) maxDist;
        float stepZ = dz / (float) maxDist;
        WorldGenLevel lvl = ctx.level;

        for (int i = 0; i <= maxDist; i++) {
            BlockPos logPos = from.offset(
                    Mth.floor(0.5f + i * stepX),
                    Mth.floor(0.5f + i * stepY),
                    Mth.floor(0.5f + i * stepZ)
            );
            if (!isReplaceable(lvl, logPos)) continue;
            BlockState logState = ctx.logFor(getLogAxis(from, logPos));
            lvl.setBlock(logPos, logState, SET_BLOCK_FLAGS);
        }
    }

    private static Direction.Axis getLogAxis(BlockPos from, BlockPos to) {
        int dx = Math.abs(to.getX() - from.getX());
        int dz = Math.abs(to.getZ() - from.getZ());
        if (dx > dz) return Direction.Axis.X;
        if (dz > dx) return Direction.Axis.Z;
        return Direction.Axis.Y;
    }

    private void generateLeaves(GenCtx ctx) {
        for (FoliageCoordinates coord : ctx.foliageCoords) {
            generateLeafNode(ctx, coord.pos());
        }
    }

    private void generateTrunk(GenCtx ctx) {
        BlockPos basePos = ctx.basePos;
        BlockPos trunkTop = basePos.above(ctx.height);
        limb(ctx, basePos, trunkTop);
        if (trunkSize == 2) {
            limb(ctx, basePos.east(), trunkTop.east());
            limb(ctx, basePos.east().south(), trunkTop.east().south());
            limb(ctx, basePos.south(), trunkTop.south());
        }
    }

    private void generateLeafNodeBases(GenCtx ctx) {
        BlockPos basePos = ctx.basePos;
        int baseY = basePos.getY();
        float minBranchDist = ctx.heightLimit * 0.2f;
        for (FoliageCoordinates coord : ctx.foliageCoords) {
            int branchBaseY = coord.branchBase();
            if ((branchBaseY - baseY) < minBranchDist) continue;
            BlockPos branchBase = new BlockPos(basePos.getX(), branchBaseY, basePos.getZ());
            limb(ctx, branchBase, coord.pos());
        }
    }

    private int checkBlockLine(GenCtx ctx, BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();
        int maxDist = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
        if (maxDist == 0) return -1;

        float stepX = dx / (float) maxDist;
        float stepY = dy / (float) maxDist;
        float stepZ = dz / (float) maxDist;
        WorldGenLevel lvl = ctx.level;

        for (int i = 0; i <= maxDist; i++) {
            BlockPos checkPos = from.offset(
                    Mth.floor(0.5f + i * stepX),
                    Mth.floor(0.5f + i * stepY),
                    Mth.floor(0.5f + i * stepZ)
            );
            if (!isReplaceable(lvl, checkPos)) return i;
        }
        return -1;
    }

    private boolean validTreeLocation(GenCtx ctx) {
        WorldGenLevel lvl = ctx.level;
        BlockPos basePos = ctx.basePos;

        BlockState groundState = lvl.getBlockState(basePos.below());
        if (!groundState.is(BlockTags.DIRT) && !groundState.is(Blocks.GRASS_BLOCK)) {
            return false;
        }
        int checkResult = checkBlockLine(ctx, basePos, basePos.above(ctx.heightLimit - 1));
        if (checkResult == -1) return true;
        if (checkResult < 6) return false;
        ctx.heightLimit = checkResult;
        return true;
    }

    private static boolean isReplaceable(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir()
                || state.is(BlockTags.LEAVES)
                || state.canBeReplaced()
                || state.is(BlockTags.LOGS)
                || state.is(BlockTags.SAPLINGS);
    }

    private BlockState getLogBlock(Direction.Axis axis) {
        BlockState log;
        if (treeType == TreeType.SILVERWOOD) {
            log = ModBlocks.SILVERWOOD_LOG.get().defaultBlockState();
        } else if (treeType == TreeType.OAK) {
            log = Blocks.OAK_LOG.defaultBlockState();
        } else {
            log = ModBlocks.GREATWOOD_LOG.get().defaultBlockState();
        }
        if (log.hasProperty(BlockStateProperties.AXIS)) {
            log = log.setValue(BlockStateProperties.AXIS, axis);
        }
        return log;
    }

    private BlockState getLeafBlock() {
        if (treeType == TreeType.SILVERWOOD) {
            return ModBlocks.SILVERWOOD_LEAVES.get().defaultBlockState();
        } else if (treeType == TreeType.OAK) {
            return Blocks.OAK_LEAVES.defaultBlockState()
                    .setValue(LeavesBlock.PERSISTENT, true);
        } else {
            return ModBlocks.GREATWOOD_LEAVES.get().defaultBlockState();
        }
    }

    public enum TreeType {
        GREATWOOD,
        SILVERWOOD,
        OAK
    }

    private record FoliageCoordinates(BlockPos pos, int branchBase) {}

    private static final class GenCtx {
        final WorldGenLevel level;
        final RandomSource rand;
        final BlockPos basePos;
        final BlockState leafBlock;
        final BlockState logX, logY, logZ;

        int heightLimit;
        int height;
        List<FoliageCoordinates> foliageCoords;

        GenCtx(WorldGenLevel level, RandomSource rand, BlockPos basePos,
               BlockState leafBlock, BlockState logX, BlockState logY, BlockState logZ) {
            this.level = level;
            this.rand = rand;
            this.basePos = basePos;
            this.leafBlock = leafBlock;
            this.logX = logX;
            this.logY = logY;
            this.logZ = logZ;
        }

        BlockState logFor(Direction.Axis axis) {
            return axis == Direction.Axis.X ? logX
                    : axis == Direction.Axis.Y ? logY : logZ;
        }
    }
}