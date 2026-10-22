package arcaneresearch.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.world.structures.FeatureStructure;
import arcaneresearch.common.world.structures.FeatureStructurePiece;
import arcaneresearch.common.world.structures.PoolStructure;





public class ModStructures {

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, Arcaneresearch.MODID);

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, Arcaneresearch.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<PoolStructure>> POOL =
            STRUCTURE_TYPES.register("pool", () -> explicitCodec(PoolStructure.CODEC));

    public static final DeferredHolder<StructureType<?>, StructureType<FeatureStructure>> FEATURE =
            STRUCTURE_TYPES.register("feature", () -> explicitCodec(FeatureStructure.CODEC));

    public static final DeferredHolder<StructurePieceType, StructurePieceType> FEATURE_PIECE =
            STRUCTURE_PIECE_TYPES.register("feature_piece", () -> new StructurePieceType() {
                @Override
                public StructurePiece load(StructurePieceSerializationContext context, CompoundTag tag) {
                    return new FeatureStructurePiece(tag);
                }
            });

    
    private static <S extends Structure> StructureType<S> explicitCodec(
            com.mojang.serialization.MapCodec<S> codec) {
        return () -> codec;
    }

    public static void register(IEventBus modEventBus) {
        STRUCTURE_TYPES.register(modEventBus);
        STRUCTURE_PIECE_TYPES.register(modEventBus);
    }
}
