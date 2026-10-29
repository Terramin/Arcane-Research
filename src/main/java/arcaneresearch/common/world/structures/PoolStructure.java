package arcaneresearch.common.world.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import arcaneresearch.init.ModStructures;

import java.util.Optional;

public class PoolStructure extends Structure {

    
    public static final com.mojang.serialization.MapCodec<PoolStructure> CODEC = RecordCodecBuilder.<PoolStructure>mapCodec(instance ->
            instance.group(
                    settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter((PoolStructure s) -> s.startPool),
                    Codec.intRange(1, 7).fieldOf("size").forGetter((PoolStructure s) -> s.maxDepth),
                    Codec.INT.fieldOf("y_offset").forGetter((PoolStructure s) -> s.yOffset),
                    Codec.INT.fieldOf("flat_radius").forGetter((PoolStructure s) -> s.flatRadius),
                    Codec.INT.fieldOf("flat_variance").forGetter((PoolStructure s) -> s.flatVariance),
                    Codec.INT.fieldOf("min_y").forGetter((PoolStructure s) -> s.minY),
                    Codec.INT.fieldOf("max_y").forGetter((PoolStructure s) -> s.maxY)
            ).apply(instance, PoolStructure::new));

    private final Holder<StructureTemplatePool> startPool;
    private final int maxDepth;
    private final int yOffset;
    private final int flatRadius;
    private final int flatVariance;
    private final int minY;
    private final int maxY;

    public PoolStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool, int maxDepth,
                         int yOffset, int flatRadius, int flatVariance, int minY, int maxY) {
        super(settings);
        this.startPool = startPool;
        this.maxDepth = maxDepth;
        this.yOffset = yOffset;
        this.flatRadius = flatRadius;
        this.flatVariance = flatVariance;
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (context == null) {
            return Optional.empty();
        }
        ChunkPos chunkPos = context.chunkPos();
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        LevelHeightAccessor heightAccessor = context.heightAccessor();
        RandomState randomState = context.randomState();

        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();
        int surfaceY = chunkGenerator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, randomState);
        int groundY = surfaceY - 1;

        int minY = this.minY;
        int maxY = this.maxY;
        if (groundY < minY || groundY > maxY) {
            return Optional.empty();
        }
        // world bounds
        if (groundY < heightAccessor.getMinY()
                || groundY > heightAccessor.getMaxY()) {
            return Optional.empty();
        }

        NoiseColumn column = chunkGenerator.getBaseColumn(x, z, heightAccessor, randomState);
        BlockState topState;
        try {
            topState = column.getBlock(groundY);
        } catch (Exception ignored) {
            return Optional.empty();
        }
        if (topState == null || !topState.getFluidState().isEmpty() || !topState.canOcclude()) {
            return Optional.empty();
        }

        int r = this.flatRadius;
        int flatVariance = this.flatVariance;
        int h00 = chunkGenerator.getBaseHeight(x - r, z - r, Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, randomState);
        if (Math.abs(h00 - surfaceY) > flatVariance) {
            return Optional.empty();
        }
        int h10 = chunkGenerator.getBaseHeight(x + r, z - r, Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, randomState);
        if (Math.abs(h10 - surfaceY) > flatVariance) {
            return Optional.empty();
        }
        int h01 = chunkGenerator.getBaseHeight(x - r, z + r, Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, randomState);
        if (Math.abs(h01 - surfaceY) > flatVariance) {
            return Optional.empty();
        }
        int h11 = chunkGenerator.getBaseHeight(x + r, z + r, Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, randomState);
        if (Math.abs(h11 - surfaceY) > flatVariance) {
            return Optional.empty();
        }

        BlockPos structurePos = new BlockPos(x, groundY + this.yOffset, z);
        
        
        return JigsawPlacement.addPieces(
                context,
                this.startPool,
                Optional.empty(),
                this.maxDepth,
                structurePos,
                false,
                Optional.empty(),
                new JigsawStructure.MaxDistance(80),
                PoolAliasLookup.EMPTY,
                DimensionPadding.ZERO,
                LiquidSettings.APPLY_WATERLOGGING);
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.POOL.get();
    }
}
