package arcaneresearch.client.fx.other;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.fx.FXDispatcher;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import arcaneresearch.client.fx.BeamEngine;
import arcaneresearch.client.fx.particles.TCSprites;
import net.minecraft.util.ARGB;

// NEOFORGE_PORT: custom geometry -> BeamEngine (see BeamEngine.BeamEffect).
public class FXTimedStream extends SingleQuadParticle implements BeamEngine.BeamEffect {

    private static final Identifier ESSENTIA_TEX = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/essentia.png");

    private static final float STEP = 0.35f;
    private static final float BASE_WIDTH = 0.09f;

    private List<Vec3> path;
    private double totalLen;
    private int duration;
    private long startTick;

    private float renderAge = -1.0f;
    private long lastFrameNanos = -1L;

    public FXTimedStream(ClientLevel level, List<Vec3> path, int color, int duration, long startTick) {
        super(level, path.get(path.size() - 1).x, path.get(path.size() - 1).y, path.get(path.size() - 1).z, 0.0, 0.0, 0.0, null); // NEOFORGE_PORT: sprite unused (custom UVs + raw layer)
        this.path = new ArrayList<>(path);
        double len = 0.0;
        for (int i = 0; i < this.path.size() - 1; i++) {
            len += this.path.get(i).distanceTo(this.path.get(i + 1));
        }
        this.totalLen = len;
        this.duration = Math.max(1, duration);
        this.startTick = startTick;

        Color c = new Color(color);
        this.rCol = c.getRed() / 255.0f;
        this.gCol = c.getGreen() / 255.0f;
        this.bCol = c.getBlue() / 255.0f;
    }

    private static float clamp01(float t) {
        return t < 0.0f ? 0.0f : (Math.min(t, 1.0f));
    }

    private static float smootherstep(float t) {
        t = clamp01(t);
        return t * t * t * (t * (t * 6.0f - 15.0f) + 10.0f);
    }

    public void update(List<Vec3> waypoints, int duration, long startTick) {
        this.path = new ArrayList<>(waypoints);
        double len = 0.0;
        for (int i = 0; i < this.path.size() - 1; i++) {
            len += this.path.get(i).distanceTo(this.path.get(i + 1));
        }
        this.totalLen = len;
        this.duration = Math.max(1, duration);
        this.startTick = startTick;
    }

    private int age() {
        return (int) Math.max(0.0f, FXDispatcher.serverAge(this.startTick, 0.0f));
    }

    private float displayAge(float partialTicks) {
        float target = FXDispatcher.serverAge(this.startTick, partialTicks);
        long now = System.nanoTime();
        if (renderAge < 0.0f || lastFrameNanos < 0L) {
            renderAge = target;
            lastFrameNanos = now;
            return renderAge;
        }
        float dt = (now - lastFrameNanos) / 50_000_000.0f;
        lastFrameNanos = now;
        if (dt < 0.0f) dt = 0.0f;
        if (dt > 5.0f) dt = 5.0f;
        float diff = target - renderAge;
        if (diff > 10.0f || diff < -10.0f) {
            renderAge = target;
        } else {
            renderAge += diff * (1.0f - (float) Math.exp(-dt * 0.5f));
        }
        return renderAge;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (age() >= this.duration) {
            this.remove();
        }
    }

    private Vec3 pointAt(double dist) {
        double d = Mth.clamp((float) dist, 0.0f, (float) this.totalLen);
        for (int i = 0; i < this.path.size() - 1; i++) {
            double seg = this.path.get(i).distanceTo(this.path.get(i + 1));
            if (d <= seg || i == this.path.size() - 2) {
                double t = seg < 1.0e-6 ? 0.0 : d / seg;
                Vec3 a = this.path.get(i);
                Vec3 b = this.path.get(i + 1);
                return new Vec3(
                        a.x + (b.x - a.x) * t,
                        a.y + (b.y - a.y) * t,
                        a.z + (b.z - a.z) * t);
            }
            d -= seg;
        }
        return this.path.get(this.path.size() - 1);
    }

    @Override
public void extract(QuadParticleRenderState state, Camera camera, float partialTicks) {
    }

