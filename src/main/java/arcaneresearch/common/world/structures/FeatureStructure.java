package arcaneresearch.common.world.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import arcaneresearch.init.ModStructures;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FeatureStructure extends Structure {
    public static final Codec<FeatureStructure> CODEC = RecordCodecBuilder.<FeatureStructure>create(instance ->
            instance.group(
                    settingsCodec(instance),
                    ResourceKey.codec(Registries.PLACED_FEATURE).fieldOf("feature")
                            .forGetter((FeatureStructure s) -> s.featureKey),
                    Codec.INT.fieldOf("box_half_width").forGetter((FeatureStructure s) -> s.boxHalfWidth),
                    Codec.INT.fieldOf("box_height_up").forGetter((FeatureStructure s) -> s.boxHeightUp),
                    Codec.INT.fieldOf("box_height_down").forGetter((FeatureStructure s) -> s.boxHeightDown)
            ).apply(instance, FeatureStructure::new));

    private final ResourceKey<PlacedFeature> featureKey;
    private final int boxHalfWidth;
    private final int boxHeightUp;
    private final int boxHeightDown;

    public FeatureStructure(StructureSettings settings, ResourceKey<PlacedFeature> featureKey,
                            int boxHalfWidth, int boxHeightUp, int boxHeightDown) {
        super(settings);
        this.featureKey = featureKey;
        this.boxHalfWidth = boxHalfWidth;
        this.boxHeightUp = boxHeightUp;
        this.boxHeightDown = boxHeightDown;
    }

    @Override
    public @NotNull Optional<GenerationStub> findGenerationPoint(@NotNull GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        LevelHeightAccessor heightAccessor = context.heightAccessor();
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        RandomState randomState = context.randomState();

        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        int min = heightAccessor.getMinBuildHeight();
        int max = heightAccessor.getMaxBuildHeight();

        int y = chunkGenerator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                heightAccessor, randomState);

        int halfWidth = this.boxHalfWidth;
        ResourceKey<PlacedFeature> key = this.featureKey;

        if (y < min + 2 || y > max - 20) {
            int flat = min + 2;
            if (flat > max - 20) {
                return Optional.empty();
            }
            BlockPos flatPos = new BlockPos(x, flat, z);
            BoundingBox flatBox = new BoundingBox(x - halfWidth, flat - this.boxHeightDown, z - halfWidth,
                    x + halfWidth, flat + this.boxHeightUp, z + halfWidth);
            return Optional.of(new GenerationStub(flatPos,
                    builder -> builder.addPiece(new FeatureStructurePiece(key, flatBox, flatPos))));
        }

        NoiseColumn column = chunkGenerator.getBaseColumn(x, z, heightAccessor, randomState);
        int blockY = y - 1;
        if (blockY < min) {
            return Optional.empty();
        }
        BlockState topState;
        try {
            topState = column.getBlock(blockY);
        } catch (Exception ignored) {
            return Optional.empty();
        }
        if (!topState.getFluidState().isEmpty()) {
            return Optional.empty();
        }

        BlockPos pos = new BlockPos(x, y, z);
        BoundingBox box = new BoundingBox(x - halfWidth, y - this.boxHeightDown, z - halfWidth,
                x + halfWidth, y + this.boxHeightUp, z + halfWidth);
        return Optional.of(new GenerationStub(pos,
                builder -> builder.addPiece(new FeatureStructurePiece(key, box, pos))));
    }

    @Override
    public @NotNull StructureType<?> type() {
        return ModStructures.FEATURE.get();
    }
}