package arcaneresearch.client.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class FXVent2 extends ArcaneresearchParticle {
    private static final float UV_STEP = 0.015625f;
    private static final float SIZE_FACTOR = 0.3f;
    private static final double WANDER_SCALE = 0.0075;
    private static final float COLOR_JITTER = 0.05f;
    private static final float GRAV_JITTER = 0.0075f;
    private static final float GROW_FACTOR = 1.2f;
    private static final float GROUND_DAMPEN = 0.7f;
    private static final float MIN_HEADING_LEN = 1.0e-6f;

    private final Vector3f renderScratch = new Vector3f();

    private float maxScale;
    private float grav;

    public FXVent2(ClientLevel level, double x, double y, double z,
                   double vx, double vy, double vz, int color) {
        super(level, x, y, z, vx, vy, vz);

        this.setSize(0.02f, 0.02f);
        this.quadSize = this.random.nextFloat() * 0.1f + 0.05f;
        this.maxScale = 1.0f;

        float baseR = ((color >> 16) & 0xFF) / 255.0f;
        float baseG = ((color >> 8) & 0xFF) / 255.0f;
        float baseB = (color & 0xFF) / 255.0f;

        this.rCol = Mth.clamp(baseR + (float) this.random.nextGaussian() * COLOR_JITTER, 0.0f, 1.0f);
        this.gCol = Mth.clamp(baseG + (float) this.random.nextGaussian() * COLOR_JITTER, 0.0f, 1.0f);
        this.bCol = Mth.clamp(baseB + (float) this.random.nextGaussian() * COLOR_JITTER, 0.0f, 1.0f);

        this.startR = this.rCol;
        this.startG = this.gCol;
        this.startB = this.bCol;
        this.endR = this.rCol;
        this.endG = this.gCol;
        this.endB = this.bCol;

        this.alpha = 1.0f;
        this.grav = (float) (this.random.nextGaussian() * GRAV_JITTER);
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
        float len = Mth.sqrt((float) (vx * vx + vy * vy + vz * vz));

        if (len > MIN_HEADING_LEN) {
            double inv = 1.0 / len;
            vx *= inv;
            vy *= inv;
            vz *= inv;
        }

        double wander = WANDER_SCALE * spread;

        vx += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * wander;
        vy += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * wander;
        vz += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * wander;

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
            this.quadSize *= GROW_FACTOR;
        }

        if (this.quadSize > this.maxScale) {
            this.quadSize = this.maxScale;
        }

        if (this.onGround) {
            this.xd *= GROUND_DAMPEN;
            this.zd *= GROUND_DAMPEN;
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
        float invMax = this.maxScale > 0.0f ? 1.0f / this.maxScale : 0.0f;

        Vec3 cameraPos = camera.getPosition();

        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        int spriteIndex = (int) (1.0f + this.quadSize * invMax * 4.0f);

        float u0 = (spriteIndex % 16) / 64.0f;
        float u1 = u0 + UV_STEP;
        float v0 = (spriteIndex / 16) / 64.0f;
        float v1 = v0 + UV_STEP;

        float size = SIZE_FACTOR * this.quadSize;
        float displayAlpha = Mth.clamp(this.alpha * ((this.maxScale - this.quadSize) * invMax), 0.0f, 1.0f);

        int light = this.getLightColor(partialTicks);

        Quaternionf quaternion = camera.rotation();

        float cr = this.rCol;
        float cg = this.gCol;
        float cb = this.bCol;

        Vector3f v = renderScratch;

        v.set(-1.0F, -1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u1, v1).color(cr, cg, cb, displayAlpha).uv2(light).endVertex();

        v.set(-1.0F, 1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u1, v0).color(cr, cg, cb, displayAlpha).uv2(light).endVertex();

        v.set(1.0F, 1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u0, v0).color(cr, cg, cb, displayAlpha).uv2(light).endVertex();

        v.set(1.0F, -1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u0, v1).color(cr, cg, cb, displayAlpha).uv2(light).endVertex();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return FXGeneric.TC_NORMAL;
    }
}