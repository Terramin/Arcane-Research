package arcaneresearch.client.fx.other;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;

import net.minecraft.resources.Identifier; 
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.client.fx.particles.ArcaneresearchParticle;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import arcaneresearch.client.fx.BeamEngine;
import arcaneresearch.client.fx.particles.TCSprites;
import net.minecraft.util.ARGB;


public class FXVoidStream extends SingleQuadParticle implements BeamEngine.BeamEffect {

    
    private static final Identifier VOID_TEX = Identifier.parse("textures/entity/end_portal/end_portal.png");

    private final double targetX, targetY, targetZ;
    private final double startX, startY, startZ;
    private final int seed;
    public int length;

    private final List<StreamPoint> points = new ArrayList<>();
    private int growing = -1;

    private static class StreamPoint {
        double x, y, z;
        float scale;

        StreamPoint(double x, double y, double z, float scale) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.scale = scale;
        }
    }

    public FXVoidStream(ClientLevel level, double x, double y, double z,
                        double tx, double ty, double tz,
                        int seed, float scale) {
        super(level, x, y, z, null);

        this.seed = seed;
        this.length = 40;

        this.targetX = tx;
        this.targetY = ty;
        this.targetZ = tz;

        double dx = tx - x;
        double dy = ty - y;
        double dz = tz - z;
        int base = (int) (Mth.sqrt((float) (dx * dx + dy * dy + dz * dz)) * 21.0f);
        if (base < 1) base = 1;
        this.lifetime = base * 2;

        this.xd = Mth.sin(seed / 4.0f) * 0.025;
        this.yd = Mth.sin(seed / 3.0f) * 0.025;
        this.zd = Mth.sin(seed / 2.0f) * 0.025;

        this.gravity = 0.2f;
        this.quadSize = (float) (scale * (1.0 + this.random.nextGaussian() * 0.15));

        this.points.add(new StreamPoint(0, 0, 0, 0.001f));
        this.points.add(new StreamPoint(0, 0, 0, 0.001f));

        this.startX = x;
        this.startY = y;
        this.startZ = z;

        
        
        
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime || this.length < 1) {
            this.remove();
            return;
        }

        this.yd += 0.01 * this.gravity;
        this.x += this.xd;
        this.y += this.yd;
        this.z += this.zd;

        this.xd *= 0.985;
        this.yd *= 0.985;
        this.zd *= 0.985;

        this.xd = Mth.clamp(this.xd, -0.04, 0.04);
        this.yd = Mth.clamp(this.yd, -0.04, 0.04);
        this.zd = Mth.clamp(this.zd, -0.04, 0.04);

        double dx = this.targetX - this.x;
        double dy = this.targetY - this.y;
        double dz = this.targetZ - this.z;
        double dist = Mth.sqrt((float)(dx * dx + dy * dy + dz * dz));

        if (dist < 1.0e-4) dist = 1.0e-4;

        dx /= dist;
        dy /= dist;
        dz /= dist;

        double d13 = 0.01;
        this.xd += dx * (d13 / Math.min(1.0, dist)) + this.random.nextGaussian() * 0.015;
        this.yd += dy * (d13 / Math.min(1.0, dist)) + this.random.nextGaussian() * 0.015;
        this.zd += dz * (d13 / Math.min(1.0, dist)) + this.random.nextGaussian() * 0.015;

        float scale = this.quadSize * (0.75f + Mth.sin((seed + this.age) / 2.0f) * 0.25f);
        if (dist < 0.5) {
            float f = Mth.sin((float)(dist * Math.PI / 2.0));
            scale *= f;
            this.quadSize *= f;
        }

        if (this.quadSize > 0.001f) {
            this.points.add(new StreamPoint(
                this.x - this.startX,
                this.y - this.startY,
                this.z - this.startZ,
                scale
            ));
        } else {
            if (this.growing < 0) {
                this.growing = this.age;
            }
            this.length--;
        }

        while (this.points.size() > this.length) {
            this.points.remove(0);
        }
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
        
        return BeamEngine.normalBeamNoDepth(VOID_TEX);
    }

    @Override
    
    public boolean beamTick() {
        tick();
        return !this.removed;
    }

    @Override
    public void emitQuads(VertexConsumer consumer, Camera camera, float partialTicks) {
        if (this.points.size() < 3) return;



        double camX = camera.position().x;
        double camY = camera.position().y;
        double camZ = camera.position().z;

        int n = this.points.size();
        float[] wx = new float[n];
        float[] wy = new float[n];
        float[] wz = new float[n];
        float[] wr = new float[n];
        for (int i = 0; i < n; i++) {
            StreamPoint p = this.points.get(i);
            float variance = 1.0f + Mth.sin((i + this.age) / 3.0f) * 0.2f;
            wx[i] = (float)(this.startX + p.x + Mth.sin((i + this.age) / 6.0f) * 0.01f - camX);
            wy[i] = (float)(this.startY + p.y + Mth.sin((i + this.age) / 7.0f) * 0.01f - camY);
            wz[i] = (float)(this.startZ + p.z + Mth.sin((i + this.age) / 8.0f) * 0.01f - camZ);
            wr[i] = p.scale * variance;
        }

        float baseScale = this.quadSize;
        for (int i = 0; i < n; i++) {
            int head = n - 1 - i;
            if (head == 0 || head == 1) {
                wr[i] = 0.0f;
            } else if (head == 2) {
                wr[i] = (baseScale * 0.5f + wr[i]) / 2.0f;
            } else if (head == 3) {
                wr[i] = (baseScale + wr[i]) / 2.0f;
            } else if (head == 4) {
                wr[i] = (baseScale + wr[i] * 2.0f) / 3.0f;
            }
            if (i < 9) {
                float fade = Mth.cos((11 - i) / 10.0f * ((float) Math.PI / 2.0f));
                if (fade < 0.0f) fade = 0.0f;
                wr[i] *= fade;
            }
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
            float tx = wx[p1] - wx[p0];
            float ty = wy[p1] - wy[p0];
            float tz = wz[p1] - wz[p0];
            float tlen = Mth.sqrt(tx * tx + ty * ty + tz * tz);
            float vx = -wx[i];
            float vy = -wy[i];
            float vz = -wz[i];
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
            if (clen < 1.0e-4f) {
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
        float time = (this.age + partialTicks) * 0.01f;
        float dist = 0.0f;
        for (int i = 0; i < n - 1; i++) {
            float v_x = wx[i + 1] - wx[i];
            float v_y = wy[i + 1] - wy[i];
            float v_z = wz[i + 1] - wz[i];
            float segLen = Mth.sqrt(
                    v_x * v_x
                    + v_y * v_y
                    + v_z * v_z);

            float r0 = wr[i];
            float r1 = wr[i + 1];

            float u0 = dist * 0.5f + time;
            dist += segLen;
            float u1 = dist * 0.5f + time;
            float v0 = dist * 0.1f;
            float v1 = v0 + 0.25f;

            consumer.addVertex(wx[i] - sx[i] * r0, wy[i] - sy[i] * r0, wz[i] - sz[i] * r0).setUv(u0, v1).setColor(1f, 1f, 1f, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(wx[i] + sx[i] * r0, wy[i] + sy[i] * r0, wz[i] + sz[i] * r0).setUv(u0, v0).setColor(1f, 1f, 1f, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(wx[i + 1] + sx[i + 1] * r1, wy[i + 1] + sy[i + 1] * r1, wz[i + 1] + sz[i + 1] * r1).setUv(u1, v0).setColor(1f, 1f, 1f, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(wx[i + 1] - sx[i + 1] * r1, wy[i + 1] - sy[i + 1] * r1, wz[i + 1] - sz[i + 1] * r1).setUv(u1, v1).setColor(1f, 1f, 1f, 1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 1.0F, 0.0F);
        }


    }

    public FXVoidStream setTCGravity(float value) {
        this.gravity = value;
        return this;
    }
}