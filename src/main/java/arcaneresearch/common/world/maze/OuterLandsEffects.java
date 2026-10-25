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
@OnlyIn(Dist.CLIENT)
public class OuterLandsEffects extends DimensionSpecialEffects {
    public OuterLandsEffects() {
        super(Float.NaN, false, SkyType.NORMAL, false, false);
    }
    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 biomeFogColor, float daylight) {
        float[] fog = WorldProviderOuter.getFogColor();
        return new Vec3(fog[0], fog[1], fog[2]);
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
            event.register(WorldProviderOuter.EFFECTS, new OuterLandsEffects());
            event.register(new ResourceLocation(Arcaneresearch.MODID, "outer_lands"), new OuterLandsEffects());
        }
    }
}
