package arcaneresearch.client.fx.particles;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.util.Mth;
import arcaneresearch.client.fx.particles.TCSprites;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import org.joml.Quaternionf;

import java.awt.Color;
import net.minecraft.client.particle.SingleQuadParticle;

public class FXVent2 extends ArcaneresearchParticle {

    private float maxScale;
    private float grav;

    public FXVent2(ClientLevel level, double x, double y, double z,
                    double vx, double vy, double vz, int color) {
        super(level, x, y, z, vx, vy, vz);

        this.setSize(0.02f, 0.02f);
        this.quadSize = this.random.nextFloat() * 0.1f + 0.05f;

        this.maxScale = 1.0f;

        Color c = new Color(color);
        this.rCol = (float) Mth.clamp(c.getRed() / 255.0f + this.random.nextGaussian() * 0.05, 0.0, 1.0);
        this.gCol = (float) Mth.clamp(c.getGreen() / 255.0f + this.random.nextGaussian() * 0.05, 0.0, 1.0);
        this.bCol = (float) Mth.clamp(c.getBlue() / 255.0f + this.random.nextGaussian() * 0.05, 0.0, 1.0);
        this.startR = this.rCol;
        this.startG = this.gCol;
        this.startB = this.bCol;
        this.endR = this.rCol;
        this.endG = this.gCol;
        this.endB = this.bCol;

        this.alpha = 1.0f;
        this.grav = (float) (this.random.nextGaussian() * 0.0075);
        this.gravity = 0;
        this.slowDown = 0.85;
        this.lifetime = Integer.MAX_VALUE;
        this.noClip = false;
    }

    public FXVent2 setScale(float f) {
        this.quadSize *= f;
        this.maxScale *= f;
        return this;
    }

    public FXVent2 setHeading(double vx, double vy, double vz, float speed, float spread) {
        float len = Mth.sqrt((float)(vx * vx + vy * vy + vz * vz));
        vx /= len;
        vy /= len;
        vz /= len;
        vx += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075 * spread;
        vy += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075 * spread;
        vz += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * 0.0075 * spread;
        this.xd = vx * speed;
        this.yd = vy * speed;
        this.zd = vz * speed;
        return this;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.age++;

        if (this.quadSize >= this.maxScale) {
            this.remove();
            return;
        }

        this.yd += this.grav;

        this.move(this.xd, this.yd, this.zd);

        this.xd *= this.slowDown;
        this.yd *= this.slowDown;
        this.zd *= this.slowDown;

        if (this.quadSize < this.maxScale) {
            this.quadSize *= 1.2f;
        }
        if (this.quadSize > this.maxScale) {
            this.quadSize = this.maxScale;
        }

        if (this.onGround) {
            this.xd *= 0.7;
            this.zd *= 0.7;
        }
    }

    public FXVent2 setRGB(float r, float g, float b) {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
        this.startR = r;
        this.startG = g;
        this.startB = b;
        this.endR = r;
        this.endG = g;
        this.endB = b;
        return this;
    }


    
    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTicks) {
        Vec3 cameraPos = camera.position();
        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        int spriteIndex = (int) (1.0f + this.quadSize / this.maxScale * 4.0f);
        float u0 = (spriteIndex % 16) / 64.0f;
        float u1 = u0 + 0.015625f;
        float v0 = (spriteIndex / 16) / 64.0f;
        float v1 = v0 + 0.015625f;

        float size = 0.3f * this.quadSize;

        float displayAlpha = this.alpha * ((this.maxScale - this.quadSize) / this.maxScale);

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
}