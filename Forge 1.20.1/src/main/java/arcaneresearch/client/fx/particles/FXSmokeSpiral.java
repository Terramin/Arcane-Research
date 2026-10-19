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
public class FXSmokeSpiral extends TextureSheetParticle {
    protected float radius;
    protected int startAngle;
    protected int minY;
    protected static final int GRID_SIZE = 64;
    public FXSmokeSpiral(ClientLevel level, double x, double y, double z,
                         float radius, int startAngle, int minY) {
        super(level, x, y, z, 0, 0, 0);
        this.radius = radius;
        this.startAngle = startAngle;
        this.minY = minY;
        this.gravity = -0.01f;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.quadSize = 1.0f;
        this.lifetime = 20 + this.random.nextInt(10);
        this.setSize(0.01f, 0.01f);
        this.alpha = 1.0f;
    }
    @Override
    public void tick() {
        this.alpha = (this.lifetime - this.age) / (float) this.lifetime;
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }
    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 cameraPos = camera.getPosition();
        float progress = (this.age + partialTicks) / this.lifetime;
        float spiralAngle = this.startAngle + 720.0f * progress;
        float pitchAngle = 90.0f - 180.0f * progress;
        float spiralRad = spiralAngle / 180.0f * (float) Math.PI;
        float pitchRad = pitchAngle / 180.0f * (float) Math.PI;
        float offsetX = -Mth.sin(spiralRad) * Mth.cos(pitchRad) * this.radius;
        float offsetY = -Mth.sin(pitchRad) * this.radius;
        float offsetZ = Mth.cos(spiralRad) * Mth.cos(pitchRad) * this.radius;
        float finalY = (float) Math.max(this.y + offsetY, this.minY + 0.1);
        float x = (float) (this.x + offsetX - cameraPos.x());
        float y = (float) (finalY - cameraPos.y());
        float z = (float) (this.z + offsetZ - cameraPos.z());
        int spriteIndex = (int) (1.0f + progress * 4.0f);
        float u0 = (spriteIndex % 16) / 64.0f;
        float u1 = u0 + 0.015625f;
        float v0 = (spriteIndex / 16) / 64.0f;
        float v1 = v0 + 0.015625f;
        float size = 0.15f * this.quadSize;
        float displayAlpha = 0.66f * this.alpha;
        int light = this.getLightColor(partialTicks);
        Quaternionf quaternion = camera.rotation();
        Vector3f[] vertices = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        for (int i = 0; i < 4; ++i) {
            Vector3f vertex = vertices[i];
            vertex.rotate(quaternion);
            vertex.mul(size);
            vertex.add(x, y, z);
        }
        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z())
                .uv(u1, v1).color(this.rCol, this.gCol, this.bCol, displayAlpha)
                .uv2(light).endVertex();
        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
                .uv(u1, v0).color(this.rCol, this.gCol, this.bCol, displayAlpha)
                .uv2(light).endVertex();
        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
                .uv(u0, v0).color(this.rCol, this.gCol, this.bCol, displayAlpha)
                .uv2(light).endVertex();
        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
                .uv(u0, v1).color(this.rCol, this.gCol, this.bCol, displayAlpha)
                .uv2(light).endVertex();
    }
    @Override
    public ParticleRenderType getRenderType() {
        return FXGeneric.TC_NORMAL;
    }
    @Override
    public void setColor(float r, float g, float b) {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
    }
}
