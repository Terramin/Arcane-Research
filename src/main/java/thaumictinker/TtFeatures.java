package thaumictinker;

import thaumictinker.attractor.AttractorFeature;
import thaumictinker.crops.CropsFeature;
import thaumictinker.darkquartz.DarkQuartzFeature;
import thaumictinker.decor.DecorFeature;
import thaumictinker.elements.ElementsFeature;
import thaumictinker.enchanting.EnchantingFeature;
import thaumictinker.foci.FociFeature;
import thaumictinker.funnel.FunnelFeature;
import thaumictinker.gas.GasFeature;
import thaumictinker.gear.GearFeature;
import thaumictinker.awakenedarmor.AwakenedArmorFeature;
import thaumictinker.kami.KamiFeature;
import thaumictinker.kami.foci.KamiFociFeature;
import thaumictinker.kami.gateway.GatewayFeature;
import thaumictinker.kami.tool.AwakenedToolsFeature;
import thaumictinker.kami.trinkets.TrinketsFeature;
import thaumictinker.mobilizer.MobilizerFeature;
import thaumictinker.necromancy.NecromancyFeature;
import thaumictinker.repairer.RepairerFeature;
import thaumictinker.tablet.TabletFeature;
import thaumictinker.transvector.TransvectorFeature;
import thaumictinker.registry.TtRegistry;

public final class TtFeatures {

    private static boolean loaded;

    private TtFeatures() {
    }

    public static void load() {
        if (loaded) {
            return;
        }
        loaded = true;

        DarkQuartzFeature.load();
        DecorFeature.load();
        FunnelFeature.load();
        GasFeature.load();
        RepairerFeature.load();
        MobilizerFeature.load();
        TransvectorFeature.load();
        FociFeature.load();
        EnchantingFeature.load();
        CropsFeature.load();
        ElementsFeature.load();
        NecromancyFeature.load();
        AttractorFeature.load();
        TabletFeature.load();
        GearFeature.load();
        KamiFeature.load();
        TrinketsFeature.load();
        GatewayFeature.load();
        KamiFociFeature.load();
        AwakenedToolsFeature.load();
        AwakenedArmorFeature.load();

        TtRegistry.tabIcon(() -> DarkQuartzFeature.DARK_QUARTZ);
    }
}
