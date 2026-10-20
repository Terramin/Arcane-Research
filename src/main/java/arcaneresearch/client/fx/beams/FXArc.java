package arcaneresearch.client.fx.beams;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import arcaneresearch.client.fx.particles.FXGeneric;
import arcaneresearch.client.lib.events.handler.ParticleHandler;
import arcaneresearch.common.lib.utils.Utils;
import java.util.ArrayList;
import java.util.List;
@OnlyIn(Dist.CLIENT)
public class FXArc extends TextureSheetParticle {
    protected static final ResourceLocation BEAM_TEXTURE = new ResourceLocation("arcaneresearch", "textures/misc/beamh.png");
    protected List<Vec3> points = new ArrayList<>();
    protected double targetX;
    protected double targetY;
    protected double targetZ;
    protected float length;
    public FXArc(ClientLevel level, double x, double y, double z,
                 double targetX, double targetY, double targetZ,
                 float r, float g, float b, double heightGravity) {
        super(level, x, y, z, 0, 0, 0);
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
        this.setSize(0.02f, 0.02f);
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.targetX = targetX - x;
        this.targetY = targetY - y;
        this.targetZ = targetZ - z;
        this.lifetime = 3;
        calculateArcPoints(heightGravity);
        for (int i = 1; i < points.size() - 1; i++) {
            spawnEmber(points.get(i));
        }
    }
    protected void calculateArcPoints(double heightGravity) {
        Vec3 start = Vec3.ZERO;
        Vec3 end = new Vec3(this.targetX, this.targetY, this.targetZ);
        this.length = (float) end.length();
        double gravity = 0.115;
        double noise = 0.25;
        Vec3 velocity = Utils.calculateVelocity(start, end, heightGravity, gravity);
        double stepLengthSq = velocity.lengthSqr();
        Vec3 current = start;
        points.add(start);
        for (int i = 0; i < 50 && current.distanceToSqr(end) > stepLengthSq; i++) {
            Vec3 next = current.add(velocity);
            current = next;
            Vec3 noisyPoint = next.add(
                    (this.random.nextDouble() - this.random.nextDouble()) * noise,
                    (this.random.nextDouble() - this.random.nextDouble()) * noise,
                    (this.random.nextDouble() - this.random.nextDouble()) * noise
            );
            points.add(noisyPoint);
            velocity = velocity.subtract(0, gravity / 1.9, 0);
        }
        points.add(end);
    }
    private void spawnEmber(Vec3 offset) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        FXGeneric fb = new FXGeneric(this.level,
                this.x + offset.x, this.y + offset.y, this.z + offset.z, 0.0, 0.0, 0.0);
        int emberAge = 30 + this.random.nextInt(20);
        fb.setMaxAge(emberAge);
        fb.setColorRange(Mth.clamp(this.rCol * 3.0f, 0.0f, 1.0f),
                Mth.clamp(this.gCol * 3.0f, 0.0f, 1.0f),
                Mth.clamp(this.bCol * 3.0f, 0.0f, 1.0f),
                this.random.nextFloat(), this.random.nextFloat(), this.random.nextFloat());
        float[] alphas = new float[6 + this.random.nextInt(emberAge / 3)];
        for (int a = 1; a < alphas.length - 1; ++a) {
            alphas[a] = this.random.nextFloat();
        }
        alphas[0] = 1.0f;
        fb.setAlphaKeyframes(alphas);
        boolean sp = this.random.nextFloat() < 0.2f;
        fb.setParticles(sp ? 320 : 512, 16, 1);
        fb.setLoop(true);
        fb.setGravity(sp ? 0.0f : 0.125f);
        fb.setScaleKeyframes(0.05f, 0.0125f);
        fb.setLayer(0);
        fb.setSlowDown(0.995);
        fb.setRandomMovementScale(0.0025f, 0.001f, 0.0025f);
        int delay = 2 + this.random.nextInt(3);
        int ps = mc.options.particles().get().getId();
        if (mc.getFps() < 30) {
            ++ps;
        }
        if (this.level.random.nextInt(3) < ps) {
            return;
        }
        ParticleHandler.schedule(() -> mc.particleEngine.add(fb), delay);
    }
    @Deprecated
    protected Vec3 calculateVelocity(Vec3 start, Vec3 end, double height, double gravity) {
        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double dz = end.z - start.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        double time = horizontalDist / 0.5;
        if (time < 1) time = 1;
        double vx = dx / time;
        double vz = dz / time;
        double vy = (dy + 0.5 * gravity * time * time) / time + height;
        return new Vec3(vx, vy, vz);
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
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTicks) {
        if (points.size() < 2) return;
        Vec3 cameraPos = camera.getPosition();
        double px = Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x();
        double py = Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y();
        double pz = Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z();
        float alpha = 1.0f - (this.age + partialTicks) / this.lifetime;
        float size = 0.125f;
        int light = 0xF000F0;
        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 p1 = points.get(i);
            Vec3 p2 = points.get(i + 1);
            float x1 = (float) (px + p1.x);
            float y1 = (float) (py + p1.y);
            float z1 = (float) (pz + p1.z);
            float x2 = (float) (px + p2.x);
            float y2 = (float) (py + p2.y);
            float z2 = (float) (pz + p2.z);
            float mx = (x1 + x2) / 2;
            float my = (y1 + y2) / 2;
            float mz = (z1 + z2) / 2;
            float u = (i % 16) / 64.0f;
            renderSegmentQuad(buffer, camera, mx, my, mz, size, u, alpha, light);
        }
    }
    protected void renderSegmentQuad(VertexConsumer buffer, Camera camera,
                                     float x, float y, float z, float size,
                                     float u, float alpha, int light) {
        float u1 = u + 0.015625f;
        float v0 = 0.0f;
        float v1 = 0.015625f;
        Vector3f[] vertices = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        for (int i = 0; i < 4; ++i) {
            Vector3f vertex = vertices[i];
            vertex.rotate(camera.rotation());
            vertex.mul(size);
            vertex.add(x, y, z);
        }
        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z())
                .uv(u1, v1).color(this.rCol, this.gCol, this.bCol, alpha)
                .uv2(light).endVertex();
        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
                .uv(u1, v0).color(this.rCol, this.gCol, this.bCol, alpha)
                .uv2(light).endVertex();
        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
                .uv(u, v0).color(this.rCol, this.gCol, this.bCol, alpha)
                .uv2(light).endVertex();
        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
                .uv(u, v1).color(this.rCol, this.gCol, this.bCol, alpha)
                .uv2(light).endVertex();
    }
    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
    public void setRGB(float r, float g, float b) {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
    }
    public List<Vec3> getPoints() {
        return points;
    }
    public float getLength() {
        return length;
    }
}
