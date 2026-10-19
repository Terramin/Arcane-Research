package arcaneresearch.client.fx;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.client.lib.events.handler.ParticleHandler;
@OnlyIn(Dist.CLIENT)
public class ParticleEngine {
    public static ResourceLocation particleTexture =
            new ResourceLocation("arcaneresearch", "textures/misc/particles.png");
    public static void addEffect(Level level, Particle fx) {
        if (level == null || !level.isClientSide || fx == null) {
            return;
        }
        Minecraft.getInstance().particleEngine.add(fx);
    }
    public static void addEffectWithDelay(Level level, Particle fx, int delay) {
        if (level == null || !level.isClientSide || fx == null) {
            return;
        }
        ParticleHandler.schedule(() -> Minecraft.getInstance().particleEngine.add(fx), delay);
    }
}
