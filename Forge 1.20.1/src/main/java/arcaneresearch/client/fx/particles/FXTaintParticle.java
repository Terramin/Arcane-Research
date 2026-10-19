package arcaneresearch.client.fx.particles;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
@OnlyIn(Dist.CLIENT)
public class FXTaintParticle extends TextureSheetParticle {
    private float rotationAngle;
    private float rotationSpeed;
    private int spriteFrame;
    private static final float BASE_R = 0.6f;
    private static final float BASE_G = 0.1f;
    private static final float BASE_B = 0.8f;
    public FXTaintParticle(ClientLevel level, double x, double y, double z,
                           double vx, double vy, double vz, float scale) {
        super(level, x, y, z, vx, vy, vz);
        this.rCol = BASE_R + (random.nextFloat() - 0.5f) * 0.2f;
        this.gCol = BASE_G + random.nextFloat() * 0.15f;
        this.bCol = BASE_B + (random.nextFloat() - 0.5f) * 0.15f;
        this.alpha = 0.8f;
        this.quadSize = scale * (0.5f + random.nextFloat() * 0.5f);
        this.xd = vx + (random.nextFloat() - 0.5f) * 0.02;
        this.yd = vy + random.nextFloat() * 0.02;
        this.zd = vz + (random.nextFloat() - 0.5f) * 0.02;
        this.gravity = 0.01f;
        this.lifetime = 15 + random.nextInt(15);
        this.rotationAngle = random.nextFloat() * 360.0f;
        this.rotationSpeed = (random.nextFloat() - 0.5f) * 10.0f;
        this.spriteFrame = random.nextInt(4);
    }
    public FXTaintParticle(ClientLevel level, double x, double y, double z,
                           double vx, double vy, double vz, float scale,
                           float r, float g, float b) {
        this(level, x, y, z, vx, vy, vz, scale);
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
    }
    @Override
    public void tick() {
        this.xo = x;
        this.yo = y;
        this.zo = z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        rotationAngle += rotationSpeed;
        float swirl = Mth.sin(age * 0.3f) * 0.01f;
        xd += swirl;
        zd += Mth.cos(age * 0.3f) * 0.01f;
        x += xd;
        y += yd;
        z += zd;
        yd -= gravity * 0.5;
        yd += 0.005;
        xd *= 0.95;
        yd *= 0.95;
        zd *= 0.95;
        float lifeProgress = (float)age / lifetime;
        if (lifeProgress > 0.7f) {
            alpha = 0.8f * (1.0f - (lifeProgress - 0.7f) / 0.3f);
        }
        if (age % 3 == 0) {
            spriteFrame = (spriteFrame + 1) % 4;
        }
    }
    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 camPos = camera.getPosition();
        float px = (float)(Mth.lerp(partialTicks, xo, x) - camPos.x());
        float py = (float)(Mth.lerp(partialTicks, yo, y) - camPos.y());
        float pz = (float)(Mth.lerp(partialTicks, zo, z) - camPos.z());
        Quaternionf rotation = camera.rotation();
        float currentRot = rotationAngle + rotationSpeed * partialTicks;
        Quaternionf spinRot = new Quaternionf().rotateZ((float)Math.toRadians(currentRot));
        rotation = rotation.mul(spinRot, new Quaternionf());
        Vector3f[] vertices = new Vector3f[] {
            new Vector3f(-1.0f, -1.0f, 0.0f),
            new Vector3f(-1.0f, 1.0f, 0.0f),
            new Vector3f(1.0f, 1.0f, 0.0f),
            new Vector3f(1.0f, -1.0f, 0.0f)
        };
        for (Vector3f vertex : vertices) {
            vertex.rotate(rotation);
            vertex.mul(quadSize);
            vertex.add(px, py, pz);
        }
        int sprite = 72 + spriteFrame;
        float u0 = (sprite % 64) / 64.0f;
        float u1 = u0 + 0.015625f;
        float v0 = (sprite / 64) / 64.0f;
        float v1 = v0 + 0.015625f;
        int light = 0xF000F0;
        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z())
                .uv(u1, v1).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
                .uv(u1, v0).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
                .uv(u0, v0).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
                .uv(u0, v1).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
    }
    @Override
    public ParticleRenderType getRenderType() {
        return FXGeneric.TC_NORMAL;
    }
}
