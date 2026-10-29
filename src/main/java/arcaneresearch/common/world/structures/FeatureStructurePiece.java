package arcaneresearch.common.world.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
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
import arcaneresearch.Arcaneresearch;
import arcaneresearch.init.ModStructures;

public class FeatureStructurePiece extends StructurePiece {

    private final ResourceKey<PlacedFeature> featureKey;
    private final BlockPos genPos;

    public FeatureStructurePiece(ResourceKey<PlacedFeature> featureKey, BoundingBox box, BlockPos genPos) {
        super(ModStructures.FEATURE_PIECE.get(), 0, box);
        this.featureKey = featureKey;
        this.genPos = genPos == null ? BlockPos.ZERO : genPos;
    }

    public FeatureStructurePiece(CompoundTag tag) {
        super(ModStructures.FEATURE_PIECE.get(), tag);
        ResourceKey<PlacedFeature> key = null;
        try {
            String raw = tag.getStringOr("Feature", "");
            if (!raw.isEmpty()) {
                Identifier loc = Identifier.tryParse(raw);
                if (loc != null) {
                    key = ResourceKey.create(Registries.PLACED_FEATURE, loc);
                }
            }
        } catch (Exception ignored) {
        }
        this.featureKey = key;
        this.genPos = BlockPos.of(tag.getLongOr("Pos", 0L));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        if (this.featureKey != null) {
            tag.putString("Feature", this.featureKey.identifier().toString());
        }
        if (this.genPos != null) {
            tag.putLong("Pos", this.genPos.asLong());
        }
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
        if (this.featureKey == null || this.genPos == null) {
            return;
        }
        try {
            Registry<PlacedFeature> registry = level.registryAccess()
                    .lookupOrThrow(Registries.PLACED_FEATURE);
            var holder = registry.getOrThrow(this.featureKey);
            if (holder == null) {
                Arcaneresearch.LOGGER.warn("FeatureStructurePiece: missing placed feature {}",
                        this.featureKey.identifier());
                return;
            }
            holder.value().place(level, generator, random, this.genPos);
        } catch (Exception e) {
            Arcaneresearch.LOGGER.warn("FeatureStructurePiece: failed to place {}", this.featureKey, e);
        }
    }

    @Override
    public StructurePieceType getType() {
        return ModStructures.FEATURE_PIECE.get();
    }
}