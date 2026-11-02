package arcaneresearch.common.world.structures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import arcaneresearch.Arcaneresearch;

import java.io.InputStream;

public final class EldritchAltarStructure {
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

    private EldritchAltarStructure() {
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

        int groundY = y - 1;
        return isValidGround(level, new BlockPos(x - 3, groundY, z - 3))
                && isValidGround(level, new BlockPos(x, groundY, z))
                && isValidGround(level, new BlockPos(x + 3, groundY, z))
                && isValidGround(level, new BlockPos(x + 3, groundY, z + 3))
                && isValidGround(level, new BlockPos(x, groundY, z + 3));
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
                StructureHelper.set(level, pos, Blocks.BANNER.white().defaultBlockState()
                        .setValue(BlockStateProperties.ROTATION_16, side[2]));
            }
        }
    }

    private static volatile StructureTemplate templateCache;

    public static class Feature implements net.minecraft.world.level.levelgen.feature.Feature {
        public static final MapCodec<Feature> CODEC =
                MapCodec.unit(new Feature());

        public Feature() {
        }

        @Override
        public MapCodec<Feature> codec() {
            return CODEC;
        }

        @Override
        public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
            int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG,
                    origin.getX(), origin.getZ());
            BlockPos base = new BlockPos(origin.getX(), y, origin.getZ());
            if (placeFromTemplate(level, base, random)) {
                return true;
            }
            return generate(level, base, random);
        }
    }

    private static boolean placeFromTemplate(WorldGenLevel level, BlockPos base, RandomSource random) {
        StructureTemplate template = loadTemplate(level);
        if (template == null) {
            return false;
        }
        try {
            Vec3i size = template.getSize();
            if (size.equals(Vec3i.ZERO)) {
                return false;
            }

            BlockPos origin = base.offset(-size.getX() / 2, 0, -size.getZ() / 2);
            clearPlace(level, origin, size);
            StructurePlaceSettings settings = new StructurePlaceSettings()
                    .setMirror(Mirror.NONE)
                    .setIgnoreEntities(true);
            return template.placeInWorld(level, origin, BlockPos.ZERO, settings, random,
                    Block.UPDATE_CLIENTS);
        } catch (Exception e) {
            Arcaneresearch.LOGGER.warn("Failed to place eldritch altar structure template", e);
            return false;
        }
    }

    private static void clearPlace(WorldGenLevel level, BlockPos origin, Vec3i size) {
        final int headroom = 3;
        int maxY = origin.getY() + size.getY() + headroom;
        for (int x = 0; x < size.getX(); x++) {
            for (int z = 0; z < size.getZ(); z++) {
                for (int y = origin.getY(); y < maxY; y++) {
                    BlockPos pos = new BlockPos(origin.getX() + x, y, origin.getZ() + z);
                    if (!level.getBlockState(pos).isAir()) {
                        level.removeBlock(pos, false);
                    }
                }
            }
        }
    }

    private static StructureTemplate loadTemplate(WorldGenLevel level) {
        StructureTemplate cached = templateCache;
        if (cached == null) {
            synchronized (EldritchAltarStructure.class) {
                if (templateCache == null) {
                    templateCache = readTemplate(level);
                }
                cached = templateCache;
            }
        }
        return cached;
    }

    private static StructureTemplate readTemplate(WorldGenLevel level) {
        String path = "data/" + Arcaneresearch.MODID + "/structure/eldritch_altar_structure.nbt";
        try (InputStream in = EldritchAltarStructure.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                Arcaneresearch.LOGGER.warn("Eldritch altar structure template not found: {}", path);
                return null;
            }
            CompoundTag tag = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
            HolderGetter<Block> blockLookup = level.registryAccess()
                    .lookupOrThrow(Registries.BLOCK);
            StructureTemplate template = new StructureTemplate();
            template.load(blockLookup, tag);
            return template;
        } catch (Exception e) {
            Arcaneresearch.LOGGER.warn("Failed to read eldritch altar structure template {}", path, e);
            return null;
        }
    }
}