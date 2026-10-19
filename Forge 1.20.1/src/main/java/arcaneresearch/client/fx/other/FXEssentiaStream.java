package arcaneresearch.client.fx.other;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.client.fx.particles.ArcaneresearchParticle;
import arcaneresearch.common.lib.utils.StreamPathHelper;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@OnlyIn(Dist.CLIENT)
public class FXEssentiaStream extends ArcaneresearchParticle {
    private static final ResourceLocation ESSENTIA_TEX = new ResourceLocation(Arcaneresearch.MODID, "textures/misc/essentia.png");
    private static final Map<String, FXEssentiaStream> activeStreams = new HashMap<>();
    private final double targetX, targetY, targetZ;
    private final double startX, startY, startZ;
    private final int count;
    private final String key;
    private final List<StreamPoint> points = new ArrayList<>();
    public int length;
    private List<Vec3> waypoints = new ArrayList<>();
    private int waypointIndex = 1;
    private int growing = -1;
    public FXEssentiaStream(ClientLevel level, double x, double y, double z,
                            double tx, double ty, double tz,
                            int count, int color, float scale, int extend, double my) {
        super(level, x, y, z);
        this.count = count;
        this.length = Math.max(20, extend);
        this.targetX = tx;
        this.targetY = ty;
        this.targetZ = tz;
        this.waypoints = StreamPathHelper.findPath(level,
                new Vec3(x, y, z), new Vec3(tx, ty, tz));
        this.waypointIndex = 1;
        double pathLen = 0.0;
        for (int wi = 0; wi < this.waypoints.size() - 1; wi++) {
            pathLen += this.waypoints.get(wi).distanceTo(this.waypoints.get(wi + 1));
        }
        int base = (int) (pathLen * 21.0f);
        if (base < 1) base = 1;
        this.lifetime = base;
        BlockPos bp1 = BlockPos.containing(x, y, z);
        BlockPos bp2 = BlockPos.containing(tx, ty, tz);
        this.key = bp1.asLong() + "" + bp2.asLong() + "" + color;
        if (activeStreams.containsKey(key)) {
            FXEssentiaStream existing = activeStreams.get(key);
            if (existing.isAlive() && existing.points.size() < existing.length) {
                existing.length += Math.max(extend, 5);
                existing.lifetime += Math.max(extend, 5);
                this.lifetime = 0;
            }
        }
        if (this.lifetime > 0) {
            activeStreams.put(key, this);
        }
        this.xd = Mth.sin(count / 4.0f) * 0.015;
        this.yd = my + Mth.sin(count / 3.0f) * 0.015;
        this.zd = Mth.sin(count / 2.0f) * 0.015;
        Color c = new Color(color);
        this.rCol = c.getRed() / 255.0f;
        this.gCol = c.getGreen() / 255.0f;
        this.bCol = c.getBlue() / 255.0f;
        this.gravity = 0.2f;
        this.quadSize = (float) (scale * (1.0 + this.random.nextGaussian() * 0.15));
        this.points.add(new StreamPoint(0, 0, 0, 0.001f));
        this.points.add(new StreamPoint(0, 0, 0, 0.001f));
        this.startX = x;
        this.startY = y;
        this.startZ = z;
        this.noClip = true;
    }
    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime || this.length < 1) {
            this.remove();
            if (activeStreams.containsKey(key) && !activeStreams.get(key).isAlive()) {
                activeStreams.remove(key);
            }
            return;
        }
        this.yd += 0.01 * this.gravity;
        this.x += this.xd;
        this.y += this.yd;
        this.z += this.zd;
        this.xd *= 0.985;
        this.yd *= 0.985;
        this.zd *= 0.985;
        this.xd = Mth.clamp(this.xd, -0.05, 0.05);
        this.yd = Mth.clamp(this.yd, -0.05, 0.05);
        this.zd = Mth.clamp(this.zd, -0.05, 0.05);
        Vec3 steer = null;
        if (waypointIndex < waypoints.size()) {
            steer = waypoints.get(waypointIndex);
        }
        double sx = (steer != null) ? steer.x : this.targetX;
        double sy = (steer != null) ? steer.y : this.targetY;
        double sz = (steer != null) ? steer.z : this.targetZ;
        double dx = sx - this.x;
        double dy = sy - this.y;
        double dz = sz - this.z;
        double wdist = Mth.sqrt((float) (dx * dx + dy * dy + dz * dz));
        double d13 = 0.01;
        if (wdist >= 1.0e-6) {
            dx /= wdist;
            dy /= wdist;
            dz /= wdist;
            this.xd += dx * (d13 / Math.min(1.0, wdist));
            this.yd += dy * (d13 / Math.min(1.0, wdist));
            this.zd += dz * (d13 / Math.min(1.0, wdist));
        }
        if (steer != null && waypointIndex < waypoints.size() - 1 && wdist < 0.7) {
            waypointIndex++;
        }
        double fdx = this.targetX - this.x;
        double fdy = this.targetY - this.y;
        double fdz = this.targetZ - this.z;
        double dist = Mth.sqrt((float) (fdx * fdx + fdy * fdy + fdz * fdz));
        float scale = this.quadSize * (0.75f + Mth.sin((count + this.age) / 2.0f) * 0.25f);
        if (dist < 1.0) {
            float f = Mth.sin((float) (dist * Math.PI / 2.0));
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
            int ticksLeft = this.lifetime - this.age;
            if (ticksLeft <= 1) {
                this.points.clear();
            } else {
                int target = (int) Math.ceil(this.points.size() * ((double) (ticksLeft - 1) / ticksLeft));
                while (this.points.size() > target + 1) {
                    this.points.remove(0);
                }
            }
            FXDispatcher.INSTANCE.essentiaDropFx(
                    this.targetX + this.random.nextGaussian() * 0.075,
                    this.targetY + this.random.nextGaussian() * 0.075,
                    this.targetZ + this.random.nextGaussian() * 0.075,
                    this.rCol, this.gCol, this.bCol, 0.5f
            );
        }
        while (this.points.size() > this.length) {
            this.points.remove(0);
        }
        if (this.points.size() > 2 && this.random.nextBoolean()) {
            int q = this.random.nextInt(Math.min(3, this.points.size()));
            if (this.random.nextBoolean() && this.points.size() > 2) {
                q = this.points.size() - 2;
            }
            StreamPoint p = this.points.get(q);
            FXDispatcher.INSTANCE.essentiaDropFx(
                    p.x + this.startX, p.y + this.startY, p.z + this.startZ,
                    this.rCol, this.gCol, this.bCol, 0.5f
            );
        }
    }
    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        if (this.points.size() < 3) return;
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, ESSENTIA_TEX);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        RenderSystem.disableCull();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        double camX = camera.getPosition().x;
        double camY = camera.getPosition().y;
        double camZ = camera.getPosition().z;
        int n = this.points.size();
        float[] wx = new float[n];
        float[] wy = new float[n];
        float[] wz = new float[n];
        float[] wr = new float[n];
        for (int i = 0; i < n; i++) {
            StreamPoint p = this.points.get(i);
            float variance = 1.0f + Mth.sin((i + this.age) / 3.0f) * 0.2f;
            wx[i] = (float) (this.startX + p.x + Mth.sin((i + this.age) / 6.0f) * 0.03f - camX);
            wy[i] = (float) (this.startY + p.y + Mth.sin((i + this.age) / 7.0f) * 0.03f - camY);
            wz[i] = (float) (this.startZ + p.z + Mth.sin((i + this.age) / 8.0f) * 0.03f - camZ);
            wr[i] = p.scale * variance;
        }
        if (n >= 3) {
            wr[n - 1] = 0.0f;
            wr[n - 2] = 0.0f;
            if (n >= 6) {
                wr[n - 3] = (this.quadSize * 0.5f + wr[n - 3]) / 2.0f;
                wr[n - 4] = (this.quadSize + wr[n - 4]) / 2.0f;
                wr[n - 5] = (this.quadSize + wr[n - 5] * 2.0f) / 3.0f;
            }
            for (int i = 0; i < n && i < 9; i++) {
                int c = n - 1 - i;
                float f = Math.abs(Mth.cos((c - n - 12) / 10.0f * (float) (Math.PI / 2.0)));
                wr[i] *= f;
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
        float dist = 0.0f;
        float r = this.rCol;
        float g = this.gCol;
        float b = this.bCol;
        for (int pass = 0; pass < 2; pass++) {
            dist = 0.0f;
            for (int i = 0; i < n - 1; i++) {
                float V_x = wx[i + 1] - wx[i];
                float V_y = wy[i + 1] - wy[i];
                float V_z = wz[i + 1] - wz[i];
                float segLen = Mth.sqrt(
                        V_x * V_x
                                + V_y * V_y
                                + V_z * V_z);
                float u0 = dist * 0.5f;
                dist += segLen;
                float u1 = dist * 0.5f;
                float r0 = wr[i];
                float r1 = wr[i + 1];
                builder.vertex(wx[i] - sx[i] * r0, wy[i] - sy[i] * r0, wz[i] - sz[i] * r0).uv(u0, 1).color(r, g, b, 1.0f).endVertex();
                builder.vertex(wx[i] + sx[i] * r0, wy[i] + sy[i] * r0, wz[i] + sz[i] * r0).uv(u0, 0).color(r, g, b, 1.0f).endVertex();
                builder.vertex(wx[i + 1] + sx[i + 1] * r1, wy[i + 1] + sy[i + 1] * r1, wz[i + 1] + sz[i + 1] * r1).uv(u1, 0).color(r, g, b, 1.0f).endVertex();
                builder.vertex(wx[i + 1] - sx[i + 1] * r1, wy[i + 1] - sy[i + 1] * r1, wz[i + 1] - sz[i + 1] * r1).uv(u1, 1).color(r, g, b, 1.0f).endVertex();
            }
            float capR0 = wr[0];
            if (capR0 > 1.0e-4f) {
                builder.vertex(wx[0] - capR0, wy[0] - capR0, wz[0]).uv(0, 1).color(r, g, b, 1.0f).endVertex();
                builder.vertex(wx[0] - capR0, wy[0] + capR0, wz[0]).uv(0, 0).color(r, g, b, 1.0f).endVertex();
                builder.vertex(wx[0] + capR0, wy[0] + capR0, wz[0]).uv(1, 0).color(r, g, b, 1.0f).endVertex();
                builder.vertex(wx[0] + capR0, wy[0] - capR0, wz[0]).uv(1, 1).color(r, g, b, 1.0f).endVertex();
            }
            float capR1 = wr[n - 1];
            if (capR1 > 1.0e-4f) {
                builder.vertex(wx[n - 1] - capR1, wy[n - 1] - capR1, wz[n - 1]).uv(0, 1).color(r, g, b, 1.0f).endVertex();
                builder.vertex(wx[n - 1] - capR1, wy[n - 1] + capR1, wz[n - 1]).uv(0, 0).color(r, g, b, 1.0f).endVertex();
                builder.vertex(wx[n - 1] + capR1, wy[n - 1] + capR1, wz[n - 1]).uv(1, 0).color(r, g, b, 1.0f).endVertex();
                builder.vertex(wx[n - 1] + capR1, wy[n - 1] - capR1, wz[n - 1]).uv(1, 1).color(r, g, b, 1.0f).endVertex();
            }
        }
        Tesselator.getInstance().end();
        RenderSystem.enableCull();
    }
    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }
    @Override
    public boolean shouldCull() {
        return false;
    }
    public FXEssentiaStream setTCGravity(float value) {
        this.gravity = value;
        return this;
    }
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
}
