package arcaneresearch.common.lib.aura;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;
public class AuraFX {
    private AuraFX() {
    }
    public static void nodeSparkle(Level level, BlockPos pos, int color) {
        if (level == null || !(level instanceof ServerLevel server) || pos == null) {
            return;
        }
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        server.sendParticles(new DustParticleOptions(new Vector3f(r, g, b), 1.0F),
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                6, 0.35D, 0.35D, 0.35D, 0.12D);
    }
}
