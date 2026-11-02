package arcaneresearch.client.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import arcaneresearch.init.ModSounds;

import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class FXSwarm extends TextureSheetParticle {
    private static final int FULLBRIGHT = 0xF000F0;
    private static final float UV_STEP = 0.015625f;
    private static final float SIZE_FACTOR = 0.1f;
    private static final float DEATH_LIMIT = 50.0f;
    private static final double WANDER_SCALE = 0.0075;
    private static final float MIN_HEADING_LEN = 1.0e-6f;

    private static final ArrayList<Long> buzzCount = new ArrayList<>();

    private Entity target;
    private float turnSpeed;
    private float speed;
    private int deathTimer = 0;
    private float rotationPitch;
    private float rotationYaw;
    private int particle = 40;

    private final Vector3f renderScratch = new Vector3f();

    public FXSwarm(ClientLevel level, double x, double y, double z,
                   Entity target, float r, float g, float b) {
        super(level, x, y, z, 0, 0, 0);

        this.target = target;
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
        this.quadSize = random.nextFloat() * 0.5f + 1.0f;

        float f3 = 0.2f;
        this.xd = (random.nextFloat() - random.nextFloat()) * f3;
        this.yd = (random.nextFloat() - random.nextFloat()) * f3;
        this.zd = (random.nextFloat() - random.nextFloat()) * f3;

        this.gravity = 0.1f;
        this.turnSpeed = 10.0f;
        this.speed = 0.2f;
        this.lifetime = Integer.MAX_VALUE;
    }

    public FXSwarm(ClientLevel level, double x, double y, double z,
                   Entity target, float r, float g, float b,
                   float sp, float ts, float pg) {
        this(level, x, y, z, target, r, g, b);
        this.speed = sp;
        this.turnSpeed = ts;
        this.gravity = pg;
    }

    @Override
    public void tick() {
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.age++;

        boolean targetDead = target == null || !target.isAlive() ||
                (target instanceof LivingEntity living && living.deathTime > 0);

        if (targetDead) {
            deathTimer++;
            xd *= 0.9;
            zd *= 0.9;
            yd -= gravity / 2.0f;

            if (deathTimer > 50) {
                this.remove();
                return;
            }
        } else {
            yd += gravity;
        }

        move(xd, yd, zd);

        xd *= 0.985;
        yd *= 0.985;
        zd *= 0.985;

        if (!targetDead) {
            boolean targetHurt = target instanceof LivingEntity living && living.hurtTime > 0;

            double distSq = (x - target.getX()) * (x - target.getX())
                    + (y - target.getY()) * (y - target.getY())
                    + (z - target.getZ()) * (z - target.getZ());

            int turnJitter = Math.max(1, (int) (turnSpeed / 2.0f));
            int jitterA = random.nextInt(turnJitter);
            int jitterB = random.nextInt(turnJitter);

            if (distSq > target.getBbWidth() * target.getBbWidth() && !targetHurt) {
                faceEntity(target, turnJitter + jitterA, turnJitter + jitterB);
            } else {
                faceEntity(target, -(turnJitter + jitterA), -(turnJitter + jitterB));
            }

            float yawRad = rotationYaw * Mth.DEG_TO_RAD;
            float pitchRad = rotationPitch * Mth.DEG_TO_RAD;

            float cosPitch = Mth.cos(pitchRad);
            float sinPitch = Mth.sin(pitchRad);
            float sinYaw = Mth.sin(yawRad);
            float cosYaw = Mth.cos(yawRad);

            xd = -sinYaw * cosPitch;
            zd = cosYaw * cosPitch;
            yd = -sinPitch;

            setHeading(xd, yd, zd, speed, 15.0f);
        }

        if (buzzCount.size() < 3
                && random.nextInt(50) == 0
                && level.getNearestPlayer(x, y, z, 8.0, false) != null) {
            level.playLocalSound(x, y, z, ModSounds.FLY.get(), SoundSource.HOSTILE,
                    0.03f, 0.5f + random.nextFloat() * 0.4f, false);
            buzzCount.add(System.nanoTime() + 1500000L);
        }

        if (buzzCount.size() >= 3 && buzzCount.get(0) < System.nanoTime()) {
            buzzCount.remove(0);
        }
    }

    private void faceEntity(Entity target, float yawChange, float pitchChange) {
        double dx = target.getX() - x;
        double dz = target.getZ() - z;

        AABB targetBB = target.getBoundingBox();
        double dy = (targetBB.minY + targetBB.maxY) / 2.0 - y;

        double horizDist = Mth.sqrt((float) (dx * dx + dz * dz));

        float targetYaw = (float) (Math.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0f;
        float targetPitch = (float) (-(Math.atan2(dy, horizDist) * Mth.RAD_TO_DEG));

        rotationPitch = updateRotation(rotationPitch, targetPitch, pitchChange);
        rotationYaw = updateRotation(rotationYaw, targetYaw, yawChange);
    }

    private float updateRotation(float current, float target, float maxChange) {
        float delta = Mth.wrapDegrees(target - current);

        if (delta > maxChange) delta = maxChange;
        if (delta < -maxChange) delta = -maxChange;

        return current + delta;
    }

    private void setHeading(double dx, double dy, double dz, float speed, float randomness) {
        float length = Mth.sqrt((float) (dx * dx + dy * dy + dz * dz));

        if (length <= MIN_HEADING_LEN) return;

        double invLen = 1.0 / length;
        dx *= invLen;
        dy *= invLen;
        dz *= invLen;

        double wander = WANDER_SCALE * randomness;

        dx += random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * wander;
        dy += random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * wander;
        dz += random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * wander;

        xd = dx * speed;
        yd = dy * speed;
        zd = dz * speed;
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 camPos = camera.getPosition();

        float px = (float) (Mth.lerp(partialTicks, xo, x) - camPos.x());
        float py = (float) (Mth.lerp(partialTicks, yo, y) - camPos.y());
        float pz = (float) (Mth.lerp(partialTicks, zo, z) - camPos.z());

        float bob = Mth.sin(age / 3.0f) * 0.25f + 1.0f;
        float size = SIZE_FACTOR * quadSize * bob;

        float trans = Mth.clamp((DEATH_LIMIT - deathTimer) / DEATH_LIMIT, 0.0f, 1.0f);

        float dd = 1.0f;

        if (target instanceof LivingEntity living && living.hurtTime > 0) {
            dd = 2.0f;
        }

        Quaternionf rotation = camera.rotation();

        int frame = 7 + age % 8;
        float u0 = frame / 64.0f;
        float u1 = u0 + UV_STEP;
        float v0 = 0.0625f;
        float v1 = v0 + UV_STEP;

        float cr = this.rCol;
        float cg = this.gCol / dd;
        float cb = this.bCol / dd;

        Vector3f v = renderScratch;

        v.set(-1.0f, -1.0f, 0.0f).rotate(rotation).mul(size).add(px, py, pz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u1, v1).color(cr, cg, cb, trans).uv2(FULLBRIGHT).endVertex();

        v.set(-1.0f, 1.0f, 0.0f).rotate(rotation).mul(size).add(px, py, pz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u1, v0).color(cr, cg, cb, trans).uv2(FULLBRIGHT).endVertex();

        v.set(1.0f, 1.0f, 0.0f).rotate(rotation).mul(size).add(px, py, pz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u0, v0).color(cr, cg, cb, trans).uv2(FULLBRIGHT).endVertex();

        v.set(1.0f, -1.0f, 0.0f).rotate(rotation).mul(size).add(px, py, pz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u0, v1).color(cr, cg, cb, trans).uv2(FULLBRIGHT).endVertex();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return FXGeneric.TC_NORMAL;
    }
}