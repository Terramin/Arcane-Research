package thaumictinker.attractor;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import thaumictinker.registry.TtRegistry;

 



 
public final class AttractorFeature {

    public static final String MAGNETS = "MAGNETS";
    public static final String MAGNET = "MAGNET";
    public static final String MOB_MAGNET = "MOB_MAGNET";

    public static final AttractorBlock KINETIC_ATTRACTOR = TtRegistry.hiddenBlock("kinetic_attractor",
            new AttractorBlock(attractorProperties(), false));

    public static final AttractorBlock CORPOREAL_ATTRACTOR = TtRegistry.hiddenBlock("corporeal_attractor",
            new AttractorBlock(attractorProperties(), true));

    public static final SoulMouldItem SOUL_MOULD =
            (SoulMouldItem) TtRegistry.itemWithTab("soul_mould",
                    () -> new SoulMouldItem(new Item.Properties().stacksTo(1)));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<AttractorBlockEntity> KINETIC_ENTITY =
            TtRegistry.blockEntity("kinetic_attractor",
                    net.minecraft.world.level.block.entity.BlockEntityType.Builder
                            .of(AttractorBlockEntity::new, KINETIC_ATTRACTOR).build(null));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<CorporealAttractorBlockEntity> CORPOREAL_ENTITY =
            TtRegistry.blockEntity("corporeal_attractor",
                    net.minecraft.world.level.block.entity.BlockEntityType.Builder
                            .of(CorporealAttractorBlockEntity::new, CORPOREAL_ATTRACTOR).build(null));

    public static final net.minecraft.world.inventory.MenuType<CorporealAttractorMenu> CORPOREAL_MENU =
            TtRegistry.menu("corporeal_attractor",
                    net.minecraftforge.common.extensions.IForgeMenuType.create(CorporealAttractorMenu::new));

    static {
        TtRegistry.itemWithTab("kinetic_attractor",
                () -> new AttractorItem(KINETIC_ATTRACTOR, new Item.Properties()));
        TtRegistry.itemWithTab("corporeal_attractor",
                () -> new AttractorItem(CORPOREAL_ATTRACTOR, new Item.Properties()));
    }

    private AttractorFeature() {
    }

    public static void load() {
    }

    private static BlockBehaviour.Properties attractorProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(1.7F, 1.0F)
                .sound(SoundType.WOOD);
    }
}
