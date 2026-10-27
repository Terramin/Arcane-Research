package arcaneresearch.common.world.structures;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.jetbrains.annotations.NotNull;

public final class EldritchRingStructure {
    public static final String PAVEMENT = "arcaneresearch:ancient_stone";
    public static final String ALTAR_SEAT = "arcaneresearch:eldritch_stone_tile";
    public static final String OBELISK = "arcaneresearch:eldritch_pillar";
    public static final String OBELISK_CAP = "arcaneresearch:eldritch_stone_tile";

    private static final int[][] BANNER_SIDES = {
            {-3, 0, 12},
            {3, 0, 4},
            {0, -3, 0},
            {0, 3, 8}
    };

    private EldritchRingStructure() {
    }

    public static boolean isValidGround(LevelAccessor level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }
        BlockState above = level.getBlockState(pos.above());
        if (!above.isAir() && !above.canBeReplaced()) {
            return false;
        }
        BlockState ground = level.getBlockState(pos);
        return ground.is(Blocks.SNOW) || ground.is(Blocks.SNOW_BLOCK)
                || StructureHelper.isSurfaceGround(ground);
    }

    public static boolean canGenerate(LevelAccessor level, int x, int y, int z) {
        if (level == null) {
            return false;
        }
        return isValidGround(level, new BlockPos(x - 3, y, z - 3))
                && isValidGround(level, new BlockPos(x, y, z))
                && isValidGround(level, new BlockPos(x + 3, y, z))
                && isValidGround(level, new BlockPos(x + 3, y, z + 3))
                && isValidGround(level, new BlockPos(x, y, z + 3));
    }

    public static boolean generate(LevelAccessor level, BlockPos center, RandomSource random) {
        if (level == null || center == null || random == null) {
            return false;
        }
        int i = center.getX();
        int j = center.getY();
        int k = center.getZ();
        if (!canGenerate(level, i, j, k)) {
            return false;
        }

        BlockState pavement = StructureHelper.state(PAVEMENT);
        BlockState altarSeat = StructureHelper.state(ALTAR_SEAT);
        BlockState obelisk = StructureHelper.state(OBELISK);
        BlockState cap = StructureHelper.state(OBELISK_CAP);
        BlockState altar = StructureHelper.state("arcaneresearch:eldritch_altar");
        BlockState netherrack = Blocks.NETHERRACK.defaultBlockState();

        for (int x = i - 3; x <= i + 3; x++) {
            for (int z = k - 3; z <= k + 3; z++) {
                if ((x != i - 3 && x != i + 3) || (z != k - 3 && z != k + 3)) {
                    for (int q = -4; q < 5; q++) {
                        BlockPos current = new BlockPos(x, j + q, z);
                        if (q <= 0 && StructureHelper.replaceable(level, current)) {
                            if (random.nextInt(4) == 0) {
                                StructureHelper.set(level, current, netherrack);
                            } else {
                                StructureHelper.set(level, current, pavement);
                            }
                        }
                        if (q > 0) {
                            StructureHelper.clearAbove(level, current);
                        }
                    }
                    if (x == i && z == k) {
                        StructureHelper.set(level, new BlockPos(x, j + 1, z), altar);
                        StructureHelper.set(level, new BlockPos(x, j, z), pavement);
                        placeBanners(level, new BlockPos(x, j + 1, z));
                        StructureHelper.set(level, new BlockPos(x, j + 3, z), obelisk);
                        StructureHelper.set(level, new BlockPos(x, j + 4, z), obelisk);
                        StructureHelper.set(level, new BlockPos(x, j + 5, z), obelisk);
                        StructureHelper.set(level, new BlockPos(x, j + 6, z), obelisk);
                        StructureHelper.set(level, new BlockPos(x, j + 7, z), obelisk);
                    } else if (isEdgeObelisk(x - i, z - k)) {
                        StructureHelper.set(level, new BlockPos(x, j, z), pavement);
                        StructureHelper.set(level, new BlockPos(x, j + 1, z), cap);
                    }
                }
            }
        }
        return true;
    }

    static boolean isEdgeObelisk(int dx, int dz) {
        return ((Math.abs(dx) == 3 && Math.abs(dz) % 2 == 1)
                || (Math.abs(dz) == 3 && Math.abs(dx) % 2 == 1))
                && Math.abs(dx) != Math.abs(dz);
    }

    static void placeBanners(LevelAccessor level, BlockPos altar) {
        if (level == null || altar == null) {
            return;
        }
        for (int[] side : BANNER_SIDES) {
            BlockPos pos = altar.offset(side[0], 0, side[1]);
            if (level.getBlockState(pos).isAir()) {
                StructureHelper.set(level, pos, Blocks.WHITE_BANNER.defaultBlockState()
                        .setValue(BlockStateProperties.ROTATION_16, side[2]));
            }
        }
    }

    public static class Feature extends net.minecraft.world.level.levelgen.feature.Feature<NoneFeatureConfiguration> {
        public Feature(Codec<NoneFeatureConfiguration> codec) {
            super(codec);
        }

        @Override
        public boolean place(@NotNull FeaturePlaceContext<NoneFeatureConfiguration> context) {
            LevelAccessor level = context.level();
            BlockPos origin = context.origin();
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG,
                    origin.getX(), origin.getZ());
            return generate(level, new BlockPos(origin.getX(), y, origin.getZ()), context.random());
        }
    }
}