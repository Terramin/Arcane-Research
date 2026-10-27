package arcaneresearch.common.world.maze;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class OuterLandsEffects extends DimensionSpecialEffects {

    private static final OuterLandsEffects INSTANCE = new OuterLandsEffects();
    private static final Vec3 FOG = computeFog();

    public OuterLandsEffects() {
        super(Float.NaN, false, SkyType.NORMAL, false, false);
    }

    private static Vec3 computeFog() {
        float[] fog = WorldProviderOuter.getFogColor();
        if (fog.length < 3) {
            return Vec3.ZERO;
        }
        return new Vec3(fog[0], fog[1], fog[2]);
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 biomeFogColor, float daylight) {
        return FOG;
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    @Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registrar {
        private Registrar() {
        }

        @SubscribeEvent
        public static void onRegisterEffects(RegisterDimensionSpecialEffectsEvent event) {
            if (event == null) {
                return;
            }
            event.register(WorldProviderOuter.EFFECTS, INSTANCE);
            event.register(ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "outer_lands"),
                    INSTANCE);
        }
    }
}