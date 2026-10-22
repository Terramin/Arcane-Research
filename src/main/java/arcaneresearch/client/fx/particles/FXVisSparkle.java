package arcaneresearch.client.fx.particles;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.Mth;
import arcaneresearch.client.fx.particles.TCSprites;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import org.joml.Quaternionf;


public class FXVisSparkle extends SingleQuadParticle {

    protected double targetX;
    protected double targetY;
    protected double targetZ;
    protected float sizeMod;

    protected int spriteIndexX = 0;
    protected int spriteIndexY = 0;
    protected static final int GRID_SIZE = 64;

    public FXVisSparkle(ClientLevel level, double x, double y, double z,
                        double targetX, double targetY, double targetZ) {
        super(level, x, y, z, 0, 0, 0, null);

        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;

        this.rCol = 0.6f;
        this.gCol = 0.6f;
        this.bCol = 0.6f;

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

        this.xd *= 0.985;
        this.yd *= 0.985;
        this.zd *= 0.985;

        double dx = targetX - this.x;
        double dy = targetY - this.y;
        double dz = targetZ - this.z;

        double accel = 0.1;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (distance < 2.0) {
            this.quadSize *= 0.95f;
        }

        if (distance < 0.2) {
            this.lifetime = this.age;
        }

        if (this.age < 10) {
            this.quadSize = this.age / this.sizeMod;
        }

        if (distance > 0) {
            dx /= distance;
            dy /= distance;
            dz /= distance;

            this.xd += dx * accel;
            this.yd += dy * accel;
            this.zd += dz * accel;

            this.xd = Mth.clamp(this.xd, -0.1, 0.1);
            this.yd = Mth.clamp(this.yd, -0.1, 0.1);
            this.zd = Mth.clamp(this.zd, -0.1, 0.1);
        }

        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }


    
    @Override
    protected int getLightCoords(float partialTick) {
        return 0xF000F0;
    }

    
    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTicks) {
        float bob = Mth.sin(this.age / 3.0f) * 0.3f + 6.0f;

        Vec3 cameraPos = camera.position();
        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        float size = 0.1f * this.quadSize * bob;

        int frame = this.age % 16;
        float u0 = frame / 64.0f;
        float u1 = u0 + 0.015625f;
        float v0 = 0.125f;
        float v1 = v0 + 0.015625f;

        int light = this.getLightCoords(partialTicks);

        org.joml.Quaternionf quaternion = camera.rotation();
        state.add(getLayer(), x, y, z,
                quaternion.x(), quaternion.y(), quaternion.z(), quaternion.w(), size,
                (u0), (u1),
                (v0), (v1),
                net.minecraft.util.ARGB.color((int) ((0.5f) * 255.0F), (int) ((this.rCol) * 255.0F), (int) ((this.gCol) * 255.0F), (int) ((this.bCol) * 255.0F)), light);
    }

    @Override
    public net.minecraft.client.particle.ParticleRenderType getGroup() {
        return net.minecraft.client.particle.ParticleRenderType.SINGLE_QUADS;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TCSprites.additive();
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