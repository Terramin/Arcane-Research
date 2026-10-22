package arcaneresearch.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.resources.Identifier; 
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.client.lib.events.handler.ParticleHandler;

public class ParticleEngine {
    public static Identifier particleTexture =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/particles.png");

    public static void addEffect(Level level, Particle fx) {
        if (level == null || !level.isClientSide() || fx == null) {
            return;
        }
        
        if (fx instanceof BeamEngine.BeamEffect beam) {
            BeamEngine.add(beam);
            return;
        }
        Minecraft.getInstance().particleEngine.add(fx);
    }

    public static void addEffectWithDelay(Level level, Particle fx, int delay) {
        if (level == null || !level.isClientSide() || fx == null) {
            return;
        }
        ParticleHandler.schedule(() -> addEffect(level, fx), delay);
    }
}