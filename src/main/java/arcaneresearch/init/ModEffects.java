package arcaneresearch.init;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.potions.PotionFluxTaint;
import arcaneresearch.api.potions.PotionVisExhaust;
import arcaneresearch.common.lib.potions.*;
public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Arcaneresearch.MODID);
    public static final RegistryObject<MobEffect> FLUX_TAINT =
            MOB_EFFECTS.register("flux_taint", PotionFluxTaint::new);
    public static final RegistryObject<MobEffect> VIS_EXHAUST =
            MOB_EFFECTS.register("vis_exhaust", PotionVisExhaust::new);
    public static final RegistryObject<MobEffect> WARP_WARD =
            MOB_EFFECTS.register("warp_ward", PotionWarpWard::new);
    public static final RegistryObject<MobEffect> UNNATURAL_HUNGER =
            MOB_EFFECTS.register("unnatural_hunger", PotionUnnaturalHunger::new);
    public static final RegistryObject<MobEffect> THAUMARHIA =
            MOB_EFFECTS.register("thaumarhia", PotionThaumarhia::new);
    public static final RegistryObject<MobEffect> SUN_SCORNED =
            MOB_EFFECTS.register("sun_scorned", PotionSunScorned::new);
    public static final RegistryObject<MobEffect> DEATH_GAZE =
            MOB_EFFECTS.register("death_gaze", PotionDeathGaze::new);
    public static final RegistryObject<MobEffect> BLURRED_VISION =
            MOB_EFFECTS.register("blurred_vision", PotionBlurredVision::new);
    public static final RegistryObject<MobEffect> INFECTIOUS_VIS_EXHAUST =
            MOB_EFFECTS.register("infectious_vis_exhaust", PotionInfectiousVisExhaust::new);
}
