package arcaneresearch.client.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
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

@OnlyIn(Dist.CLIENT)
public class FXSwarmRunes extends ArcaneresearchParticle {
    private static final int LIGHT = 240;
    private static final float UV_STEP = 0.015625f;
    private static final float V_START = 0.09375f;
    private static final float SIZE_FACTOR = 0.07f;
    private static final float DEATH_LIMIT = 50.0f;
    private static final float ALPHA_FACTOR = 0.66f;
    private static final double WANDER_SCALE = 0.0075;
    private static final float MIN_HEADING_LEN = 1.0e-6f;
    private static final int MAX_AGE = 200;

    private final Vector3f renderScratch = new Vector3f();

    private Entity target;
    private float turnSpeed;
    private float speed;
    private int deathTimer;
    private float rotationPitch;
    private float rotationYaw;
    public int particle;

    public FXSwarmRunes(ClientLevel level, double x, double y, double z,
                        Entity target, float r, float g, float b) {
        super(level, x, y, z);

        this.turnSpeed = 10.0f;
        this.speed = 0.2f;
        this.deathTimer = 0;
        this.particle = 0;

        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
        this.startR = r;
        this.startG = g;
        this.startB = b;
        this.endR = r;
        this.endG = g;
        this.endB = b;

        this.quadSize = this.random.nextFloat() * 0.5f + 1.0f;
        this.target = target;

        float f3 = 0.2f;
        this.xd = (this.random.nextFloat() - this.random.nextFloat()) * f3;
        this.yd = (this.random.nextFloat() - this.random.nextFloat()) * f3;
        this.zd = (this.random.nextFloat() - this.random.nextFloat()) * f3;

        this.gravity = 0.1f;
        this.lifetime = 250;
        this.noClip = true;
    }

    public FXSwarmRunes(ClientLevel level, double x, double y, double z,
                        Entity target, float r, float g, float b,
                        float sp, float ts, float pg) {
        this(level, x, y, z, target, r, g, b);
        this.speed = sp;
        this.turnSpeed = ts;
        this.gravity = pg;
        this.particle = this.random.nextInt(16);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.age++;

        boolean targetDead = target == null || !target.isAlive() ||
                (target instanceof LivingEntity living && living.deathTime > 0);

        if (this.age > MAX_AGE || targetDead) {
            this.deathTimer++;
            this.xd *= 0.9;
            this.zd *= 0.9;
            this.yd -= this.gravity / 2.0f;

            if (this.deathTimer > (int) DEATH_LIMIT) {
                this.remove();
                return;
            }
        } else {
            this.yd += this.gravity;
        }

        this.x += this.xd;
        this.y += this.yd;
        this.z += this.zd;

        this.xd *= 0.985;
        this.yd *= 0.985;
        this.zd *= 0.985;

        if (this.age < MAX_AGE && target != null && target.isAlive() &&
                !(target instanceof LivingEntity living && living.deathTime > 0)) {

            boolean hurt = target instanceof LivingEntity living && living.hurtTime > 0;

            double dx = this.x - target.getX();
            double dy = this.y - target.getY();
            double dz = this.z - target.getZ();
            double distSq = dx * dx + dy * dy + dz * dz;

            double targetWidth = target.getBbWidth();
            int turnJitter = Math.max(1, (int) (turnSpeed / 2.0f));
            int jitterA = this.random.nextInt(turnJitter);
            int jitterB = this.random.nextInt(turnJitter);

            if (distSq > targetWidth * targetWidth && !hurt) {
                faceEntity(target, turnJitter + jitterA, turnJitter + jitterB);
            } else {
                if (hurt && distSq < targetWidth * targetWidth) {
                    this.age += 100;
                }
                faceEntity(target, -(turnJitter + jitterA), -(turnJitter + jitterB));
            }

            float yawRad = this.rotationYaw * Mth.DEG_TO_RAD;
            float pitchRad = this.rotationPitch * Mth.DEG_TO_RAD;

            float cosPitch = Mth.cos(pitchRad);
            float sinPitch = Mth.sin(pitchRad);
            float sinYaw = Mth.sin(yawRad);
            float cosYaw = Mth.cos(yawRad);

            this.xd = -sinYaw * cosPitch;
            this.zd = cosYaw * cosPitch;
            this.yd = -sinPitch;

            setHeading(this.xd, this.yd, this.zd, speed, 15.0f);
        }
    }

    private void faceEntity(Entity target, float yawDelta, float pitchDelta) {
        double dx = target.getX() - this.x;
        double dz = target.getZ() - this.z;

        AABB bb = target.getBoundingBox();
        double dy = (bb.minY + bb.maxY) / 2.0 - this.y;

        double dist = Mth.sqrt((float) (dx * dx + dz * dz));

        float targetYaw = (float) (Math.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0f;
        float targetPitch = (float) (-(Math.atan2(dy, dist) * Mth.RAD_TO_DEG));

        this.rotationPitch = updateRotation(this.rotationPitch, targetPitch, pitchDelta);
        this.rotationYaw = updateRotation(this.rotationYaw, targetYaw, yawDelta);
    }

    private float updateRotation(float current, float target, float maxDelta) {
        float delta = Mth.wrapDegrees(target - current);

        if (delta > maxDelta) delta = maxDelta;
        if (delta < -maxDelta) delta = -maxDelta;

        return current + delta;
    }

    private void setHeading(double vx, double vy, double vz, float speed, float spread) {
        float len = Mth.sqrt((float) (vx * vx + vy * vy + vz * vz));

        if (len <= MIN_HEADING_LEN) return;

        double inv = 1.0 / len;
        vx *= inv;
        vy *= inv;
        vz *= inv;

        double wander = WANDER_SCALE * spread;

        vx += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * wander;
        vy += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * wander;
        vz += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * wander;

        this.xd = vx * speed;
        this.yd = vy * speed;
        this.zd = vz * speed;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        float bob = Mth.sin(this.age / 3.0f) * 0.25f + 1.0f;
        float trans = Mth.clamp((DEATH_LIMIT - this.deathTimer) / DEATH_LIMIT * ALPHA_FACTOR, 0.0f, 1.0f);

        Vec3 cameraPos = camera.getPosition();

        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        Quaternionf quaternion = camera.rotation();

        float size = SIZE_FACTOR * this.quadSize * bob;

        float u0 = this.particle / 64.0f;
        float u1 = u0 + UV_STEP;
        float v0 = V_START;
        float v1 = v0 + UV_STEP;

        float cr = this.rCol;
        float cg = this.gCol;
        float cb = this.bCol;

        Vector3f v = renderScratch;

        v.set(-1.0F, -1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u1, v1).color(cr, cg, cb, trans).uv2(LIGHT).endVertex();

        v.set(-1.0F, 1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u1, v0).color(cr, cg, cb, trans).uv2(LIGHT).endVertex();

        v.set(1.0F, 1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u0, v0).color(cr, cg, cb, trans).uv2(LIGHT).endVertex();

        v.set(1.0F, -1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u0, v1).color(cr, cg, cb, trans).uv2(LIGHT).endVertex();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return FXGeneric.TC_ADDITIVE;
    }
}