package arcaneresearch.client.fx.particles;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import arcaneresearch.client.fx.FXDispatcher;
import java.util.ArrayList;
import java.util.List;
@OnlyIn(Dist.CLIENT)
public class FXEssentiaTrail extends TextureSheetParticle {
    private final List<Vec3> trailPoints = new ArrayList<>();
    private final List<Float> trailRadii = new ArrayList<>();
    private double targetX;
    private double targetY;
    private double targetZ;
    private double startX;
    private double startY;
    private double startZ;
    private int maxLength;
    private int count;
    private boolean growing = true;
    private static final int PARTICLE_INDEX = 144;
    public FXEssentiaTrail(ClientLevel level, double sx, double sy, double sz,
                           double tx, double ty, double tz,
                           int color, float scale, int extend) {
        super(level, sx, sy, sz, 0, 0, 0);
        this.startX = sx;
        this.startY = sy;
        this.startZ = sz;
        this.targetX = tx;
        this.targetY = ty;
        this.targetZ = tz;
        this.rCol = ((color >> 16) & 0xFF) / 255.0f;
        this.gCol = ((color >> 8) & 0xFF) / 255.0f;
        this.bCol = (color & 0xFF) / 255.0f;
        this.quadSize = scale * (1.0f + (float)random.nextGaussian() * 0.15f);
        this.maxLength = Math.max(20, extend);
        this.count = 0;
        double dx = tx - sx;
        double dy = ty - sy;
        double dz = tz - sz;
        int base = (int)(Mth.sqrt((float)(dx * dx + dy * dy + dz * dz)) * 21.0f);
        if (base < 1) base = 1;
        this.lifetime = base + maxLength;
        this.xd = Mth.sin(count / 4.0f) * 0.015f;
        this.yd = Mth.sin(count / 3.0f) * 0.015f;
        this.zd = Mth.sin(count / 2.0f) * 0.015f;
        this.gravity = 0.2f;
        trailPoints.add(new Vec3(0, 0, 0));
        trailPoints.add(new Vec3(0, 0, 0));
        trailRadii.add(0.001f);
        trailRadii.add(0.001f);
    }
    @Override
    public void tick() {
        this.xo = x;
        this.yo = y;
        this.zo = z;
        if (this.age++ >= this.lifetime || maxLength < 1) {
            this.remove();
            return;
        }
        yd += 0.01 * gravity;
        move(xd, yd, zd);
        xd *= 0.985;
        yd *= 0.985;
        zd *= 0.985;
        xd = Mth.clamp((float)xd, -0.05f, 0.05f);
        yd = Mth.clamp((float)yd, -0.05f, 0.05f);
        zd = Mth.clamp((float)zd, -0.05f, 0.05f);
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        double dist = Mth.sqrt((float)(dx * dx + dy * dy + dz * dz));
        double accel = 0.01;
        if (dist < 1.0e-4) { dist = 1.0e-4; }
        dx /= dist;
        dy /= dist;
        dz /= dist;
        xd += dx * (accel / Math.min(1.0, dist));
        yd += dy * (accel / Math.min(1.0, dist));
        zd += dz * (accel / Math.min(1.0, dist));
        float currentScale = quadSize * (0.75f + Mth.sin((count + age) / 2.0f) * 0.25f);
        if (dist < 1.0) {
            float f = Mth.sin((float)(dist * Math.PI / 2.0));
            currentScale *= f;
            quadSize *= f;
        }
        if (quadSize > 0.001) {
            Vec3 relPos = new Vec3(x - startX, y - startY, z - startZ);
            trailPoints.add(relPos);
            trailRadii.add(currentScale);
            growing = true;
        } else {
            if (growing) {
                growing = false;
            }
            maxLength--;
            FXDispatcher.INSTANCE.essentiaDropFx(
                    targetX + random.nextGaussian() * 0.075,
                    targetY + random.nextGaussian() * 0.075,
                    targetZ + random.nextGaussian() * 0.075,
                    rCol, gCol, bCol, 0.5f);
        }
        while (trailPoints.size() > maxLength) {
            trailPoints.remove(0);
            trailRadii.remove(0);
        }
        if (trailPoints.size() > 2 && random.nextBoolean()) {
            int idx = random.nextInt(3);
            if (random.nextBoolean()) {
                idx = trailPoints.size() - 2;
            }
            if (idx < trailPoints.size()) {
                Vec3 p = trailPoints.get(idx);
                FXDispatcher.INSTANCE.essentiaDropFx(
                        p.x + startX, p.y + startY, p.z + startZ,
                        rCol, gCol, bCol, 0.5f);
            }
        }
        count++;
    }
    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        if (trailPoints.size() < 3) return;
        Vec3 camPos = camera.getPosition();
        Quaternionf rotation = camera.rotation();
        for (int i = 0; i < trailPoints.size(); i++) {
            Vec3 point = trailPoints.get(i);
            float radius = trailRadii.get(i);
            float variance = 1.0f + Mth.sin((i + age) / 3.0f) * 0.2f;
            float wobbleX = Mth.sin((i + age) / 6.0f) * 0.03f;
            float wobbleY = Mth.sin((i + age) / 7.0f) * 0.03f;
            float wobbleZ = Mth.sin((i + age) / 8.0f) * 0.03f;
            float px = (float)(startX + point.x + wobbleX - camPos.x());
            float py = (float)(startY + point.y + wobbleY - camPos.y());
            float pz = (float)(startZ + point.z + wobbleZ - camPos.z());
            float size = radius * variance;
            if (i > trailPoints.size() - 10) {
                size *= Mth.cos((float)((i - (trailPoints.size() - 12)) / 10.0f * Math.PI / 2.0));
            }
            if (i < 5) {
                size *= i / 5.0f;
            }
            if (size < 0.001f) continue;
            Vector3f[] vertices = new Vector3f[] {
                new Vector3f(-1.0f, -1.0f, 0.0f),
                new Vector3f(-1.0f, 1.0f, 0.0f),
                new Vector3f(1.0f, 1.0f, 0.0f),
                new Vector3f(1.0f, -1.0f, 0.0f)
            };
            for (Vector3f vertex : vertices) {
                vertex.rotate(rotation);
                vertex.mul(size * 0.1f);
                vertex.add(px, py, pz);
            }
            float u0 = (PARTICLE_INDEX % 64) / 64.0f;
            float u1 = u0 + 0.015625f;
            float v0 = (PARTICLE_INDEX / 64) / 64.0f;
            float v1 = v0 + 0.015625f;
            float colorVar = 1.0f - Mth.sin((i + age) / 2.0f) * 0.1f;
            int light = 0xF000F0;
            buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z())
                    .uv(u1, v1).color(rCol * colorVar, gCol * colorVar, bCol * colorVar, 1.0f)
                    .uv2(light).endVertex();
            buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
                    .uv(u1, v0).color(rCol * colorVar, gCol * colorVar, bCol * colorVar, 1.0f)
                    .uv2(light).endVertex();
            buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
                    .uv(u0, v0).color(rCol * colorVar, gCol * colorVar, bCol * colorVar, 1.0f)
                    .uv2(light).endVertex();
            buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
                    .uv(u0, v1).color(rCol * colorVar, gCol * colorVar, bCol * colorVar, 1.0f)
                    .uv2(light).endVertex();
        }
    }
    @Override
    public ParticleRenderType getRenderType() {
        return FXGeneric.TC_ADDITIVE;
    }
}
