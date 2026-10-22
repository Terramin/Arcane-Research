package thaumictinker.transvector;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

import thaumictinker.camo.CamoBlockEntity;
import thaumictinker.registry.TtRegistry;

 




 
public final class TransvectorFeature {

    public static final String INTERFACE = "INTERFACE";
    public static final String INTERFACE_BINDER = "INTERFACE1";
    public static final String DISLOCATOR = "DISLOCATOR";
    public static final String PLATFORM = "PLATFORM";

    public static final TransvectorInterfaceBlock TRANSVECTOR_INTERFACE = TtRegistry.blockWithItem(
            "transvector_interface", new TransvectorInterfaceBlock(machine()));
    public static final TransvectorDislocatorBlock TRANSVECTOR_DISLOCATOR = TtRegistry.blockWithItem(
            "transvector_dislocator", new TransvectorDislocatorBlock(machine()));
    public static final EtherealPlatformBlock ETHEREAL_PLATFORM = TtRegistry.blockWithItem(
            "ethereal_platform", new EtherealPlatformBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .instrument(NoteBlockInstrument.BANJO)
                    .strength(2.0F, 5.0F)
                    .sound(SoundType.WOOD)));

    public static final Item TRANSVECTOR_BINDER = TtRegistry.itemWithTab("transvector_binder",
            () -> new TransvectorBinderItem(new Item.Properties().stacksTo(1)));

    public static final BlockEntityType<TransvectorInterfaceBlockEntity> INTERFACE_ENTITY = TtRegistry.blockEntity(
            "transvector_interface",
            BlockEntityType.Builder.of(TransvectorInterfaceBlockEntity::new, TRANSVECTOR_INTERFACE).build(null));
    public static final BlockEntityType<TransvectorDislocatorBlockEntity> DISLOCATOR_ENTITY = TtRegistry.blockEntity(
            "transvector_dislocator",
            BlockEntityType.Builder.of(TransvectorDislocatorBlockEntity::new, TRANSVECTOR_DISLOCATOR).build(null));
    public static final BlockEntityType<CamoBlockEntity> PLATFORM_ENTITY = TtRegistry.blockEntity(
            "ethereal_platform", platformEntity());

    private static BlockEntityType<CamoBlockEntity> platformEntity() {
        return BlockEntityType.Builder.<CamoBlockEntity>of(
                (pos, state) -> new CamoBlockEntity(PLATFORM_ENTITY, pos, state), ETHEREAL_PLATFORM).build(null);
    }

    private TransvectorFeature() {
    }

    public static void load() {
    }

    private static BlockBehaviour.Properties machine() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .strength(3.0F, 10.0F)
                .sound(SoundType.STONE);
    }
}
