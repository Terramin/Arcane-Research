package arcaneresearch.client.fx.beams;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
@OnlyIn(Dist.CLIENT)
public class FXBolt extends TextureSheetParticle {
    protected static final ResourceLocation BEAM_TEXTURE = new ResourceLocation("arcaneresearch", "textures/misc/essentia.png");
    protected static final double BEHIND_START = 1.0;
    protected static final double FIRST_PERSON_DIST = 0.4;
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
        super(level, x, y, z, 0, 0, 0);
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
    @Override
    public void render(@NotNull VertexConsumer ignored, @NotNull Camera camera, float partialTicks) {
        calculatePoints(partialTicks);
        if (points.size() < 2) {
            return;
        }
        Vec3 cameraPos = camera.getPosition();
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
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, BEAM_TEXTURE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 1);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (int pass = 0; pass < 2; pass++) {
            float widthScale = (pass == 0) ? 0.15f : 0.15f / 3.0f;
            float passAlpha = 1.0f;
            float pr = this.rCol;
            float pg = this.gCol;
            float pb = this.bCol;
            if (tail) {
                Vec3 p0 = points.get(0);
                float w0 = pointWidths.get(0) * widthScale;
                double x0 = px + p0.x;
                double y0 = py + p0.y;
                double z0 = pz + p0.z;
                buffer.vertex(tailX - sx[0] * w0, tailY - sy[0] * w0, tailZ - sz[0] * w0)
                        .uv(0, 0).color(pr, pg, pb, passAlpha).endVertex();
                buffer.vertex(tailX + sx[0] * w0, tailY + sy[0] * w0, tailZ + sz[0] * w0)
                        .uv(0, 1).color(pr, pg, pb, passAlpha).endVertex();
                buffer.vertex(x0 + sx[0] * w0, y0 + sy[0] * w0, z0 + sz[0] * w0)
                        .uv(0, 1).color(pr, pg, pb, passAlpha).endVertex();
                buffer.vertex(x0 - sx[0] * w0, y0 - sy[0] * w0, z0 - sz[0] * w0)
                        .uv(0, 0).color(pr, pg, pb, passAlpha).endVertex();
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
                buffer.vertex(x0 - sx[a] * w0, y0 - sy[a] * w0, z0 - sz[a] * w0)
                        .uv(u0, 0).color(pr, pg, pb, passAlpha).endVertex();
                buffer.vertex(x0 + sx[a] * w0, y0 + sy[a] * w0, z0 + sz[a] * w0)
                        .uv(u0, 1).color(pr, pg, pb, passAlpha).endVertex();
                buffer.vertex(x1 + sx[a + 1] * w1, y1 + sy[a + 1] * w1, z1 + sz[a + 1] * w1)
                        .uv(u1, 1).color(pr, pg, pb, passAlpha).endVertex();
                buffer.vertex(x1 - sx[a + 1] * w1, y1 - sy[a + 1] * w1, z1 - sz[a + 1] * w1)
                        .uv(u1, 0).color(pr, pg, pb, passAlpha).endVertex();
            }
        }
        tesselator.end();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.blendFunc(770, 771);
        RenderSystem.disableBlend();
    }
    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }
    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
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
