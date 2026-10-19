package arcaneresearch.client.fx.beams;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.fx.BeamEngine;
import arcaneresearch.client.fx.particles.TCSprites;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// NEOFORGE_PORT: 1:1 logic from Forge 1.20.1 (points/tick/widths/colors kept).
// REQUIRED changes: TextureSheetParticle removed -> SingleQuadParticle + BeamEngine.BeamEffect;
// immediate-mode render (Tesselator/RenderSystem/GameRenderer) removed -> emitQuads() submits
// world-space ribbon quads through BeamEngine (SubmitCustomGeometryEvent); texture binds via
// the RenderType (raw texture, same UV fractions as Forge); fullbright via emissive type.
public class FXBolt extends SingleQuadParticle implements BeamEngine.BeamEffect {

    protected static final Identifier BEAM_TEXTURE =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/essentia.png");
    protected static final double BEHIND_START = 1.0;
    protected static final double FIRST_PERSON_DIST = 1.0;
    protected float beamWidth;
    protected List<Vec3> points = new ArrayList<>();
    protected List<Float> pointWidths = new ArrayList<>();
    protected float distanceRandom;
    protected long seed;
    protected double targetX;
    protected double targetY;
    protected double targetZ;
    protected float length;

    public FXBolt(ClientLevel level, double x, double y, double z,
                  double targetX, double targetY, double targetZ,
                  float r, float g, float b, float width) {
        super(level, x, y, z, 0, 0, 0, null); // NEOFORGE_PORT: sprite unused (custom UVs + raw layer)

        this.rCol = r;
        this.gCol = g;
        this.bCol = b;

        this.beamWidth = width;
        this.setSize(0.02f, 0.02f);
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;

        this.targetX = targetX - x;
        this.targetY = targetY - y;
        this.targetZ = targetZ - z;

        this.lifetime = 3;

        this.setBoundingBox(new net.minecraft.world.phys.AABB(
                Math.min(x, targetX) - 1.0, Math.min(y, targetY) - 1.0, Math.min(z, targetZ) - 1.0,
                Math.max(x, targetX) + 1.0, Math.max(y, targetY) + 1.0, Math.max(z, targetZ) + 1.0));

        Vec3 start = Vec3.ZERO;
        Vec3 end = new Vec3(this.targetX, this.targetY, this.targetZ);
        this.length = (float) (end.length() * Math.PI);

        this.seed = this.random.nextInt(1000);
        this.distanceRandom = (float) (this.random.nextInt(50) * Math.PI);

        calculatePoints(0);
    }

    protected void calculatePoints(float partialTicks) {
        Random rr = new Random(seed);
        points.clear();
        pointWidths.clear();

        Vec3 start = Vec3.ZERO;
        Vec3 end = new Vec3(this.targetX, this.targetY, this.targetZ);

        int steps = (int) this.length;
        if (steps < 2) steps = 2;

        points.add(start);
        pointWidths.add(this.beamWidth);

        float amplitude = (this.age + partialTicks) / 10.0f;

        for (int i = 1; i < steps - 1; i++) {
            float dist = i * (this.length / steps) + this.distanceRandom;

            double dx = this.targetX / steps * i + Mth.sin(dist / 4.0f) * amplitude;
            double dy = this.targetY / steps * i + Mth.sin(dist / 3.0f) * amplitude;
            double dz = this.targetZ / steps * i + Mth.sin(dist / 2.0f) * amplitude;

            dx += (rr.nextFloat() - rr.nextFloat()) * 0.1f;
            dy += (rr.nextFloat() - rr.nextFloat()) * 0.1f;
            dz += (rr.nextFloat() - rr.nextFloat()) * 0.1f;

            points.add(new Vec3(dx, dy, dz));

            float widthMod = (rr.nextInt(4) == 0) ? (1.0f - this.age * 0.25f) : 1.0f;
            pointWidths.add(this.beamWidth * widthMod);
        }

        points.add(end);
        pointWidths.add(this.beamWidth);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    // NEOFORGE_PORT: engine must NOT submit this (BeamEngine does); NO_RENDER skips grouping.
    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTicks) {
    }

    @Override
    public ParticleRenderType getGroup() {
        return ParticleRenderType.NO_RENDER;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TCSprites.translucent();
    }

    @Override
    public RenderType beamRenderType() {
        // NEOFORGE_PORT: old blendFunc(770, 1) additive + fullbright -> emissive translucent.
        return BeamEngine.additiveBeam(BEAM_TEXTURE);
    }

    @Override
    // NEOFORGE_PORT: BeamEngine drives ticking (engine never sees these).
    public boolean beamTick() {
        tick();
        return !this.removed;
    }

