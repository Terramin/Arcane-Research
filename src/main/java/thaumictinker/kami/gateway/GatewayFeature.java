package thaumictinker.kami.gateway;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.extensions.IForgeMenuType;

import thaumictinker.registry.TtRegistry;

 



 
public final class GatewayFeature {

    public static final String WARP_GATE_RESEARCH = "WARP_GATE";
    public static final String SKY_PEARL_RECIPE = "SKY_PEARL";
    public static final String CHEST_GEM_RESEARCH = "ICHORCLOTH_CHEST_GEM";
    public static final String BOOTS_GEM_RESEARCH = "ICHORCLOTH_BOOTS_GEM";

    public static final WarpGateBlock WARP_GATE =
            (WarpGateBlock) TtRegistry.blockWithItem("warp_gate",
                    new WarpGateBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(5.0F, 2000.0F)
                            .sound(SoundType.STONE)));

    public static final SkyPearlItem SKY_PEARL = (SkyPearlItem) TtRegistry.itemWithTab(
            "sky_pearl", () -> new SkyPearlItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final BlockEntityType<WarpGateBlockEntity> WARP_GATE_ENTITY =
            TtRegistry.blockEntity("warp_gate",
                    BlockEntityType.Builder.of(WarpGateBlockEntity::new, WARP_GATE).build(null));

    public static final MenuType<WarpGateMenu> WARP_GATE_MENU =
            TtRegistry.menu("warp_gate", IForgeMenuType.create(WarpGateMenu::new));

    private GatewayFeature() {
    }

    public static void load() {
    }
}
