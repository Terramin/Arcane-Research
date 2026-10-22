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

import java.awt.Color;


public class FXVent extends SingleQuadParticle {

    
    protected float endScale;
    protected static final int GRID_SIZE = 64;

    public FXVent(ClientLevel level, double x, double y, double z,
                  double vx, double vy, double vz, int color) {
        super(level, x, y, z, vx, vy, vz, null);

        this.endScale = 1.0f;
        this.setSize(0.02f, 0.02f);
        this.quadSize = this.random.nextFloat() * 0.1f + 0.05f;

        this.xd = vx;
        this.yd = vy;
        this.zd = vz;

        Color c = new Color(color);
        this.rCol = c.getRed() / 255.0f;
        this.gCol = c.getGreen() / 255.0f;
        this.bCol = c.getBlue() / 255.0f;

        setHeading(vx, vy, vz, 0.125f, 5.0f);

        Entity viewEntity = Minecraft.getInstance().getCameraEntity();
        int visibleDistance = Minecraft.getInstance().options.graphicsPreset().get().ordinal() > 0 ? 50 : 25;
        if (viewEntity != null && viewEntity.distanceToSqr(x, y, z) > visibleDistance * visibleDistance) {
            this.lifetime = 0;
        }

        this.alpha = 1.0f;
    }

    public void setHeading(double vx, double vy, double vz, float speed, float spread) {
        double length = Math.sqrt(vx * vx + vy * vy + vz * vz);
        if (length > 0) {
            vx /= length;
            vy /= length;
            vz /= length;
        }

        vx += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075 * spread;
        vy += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075 * spread;
        vz += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075 * spread;

        this.xd = vx * speed;
        this.yd = vy * speed;
        this.zd = vz * speed;
    }

    public void setScale(float scale) {
        this.quadSize *= scale;
        this.endScale *= scale;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.age++;

        if (this.quadSize >= this.endScale) {
            this.remove();
            return;
        }

        this.yd += 0.0025;

        this.move(this.xd, this.yd, this.zd);

        this.xd *= 0.85;
        this.yd *= 0.85;
        this.zd *= 0.85;

        if (this.quadSize < this.endScale) {
            this.quadSize *= 1.15f;
        }
        if (this.quadSize > this.endScale) {
            this.quadSize = this.endScale;
        }

        if (this.onGround) {
            this.xd *= 0.7;
            this.zd *= 0.7;
        }
    }


    
    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTicks) {
        Vec3 cameraPos = camera.position();
        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        int spriteIndex = (int) (1.0f + this.quadSize / this.endScale * 4.0f);
        float u0 = (spriteIndex % 16) / 64.0f;
        float u1 = u0 + 0.015625f;
        float v0 = (spriteIndex / 16) / 64.0f;
        float v1 = v0 + 0.015625f;

        float size = 0.3f * this.quadSize;

        float displayAlpha = this.alpha * ((this.endScale - this.quadSize) / this.endScale);

        int light = this.getLightCoords(partialTicks);

        org.joml.Quaternionf quaternion = camera.rotation();
        state.add(getLayer(), x, y, z,
                quaternion.x(), quaternion.y(), quaternion.z(), quaternion.w(), size,
                (u0), (u1),
                (v0), (v1),
                net.minecraft.util.ARGB.color((int) ((displayAlpha) * 255.0F), (int) ((this.rCol) * 255.0F), (int) ((this.gCol) * 255.0F), (int) ((this.bCol) * 255.0F)), light);
    }

    @Override
    public net.minecraft.client.particle.ParticleRenderType getGroup() {
        return net.minecraft.client.particle.ParticleRenderType.SINGLE_QUADS;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TCSprites.translucent();
    }

    public void setRGB(float r, float g, float b) {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
    }
}