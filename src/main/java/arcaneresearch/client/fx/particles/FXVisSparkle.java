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
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class FXVisSparkle extends TextureSheetParticle {
    protected static final int GRID_SIZE = 64;

    private static final int FULLBRIGHT = 0xF000F0;
    private static final float UV_STEP = 0.015625f;
    private static final float SIZE_FACTOR = 0.1f;
    private static final float RENDER_ALPHA = 0.5f;
    private static final double ACCEL = 0.1;
    private static final double MAX_VEL = 0.1;
    private static final double VEL_DAMPEN = 0.985;
    private static final float SHRINK_FACTOR = 0.95f;

    private final Vector3f renderScratch = new Vector3f();

    protected double targetX;
    protected double targetY;
    protected double targetZ;
    protected float sizeMod;
    protected int spriteIndexX = 0;
    protected int spriteIndexY = 0;

    public FXVisSparkle(ClientLevel level, double x, double y, double z,
                        double targetX, double targetY, double targetZ) {
        super(level, x, y, z, 0, 0, 0);

        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;

        this.quadSize = 0.0f;
        this.lifetime = 1000;

        float f3 = 0.01f;
        this.xd = (float) this.random.nextGaussian() * f3;
        this.yd = (float) this.random.nextGaussian() * f3;
        this.zd = (float) this.random.nextGaussian() * f3;

        this.sizeMod = 45 + this.random.nextInt(15);

        this.rCol = 0.2f;
        this.gCol = 0.6f + this.random.nextFloat() * 0.3f;
        this.bCol = 0.2f;

        this.gravity = 0.2f;
    }

    public void setParticleTextureIndex(int index) {
        if (index < 0) index = 0;
        this.spriteIndexX = index % GRID_SIZE;
        this.spriteIndexY = index / GRID_SIZE;
    }

    @Override
    protected float getU0() {
        return (float) spriteIndexX / (float) GRID_SIZE;
    }

    @Override
    protected float getU1() {
        return ((float) spriteIndexX + 1.0f) / (float) GRID_SIZE;
    }

    @Override
    protected float getV0() {
        return (float) spriteIndexY / (float) GRID_SIZE;
    }

    @Override
    protected float getV1() {
        return ((float) spriteIndexY + 1.0f) / (float) GRID_SIZE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.move(this.xd, this.yd, this.zd);

        this.xd *= VEL_DAMPEN;
        this.yd *= VEL_DAMPEN;
        this.zd *= VEL_DAMPEN;

        double dx = targetX - this.x;
        double dy = targetY - this.y;
        double dz = targetZ - this.z;

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance < 2.0) {
            this.quadSize *= SHRINK_FACTOR;
        }

        if (distance < 0.2) {
            this.lifetime = this.age;
        }

        if (this.age < 10) {
            this.quadSize = this.age / this.sizeMod;
        }

        if (distance > 0) {
            double inv = 1.0 / distance;

            this.xd += dx * inv * ACCEL;
            this.yd += dy * inv * ACCEL;
            this.zd += dz * inv * ACCEL;

            this.xd = Mth.clamp(this.xd, -MAX_VEL, MAX_VEL);
            this.yd = Mth.clamp(this.yd, -MAX_VEL, MAX_VEL);
            this.zd = Mth.clamp(this.zd, -MAX_VEL, MAX_VEL);
        }

        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        float bob = Mth.sin(this.age / 3.0f) * 0.3f + 6.0f;

        Vec3 cameraPos = camera.getPosition();

        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        float size = SIZE_FACTOR * this.quadSize * bob;

        int frame = this.age % 16;
        float u0 = frame / 64.0f;
        float u1 = u0 + UV_STEP;
        float v0 = 0.125f;
        float v1 = v0 + UV_STEP;

        Quaternionf quaternion = camera.rotation();

        float cr = this.rCol;
        float cg = this.gCol;
        float cb = this.bCol;

        Vector3f v = renderScratch;

        v.set(-1.0F, -1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u1, v1).color(cr, cg, cb, RENDER_ALPHA).uv2(FULLBRIGHT).endVertex();

        v.set(-1.0F, 1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u1, v0).color(cr, cg, cb, RENDER_ALPHA).uv2(FULLBRIGHT).endVertex();

        v.set(1.0F, 1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u0, v0).color(cr, cg, cb, RENDER_ALPHA).uv2(FULLBRIGHT).endVertex();

        v.set(1.0F, -1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u0, v1).color(cr, cg, cb, RENDER_ALPHA).uv2(FULLBRIGHT).endVertex();
    }

    @Override
    public int getLightColor(float partialTick) {
        return FULLBRIGHT;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return FXGeneric.TC_ADDITIVE;
    }

    @Override
    public void setColor(float r, float g, float b) {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public void setTarget(double x, double y, double z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
    }
}