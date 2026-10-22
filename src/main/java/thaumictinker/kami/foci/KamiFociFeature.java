package thaumictinker.kami.foci;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import thaumictinker.registry.TtRegistry;

 



 
public final class KamiFociFeature {

    public static final String SHADOWBEAM = "FOCUS_SHADOWBEAM";
    public static final String XP_DRAIN = "FOCUS_XP_DRAIN";
    public static final String RECALL = "FOCUS_RECALL";
    public static final String WARP_GATE = "WARP_GATE";

    public static final ShadowbeamFocus FOCUS_SHADOWBEAM =
            (ShadowbeamFocus) TtRegistry.itemWithTab("focus_shadowbeam",
                    () -> new ShadowbeamFocus());

    public static final XpDrainFocus FOCUS_XP_DRAIN =
            (XpDrainFocus) TtRegistry.itemWithTab("focus_xp_drain",
                    () -> new XpDrainFocus());

    public static final RecallFocus FOCUS_RECALL =
            (RecallFocus) TtRegistry.itemWithTab("focus_recall",
                    () -> new RecallFocus());

     



 
    public static final EntityType<ShadowbeamFocus.Beam> BEAM_TYPE =
            TtRegistry.entity("shadowbeam_staff_beam",
                    EntityType.Builder.<ShadowbeamFocus.Beam>of(ShadowbeamFocus.Beam::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .noSave()
                            .noSummon()
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build("thaumictinker:shadowbeam_staff_beam"));

    private KamiFociFeature() {
    }

    public static void load() {
    }
}
