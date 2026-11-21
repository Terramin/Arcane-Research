package arcaneresearch.init;

import arcaneresearch.api.lore.LoreAPI;
import arcaneresearch.common.blocks.misc.BlockNitor;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;

public final class ModLore {

    private ModLore() {
    }

    public static void init() {

        LoreAPI.register(ModItems.SALIS_MUNDUS.get(), "lore.arcaneresearch.salis_mundus");
        LoreAPI.register(ModItems.ALUMENTUM.get(), "lore.arcaneresearch.alumentum");

        LoreAPI.register(ModItems.ELEMENTAL_AXE.get(), "lore.arcaneresearch.elemental_axe");
        LoreAPI.register(ModItems.ELEMENTAL_PICK.get(), "lore.arcaneresearch.elemental_pick");
        LoreAPI.register(ModItems.ELEMENTAL_SWORD.get(), "lore.arcaneresearch.elemental_sword");
        LoreAPI.register(ModItems.ELEMENTAL_SHOVEL.get(), "lore.arcaneresearch.elemental_shovel");
        LoreAPI.register(ModItems.ELEMENTAL_HOE.get(), "lore.arcaneresearch.elemental_hoe");

        LoreAPI.register(ModItems.THAUMONOMICON.get(), "lore.arcaneresearch.thaumonomicon");
        LoreAPI.register(ModItems.THAUMOMETER.get(), "lore.arcaneresearch.thaumometer");
        LoreAPI.register(ModItems.GOGGLES.get(), "lore.arcaneresearch.goggles");

        LoreAPI.register(ModItems.PORTAL_SCROLL.get(), "lore.arcaneresearch.portal_scroll");
        LoreAPI.register(ModItems.TRAVELLER_BOOTS.get(), "lore.arcaneresearch.traveller_boots");
        LoreAPI.register(ModItems.RUNIC_RING.get(), "lore.arcaneresearch.runic_ring");
        LoreAPI.register(ModItems.CRIMSON_CULT_BLADE.get(), "lore.arcaneresearch.crimson_cult_blade");
        LoreAPI.register(ModBlocks.DIOPTRA.get(), "lore.arcaneresearch.dioptra");
        LoreAPI.register(ModBlocks.CRUCIBLE.get(), "lore.arcaneresearch.crucible");

        registerNitor();
    }

    private static void registerNitor() {
        for (Block block : BuiltInRegistries.BLOCK) {
            if (block instanceof BlockNitor) {
                LoreAPI.register(block, "lore.arcaneresearch.nitor");
            }
        }
    }
}
