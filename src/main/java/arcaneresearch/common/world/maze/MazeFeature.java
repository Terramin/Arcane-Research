package arcaneresearch.common.world.maze;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Maze room feature for the Outer Lands dimension.
 * Ensures the labyrinth exists for the world seed, then builds the cell
 * under the current chunk. Hooked via datapack (maze_room placed feature).
 */
// NEOFORGE_PORT: Feature is now an interface (no generics, no FeaturePlaceContext /
// NoneFeatureConfiguration): implements Feature with MapCodec codec() + place(
// WorldGenLevel, ChunkGenerator, RandomSource, BlockPos). Codec is MapCodec.unit(...):
// features are placed from code with fixed params (never deserialized from datapacks),
// registry-id dispatch resolves the registered instance on load.
public class MazeFeature implements Feature {
    public static final MapCodec<MazeFeature> CODEC =
            MapCodec.unit(new MazeFeature());
    public static final ResourceKey<Level> DIMENSION =
            ResourceKey.create(Registries.DIMENSION, Identifier.parse("arcaneresearch:outer_lands"));
    public static final int SIZE = 11;
    public static final int FLOOR_Y = 50;

    public MazeFeature() {
    }

    /**
     * Порт TC4: первичная раскладка лабиринта через MazeThread
     * (w=SIZE,h=SIZE, центр SIZE/2+1 — там же портальная комната).
     */
    public static void ensureMaze(ServerLevel level) {
        if (!MazeHandler.labyrinth.isEmpty()) {
            return;
        }
        MazeThread.generate(level, SIZE / 2 + 1, SIZE / 2 + 1, SIZE, SIZE);
    }

    @Override
    public MapCodec<MazeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel region, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        ServerLevel level = region.getLevel();
        if (!level.dimension().equals(DIMENSION)) {
            return false;
        }
        ensureMaze(level);
        // Регион, а не ServerLevel: записи за пределы региона тихо
        // отбрасываются вместо форсированной загрузки чанков (дедлок).
        MazeHandler.generateEldritch(region, random,
                origin.getX() >> 4, origin.getZ() >> 4);
        return true;
    }
}
