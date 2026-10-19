package arcaneresearch.client.fx.particles;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
@OnlyIn(Dist.CLIENT)
public class FXBoreSparkle extends TextureSheetParticle {
    private Entity target;
    private double targetX;
    private double targetY;
    private double targetZ;
    private int particle = 24;
    public FXBoreSparkle(ClientLevel level, double x, double y, double z,
                         double tx, double ty, double tz) {
        super(level, x, y, z, 0, 0, 0);
        this.rCol = 0.2f;
        this.gCol = 0.6f + random.nextFloat() * 0.3f;
        this.bCol = 0.2f;
        this.quadSize = random.nextFloat() * 0.5f + 0.5f;
        this.targetX = tx;
        this.targetY = ty;
        this.targetZ = tz;
        double dx = tx - x;
        double dy = ty - y;
        double dz = tz - z;
        int base = (int)(Mth.sqrt((float)(dx * dx + dy * dy + dz * dz)) * 10.0f);
        if (base < 1) base = 1;
        this.lifetime = base / 2 + random.nextInt(base);
        float f3 = 0.01f;
        this.xd = (float)random.nextGaussian() * f3;
        this.yd = (float)random.nextGaussian() * f3;
        this.zd = (float)random.nextGaussian() * f3;
        this.gravity = 0.2f;
        Minecraft mc = Minecraft.getInstance();
        int visibleDistance = mc.options.graphicsMode().get().getId() > 0 ? 64 : 32;
        if (mc.cameraEntity != null && mc.cameraEntity.distanceToSqr(x, y, z) > visibleDistance * visibleDistance) {
            this.lifetime = 0;
        }
    }
    public FXBoreSparkle(ClientLevel level, double x, double y, double z, Entity target) {
        this(level, x, y, z, target.getX(), target.getY() + target.getEyeHeight(), target.getZ());
        this.target = target;
    }
    @Override
    public void tick() {
        this.xo = x;
        this.yo = y;
        this.zo = z;
        if (target != null) {
            targetX = target.getX();
            targetY = target.getY() + target.getEyeHeight();
            targetZ = target.getZ();
        }
        if (this.age++ >= this.lifetime ||
            (Mth.floor(x) == Mth.floor(targetX) &&
             Mth.floor(y) == Mth.floor(targetY) &&
             Mth.floor(z) == Mth.floor(targetZ))) {
            this.remove();
            return;
        }
        move(xd, yd, zd);
        xd *= 0.985;
        yd *= 0.95;
        zd *= 0.985;
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        double dist = Mth.sqrt((float)(dx * dx + dy * dy + dz * dz));
        double clamp = Math.min(0.25, dist / 15.0);
        if (dist < 2.0) {
            quadSize *= 0.9f;
        }
        dx /= dist;
        dy /= dist;
        dz /= dist;
        xd += dx * clamp;
        yd += dy * clamp;
        zd += dz * clamp;
        xd = Mth.clamp((float)xd, (float)-clamp, (float)clamp);
        yd = Mth.clamp((float)yd, (float)-clamp, (float)clamp);
        zd = Mth.clamp((float)zd, (float)-clamp, (float)clamp);
        xd += random.nextGaussian() * 0.01;
        yd += random.nextGaussian() * 0.01;
        zd += random.nextGaussian() * 0.01;
    }
    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 camPos = camera.getPosition();
        float px = (float)(Mth.lerp(partialTicks, xo, x) - camPos.x());
        float py = (float)(Mth.lerp(partialTicks, yo, y) - camPos.y());
        float pz = (float)(Mth.lerp(partialTicks, zo, z) - camPos.z());
        float bob = Mth.sin(age / 3.0f) * 0.5f + 1.0f;
        float size = 0.1f * quadSize * bob;
        Quaternionf rotation = camera.rotation();
        Vector3f[] vertices = new Vector3f[] {
            new Vector3f(-1.0f, -1.0f, 0.0f),
            new Vector3f(-1.0f, 1.0f, 0.0f),
            new Vector3f(1.0f, 1.0f, 0.0f),
            new Vector3f(1.0f, -1.0f, 0.0f)
        };
        for (Vector3f vertex : vertices) {
            vertex.rotate(rotation);
            vertex.mul(size);
            vertex.add(px, py, pz);
        }
        int frame = age % 4;
        float u0 = frame / 64.0f;
        float u1 = u0 + 0.015625f;
        float v0 = 0.0625f;
        float v1 = v0 + 0.015625f;
        int light = 0xF000F0;
        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z())
                .uv(u1, v1).color(rCol, gCol, bCol, 1.0f).uv2(light).endVertex();
        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
                .uv(u1, v0).color(rCol, gCol, bCol, 1.0f).uv2(light).endVertex();
        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
                .uv(u0, v0).color(rCol, gCol, bCol, 1.0f).uv2(light).endVertex();
        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
                .uv(u0, v1).color(rCol, gCol, bCol, 1.0f).uv2(light).endVertex();
    }
    @Override
    public ParticleRenderType getRenderType() {
        return FXGeneric.TC_ADDITIVE;
    }
    public void setGravity(float value) {
        this.gravity = value;
    }
}
