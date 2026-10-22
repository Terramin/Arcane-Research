package arcaneresearch.client.fx.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.Mth;
import arcaneresearch.client.fx.particles.TCSprites;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;


public class FXSlimyBubble extends SingleQuadParticle {

    private int particle = 144;

    public FXSlimyBubble(ClientLevel level, double x, double y, double z, float scale) {
        super(level, x, y, z, 0, 0, 0, null);

        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;

        this.gravity = 0.0f;
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;

        this.quadSize = scale;
        this.lifetime = 15 + random.nextInt(5);
        this.setSize(0.01f, 0.01f);
    }

    public FXSlimyBubble(ClientLevel level, double x, double y, double z, float scale, float r, float g, float b) {
        this(level, x, y, z, scale);
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
    }

    @Override
    public void tick() {
        this.xo = x;
        this.yo = y;
        this.zo = z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        if (age - 1 < 6) {

            particle = 144 + age / 2;
            if (age == 5) {

                y += 0.1;
            }
        } else if (age < lifetime - 4) {

            yd += 0.005;
            particle = 147 + (age % 4) / 2;
        } else {

            yd /= 2.0;
            particle = 150 - (lifetime - age) / 2;
        }

        y += yd;
    }


    @Override
    public net.minecraft.client.particle.ParticleRenderType getGroup() {
        return net.minecraft.client.particle.ParticleRenderType.SINGLE_QUADS;
    }

    @Override
    protected float getU0() {
        return ((particle % 16) / 64.0f);
    }

    @Override
    protected float getU1() {
        return ((particle % 16) / 64.0f + 0.015625f);
    }

    @Override
    protected float getV0() {
        return ((particle / 16) / 64.0f);
    }

    @Override
    protected float getV1() {
        return ((particle / 16) / 64.0f + 0.015625f);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TCSprites.translucent();
    }
}