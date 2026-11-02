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
public class FXFireMote extends TextureSheetParticle {
    protected static final int GRID_SIZE = 64;
    private static final int FULLBRIGHT = 0xF000F0;
    private static final float TWO_PI = (float) (Math.PI * 2.0);

    protected float baseScale;
    protected float baseAlpha;
    protected int glowLayer;
    protected int spriteIndexX = 0;
    protected int spriteIndexY = 0;

    private final Vector3f renderScratch = new Vector3f();
    private final Quaternionf renderQuat = new Quaternionf();

    public FXFireMote(ClientLevel level, double x, double y, double z,
                      double vx, double vy, double vz,
                      float r, float g, float b, float scale, int layer) {
        super(level, x, y, z, 0, 0, 0);

        if (r > 1.0f) r /= 255.0f;
        if (g > 1.0f) g /= 255.0f;
        if (b > 1.0f) b /= 255.0f;

        this.rCol = r;
        this.gCol = g;
        this.bCol = b;

        this.glowLayer = layer;
        this.lifetime = 16;
        this.quadSize = scale;
        this.baseScale = scale;
        this.baseAlpha = 1.0f;

        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        this.roll = TWO_PI;

        setParticleTextureIndex(7);
    }

    public FXFireMote(ClientLevel level, double x, double y, double z,
                      double vx, double vy, double vz,
                      float r, float g, float b, float scale) {
        this(level, x, y, z, vx, vy, vz, r, g, b, scale, 0);
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
        super.tick();

        if (this.removed) return;

        if (this.random.nextInt(6) == 0) {
            this.age++;
        }

        if (this.age >= this.lifetime) {
            this.remove();
            return;
        }

        float lifespan = (float) this.age / (float) this.lifetime;

        this.quadSize = baseScale - baseScale * lifespan;
        this.baseAlpha = 1.0f - lifespan;

        this.oRoll = this.roll;
        this.roll += 1.0f;
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 cameraPos = camera.getPosition();

        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        float size = 0.1f * this.getQuadSize(partialTicks);

        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();

        int light = this.getLightColor(partialTicks);

        Quaternionf quaternion = renderQuat.set(camera.rotation());

        if (this.roll != 0.0F) {
            float rollAngle = Mth.lerp(partialTicks, this.oRoll, this.roll);
            quaternion.rotateZ(rollAngle);
        }

        float cr = this.rCol;
        float cg = this.gCol;
        float cb = this.bCol;
        float ca = this.alpha * this.baseAlpha;

        Vector3f v = renderScratch;

        v.set(-1.0F, -1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u1, v1).color(cr, cg, cb, ca).uv2(light).endVertex();

        v.set(-1.0F, 1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u1, v0).color(cr, cg, cb, ca).uv2(light).endVertex();

        v.set(1.0F, 1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u0, v0).color(cr, cg, cb, ca).uv2(light).endVertex();

        v.set(1.0F, -1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u0, v1).color(cr, cg, cb, ca).uv2(light).endVertex();
    }

    @Override
    public int getLightColor(float partialTick) {
        return FULLBRIGHT;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return glowLayer == 0 ? FXGeneric.TC_ADDITIVE : FXGeneric.TC_NORMAL;
    }

    @Override
    public void setColor(float r, float g, float b) {
        if (r > 1.0f) r /= 255.0f;
        if (g > 1.0f) g /= 255.0f;
        if (b > 1.0f) b /= 255.0f;
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
    }

    public void setScale(float scale) {
        this.quadSize = scale;
        this.baseScale = scale;
    }

    public void setMaxAge(int maxAge) {
        this.lifetime = maxAge;
    }

    @Override
    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setLayer(int layer) {
        this.glowLayer = layer;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }
}