package thaumictinker.mobilizer;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

import thaumictinker.registry.TtRegistry;

 


 
public final class MobilizerFeature {

    public static final String MOBILIZER_RESEARCH = "LEVITATOR_LOCOMOTIVE";

    public static final MobilizerBlock MOBILIZER = TtRegistry.blockWithItem("levitational_locomotive",
            new MobilizerBlock(properties()));
    public static final RelayBlock RELAY = TtRegistry.blockWithItem("levitational_locomotive_relay",
            new RelayBlock(properties()));

    public static final BlockEntityType<MobilizerBlockEntity> MOBILIZER_ENTITY = TtRegistry.blockEntity(
            "levitational_locomotive",
            BlockEntityType.Builder.of(MobilizerBlockEntity::new, MOBILIZER).build(null));
    public static final BlockEntityType<RelayBlockEntity> RELAY_ENTITY = TtRegistry.blockEntity(
            "levitational_locomotive_relay",
            BlockEntityType.Builder.of(RelayBlockEntity::new, RELAY).build(null));

    private MobilizerFeature() {
    }

    public static void load() {
    }

    private static BlockBehaviour.Properties properties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .strength(0.0F)
                .sound(SoundType.STONE);
    }
}
