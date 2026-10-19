package arcaneresearch.client.fx.particles;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.awt.Color;
@OnlyIn(Dist.CLIENT)
public class FXVent2 extends ArcaneresearchParticle {
    private float maxScale;
    private float grav;
    public FXVent2(ClientLevel level, double x, double y, double z,
                   double vx, double vy, double vz, int color) {
        super(level, x, y, z, vx, vy, vz);
        this.setSize(0.02f, 0.02f);
        this.quadSize = this.random.nextFloat() * 0.1f + 0.05f;
        this.maxScale = 1.0f;
        Color c = new Color(color);
        this.rCol = (float) Mth.clamp(c.getRed() / 255.0f + this.random.nextGaussian() * 0.05, 0.0, 1.0);
        this.gCol = (float) Mth.clamp(c.getGreen() / 255.0f + this.random.nextGaussian() * 0.05, 0.0, 1.0);
        this.bCol = (float) Mth.clamp(c.getBlue() / 255.0f + this.random.nextGaussian() * 0.05, 0.0, 1.0);
        this.startR = this.rCol;
        this.startG = this.gCol;
        this.startB = this.bCol;
        this.endR = this.rCol;
        this.endG = this.gCol;
        this.endB = this.bCol;
        this.alpha = 1.0f;
        this.grav = (float) (this.random.nextGaussian() * 0.0075);
        this.gravity = 0;
        this.slowDown = 0.85;
        this.lifetime = Integer.MAX_VALUE;
        this.noClip = false;
    }
    public FXVent2 setScale(float f) {
        this.quadSize *= f;
        this.maxScale *= f;
        return this;
    }
    public FXVent2 setHeading(double vx, double vy, double vz, float speed, float spread) {
        float len = Mth.sqrt((float)(vx * vx + vy * vy + vz * vz));
        vx /= len;
        vy /= len;
        vz /= len;
        vx += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075 * spread;
        vy += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075 * spread;
        vz += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075 * spread;
        this.xd = vx * speed;
        this.yd = vy * speed;
        this.zd = vz * speed;
        return this;
    }
    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.age++;
        if (this.quadSize >= this.maxScale) {
            this.remove();
            return;
        }
        this.yd += this.grav;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= this.slowDown;
        this.yd *= this.slowDown;
        this.zd *= this.slowDown;
        if (this.quadSize < this.maxScale) {
            this.quadSize *= 1.2f;
        }
        if (this.quadSize > this.maxScale) {
            this.quadSize = this.maxScale;
        }
        if (this.onGround) {
            this.xd *= 0.7;
            this.zd *= 0.7;
        }
    }
    public FXVent2 setRGB(float r, float g, float b) {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
        this.startR = r;
        this.startG = g;
        this.startB = b;
        this.endR = r;
        this.endG = g;
        this.endB = b;
        return this;
    }
    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 cameraPos = camera.getPosition();
        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());
        int spriteIndex = (int) (1.0f + this.quadSize / this.maxScale * 4.0f);
        float u0 = (spriteIndex % 16) / 64.0f;
        float u1 = u0 + 0.015625f;
        float v0 = (spriteIndex / 16) / 64.0f;
        float v1 = v0 + 0.015625f;
        float size = 0.3f * this.quadSize;
        float displayAlpha = this.alpha * ((this.maxScale - this.quadSize) / this.maxScale);
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
}
