package arcaneresearch.common.world.maze;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

public class MazeFeature extends Feature<NoneFeatureConfiguration> {
    public static final ResourceKey<Level> DIMENSION = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "outer_lands"));

    public static final int SIZE = 11;
    public static final int FLOOR_Y = 50;

    public MazeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    public static void ensureMaze(ServerLevel level) {
        ConcurrentHashMap<CellLoc, Short> labyrinth = MazeHandler.labyrinth;
        if (labyrinth != null && !labyrinth.isEmpty()) {
            return;
        }
        if (level == null) {
            return;
        }
        MazeThread.generate(level, SIZE / 2 + 1, SIZE / 2 + 1, SIZE, SIZE);
    }

    @Override
    public boolean place(@NotNull FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel worldGen = context.level();
        ServerLevel level = worldGen.getLevel();
        if (DIMENSION == null || !DIMENSION.equals(level.dimension())) {
            return false;
        }
        ensureMaze(level);

        BlockPos origin = context.origin();
        RandomSource random = context.random();
        MazeHandler.generateEldritch(worldGen, random,
                origin.getX() >> 4, origin.getZ() >> 4);
        return true;
    }
}