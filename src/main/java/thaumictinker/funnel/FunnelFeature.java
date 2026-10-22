package thaumictinker.funnel;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

import thaumictinker.registry.TtRegistry;

 


 
public final class FunnelFeature {

    public static final String FUNNEL_RESEARCH = "FUNNEL";

    public static final FunnelBlock FUNNEL = TtRegistry.blockWithItem("essentia_funnel", new FunnelBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(3.0F, 8.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()));

    public static final BlockEntityType<FunnelBlockEntity> FUNNEL_ENTITY = TtRegistry.blockEntity(
            "essentia_funnel",
            BlockEntityType.Builder.of(FunnelBlockEntity::new, FUNNEL).build(null));

    private FunnelFeature() {
    }

    public static void load() {
    }
}
