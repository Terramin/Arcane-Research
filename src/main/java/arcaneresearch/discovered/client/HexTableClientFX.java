package arcaneresearch.discovered.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import arcaneresearch.client.fx.particles.FXGeneric;

/**
 * Client half of {@code HexNoteTableBlock.animateTick} (TC4 BlockResearchTable sparkles).
 *
 * NEOFORGE_PORT: @OnlyIn no longer strips members in NeoForge 26, so the particle code
 * (FXGeneric -> Particle) lives here; the common block only calls in on the client side
 * and never links client classes on a dedicated server.
 */
public final class HexTableClientFX {
    private HexTableClientFX() {
    }

    public static void researchSparkle(Level level, BlockPos pos, RandomSource random) {
        double xx = random.nextGaussian() / 2.0;
        double zz = random.nextGaussian() / 2.0;
        double yy = 1.5 + random.nextFloat();
        int a = 40 + random.nextInt(20);
        FXGeneric fb = new FXGeneric((ClientLevel) level,
                pos.getX() + 0.5 + xx, pos.getY() + yy, pos.getZ() + 0.5 + zz,
                -xx / a, -(yy - 0.85) / a, -zz / a);
        fb.setMaxAge(a);
        fb.setColor(0.5f + random.nextFloat() * 0.5f,
                0.5f + random.nextFloat() * 0.5f,
                0.5f + random.nextFloat() * 0.5f);
        fb.setAlphaKeyframes(0.0f, 0.25f, 0.5f, 0.75f, 0.0f);
        fb.setParticles(384 + random.nextInt(16), 1, 1);
        fb.setScaleKeyframes((0.8f + random.nextFloat() * 0.3f) * 0.1f, 0.3f * 0.1f);
        fb.setLayer(0);
        Minecraft.getInstance().particleEngine.add(fb);
    }
}
