package thaumictinker.repairer;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockBehaviour;

import thaumictinker.registry.TtRegistry;

 


 
public final class RepairerFeature {

    public static final String REPAIRER_RESEARCH = "REPAIRER";

    public static final RepairerBlock REPAIRER = TtRegistry.blockWithItem("thaumic_restorer", new RepairerBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(5.0F, 10.0F)
                    .sound(SoundType.STONE)));

    public static final BlockEntityType<RepairerBlockEntity> REPAIRER_ENTITY = TtRegistry.blockEntity(
            "thaumic_restorer",
            BlockEntityType.Builder.of(RepairerBlockEntity::new, REPAIRER).build(null));

    private RepairerFeature() {
    }

    public static void load() {
    }
}
