package arcaneresearch.common.lib.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

// NEOFORGE_PORT: 1:1 from Forge common/lib/aura/AuraFX.java.
// REQUIRED: DustParticleOptions(Vector3f, float) removed in 26.x ->
//   DustParticleOptions(int packedColor, float scale). Color math preserved.
public class AuraFX {
    private AuraFX() {
    }

    public static void nodeSparkle(Level level, BlockPos pos, int color) {
        if (level == null || !(level instanceof ServerLevel server) || pos == null) {
            return;
        }
        server.sendParticles(new DustParticleOptions(color, 1.0F),
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                6, 0.35D, 0.35D, 0.35D, 0.12D);
    }
}
