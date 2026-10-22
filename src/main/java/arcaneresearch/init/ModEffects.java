package arcaneresearch.init;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.potions.PotionFluxTaint;
import arcaneresearch.api.potions.PotionVisExhaust;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;





public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, Arcaneresearch.MODID);

    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }

    public static final DeferredHolder<MobEffect, MobEffect> FLUX_TAINT =
            MOB_EFFECTS.register("flux_taint", PotionFluxTaint::new);

    public static final DeferredHolder<MobEffect, MobEffect> VIS_EXHAUST =
            MOB_EFFECTS.register("vis_exhaust", PotionVisExhaust::new);

    public static final DeferredHolder<MobEffect, MobEffect> WARP_WARD =
            MOB_EFFECTS.register("warp_ward", arcaneresearch.common.lib.potions.PotionWarpWard::new);

    public static final DeferredHolder<MobEffect, MobEffect> UNNATURAL_HUNGER =
            MOB_EFFECTS.register("unnatural_hunger", arcaneresearch.common.lib.potions.PotionUnnaturalHunger::new);

    public static final DeferredHolder<MobEffect, MobEffect> THAUMARHIA =
            MOB_EFFECTS.register("thaumarhia", arcaneresearch.common.lib.potions.PotionThaumarhia::new);

    public static final DeferredHolder<MobEffect, MobEffect> SUN_SCORNED =
            MOB_EFFECTS.register("sun_scorned", arcaneresearch.common.lib.potions.PotionSunScorned::new);

    public static final DeferredHolder<MobEffect, MobEffect> DEATH_GAZE =
            MOB_EFFECTS.register("death_gaze", arcaneresearch.common.lib.potions.PotionDeathGaze::new);

    public static final DeferredHolder<MobEffect, MobEffect> BLURRED_VISION =
            MOB_EFFECTS.register("blurred_vision", arcaneresearch.common.lib.potions.PotionBlurredVision::new);

    public static final DeferredHolder<MobEffect, MobEffect> INFECTIOUS_VIS_EXHAUST =
            MOB_EFFECTS.register("infectious_vis_exhaust",
                    arcaneresearch.common.lib.potions.PotionInfectiousVisExhaust::new);
}
