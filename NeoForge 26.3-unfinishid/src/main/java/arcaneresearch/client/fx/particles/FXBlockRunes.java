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

// NEOFORGE_PORT: fixed-orientation quad -> extract() with custom quaternion.
public class FXBlockRunes extends SingleQuadParticle {

    protected double offsetX;
    protected double offsetY;
    protected float rotation;
    protected int runeIndex;

    protected static final int GRID_SIZE = 64;

    public FXBlockRunes(ClientLevel level, double x, double y, double z,
                        float r, float g, float b, int duration) {
        super(level, x, y, z, 0, 0, 0, null);

        this.offsetX = 0.0;
        this.offsetY = 0.0;
        this.rotation = 0.0f;
        this.runeIndex = 0;

        if (r == 0.0f) r = 1.0f;

        this.rotation = this.random.nextInt(4) * 90.0f;

        this.rCol = r;
        this.gCol = g;
        this.bCol = b;

        this.gravity = 0.0f;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;

        this.lifetime = 3 * duration;
        this.setSize(0.01f, 0.01f);

        this.runeIndex = (int) (Math.random() * 16.0 + 224.0);

        this.offsetX = this.random.nextFloat() * 0.2;
        this.offsetY = -0.3 + this.random.nextFloat() * 0.6;

        this.quadSize = (float) (1.0 + this.random.nextGaussian() * 0.1);
        this.alpha = 0.0f;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        float threshold = this.lifetime / 5.0f;
        if (this.age <= threshold) {
            this.alpha = this.age / threshold;
        } else {
            this.alpha = (this.lifetime - this.age) / (float) this.lifetime;
        }

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.yd -= 0.04 * this.gravity;
        this.x += this.xd;
        this.y += this.yd;
        this.z += this.zd;
    }


    // NEOFORGE_PORT: getLightColor -> getLightCoords (same fullbright contract).
    @Override
    protected int getLightCoords(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public net.minecraft.client.particle.ParticleRenderType getGroup() {
        return net.minecraft.client.particle.ParticleRenderType.SINGLE_QUADS;
    }

    // NEOFORGE_PORT: same math as the old render(); fixed quat -> state.add.
    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTicks) {
        Vec3 cameraPos = camera.position();
        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        float u0 = (runeIndex % 16) / 64.0f;
        float u1 = u0 + 0.015625f;
        float v0 = 0.09375f;
        float v1 = v0 + 0.015625f;

        float size = 0.3f * this.quadSize;
        float displayAlpha = this.alpha / 2.0f;

        int light = this.getLightCoords(partialTicks);

        org.joml.Quaternionf quaternion = new org.joml.Quaternionf();
        quaternion.rotateY((float) Math.toRadians(rotation));
        quaternion.rotateZ((float) Math.toRadians(90.0f));

        float ox = (float) this.offsetX;
        float oy = (float) this.offsetY;

        state.add(getLayer(), x + ox, y + oy, (float) (z - 0.51),
                quaternion.x(), quaternion.y(), quaternion.z(), quaternion.w(), size,
                (u0), (u1),
                (v0), (v1),
                net.minecraft.util.ARGB.color((int) ((displayAlpha) * 255.0F), (int) ((this.rCol) * 255.0F), (int) ((this.gCol) * 255.0F), (int) ((this.bCol) * 255.0F)), light);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TCSprites.translucent();
    }

    public void setScale(float scale) {
        this.quadSize = scale;
    }

    public void setOffsetX(double offset) {
        this.offsetX = offset;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }
}