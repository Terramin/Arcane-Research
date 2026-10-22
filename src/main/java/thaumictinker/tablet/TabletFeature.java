package thaumictinker.tablet;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import thaumictinker.registry.TtRegistry;

 




 
public final class TabletFeature {

    public static final String ANIMATION_TABLET = "ANIMATION_TABLET";

    public static final DynamismTabletBlock DYNAMISM_TABLET =
            (DynamismTabletBlock) TtRegistry.blockWithItem("dynamism_tablet",
                    new DynamismTabletBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(3.0F, 50.0F)
                            .sound(SoundType.METAL)));

    public static final net.minecraft.world.level.block.entity.BlockEntityType<DynamismTabletBlockEntity> TABLET_ENTITY =
            TtRegistry.blockEntity("dynamism_tablet",
                    net.minecraft.world.level.block.entity.BlockEntityType.Builder
                            .of(DynamismTabletBlockEntity::new, DYNAMISM_TABLET).build(null));

    public static final net.minecraft.world.inventory.MenuType<DynamismTabletMenu> TABLET_MENU =
            TtRegistry.menu("dynamism_tablet",
                    net.minecraftforge.common.extensions.IForgeMenuType.create(DynamismTabletMenu::new));

    private TabletFeature() {
    }

    public static void load() {
    }
}
