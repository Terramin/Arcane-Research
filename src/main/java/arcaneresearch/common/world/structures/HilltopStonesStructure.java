package arcaneresearch.common.world.structures;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import arcaneresearch.init.ModEntities;
import org.jetbrains.annotations.NotNull;

public final class HilltopStonesStructure {
    public static final String PAVEMENT = "arcaneresearch:ancient_stone";

    private static final int[][] VINE_DIRS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    private static final BooleanProperty[] VINE_PROPS = {
            VineBlock.WEST, VineBlock.EAST, VineBlock.NORTH, VineBlock.SOUTH};

    private HilltopStonesStructure() {
    }

    public static boolean isValidGround(LevelAccessor level, BlockPos pos) {
        if (level == null || pos == null || pos.getY() < 85) {
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
        return isValidGround(level, new BlockPos(x - 2, y, z - 2))
                && isValidGround(level, new BlockPos(x, y, z))
                && isValidGround(level, new BlockPos(x + 2, y, z))
                && isValidGround(level, new BlockPos(x + 2, y, z + 2))
                && isValidGround(level, new BlockPos(x, y, z + 2));
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
        BlockState netherrack = Blocks.NETHERRACK.defaultBlockState();
        BlockState vineState = Blocks.VINE.defaultBlockState();
        int minY = level.getMinBuildHeight();

        for (int x = i - 3; x <= i + 3; x++) {
            for (int z = k - 3; z <= k + 3; z++) {
                if ((x != i - 3 && x != i + 3) || (z != k - 3 && z != k + 3)) {
                    StructureHelper.set(level, new BlockPos(x, j, z),
                            random.nextBoolean() ? pavement : netherrack);

                    boolean stop = false;
                    for (int y = 1; y < 5; y++) {
                        if (j - y < minY) {
                            break;
                        }
                        if (x == i && z == k && y == 1) {
                            StructureHelper.set(level, new BlockPos(x, j + y, z), pavement);
                            BlockPos chestPos = new BlockPos(x, j + y + 1, z);
                            StructureHelper.set(level, chestPos, Blocks.CHEST.defaultBlockState());
                            BlockEntity chest = level.getBlockEntity(chestPos);
                            if (chest instanceof RandomizableContainerBlockEntity container) {
                                container.setLootTable(BuiltInLootTables.SIMPLE_DUNGEON, random.nextLong());
                            }
                            BlockPos spawnerPos = new BlockPos(x, j, z);
                            StructureHelper.set(level, spawnerPos, Blocks.SPAWNER.defaultBlockState());
                            BlockEntity spawner = level.getBlockEntity(spawnerPos);
                            EntityType<?> wisp = ModEntities.WISP.get();
                            if (spawner instanceof SpawnerBlockEntity spawnerTile) {
                                spawnerTile.setEntityId(wisp, random);
                            }
                        }
                        if (!stop && isPillarSpot(x - i, z - k)) {
                            StructureHelper.set(level, new BlockPos(x, j + y, z), pavement);
                            if (y >= 2 && random.nextBoolean()) {
                                stop = true;
                                growVines(level, new BlockPos(x, j + y, z), random);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    static boolean isPillarSpot(int dx, int dz) {
        return ((Math.abs(dx) == 3 && Math.abs(dz) % 2 == 1)
                || (Math.abs(dz) == 3 && Math.abs(dx) % 2 == 1));
    }

    static void growVines(LevelAccessor level, BlockPos top, RandomSource random) {
        if (level == null || top == null || random == null) {
            return;
        }
        for (int d = 0; d < 4; d++) {
            if (random.nextInt(3) != 0) {
                continue;
            }
            BlockPos pos = top.offset(VINE_DIRS[d][0], 0, VINE_DIRS[d][1]);
            int left = 4;
            while (left > 0 && level.getBlockState(pos).isAir()) {
                StructureHelper.set(level, pos,
                        Blocks.VINE.defaultBlockState().setValue(VINE_PROPS[d], true));
                pos = pos.below();
                left--;
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