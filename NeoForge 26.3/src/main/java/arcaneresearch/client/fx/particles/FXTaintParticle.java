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

// NEOFORGE_PORT: camera quad -> base extract path (same math).
public class FXTaintParticle extends SingleQuadParticle {

    private float rotationAngle;
    private float rotationSpeed;
    private int spriteFrame;

    private static final float BASE_R = 0.6f;
    private static final float BASE_G = 0.1f;
    private static final float BASE_B = 0.8f;

    public FXTaintParticle(ClientLevel level, double x, double y, double z,
                           double vx, double vy, double vz, float scale) {
        super(level, x, y, z, vx, vy, vz, null);

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


    // NEOFORGE_PORT: same math as the old render(); immediate verts -> state.add.
    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTicks) {
        Vec3 camPos = camera.position();
        float px = (float) (Mth.lerp(partialTicks, xo, x) - camPos.x());
        float py = (float) (Mth.lerp(partialTicks, yo, y) - camPos.y());
        float pz = (float) (Mth.lerp(partialTicks, zo, z) - camPos.z());

        org.joml.Quaternionf rotation = camera.rotation();

        float currentRot = rotationAngle + rotationSpeed * partialTicks;
        org.joml.Quaternionf spinRot = new org.joml.Quaternionf().rotateZ((float) Math.toRadians(currentRot));
        rotation = rotation.mul(spinRot, new org.joml.Quaternionf());

        int sprite = 72 + spriteFrame;
        float u0 = (sprite % 64) / 64.0f;
        float u1 = u0 + 0.015625f;
        float v0 = (sprite / 64) / 64.0f;
        float v1 = v0 + 0.015625f;

        int light = 0xF000F0;

        state.add(getLayer(), px, py, pz,
                rotation.x(), rotation.y(), rotation.z(), rotation.w(), quadSize,
                (u0), (u1),
                (v0), (v1),
                net.minecraft.util.ARGB.color((int) ((alpha) * 255.0F), (int) ((rCol) * 255.0F), (int) ((gCol) * 255.0F), (int) ((bCol) * 255.0F)), light);
    }

    @Override
    public net.minecraft.client.particle.ParticleRenderType getGroup() {
        return net.minecraft.client.particle.ParticleRenderType.SINGLE_QUADS;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TCSprites.translucent();
    }
}