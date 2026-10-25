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
import org.joml.Quaternionf;
import org.joml.Vector3f;
import arcaneresearch.init.ModSounds;
import java.util.ArrayList;
@OnlyIn(Dist.CLIENT)
public class FXSwarm extends TextureSheetParticle {
    private static final ArrayList<Long> buzzCount = new ArrayList<>();
    private Entity target;
    private float turnSpeed;
    private float speed;
    private int deathTimer = 0;
    private float rotationPitch;
    private float rotationYaw;
    private int particle = 40;
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
            Vec3 pos = new Vec3(x, y, z);
            double distSq = pos.distanceToSqr(target.getX(), target.getY(), target.getZ());
            if (distSq > target.getBbWidth() * target.getBbWidth() && !targetHurt) {
                faceEntity(target, turnSpeed / 2.0f + random.nextInt((int)(turnSpeed / 2.0f)),
                                   turnSpeed / 2.0f + random.nextInt((int)(turnSpeed / 2.0f)));
            } else {
                faceEntity(target, -(turnSpeed / 2.0f + random.nextInt((int)(turnSpeed / 2.0f))),
                                   -(turnSpeed / 2.0f + random.nextInt((int)(turnSpeed / 2.0f))));
            }
            xd = -Mth.sin(rotationYaw / 180.0f * (float)Math.PI) *
                  Mth.cos(rotationPitch / 180.0f * (float)Math.PI);
            zd = Mth.cos(rotationYaw / 180.0f * (float)Math.PI) *
                 Mth.cos(rotationPitch / 180.0f * (float)Math.PI);
            yd = -Mth.sin(rotationPitch / 180.0f * (float)Math.PI);
            setHeading(xd, yd, zd, speed, 15.0f);
        }
        if (buzzCount.size() < 3 && random.nextInt(50) == 0 &&
            level.getNearestPlayer(x, y, z, 8.0, false) != null) {
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
        AABB selfBB = new AABB(x - 0.01, y - 0.01, z - 0.01, x + 0.01, y + 0.01, z + 0.01);
        double dy = (targetBB.minY + targetBB.maxY) / 2.0 - (selfBB.minY + selfBB.maxY) / 2.0;
        double horizDist = Mth.sqrt((float)(dx * dx + dz * dz));
        float targetYaw = (float)(Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
        float targetPitch = (float)(-(Math.atan2(dy, horizDist) * 180.0 / Math.PI));
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
        float length = Mth.sqrt((float)(dx * dx + dy * dy + dz * dz));
        dx /= length;
        dy /= length;
        dz /= length;
        dx += random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * 0.0075 * randomness;
        dy += random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * 0.0075 * randomness;
        dz += random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * 0.0075 * randomness;
        xd = dx * speed;
        yd = dy * speed;
        zd = dz * speed;
    }
    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 camPos = camera.getPosition();
        float px = (float)(Mth.lerp(partialTicks, xo, x) - camPos.x());
        float py = (float)(Mth.lerp(partialTicks, yo, y) - camPos.y());
        float pz = (float)(Mth.lerp(partialTicks, zo, z) - camPos.z());
        float bob = Mth.sin(age / 3.0f) * 0.25f + 1.0f;
        float size = 0.1f * quadSize * bob;
        float trans = (50.0f - deathTimer) / 50.0f;
        float dd = 1.0f;
        if (target instanceof LivingEntity living && living.hurtTime > 0) {
            dd = 2.0f;
        }
        Quaternionf rotation = camera.rotation();
        Vector3f[] vertices = new Vector3f[] {
            new Vector3f(-1.0f, -1.0f, 0.0f),
            new Vector3f(-1.0f, 1.0f, 0.0f),
            new Vector3f(1.0f, 1.0f, 0.0f),
            new Vector3f(1.0f, -1.0f, 0.0f)
        };
        for (Vector3f vertex : vertices) {
            vertex.rotate(rotation);
            vertex.mul(size);
            vertex.add(px, py, pz);
        }
        int frame = 7 + age % 8;
        float u0 = frame / 64.0f;
        float u1 = u0 + 0.015625f;
        float v0 = 0.0625f;
        float v1 = v0 + 0.015625f;
        int light = 0xF000F0;
        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z())
                .uv(u1, v1).color(rCol, gCol / dd, bCol / dd, trans).uv2(light).endVertex();
        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z())
                .uv(u1, v0).color(rCol, gCol / dd, bCol / dd, trans).uv2(light).endVertex();
        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z())
                .uv(u0, v0).color(rCol, gCol / dd, bCol / dd, trans).uv2(light).endVertex();
        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z())
                .uv(u0, v1).color(rCol, gCol / dd, bCol / dd, trans).uv2(light).endVertex();
    }
    @Override
    public ParticleRenderType getRenderType() {
        return FXGeneric.TC_NORMAL;
    }
}
