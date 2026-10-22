package arcaneresearch.common.world.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import arcaneresearch.init.ModStructures;

import java.util.Optional;

public class FeatureStructure extends Structure {

    
    public static final com.mojang.serialization.MapCodec<FeatureStructure> CODEC = RecordCodecBuilder.<FeatureStructure>mapCodec(instance ->
            instance.group(
                    settingsCodec(instance),
                    ResourceKey.codec(Registries.PLACED_FEATURE).fieldOf("feature").forGetter((FeatureStructure s) -> s.featureKey),
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
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        int min = context.heightAccessor().getMinY();
        
        int max = context.heightAccessor().getMaxY() + 1;
        int y = context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                context.heightAccessor(), context.randomState());
        if (y < min + 2 || y > max - 20) {
            
            
            
            
            
            int flat = min + 2;
            if (flat > max - 20) {
                return Optional.empty();
            }
            BlockPos flatPos = new BlockPos(x, flat, z);
            int h = this.boxHalfWidth;
            BoundingBox flatBox = new BoundingBox(x - h, flat - this.boxHeightDown, z - h,
                    x + h, flat + this.boxHeightUp, z + h);
            return Optional.of(new GenerationStub(flatPos,
                    builder -> builder.addPiece(new FeatureStructurePiece(this.featureKey, flatBox, flatPos))));
        }
        NoiseColumn column = context.chunkGenerator().getBaseColumn(x, z,
                context.heightAccessor(), context.randomState());
        if (column.getBlock(y - 1).liquid()) {
            return Optional.empty();
        }
        BlockPos pos = new BlockPos(x, y, z);

        int h = this.boxHalfWidth;
        BoundingBox box = new BoundingBox(x - h, y - this.boxHeightDown, z - h,
                x + h, y + this.boxHeightUp, z + h);
        return Optional.of(new GenerationStub(pos,
                builder -> builder.addPiece(new FeatureStructurePiece(this.featureKey, box, pos))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.FEATURE.get();
    }
}
