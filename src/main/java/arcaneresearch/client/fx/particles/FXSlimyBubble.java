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
public class FXSlimyBubble extends TextureSheetParticle {
    private int particle = 144;
    public FXSlimyBubble(ClientLevel level, double x, double y, double z, float scale) {
        super(level, x, y, z, 0, 0, 0);
        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;
        this.gravity = 0.0f;
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.quadSize = scale;
        this.lifetime = 15 + random.nextInt(5);
        this.setSize(0.01f, 0.01f);
    }
    public FXSlimyBubble(ClientLevel level, double x, double y, double z, float scale, float r, float g, float b) {
        this(level, x, y, z, scale);
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
        if (age - 1 < 6) {
            particle = 144 + age / 2;
            if (age == 5) {
                y += 0.1;
            }
        } else if (age < lifetime - 4) {
            yd += 0.005;
            particle = 147 + (age % 4) / 2;
        } else {
            yd /= 2.0;
            particle = 150 - (lifetime - age) / 2;
        }
        y += yd;
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
        float size = quadSize;
        for (Vector3f vertex : vertices) {
            vertex.rotate(rotation);
            vertex.mul(size);
            vertex.add(px, py, pz);
        }
        float u0 = (particle % 16) / 64.0f;
        float u1 = u0 + 0.015625f;
        float v0 = (particle / 16) / 64.0f;
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
        return FXGeneric.TC_NORMAL;
    }
}
