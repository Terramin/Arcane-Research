package arcaneresearch.client.fx;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

// NEOFORGE_PORT: copied 1:1 from Forge 1.20.1 WandFX.java; mechanical fixes:
// level.isClientSide -> level.isClientSide(), level.random -> level.getRandom().
// Custom-geometry beams bypass vanilla gating via BeamEngine (as in Forge,
// where they went straight to particleEngine).
public class WandFX {
    private WandFX() {
    }

    public static void sparkle(Level level, float x, float y, float z, float size, int color, float gravity) {
        if (level == null || !level.isClientSide()) {
            return;
        }
        try {
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;
            arcaneresearch.client.fx.FXDispatcher.INSTANCE.drawGenericParticles(x, y, z,
                    (level.getRandom().nextFloat() - 0.5F) * 0.1F,
                    (level.getRandom().nextFloat() - 0.5F) * 0.1F - gravity,
                    (level.getRandom().nextFloat() - 0.5F) * 0.1F,
                    r, g, b, 0.9F, true, 320, 16, 1,
                    6 + level.getRandom().nextInt(4), 0,
                    Math.max(0.2F, size * 0.3F), 0.0F, 0);
        } catch (Exception ignored) {
        }
    }

    public static void arcBolt(Level level, double x, double y, double z, double tx, double ty, double tz) {
        if (level == null || !level.isClientSide()) {
            return;
        }
        try {
            arcaneresearch.client.fx.FXDispatcher.INSTANCE.arcBolt(x, y, z, tx, ty, tz, 0.5F, 1.0F, 1.0F, 1.0F);
            sparkle(level, (float) tx, (float) ty, (float) tz, 3.0F, 0x88CCFF, 0.0F);
        } catch (Exception ignored) {
        }
    }

    public static Object beamCont(Level level, LivingEntity caster, double tx, double ty, double tz,
                                  int type, int color, boolean reverse, float endmod, Object input, int impact) {
        if (level == null || caster == null || !level.isClientSide()
                || !(level instanceof net.minecraft.client.multiplayer.ClientLevel client)) {
            return input;
        }
        try {
            arcaneresearch.client.fx.beams.FXBeamWand beam =
                    input instanceof arcaneresearch.client.fx.beams.FXBeamWand b ? b : null;
            java.awt.Color c = new java.awt.Color(color);
            if (beam == null || !beam.isAlive()) {
                beam = new arcaneresearch.client.fx.beams.FXBeamWand(client, caster, tx, ty, tz,
                        c.getRed() / 255.0F, c.getGreen() / 255.0F, c.getBlue() / 255.0F, 8);
                beam.setType(type);
                beam.setEndMod(endmod);
                beam.setReverse(reverse);
                // NEOFORGE_PORT: custom geometry bypasses gating (as in Forge) via BeamEngine.
                arcaneresearch.client.fx.BeamEngine.add(beam);
            } else {
                beam.updateBeam(tx, ty, tz);
                beam.setEndMod(endmod);
                beam.impact = impact;
            }
            return beam;
        } catch (Exception ignored) {
            return input;
        }
    }

    public static void excavateFX(Level level, BlockPos pos, LivingEntity player, int progress) {
        if (level == null || pos == null) {
            return;
        }
        int clamped = Mth.clamp(progress, 0, 9);
        try {
            if (level.isClientSide()) {
                arcaneresearch.client.fx.FXDispatcher.INSTANCE.excavateFX(pos, player, clamped);
            } else if (level instanceof net.minecraft.server.level.ServerLevel server) {
                server.destroyBlockProgress(player == null ? 0 : player.getId(), pos, clamped);
            }
        } catch (Exception ignored) {
        }
    }

    public static void flameMote(Level level, double x, double y, double z,
                                 double mx, double my, double mz, float scale) {
        if (level == null || !level.isClientSide()) {
            return;
        }
        try {
            arcaneresearch.client.fx.FXDispatcher.GenPart pp =
                    new arcaneresearch.client.fx.FXDispatcher.GenPart();
            pp.grav = -0.2F;
            pp.age = 10;
            pp.alpha = new float[]{0.7F};
            pp.partStart = 640;
            pp.partInc = 1;
            pp.partNum = 10;
            pp.slowDown = 0.75;
            pp.scale = new float[]{(float) (scale + level.getRandom().nextGaussian() * 0.2)};
            arcaneresearch.client.fx.FXDispatcher.INSTANCE.drawGenericParticles(
                    x, y, z, mx, my, mz, pp);
        } catch (Exception ignored) {
        }
    }

    public static Vec3 beamStart(LivingEntity caster) {
        Vec3 look = caster.getLookAngle();
        double sx = caster.getX() + look.x * 0.5D;
        double sy = caster.getBoundingBox().minY + caster.getBbHeight() / 2.0F + 0.25D + look.y * 0.5D;
        double sz = caster.getZ() + look.z * 0.5D;
        return new Vec3(sx, sy, sz);
    }

    /**
     * Старт от кончика жезла (то же решение, что луч): в первом лице —
     * динамический кончик, иначе старый beamStart. Только для клиентских
     * вызовов (трогает клиентские классы).
     */
    public static Vec3 tipStart(LivingEntity caster) {
        if (caster == null || !caster.level().isClientSide()
                || !(caster instanceof net.minecraft.world.entity.player.Player player)) {
            return beamStart(caster);
        }
        try {
            float partial = 1.0F;
            try {
                // NEOFORGE_PORT: Minecraft.getFrameTime удалён -> DeltaTracker.
                partial = net.minecraft.client.Minecraft.getInstance().getDeltaTracker()
                        .getGameTimeDeltaPartialTick(false);
            } catch (Exception ignored) {
            }
            Vec3 tip = arcaneresearch.client.lib.events.WandTipHelper.tipWorld(player, partial);
            if (tip != null) {
                return tip;
            }
        } catch (Throwable ignored) {
        }
        return beamStart(caster);
    }
}