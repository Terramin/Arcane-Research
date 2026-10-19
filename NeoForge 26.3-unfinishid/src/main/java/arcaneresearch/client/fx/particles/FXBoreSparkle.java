package arcaneresearch.client.fx.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.Mth;
import arcaneresearch.client.fx.particles.TCSprites;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;

// NEOFORGE_PORT: camera quad -> base SingleQuadParticle.extract.
public class FXBoreSparkle extends SingleQuadParticle {

    private Entity target;
    private double targetX;
    private double targetY;
    private double targetZ;
    private int particle = 24;

    public FXBoreSparkle(ClientLevel level, double x, double y, double z,
                         double tx, double ty, double tz) {
        super(level, x, y, z, 0, 0, 0, null);

        this.rCol = 0.2f;
        this.gCol = 0.6f + random.nextFloat() * 0.3f;
        this.bCol = 0.2f;

        this.quadSize = random.nextFloat() * 0.5f + 0.5f;

        this.targetX = tx;
        this.targetY = ty;
        this.targetZ = tz;

        double dx = tx - x;
        double dy = ty - y;
        double dz = tz - z;
        int base = (int)(Mth.sqrt((float)(dx * dx + dy * dy + dz * dz)) * 10.0f);
        if (base < 1) base = 1;
        this.lifetime = base / 2 + random.nextInt(base);

        float f3 = 0.01f;
        this.xd = (float)random.nextGaussian() * f3;
        this.yd = (float)random.nextGaussian() * f3;
        this.zd = (float)random.nextGaussian() * f3;

        this.gravity = 0.2f;

        Minecraft mc = Minecraft.getInstance();
        int visibleDistance = mc.options.graphicsPreset().get().ordinal() > 0 ? 64 : 32;
        if (mc.getCameraEntity() != null && mc.getCameraEntity().distanceToSqr(x, y, z) > visibleDistance * visibleDistance) {
            this.lifetime = 0;
        }
    }

    public FXBoreSparkle(ClientLevel level, double x, double y, double z, Entity target) {
        this(level, x, y, z, target.getX(), target.getY() + target.getEyeHeight(), target.getZ());
        this.target = target;
    }

    @Override
    public void tick() {
        this.xo = x;
        this.yo = y;
        this.zo = z;

        if (target != null) {
            targetX = target.getX();
            targetY = target.getY() + target.getEyeHeight();
            targetZ = target.getZ();
        }

        if (this.age++ >= this.lifetime ||
            (Mth.floor(x) == Mth.floor(targetX) &&
             Mth.floor(y) == Mth.floor(targetY) &&
             Mth.floor(z) == Mth.floor(targetZ))) {
            this.remove();
            return;
        }

        move(xd, yd, zd);

        xd *= 0.985;
        yd *= 0.95;
        zd *= 0.985;

        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        double dist = Mth.sqrt((float)(dx * dx + dy * dy + dz * dz));
        double clamp = Math.min(0.25, dist / 15.0);

        if (dist < 2.0) {
            quadSize *= 0.9f;
        }

        dx /= dist;
        dy /= dist;
        dz /= dist;

        xd += dx * clamp;
        yd += dy * clamp;
        zd += dz * clamp;

        xd = Mth.clamp((float)xd, (float)-clamp, (float)clamp);
        yd = Mth.clamp((float)yd, (float)-clamp, (float)clamp);
        zd = Mth.clamp((float)zd, (float)-clamp, (float)clamp);

        xd += random.nextGaussian() * 0.01;
        yd += random.nextGaussian() * 0.01;
        zd += random.nextGaussian() * 0.01;
    }


    @Override
    public net.minecraft.client.particle.ParticleRenderType getGroup() {
        return net.minecraft.client.particle.ParticleRenderType.SINGLE_QUADS;
    }

    @Override
    protected float getU0() {
        return ((age % 4) / 64.0f);
    }

    @Override
    protected float getU1() {
        return ((age % 4) / 64.0f + 0.015625f);
    }

    @Override
    protected float getV0() {
        return (0.0625f);
    }

    @Override
    protected float getV1() {
        return (0.0625f + 0.015625f);
    }

    @Override
    public float getQuadSize(float partialTicks) {
        return 0.1f * this.quadSize * (Mth.sin(age / 3.0f) * 0.5f + 1.0f);
    }

    @Override
    protected int getLightCoords(float partialTicks) {
        return 0xF000F0;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TCSprites.additive();
    }

    public void setGravity(float value) {
        this.gravity = value;
    }
}