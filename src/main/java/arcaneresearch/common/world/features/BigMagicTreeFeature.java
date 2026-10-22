package arcaneresearch.common.world.features;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import arcaneresearch.init.ModBlocks;

import java.util.List;






public class BigMagicTreeFeature implements Feature {

    public static final MapCodec<BigMagicTreeFeature> CODEC =
            MapCodec.unit(new BigMagicTreeFeature());

    private static final byte[] OTHER_COORD_PAIRS = {2, 0, 0, 1, 2, 1};
    private RandomSource rand;
    private WorldGenLevel level;
    private BlockPos basePos;
    private int heightLimit;
    private int height;
    private double heightAttenuation = 0.6618;
    private double branchSlope = 0.381;
    private double scaleWidth = 1.25;
    private double leafDensity = 0.9;
    private int trunkSize = 1;
    private int heightLimitLimit = 11;
    private int leafDistanceLimit = 4;
    private List<FoliageCoordinates> foliageCoords;
    private TreeType treeType = TreeType.GREATWOOD;

    public BigMagicTreeFeature() {
    }

    public BigMagicTreeFeature(TreeType type) {
        this.treeType = type;
    }

    @Override
    public MapCodec<BigMagicTreeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos pos) {

        if (generate(level, random, pos)) {
            return true;
        }
        for (int dy : new int[]{-2, 2, -4, 4}) {
            if (generate(level, random, pos.offset(0, dy, 0))) {
                return true;
            }
        }
        return false;
    }

    public boolean generate(WorldGenLevel worldIn, RandomSource randomIn, BlockPos position) {
        this.level = worldIn;
        this.basePos = position;
        this.rand = randomIn;
        this.heightLimit = 0;

        if (heightLimit == 0) {
            heightLimit = heightLimitLimit + rand.nextInt(heightLimitLimit);
        }

        if (!validTreeLocation()) {
            level = null;
            return false;
        }

        generateLeafNodeList();
        generateLeaves();
        generateTrunk();
        generateLeafNodeBases();

        level = null;
        return true;
    }

    private void generateLeafNodeList() {
        height = (int) (heightLimit * heightAttenuation);
        if (height >= heightLimit) {
            height = heightLimit - 1;
        }

        int numLeafGroups = (int) (1.382 + Math.pow(leafDensity * heightLimit / 13.0, 2.0));
        if (numLeafGroups < 1) {
            numLeafGroups = 1;
        }

        int leafY = basePos.getY() + heightLimit - leafDistanceLimit;
        int branchStartY = basePos.getY() + height;
        int layerIndex = heightLimit - leafDistanceLimit;

        foliageCoords = Lists.newArrayList();
        foliageCoords.add(new FoliageCoordinates(basePos.above(layerIndex), branchStartY));

        while (layerIndex >= 0) {
            float layerSize = layerSize(layerIndex);

            if (layerSize >= 0.0f) {
                for (int i = 0; i < numLeafGroups; i++) {
                    double radius = scaleWidth * layerSize * (rand.nextFloat() + 0.328);
                    double angle = rand.nextFloat() * 2.0 * Math.PI;
                    double offsetX = radius * Math.sin(angle) + 0.5;
                    double offsetZ = radius * Math.cos(angle) + 0.5;

                    BlockPos nodePos = basePos.offset(Mth.floor(offsetX), layerIndex - 1, Mth.floor(offsetZ));
                    BlockPos checkPos = nodePos.above(leafDistanceLimit);

                    if (checkBlockLine(nodePos, checkPos) == -1) {
                        int distX = basePos.getX() - nodePos.getX();
                        int distZ = basePos.getZ() - nodePos.getZ();
                        double dist = Math.sqrt(distX * distX + distZ * distZ);
                        double slopeHeight = nodePos.getY() - dist * branchSlope;

                        int branchBaseY = (slopeHeight > branchStartY) ? branchStartY : (int) slopeHeight;
                        BlockPos branchBase = new BlockPos(basePos.getX(), branchBaseY, basePos.getZ());

                        if (checkBlockLine(branchBase, nodePos) == -1) {
                            foliageCoords.add(new FoliageCoordinates(nodePos, branchBaseY));
                        }
                    }
                }
            }

            layerIndex--;
        }
    }

    private void crossSection(BlockPos center, float radius) {
        int intRadius = (int) (radius + 0.618);

        for (int dx = -intRadius; dx <= intRadius; dx++) {
            for (int dz = -intRadius; dz <= intRadius; dz++) {
                double dist = Math.pow(Math.abs(dx) + 0.5, 2.0) + Math.pow(Math.abs(dz) + 0.5, 2.0);

                if (dist <= radius * radius) {
                    BlockPos leafPos = center.offset(dx, 0, dz);
                    BlockState state = level.getBlockState(leafPos);

                    if (state.isAir() || state.is(BlockTags.LEAVES)) {
                        level.setBlock(leafPos, getLeafBlock(), 2);
                    }
                }
            }
        }
    }

    private float layerSize(int layer) {
        if (layer < (float) heightLimit * 0.3f) {
            return -1.0f;
        }

        float halfHeight = heightLimit / 2.0f;
        float distFromMiddle = halfHeight - layer;
        float size;

        if (distFromMiddle == 0.0f) {
            size = halfHeight;
        } else if (Math.abs(distFromMiddle) >= halfHeight) {
            return 0.0f;
        } else {
            size = (float) Math.sqrt(halfHeight * halfHeight - distFromMiddle * distFromMiddle);
        }

        return size * 0.5f;
    }

    private float leafSize(int layer) {
        if (layer >= 0 && layer < leafDistanceLimit) {
            return (layer != 0 && layer != leafDistanceLimit - 1) ? 3.0f : 2.0f;
        }
        return -1.0f;
    }

    private void generateLeafNode(BlockPos pos) {
        for (int i = 0; i < leafDistanceLimit; i++) {
            crossSection(pos.above(i), leafSize(i));
        }
    }

    private void limb(BlockPos from, BlockPos to) {
        BlockPos delta = to.subtract(from);
        int maxDist = getGreatestDistance(delta);

        float stepX = delta.getX() / (float) maxDist;
        float stepY = delta.getY() / (float) maxDist;
        float stepZ = delta.getZ() / (float) maxDist;

        for (int i = 0; i <= maxDist; i++) {
            BlockPos logPos = from.offset(
                    Mth.floor(0.5f + i * stepX),
                    Mth.floor(0.5f + i * stepY),
                    Mth.floor(0.5f + i * stepZ)
            );

            Direction.Axis axis = getLogAxis(from, logPos);
            BlockState logState = getLogBlock(axis);

            if (isReplaceable(level, logPos)) {
                level.setBlock(logPos, logState, 2);
            }
        }
    }

    private int getGreatestDistance(BlockPos pos) {
        int x = Mth.abs(pos.getX());
        int y = Mth.abs(pos.getY());
        int z = Mth.abs(pos.getZ());
        return Math.max(z, Math.max(x, y));
    }

    private Direction.Axis getLogAxis(BlockPos from, BlockPos to) {
        Direction.Axis axis = Direction.Axis.Y;
        int dx = Math.abs(to.getX() - from.getX());
        int dz = Math.abs(to.getZ() - from.getZ());
        int maxHoriz = Math.max(dx, dz);

        if (maxHoriz > 0) {
            if (dx == maxHoriz) {
                axis = Direction.Axis.X;
            } else if (dz == maxHoriz) {
                axis = Direction.Axis.Z;
            }
        }

        return axis;
    }

    private void generateLeaves() {
        for (FoliageCoordinates coord : foliageCoords) {
            generateLeafNode(coord.pos);
        }
    }

    private boolean leafNodeNeedsBase(int distFromBase) {
        return distFromBase >= heightLimit * 0.2;
    }

    private void generateTrunk() {
        BlockPos trunkTop = basePos.above(height);

        limb(basePos, trunkTop);

        if (trunkSize == 2) {
            limb(basePos.east(), trunkTop.east());
            limb(basePos.east().south(), trunkTop.east().south());
            limb(basePos.south(), trunkTop.south());
        }
    }

    private void generateLeafNodeBases() {
        for (FoliageCoordinates coord : foliageCoords) {
            int branchBaseY = coord.branchBase;
            BlockPos branchBase = new BlockPos(basePos.getX(), branchBaseY, basePos.getZ());

            if (leafNodeNeedsBase(branchBaseY - basePos.getY())) {
                limb(branchBase, coord.pos);
            }
        }
    }

    private int checkBlockLine(BlockPos from, BlockPos to) {
        BlockPos delta = to.subtract(from);
        int maxDist = getGreatestDistance(delta);

        if (maxDist == 0) {
            return -1;
        }

        float stepX = delta.getX() / (float) maxDist;
        float stepY = delta.getY() / (float) maxDist;
        float stepZ = delta.getZ() / (float) maxDist;

        for (int i = 0; i <= maxDist; i++) {
            BlockPos checkPos = from.offset(
                    Mth.floor(0.5f + i * stepX),
                    Mth.floor(0.5f + i * stepY),
                    Mth.floor(0.5f + i * stepZ)
            );

            if (!isReplaceable(level, checkPos)) {
                return i;
            }
        }

        return -1;
    }

    private boolean validTreeLocation() {
        BlockPos groundPos = basePos.below();
        BlockState groundState = level.getBlockState(groundPos);

        if (!groundState.is(BlockTags.DIRT) && !groundState.is(Blocks.GRASS_BLOCK)) {
            return false;
        }

        int checkResult = checkBlockLine(basePos, basePos.above(heightLimit - 1));

        if (checkResult == -1) {
            return true;
        }

        if (checkResult < 6) {
            return false;
        }

        heightLimit = checkResult;
        return true;
    }

    private boolean isReplaceable(LevelAccessor level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.is(BlockTags.LEAVES) || state.canBeReplaced() ||
                state.is(BlockTags.LOGS) || state.is(BlockTags.SAPLINGS);
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
                    .setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true);
        } else {
            return ModBlocks.GREATWOOD_LEAVES.get().defaultBlockState();
        }
    }

    public BigMagicTreeFeature withTreeType(TreeType type) {
        this.treeType = type;
        return this;
    }

    public BigMagicTreeFeature withLargeTrunk() {
        this.trunkSize = 2;
        return this;
    }

    public BigMagicTreeFeature withHeightLimit(int limit) {
        this.heightLimitLimit = limit;
        return this;
    }

    public enum TreeType {
        GREATWOOD,
        SILVERWOOD,

        OAK
    }

    private static class FoliageCoordinates {
        final BlockPos pos;
        final int branchBase;

        FoliageCoordinates(BlockPos pos, int branchBase) {
            this.pos = pos;
            this.branchBase = branchBase;
        }
    }
}
