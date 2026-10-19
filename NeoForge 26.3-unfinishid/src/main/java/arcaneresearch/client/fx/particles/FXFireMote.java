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

// NEOFORGE_PORT: custom per-frame math -> extract() override (same math).
public class FXFireMote extends SingleQuadParticle {

    protected float baseScale;
    protected float baseAlpha;
    protected int glowLayer;

    protected int spriteIndexX = 0;
    protected int spriteIndexY = 0;
    protected static final int GRID_SIZE = 64;

    public FXFireMote(ClientLevel level, double x, double y, double z,
                      double vx, double vy, double vz,
                      float r, float g, float b, float scale, int layer) {
        super(level, x, y, z, 0, 0, 0, null);

        float colorR = r;
        float colorG = g;
        float colorB = b;
        if (colorR > 1.0f) colorR /= 255.0f;
        if (colorG > 1.0f) colorG /= 255.0f;
        if (colorB > 1.0f) colorB /= 255.0f;

        this.rCol = colorR;
        this.gCol = colorG;
        this.bCol = colorB;

        this.glowLayer = layer;
        this.lifetime = 16;
        this.quadSize = scale;
        this.baseScale = scale;
        this.baseAlpha = 1.0f;

        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        this.roll = (float) (Math.PI * 2);

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
    public void tick() {
        super.tick();

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
    protected int getLightCoords(float partialTick) {
        return 0xF000F0;
    }

    // NEOFORGE_PORT: grid fractions remapped into the stitched sheet sprite (same art).
    @Override
    protected float getU0() {
        return ((float) spriteIndexX / (float) GRID_SIZE);
    }

    @Override
    protected float getU1() {
        return (((float) spriteIndexX + 1.0f) / (float) GRID_SIZE);
    }

    @Override
    protected float getV0() {
        return ((float) spriteIndexY / (float) GRID_SIZE);
    }

    @Override
    protected float getV1() {
        return (((float) spriteIndexY + 1.0f) / (float) GRID_SIZE);
    }

    // NEOFORGE_PORT: same math as the old render(); immediate verts -> state.add.
    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTicks) {
        Vec3 cameraPos = camera.position();
        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        float size = 0.1f * this.getQuadSize(partialTicks);

        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();

        int light = this.getLightCoords(partialTicks);

        org.joml.Quaternionf quaternion;
        if (this.roll == 0.0F) {
            quaternion = camera.rotation();
        } else {
            quaternion = new org.joml.Quaternionf(camera.rotation());
            float rollAngle = Mth.lerp(partialTicks, this.oRoll, this.roll);
            quaternion.rotateZ(rollAngle);
        }

        float alpha = this.alpha * this.baseAlpha;

        state.add(getLayer(), x, y, z,
                quaternion.x(), quaternion.y(), quaternion.z(), quaternion.w(), size,
                u0, u1, v0, v1,
                net.minecraft.util.ARGB.color((int) ((alpha) * 255.0F), (int) ((this.rCol) * 255.0F), (int) ((this.gCol) * 255.0F), (int) ((this.bCol) * 255.0F)), light);
    }

    @Override
    public net.minecraft.client.particle.ParticleRenderType getGroup() {
        return net.minecraft.client.particle.ParticleRenderType.SINGLE_QUADS;
    }

    @Override
public SingleQuadParticle.Layer getLayer() {
        // NEOFORGE_PORT: glowLayer switch preserved (additive vs normal sheet layers).
        return glowLayer == 0 ? TCSprites.additive() : TCSprites.translucent();
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