package arcaneresearch.common.world.maze;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
public class MazeFeature extends Feature<NoneFeatureConfiguration> {
    public static final ResourceKey<Level> DIMENSION =
            ResourceKey.create(Registries.DIMENSION, new ResourceLocation("arcaneresearch:outer_lands"));
    public static final int SIZE = 11;
    public static final int FLOOR_Y = 50;
    public MazeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }
    public static void ensureMaze(ServerLevel level) {
        if (!MazeHandler.labyrinth.isEmpty()) {
            return;
        }
        MazeThread.generate(level, SIZE / 2 + 1, SIZE / 2 + 1, SIZE, SIZE);
    }
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        ServerLevel level = context.level().getLevel();
        if (!level.dimension().equals(DIMENSION)) {
            return false;
        }
        ensureMaze(level);
        BlockPos origin = context.origin();
        MazeHandler.generateEldritch(context.level(), context.random(),
                origin.getX() >> 4, origin.getZ() >> 4);
        return true;
    }
}