    @Override
    public void emitQuads(VertexConsumer consumer, Camera camera, float partialTicks) {
        calculatePoints(partialTicks);

        if (points.size() <= 2) {
            return;
        }

        Vec3 cameraPos = camera.position();
        float px = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float py = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float pz = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        int n = points.size();
        double[] sx = new double[n];
        double[] sy = new double[n];
        double[] sz = new double[n];
        for (int a = 0; a < n; a++) {
            Vec3 pPrev = points.get(Math.max(0, a - 1));
            Vec3 pNext = points.get(Math.min(n - 1, a + 1));
            double dx = pNext.x - pPrev.x;
            double dy = pNext.y - pPrev.y;
            double dz = pNext.z - pPrev.z;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len < 1.0e-6) {
                dx = 1;
                dy = 0;
                dz = 0;
                len = 1.0;
            }
            dx /= len;
            dy /= len;
            dz /= len;
            double mx = px + (points.get(a).x);
            double my = py + (points.get(a).y);
            double mz = pz + (points.get(a).z);
            double vx = -mx;
            double vy = -my;
            double vz = -mz;
            double vl = Math.sqrt(vx * vx + vy * vy + vz * vz);
            if (vl < 1.0e-6) {
                vx = 0;
                vy = 1;
                vz = 0;
                vl = 1.0;
            }
            vx /= vl;
            vy /= vl;
            vz /= vl;
            double crossX = dy * vz - dz * vy;
            double crossY = dz * vx - dx * vz;
            double crossZ = dx * vy - dy * vx;
            double cl = Math.sqrt(crossX * crossX + crossY * crossY + crossZ * crossZ);
            if (cl < 1.0e-6) {
                crossX = 1;
                crossY = 0;
                crossZ = 0;
                cl = 1.0;
            }
            sx[a] = crossX / cl;
            sy[a] = crossY / cl;
            sz[a] = crossZ / cl;
        }

        double camDist = Math.sqrt(px * px + py * py + pz * pz);
        double ol = Math.sqrt(this.targetX * this.targetX
                + this.targetY * this.targetY + this.targetZ * this.targetZ);
        boolean tail = camDist < FIRST_PERSON_DIST && ol > 1.0e-6;
        double tailX = 0.0;
        double tailY = 0.0;
        double tailZ = 0.0;
        if (tail) {
            tailX = px - this.targetX / ol * BEHIND_START;
            tailY = py - this.targetY / ol * BEHIND_START;
            tailZ = pz - this.targetZ / ol * BEHIND_START;
        }

        // NEOFORGE_PORT: POSITION_TEX_COLOR immediate quads -> new-API calls
        // (addVertex/setUv/setColor(ARGB); no endVertex; state via RenderType).
        for (int pass = 0; pass < 2; pass++) {
            float widthScale = (pass == 0) ? 0.15f : 0.15f / 3.0f;
            float passAlpha = 1.0f;
            float pr = this.rCol;
            float pg = this.gCol;
            float pb = this.bCol;
            int argb = net.minecraft.util.ARGB.color((int) ((passAlpha) * 255.0F), (int) ((pr) * 255.0F), (int) ((pg) * 255.0F), (int) ((pb) * 255.0F));
            if (tail) {
                Vec3 p0 = points.get(0);
                float w0 = pointWidths.get(0) * widthScale;
                double x0 = px + p0.x;
                double y0 = py + p0.y;
                double z0 = pz + p0.z;
                quad(consumer, tailX - sx[0] * w0, tailY - sy[0] * w0, tailZ - sz[0] * w0, 0, 0, argb,
                        tailX + sx[0] * w0, tailY + sy[0] * w0, tailZ + sz[0] * w0, 0, 1, argb,
                        x0 + sx[0] * w0, y0 + sy[0] * w0, z0 + sz[0] * w0, 0, 1, argb,
                        x0 - sx[0] * w0, y0 - sy[0] * w0, z0 - sz[0] * w0, 0, 0, argb);
            }
            for (int a = 0; a < n - 1; a++) {
                Vec3 p0 = points.get(a);
                Vec3 p1 = points.get(a + 1);
                float w0 = pointWidths.get(a) * widthScale;
                float w1 = pointWidths.get(a + 1) * widthScale;

                double x0 = px + p0.x;
                double y0 = py + p0.y;
                double z0 = pz + p0.z;
                double x1 = px + p1.x;
                double y1 = py + p1.y;
                double z1 = pz + p1.z;

                float u0 = a / (float) (n - 1);
                float u1 = (a + 1) / (float) (n - 1);
                quad(consumer, x0 - sx[a] * w0, y0 - sy[a] * w0, z0 - sz[a] * w0, u0, 0, argb,
                        x0 + sx[a] * w0, y0 + sy[a] * w0, z0 + sz[a] * w0, u0, 1, argb,
                        x1 + sx[a + 1] * w1, y1 + sy[a + 1] * w1, z1 + sz[a + 1] * w1, u1, 1, argb,
                        x1 - sx[a + 1] * w1, y1 - sy[a + 1] * w1, z1 - sz[a + 1] * w1, u1, 0, argb);
            }
        }
    }

    private static void quad(VertexConsumer consumer,
            double x0, double y0, double z0, float u0, float v0, int argb,
            double x1, double y1, double z1, float u1, float v1, int argb1,
            double x2, double y2, double z2, float u2, float v2, int argb2,
            double x3, double y3, double z3, float u3, float v3, int argb3) {
        // NEOFORGE_PORT (crash fix): full vertex for ENTITY pipelines
        // (overlay + fullbright + normal; see FXLightning.quad).
        consumer.addVertex((float) x0, (float) y0, (float) z0).setUv(u0, v0).setColor(argb)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex((float) x1, (float) y1, (float) z1).setUv(u1, v1).setColor(argb1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex((float) x2, (float) y2, (float) z2).setUv(u2, v2).setColor(argb2)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
        consumer.addVertex((float) x3, (float) y3, (float) z3).setUv(u3, v3).setColor(argb3)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
    }

    public void setRGB(float r, float g, float b) {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
    }

    public void setWidth(float width) {
        this.beamWidth = width;
    }

    public List<Vec3> getPoints() {
        return points;
    }

    public float getLength() {
        return length;
    }
}