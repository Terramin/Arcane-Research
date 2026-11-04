package arcaneresearch.common.world.structures;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
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
        int k = center.getZ();

        BlockState pavement = StructureHelper.state(PAVEMENT);
        BlockState obelisk = StructureHelper.state(OBELISK);
        BlockState cap = StructureHelper.state(OBELISK_CAP);
        BlockState altar = StructureHelper.state("arcaneresearch:eldritch_altar");
        BlockState netherrack = Blocks.NETHERRACK.defaultBlockState();

        // Adaptive base: use the lowest non-liquid surface column of the footprint so
        // the plateau does not hang in the air on slopes; water columns are skipped.
        int j = center.getY();
        for (int x = i - 3; x <= i + 3; x++) {
            for (int z = k - 3; z <= k + 3; z++) {
                int top = columnTop(level, x, z);
                if (top >= 0 && top < j) {
                    j = top;
                }
            }
        }
        if (columnTop(level, i, k) < 0) {
            return false;
        }

        final int fillDepth = 4;
        final int clearAbove = 9;
        for (int x = i - 3; x <= i + 3; x++) {
            for (int z = k - 3; z <= k + 3; z++) {
                if ((x != i - 3 && x != i + 3) || (z != k - 3 && z != k + 3)) {
                    int top = columnTop(level, x, z);
                    if (top < 0) {
                        continue; // water / invalid column
                    }
                    if (top > j + 2) {
                        top = j + 2; // do not bury the plateau in steep slopes
                    }
                    for (int y = j - fillDepth; y <= j; y++) {
                        BlockPos current = new BlockPos(x, y, z);
                        if (StructureHelper.replaceable(level, current)) {
                            if (random.nextInt(4) == 0) {
                                StructureHelper.set(level, current, netherrack);
                            } else {
                                StructureHelper.set(level, current, pavement);
                            }
                        }
                    }
                    // support column up to the column's own surface
                    if (top > j) {
                        for (int y = j + 1; y <= top; y++) {
                            BlockPos above = new BlockPos(x, y, z);
                            if (StructureHelper.replaceable(level, above)) {
                                StructureHelper.set(level, above, pavement);
                            }
                        }
                    }
                    // clear air above so nothing is buried
                    for (int y = top + 1; y <= top + clearAbove; y++) {
                        StructureHelper.clearAbove(level, new BlockPos(x, y, z));
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

    /**
     * Highest solid (non-liquid, non-air-ish) block surface Y for a column,
     * or {@code -1} when the column is liquid / unusable (e.g. water surface).
     */
    private static int columnTop(LevelAccessor level, int x, int z) {
        int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
        if (top < 0) {
            return -1;
        }
        BlockState topState = level.getBlockState(new BlockPos(x, top, z));
        if (!topState.getFluidState().isEmpty()) {
            return -1;
        }
        return top;
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
            boolean templatePlaced = placeFromTemplate(level, base, random);
            if (templatePlaced && hasAltarBlock(level, base)) {
                return true;
            }
            if (templatePlaced) {
                Arcaneresearch.LOGGER.warn("Eldritch altar template completed without an altar block at {}, falling back to code-built layout", base);
            }
            boolean built = generate(level, base, random);
            if (!built) {
                Arcaneresearch.LOGGER.warn("Eldritch altar generation completely failed at {}", base);
            } else if (!templatePlaced) {
                Arcaneresearch.LOGGER.info("Eldritch altar generated via code-built fallback at {}", base);
            }
            return built;
        }
    }

    /**
     * Verifies that the template actually wrote an altar block into the world.
     * template.placeInWorld can return true while placing nothing (all air, bad palette entries...).
     */
    private static boolean hasAltarBlock(WorldGenLevel level, BlockPos base) {
        StructureTemplate template = loadTemplate(level);
        if (template == null) {
            return false;
        }
        BlockState altar = StructureHelper.state("arcaneresearch:eldritch_altar");
        if (altar.isAir()) {
            return true; // altar block itself not registered - cannot verify
        }
        Vec3i size = template.getSize();
        BlockPos origin = base.offset(-size.getX() / 2, 0, -size.getZ() / 2);
        for (int x = 0; x < size.getX(); x++) {
            for (int z = 0; z < size.getZ(); z++) {
                for (int dy = 0; dy < size.getY(); dy++) {
                    BlockPos pos = origin.offset(x, dy, z);
                    if (level.getBlockState(pos).is(altar.getBlock())) {
                        return true;
                    }
                }
            }
        }
        return false;
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

    /**
     * Minecraft 26.x parses structure-template palettes via NbtUtils.readBlockState,
     * which expects the "id"/"properties" keys and silently turns unresolvable entries
     * into AIR. Templates saved on 1.20.x ("Name"/"Properties", DataVersion &lt; 5006)
     * would therefore place nothing. Convert legacy keys in place before loading.
     */
    private static void upgradeLegacyPaletteKeys(CompoundTag root) {
        convertLegacyPaletteList(root.getListOrEmpty("palette"));
        for (Tag sub : root.getListOrEmpty("palettes")) {
            if (sub instanceof ListTag subList) {
                convertLegacyPaletteList(subList);
            }
        }
    }

    private static void convertLegacyPaletteList(ListTag palette) {
        for (int i = 0; i < palette.size(); i++) {
            CompoundTag entry = palette.getCompoundOrEmpty(i);
            String name = entry.getStringOr("Name", "");
            if (name.isEmpty() || entry.contains("id")) {
                continue;
            }
            entry.putString("id", name);
            entry.remove("Name");
            if (entry.contains("Properties")) {
                entry.put("properties", entry.getCompoundOrEmpty("Properties"));
                entry.remove("Properties");
            }
        }
    }

    private static StructureTemplate readTemplate(WorldGenLevel level) {
        String path = "data/" + Arcaneresearch.MODID + "/structure/eldritch_altar_structure.nbt";
        try (InputStream in = EldritchAltarStructure.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                Arcaneresearch.LOGGER.warn("Eldritch altar structure template not found: {}", path);
                return null;
            }
            CompoundTag tag = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
        upgradeLegacyPaletteKeys(tag);
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