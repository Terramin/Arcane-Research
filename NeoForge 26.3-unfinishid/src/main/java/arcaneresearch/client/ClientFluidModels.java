package arcaneresearch.client;

import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.init.ModFluids;

// NEOFORGE_PORT: client visuals for Forge 1.20.1 ModFluids.initializeClient overrides.
// Forge had per-FluidType getStillTexture/getFlowingTexture/getTintColor; 26.3 wires the
// SAME textures + SAME tint values via RegisterFluidModelsEvent (FluidModel.Unbaked +
// BlockTintSources.constant). No new textures, no new colors, no behavior change.
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public final class ClientFluidModels {

    private ClientFluidModels() {
    }

    @SubscribeEvent
    public static void onRegisterFluidModels(RegisterFluidModelsEvent event) {
        Material deathTexture = new Material(ModFluids.LIQUID_DEATH_STILL);
        event.register(
                new FluidModel.Unbaked(deathTexture, deathTexture, null,
                        BlockTintSources.constant(ModFluids.LIQUID_DEATH_TINT)),
                ModFluids.LIQUID_DEATH.get(), ModFluids.LIQUID_DEATH_FLOWING.get());

        event.register(
                new FluidModel.Unbaked(new Material(ModFluids.PURIFYING_STILL),
                        new Material(ModFluids.PURIFYING_FLOWING), null,
                        BlockTintSources.constant(ModFluids.PURIFYING_TINT)),
                ModFluids.PURIFYING_FLUID.get(), ModFluids.PURIFYING_FLUID_FLOWING.get());

        Material fluxTexture = new Material(ModFluids.FLUX_GAS_STILL);
        event.register(
                new FluidModel.Unbaked(fluxTexture, fluxTexture, null,
                        BlockTintSources.constant(ModFluids.FLUX_GAS_TINT)),
                ModFluids.FLUX_GAS.get(), ModFluids.FLUX_GAS_FLOWING.get());
    }
}
