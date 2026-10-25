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
public class FXCrucibleBubble extends TextureSheetParticle {
    private float wobblePhase;
    private float wobbleSpeed;
    public FXCrucibleBubble(ClientLevel level, double x, double y, double z,
                            float r, float g, float b) {
        super(level, x, y, z, 0, 0, 0);
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
        this.alpha = 0.7f;
        this.quadSize = 0.05f + random.nextFloat() * 0.03f;
        this.xd = (random.nextFloat() - 0.5f) * 0.01;
        this.yd = 0.01 + random.nextFloat() * 0.015;
        this.zd = (random.nextFloat() - 0.5f) * 0.01;
        this.gravity = -0.001f;
        this.lifetime = 20 + random.nextInt(20);
        this.wobblePhase = random.nextFloat() * (float)Math.PI * 2;
        this.wobbleSpeed = 0.2f + random.nextFloat() * 0.1f;
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
        wobblePhase += wobbleSpeed;
        xd += Mth.sin(wobblePhase) * 0.001;
        zd += Mth.cos(wobblePhase * 0.7f) * 0.001;
        x += xd;
        y += yd;
        z += zd;
        xd *= 0.95;
        zd *= 0.95;
        if (age < lifetime * 0.3f) {
            quadSize *= 1.02f;
        } else if (age > lifetime * 0.8f) {
            quadSize *= 0.95f;
            alpha *= 0.9f;
        }
    }
    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 camPos = camera.getPosition();
        float px = (float)(Mth.lerp(partialTicks, xo, x) - camPos.x());
        float py = (float)(Mth.lerp(partialTicks, yo, y) - camPos.y());
        float pz = (float)(Mth.lerp(partialTicks, zo, z) - camPos.z());
        Quaternionf rotation = camera.rotation();
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
        int sprite = 160;
        float u0 = (sprite % 64) / 64.0f;
        float u1 = u0 + 0.015625f;
        float v0 = (sprite / 64) / 64.0f;
        float v1 = v0 + 0.015625f;
        int light = getLightColor(partialTicks);
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
        return FXGeneric.TC_ADDITIVE;
    }
}
