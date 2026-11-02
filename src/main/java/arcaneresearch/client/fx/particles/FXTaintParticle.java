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
public class FXTaintParticle extends TextureSheetParticle {
    private static final float BASE_R = 0.6f;
    private static final float BASE_G = 0.1f;
    private static final float BASE_B = 0.8f;

    private static final int FULLBRIGHT = 0xF000F0;
    private static final float UV_STEP = 0.015625f;
    private static final int SPRITE_BASE = 72;
    private static final float SWIRL_FORCE = 0.01f;
    private static final float SWIRL_PHASE_SCALE = 0.3f;
    private static final float FADE_START = 0.7f;
    private static final float FADE_LENGTH = 0.3f;
    private static final float MAX_ALPHA = 0.8f;

    private final Quaternionf renderQuat = new Quaternionf();
    private final Vector3f renderScratch = new Vector3f();

    private float rotationAngle;
    private float rotationSpeed;
    private int spriteFrame;

    public FXTaintParticle(ClientLevel level, double x, double y, double z,
                           double vx, double vy, double vz, float scale) {
        super(level, x, y, z, vx, vy, vz);

        this.rCol = BASE_R + (random.nextFloat() - 0.5f) * 0.2f;
        this.gCol = BASE_G + random.nextFloat() * 0.15f;
        this.bCol = BASE_B + (random.nextFloat() - 0.5f) * 0.15f;
        this.alpha = MAX_ALPHA;
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

        float swirlPhase = age * SWIRL_PHASE_SCALE;

        xd += Mth.sin(swirlPhase) * SWIRL_FORCE;
        zd += Mth.cos(swirlPhase) * SWIRL_FORCE;

        x += xd;
        y += yd;
        z += zd;

        yd -= gravity * 0.5;
        yd += 0.005;

        xd *= 0.95;
        yd *= 0.95;
        zd *= 0.95;

        float lifeProgress = (float) age / lifetime;

        if (lifeProgress > FADE_START) {
            float fade = (lifeProgress - FADE_START) / FADE_LENGTH;
            this.alpha = Mth.clamp(MAX_ALPHA * (1.0f - fade), 0.0f, MAX_ALPHA);
        }

        if (age % 3 == 0) {
            spriteFrame = (spriteFrame + 1) & 3;
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 camPos = camera.getPosition();

        float px = (float) (Mth.lerp(partialTicks, xo, x) - camPos.x());
        float py = (float) (Mth.lerp(partialTicks, yo, y) - camPos.y());
        float pz = (float) (Mth.lerp(partialTicks, zo, z) - camPos.z());

        float currentRot = rotationAngle + rotationSpeed * partialTicks;

        Quaternionf quaternion = renderQuat.set(camera.rotation());
        quaternion.rotateZ(currentRot * Mth.DEG_TO_RAD);

        int sprite = SPRITE_BASE + spriteFrame;
        float u0 = (sprite % 64) / 64.0f;
        float u1 = u0 + UV_STEP;
        float v0 = (sprite / 64) / 64.0f;
        float v1 = v0 + UV_STEP;

        float cr = this.rCol;
        float cg = this.gCol;
        float cb = this.bCol;
        float ca = this.alpha;
        float size = this.quadSize;

        Vector3f v = renderScratch;

        v.set(-1.0f, -1.0f, 0.0f).rotate(quaternion).mul(size).add(px, py, pz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u1, v1).color(cr, cg, cb, ca).uv2(FULLBRIGHT).endVertex();

        v.set(-1.0f, 1.0f, 0.0f).rotate(quaternion).mul(size).add(px, py, pz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u1, v0).color(cr, cg, cb, ca).uv2(FULLBRIGHT).endVertex();

        v.set(1.0f, 1.0f, 0.0f).rotate(quaternion).mul(size).add(px, py, pz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u0, v0).color(cr, cg, cb, ca).uv2(FULLBRIGHT).endVertex();

        v.set(1.0f, -1.0f, 0.0f).rotate(quaternion).mul(size).add(px, py, pz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u0, v1).color(cr, cg, cb, ca).uv2(FULLBRIGHT).endVertex();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return FXGeneric.TC_NORMAL;
    }
}