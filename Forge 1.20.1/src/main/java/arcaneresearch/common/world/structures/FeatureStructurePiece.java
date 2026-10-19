package arcaneresearch.common.world.structures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import arcaneresearch.init.ModStructures;
public class FeatureStructurePiece extends StructurePiece {
    private final ResourceKey<PlacedFeature> featureKey;
    private final BlockPos genPos;
    public FeatureStructurePiece(ResourceKey<PlacedFeature> featureKey, BoundingBox box, BlockPos genPos) {
        super(ModStructures.FEATURE_PIECE.get(), 0, box);
        this.featureKey = featureKey;
        this.genPos = genPos;
    }
    public FeatureStructurePiece(CompoundTag tag) {
        super(ModStructures.FEATURE_PIECE.get(), tag);
        this.featureKey = ResourceKey.create(Registries.PLACED_FEATURE,
                new ResourceLocation(tag.getString("Feature")));
        this.genPos = BlockPos.of(tag.getLong("Pos"));
    }
    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("Feature", this.featureKey.location().toString());
        tag.putLong("Pos", this.genPos.asLong());
    }
    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
        Registry<PlacedFeature> registry = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        PlacedFeature placed = registry.getOrThrow(this.featureKey);
        placed.place(level, generator, random, this.genPos);
    }
    @Override
    public StructurePieceType getType() {
        return ModStructures.FEATURE_PIECE.get();
    }
}
