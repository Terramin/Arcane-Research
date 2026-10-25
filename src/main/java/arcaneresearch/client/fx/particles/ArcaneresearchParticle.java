package arcaneresearch.client.fx.particles;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
@OnlyIn(Dist.CLIENT)
public class ArcaneresearchParticle extends TextureSheetParticle {
    protected float startR, startG, startB;
    protected float endR, endG, endB;
    protected float startScale;
    protected float endScale;
    protected float rotationSpeed;
    protected float rotation;
    protected double slowDown = 0.98;
    protected float windX, windZ;
    protected boolean noClip = false;
    protected int spriteStart = 0;
    protected int spriteCount = 1;
    protected int spriteIncrement = 1;
    protected boolean spriteLoop = false;
    protected int gridSize = 64;
    protected int layer = 0;
    public ArcaneresearchParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
        this.startR = this.rCol;
        this.startG = this.gCol;
        this.startB = this.bCol;
        this.endR = this.rCol;
        this.endG = this.gCol;
        this.endB = this.bCol;
        this.startScale = this.quadSize;
        this.endScale = this.quadSize;
    }
    public ArcaneresearchParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
        super(level, x, y, z, vx, vy, vz);
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.startR = this.rCol;
        this.startG = this.gCol;
        this.startB = this.bCol;
        this.endR = this.rCol;
        this.endG = this.gCol;
        this.endB = this.bCol;
        this.startScale = this.quadSize;
        this.endScale = this.quadSize;
    }
    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.oRoll = this.roll;
        this.roll += this.rotationSpeed;
        this.yd -= 0.04 * this.gravity;
        if (!noClip) {
            this.move(this.xd, this.yd, this.zd);
        } else {
            this.x += this.xd;
            this.y += this.yd;
            this.z += this.zd;
        }
        this.xd *= this.slowDown;
        this.yd *= this.slowDown;
        this.zd *= this.slowDown;
        this.xd += this.windX;
        this.zd += this.windZ;
        if (this.onGround && slowDown != 1.0) {
            this.xd *= 0.7;
            this.zd *= 0.7;
        }
        float progress = (float) this.age / (float) this.lifetime;
        this.rCol = Mth.lerp(progress, this.startR, this.endR);
        this.gCol = Mth.lerp(progress, this.startG, this.endG);
        this.bCol = Mth.lerp(progress, this.startB, this.endB);
        this.quadSize = Mth.lerp(progress, this.startScale, this.endScale);
    }
    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 cameraPos = camera.getPosition();
        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());
        Quaternionf quaternion;
        if (this.roll == 0.0F) {
            quaternion = camera.rotation();
        } else {
            quaternion = new Quaternionf(camera.rotation());
            float rollAngle = Mth.lerp(partialTicks, this.oRoll, this.roll);
            quaternion.rotateZ(rollAngle);
        }
        Vector3f[] vertices = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        float size = this.getQuadSize(partialTicks);
        for (int i = 0; i < 4; ++i) {
            Vector3f vertex = vertices[i];
            vertex.rotate(quaternion);
            vertex.mul(size);
            vertex.add(x, y, z);
        }
        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int light = this.getLightColor(partialTicks);
        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z())
                .uv(u1, v1).color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light).endVertex();
        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
                .uv(u1, v0).color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light).endVertex();
        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
                .uv(u0, v0).color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light).endVertex();
        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
                .uv(u0, v1).color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(light).endVertex();
    }
    @Override
    public ParticleRenderType getRenderType() {
        return layer == 0 ? ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT : ParticleRenderType.PARTICLE_SHEET_LIT;
    }
    public ArcaneresearchParticle setTCColor(float r, float g, float b) {
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
    public ArcaneresearchParticle setColor(float r1, float g1, float b1, float r2, float g2, float b2) {
        this.rCol = r1;
        this.gCol = g1;
        this.bCol = b1;
        this.startR = r1;
        this.startG = g1;
        this.startB = b1;
        this.endR = r2;
        this.endG = g2;
        this.endB = b2;
        return this;
    }
    public ArcaneresearchParticle setTCAlpha(float alpha) {
        this.alpha = alpha;
        return this;
    }
    public ArcaneresearchParticle setTCAlphaRange(float startAlpha, float endAlpha) {
        this.alpha = startAlpha;
        return this;
    }
    public ArcaneresearchParticle setTCScale(float scale) {
        this.quadSize = scale;
        this.startScale = scale;
        this.endScale = scale;
        return this;
    }
    public ArcaneresearchParticle setTCScaleRange(float startScale, float endScale) {
        this.quadSize = startScale;
        this.startScale = startScale;
        this.endScale = endScale;
        return this;
    }
    public ArcaneresearchParticle setTCLifetime(int lifetime) {
        this.lifetime = lifetime;
        return this;
    }
    public ArcaneresearchParticle setTCGravity(float gravity) {
        this.gravity = gravity;
        return this;
    }
    public ArcaneresearchParticle setTCRotationSpeed(float speed) {
        this.rotationSpeed = speed;
        return this;
    }
    public ArcaneresearchParticle setTCSlowDown(double slowDown) {
        this.slowDown = slowDown;
        return this;
    }
    public ArcaneresearchParticle setTCNoClip(boolean noClip) {
        this.noClip = noClip;
        return this;
    }
    public ArcaneresearchParticle setTCLayer(int layer) {
        this.layer = layer;
        return this;
    }
    public ArcaneresearchParticle setTCWind(float windX, float windZ) {
        this.windX = windX;
        this.windZ = windZ;
        return this;
    }
}
