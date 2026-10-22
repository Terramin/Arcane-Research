package thaumictinker.kami.tool;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import thaumictinker.kami.KamiFeature;
import thaumictinker.registry.TtRegistry;

 





 
public final class AwakenedToolsFeature {

    public static final String ICHOR_PICK_GEM_RESEARCH = "ICHOR_PICK_GEM";
    public static final String ICHOR_SHOVEL_GEM_RESEARCH = "ICHOR_SHOVEL_GEM";
    public static final String ICHOR_AXE_GEM_RESEARCH = "ICHOR_AXE_GEM";
    public static final String ICHOR_SWORD_GEM_RESEARCH = "ICHOR_SWORD_GEM";

    public static final Item AWAKENED_PICKAXE = TtRegistry.itemWithTab("awakened_ichorium_pickaxe",
            () -> new AwakenedPickaxeItem(KamiFeature.ICHOR_TOOL, 2, -2.8F, kamiProperties()));

    public static final Item AWAKENED_SHOVEL = TtRegistry.itemWithTab("awakened_ichorium_shovel",
            () -> new AwakenedShovelItem(KamiFeature.ICHOR_TOOL, 1.0F, -3.0F, kamiProperties()));

    public static final Item AWAKENED_AXE = TtRegistry.itemWithTab("awakened_ichorium_axe",
            () -> new AwakenedAxeItem(KamiFeature.ICHOR_TOOL, 3.0F, -3.0F, kamiProperties()));

    public static final Item AWAKENED_SWORD = TtRegistry.itemWithTab("awakened_ichorium_sword",
            () -> new AwakenedSwordItem(KamiFeature.ICHOR_TOOL, 4, -2.4F, kamiProperties()));

    private AwakenedToolsFeature() {
    }

      
    private static Item.Properties kamiProperties() {
        return new Item.Properties().rarity(Rarity.EPIC);
    }

    public static void load() {
    }
}
