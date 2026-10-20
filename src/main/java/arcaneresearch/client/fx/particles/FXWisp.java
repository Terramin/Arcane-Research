package arcaneresearch.client.fx.particles;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.Mth;
import arcaneresearch.client.fx.particles.TCSprites;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import org.joml.Quaternionf;

// NEOFORGE_PORT: custom per-frame math -> extract() override (same math).
public class FXWisp extends SingleQuadParticle {

    protected Entity target;
    protected int blendMode = 1;

    protected int spriteIndexX = 0;
    protected int spriteIndexY = 0;
    protected static final int GRID_SIZE = 64;

    public FXWisp(ClientLevel level, double x, double y, double z, Entity target) {
        super(level, x, y, z, 0, 0, 0, null);

        this.target = target;

        this.xd = this.random.nextGaussian() * 0.03;
        this.yd = -0.05;
        this.zd = this.random.nextGaussian() * 0.03;

        this.quadSize *= 0.4f;
        this.lifetime = (int) (40.0 / (Math.random() * 0.3 + 0.7));

        this.setSize(0.01f, 0.01f);

        this.blendMode = 771;

        this.rCol = this.random.nextFloat() * 0.05f;
        this.gCol = this.random.nextFloat() * 0.05f;
        this.bCol = this.random.nextFloat() * 0.05f;
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

        if (target != null && !this.onGround) {
            this.x += target.getDeltaMovement().x();
            this.z += target.getDeltaMovement().z();
        }
    }


    // NEOFORGE_PORT: getLightColor -> getLightCoords (same fullbright contract).
    @Override
    protected int getLightCoords(float partialTick) {
        return 0xF000F0;
    }

    // NEOFORGE_PORT: same math as the old render(); immediate verts -> state.add.
    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTicks) {
        net.minecraft.world.entity.Entity viewEntity = net.minecraft.client.Minecraft.getInstance().getCameraEntity();
        if (viewEntity == null) {
            return;
        }

        float ageScale = 1.0f - (float) this.age / (float) this.lifetime;

        float maxDist = 1024.0f;
        double dist = viewEntity.position().distanceToSqr(this.x, this.y, this.z);
        float distFade = (float) (1.0 - Math.min(maxDist, dist) / maxDist);

        float finalAlpha = 0.2f * ageScale * distFade;
        if (finalAlpha <= 0) {
            return;
        }

        Vec3 cameraPos = camera.position();
        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        float size = 0.5f * this.quadSize;

        int frame = this.age % 13;
        float u0 = frame / 64.0f;
        float u1 = u0 + 0.015625f;
        float v0 = 0.046875f;
        float v1 = v0 + 0.015625f;

        int light = this.getLightCoords(partialTicks);

        org.joml.Quaternionf quaternion = camera.rotation();
        state.add(getLayer(), x, y, z,
                quaternion.x(), quaternion.y(), quaternion.z(), quaternion.w(), size,
                (u0), (u1),
                (v0), (v1),
                net.minecraft.util.ARGB.color((int) ((finalAlpha) * 255.0F), (int) ((this.rCol) * 255.0F), (int) ((this.gCol) * 255.0F), (int) ((this.bCol) * 255.0F)), light);
    }

    @Override
    public net.minecraft.client.particle.ParticleRenderType getGroup() {
        return net.minecraft.client.particle.ParticleRenderType.SINGLE_QUADS;
    }

    @Override
public SingleQuadParticle.Layer getLayer() {
        // NEOFORGE_PORT: blendMode switch preserved (additive vs normal sheet layers).
        return blendMode == 1 ? TCSprites.additive() : TCSprites.translucent();
    }

    public int getFXLayer() {
        return (blendMode != 1) ? 1 : 0;
    }

    @Override
    public void setColor(float r, float g, float b) {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
    }

    public void setScale(float scale) {
        this.quadSize = scale;
    }

    public void setMaxAge(int maxAge) {
        this.lifetime = maxAge;
    }

    public void setBlendMode(int mode) {
        this.blendMode = mode;
    }
}