    @Override
    public net.minecraft.client.particle.ParticleRenderType getGroup() {
        return net.minecraft.client.particle.ParticleRenderType.NO_RENDER;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TCSprites.translucent();
    }

    @Override
    public RenderType beamRenderType() {
        return BeamEngine.normalBeam(ESSENTIA_TEX);
    }

    @Override
    // NEOFORGE_PORT: BeamEngine drives ticking (engine never sees these).
    public boolean beamTick() {
        tick();
        return !this.removed;
    }

    @Override
    public void emitQuads(VertexConsumer consumer, Camera camera, float partialTicks) {
        float ageF = displayAge(partialTicks);
        if (ageF < 0 || ageF >= this.duration || this.totalLen < 1.0e-4) {
            return;
        }

        float headTime = this.duration * 0.75f;
        float headDist = (float) (this.totalLen * smootherstep(clamp01(ageF / headTime)));
        float tailDist = ageF <= headTime ? 0.0f
                : (float) (this.totalLen * smootherstep((ageF - headTime) / (this.duration - headTime)));
        if (headDist - tailDist < 0.3f) {
            return;
        }


        double camX = camera.position().x;
        double camY = camera.position().y;
        double camZ = camera.position().z;

        List<float[]> joints = new ArrayList<>();
        joints.add(new float[]{(float) (pointAt(tailDist).x - camX),
                (float) (pointAt(tailDist).y - camY),
                (float) (pointAt(tailDist).z - camZ), tailDist});
        float d = tailDist + STEP;
        while (d < headDist - 1.0e-4f) {
            Vec3 p = pointAt(d);
            joints.add(new float[]{(float) (p.x - camX), (float) (p.y - camY), (float) (p.z - camZ), d});
            d += STEP;
        }
        joints.add(new float[]{(float) (pointAt(headDist).x - camX),
                (float) (pointAt(headDist).y - camY),
                (float) (pointAt(headDist).z - camZ), headDist});
        int n = joints.size();

        float[] wr = new float[n];
        for (int i = 0; i < n; i++) {
            float dHead = headDist - joints.get(i)[3];
            float dTail = joints.get(i)[3] - tailDist;
            float hf = smootherstep(clamp01(dHead / 1.4f));
            float tf = 0.31f + 0.69f * smootherstep(clamp01(dTail / 3.15f));
            wr[i] = BASE_WIDTH * hf * tf;
        }

        float[] sx = new float[n];
        float[] sy = new float[n];
        float[] sz = new float[n];
        float psx = 1.0f;
        float psy = 0.0f;
        float psz = 0.0f;
        for (int i = 0; i < n; i++) {
            int p0 = Math.max(0, i - 1);
            int p1 = Math.min(n - 1, i + 1);
            float tx = joints.get(p1)[0] - joints.get(p0)[0];
            float ty = joints.get(p1)[1] - joints.get(p0)[1];
            float tz = joints.get(p1)[2] - joints.get(p0)[2];
            float tlen = Mth.sqrt(tx * tx + ty * ty + tz * tz);
            float vx = -joints.get(i)[0];
            float vy = -joints.get(i)[1];
            float vz = -joints.get(i)[2];
            float vlen = Mth.sqrt(vx * vx + vy * vy + vz * vz);
            if (vlen < 1.0e-4f) {
                vx = 0.0f;
                vy = 0.0f;
                vz = 1.0f;
                vlen = 1.0f;
            }
            float cx = 0.0f;
            float cy = 0.0f;
            float cz = 0.0f;
            float clen = 0.0f;
            if (tlen > 1.0e-4f) {
                tx /= tlen;
                ty /= tlen;
                tz /= tlen;
                vx /= vlen;
                vy /= vlen;
                vz /= vlen;
                cx = ty * vz - tz * vy;
                cy = tz * vx - tx * vz;
                cz = tx * vy - ty * vx;
                clen = Mth.sqrt(cx * cx + cy * cy + cz * cz);
            }
            if (clen < 0.05f) {
                cx = psx;
                cy = psy;
                cz = psz;
            } else {
                cx /= clen;
                cy /= clen;
                cz /= clen;
                psx = cx;
                psy = cy;
                psz = cz;
            }
            sx[i] = cx;
            sy[i] = cy;
            sz[i] = cz;
        }

        float r = this.rCol;
        float g = this.gCol;
        float b = this.bCol;

        float alpha = Math.min(1.0f, ageF / 6.0f);
        for (int pass = 0; pass < 2; pass++) {

            float widen = (pass == 0) ? 1.0f : 1.04f;
            for (int i = 0; i < n - 1; i++) {
                float[] a = joints.get(i);
                float[] c = joints.get(i + 1);

                float wobA = Mth.sin(a[3] * 2.0f - ageF * 0.5f) * 0.03f;
                float wobC = Mth.sin(c[3] * 2.0f - ageF * 0.5f) * 0.03f;
                float ax = a[0] + wobA;
                float ay = a[1] + wobA * 0.7f;
                float az = a[2] - wobA;
                float cx2 = c[0] + wobC;
                float cy2 = c[1] + wobC * 0.7f;
                float cz2 = c[2] - wobC;
                float u0 = a[3] * 0.5f;
                float u1 = c[3] * 0.5f;

                float r0 = wr[i] * widen * (1.0f + 0.06f * Mth.sin(a[3] * 1.5f - ageF * 0.3f));
                float r1 = wr[i + 1] * widen * (1.0f + 0.06f * Mth.sin(c[3] * 1.5f - ageF * 0.3f));
                consumer.addVertex(ax - sx[i] * r0, ay - sy[i] * r0, az - sz[i] * r0).setUv(u0, 1).setColor(r, g, b, alpha).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
                consumer.addVertex(ax + sx[i] * r0, ay + sy[i] * r0, az + sz[i] * r0).setUv(u0, 0).setColor(r, g, b, alpha).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
                consumer.addVertex(cx2 + sx[i + 1] * r1, cy2 + sy[i + 1] * r1, cz2 + sz[i + 1] * r1).setUv(u1, 0).setColor(r, g, b, alpha).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
                consumer.addVertex(cx2 - sx[i + 1] * r1, cy2 - sy[i + 1] * r1, cz2 - sz[i + 1] * r1).setUv(u1, 1).setColor(r, g, b, alpha).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
            }

            float capR0 = wr[0];
            if (capR0 > 1.0e-4f) {
                float[] a = joints.get(0);
                consumer.addVertex(a[0] - capR0, a[1] - capR0, a[2]).setUv(0, 1).setColor(r, g, b, alpha).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
                consumer.addVertex(a[0] - capR0, a[1] + capR0, a[2]).setUv(0, 0).setColor(r, g, b, alpha).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
                consumer.addVertex(a[0] + capR0, a[1] + capR0, a[2]).setUv(1, 0).setColor(r, g, b, alpha).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
                consumer.addVertex(a[0] + capR0, a[1] - capR0, a[2]).setUv(1, 1).setColor(r, g, b, alpha).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
            }
            float capR1 = wr[n - 1];
            if (capR1 > 1.0e-4f) {
                float[] a = joints.get(n - 1);
                consumer.addVertex(a[0] - capR1, a[1] - capR1, a[2]).setUv(0, 1).setColor(r, g, b, alpha).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
                consumer.addVertex(a[0] - capR1, a[1] + capR1, a[2]).setUv(0, 0).setColor(r, g, b, alpha).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
                consumer.addVertex(a[0] + capR1, a[1] + capR1, a[2]).setUv(1, 0).setColor(r, g, b, alpha).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
                consumer.addVertex(a[0] + capR1, a[1] - capR1, a[2]).setUv(1, 1).setColor(r, g, b, alpha).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
            }
        }


        if (ageF > this.duration * 0.75f && this.random.nextBoolean()) {
            Vec3 head = pointAt(headDist);
            FXDispatcher.INSTANCE.essentiaDropFx(
                    head.x + this.random.nextGaussian() * 0.075,
                    head.y + this.random.nextGaussian() * 0.075,
                    head.z + this.random.nextGaussian() * 0.075,
                    this.rCol, this.gCol, this.bCol, 0.5f);
        }
    }

}