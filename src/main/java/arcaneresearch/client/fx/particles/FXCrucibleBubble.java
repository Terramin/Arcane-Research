package arcaneresearch.client.fx.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.Mth;
import arcaneresearch.client.fx.particles.TCSprites;


public class FXCrucibleBubble extends SingleQuadParticle {

    private float wobblePhase;
    private float wobbleSpeed;

    public FXCrucibleBubble(ClientLevel level, double x, double y, double z,
                            float r, float g, float b) {
        super(level, x, y, z, 0, 0, 0, null);

        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
        this.alpha = 0.7f;

        this.quadSize = 0.05f + random.nextFloat() * 0.03f;

        this.xd = (random.nextFloat() - 0.5f) * 0.01;
        this.yd = 0.01 + random.nextFloat() * 0.015;
        this.zd = (random.nextFloat() - 0.5f) * 0.01;

        this.gravity = -0.001f;

        this.lifetime = 20 + random.nextInt(20);

        this.wobblePhase = random.nextFloat() * (float)Math.PI * 2;
        this.wobbleSpeed = 0.2f + random.nextFloat() * 0.1f;
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

        wobblePhase += wobbleSpeed;
        xd += Mth.sin(wobblePhase) * 0.001;
        zd += Mth.cos(wobblePhase * 0.7f) * 0.001;

        x += xd;
        y += yd;
        z += zd;

        xd *= 0.95;
        zd *= 0.95;

        if (age < lifetime * 0.3f) {
            quadSize *= 1.02f;
        } else if (age > lifetime * 0.8f) {
            quadSize *= 0.95f;
            alpha *= 0.9f;
        }
    }

    
    @Override
    public ParticleRenderType getGroup() {
        return ParticleRenderType.SINGLE_QUADS;
    }

    @Override
    protected float getU0() {
        return ((160 % 64) / 64.0f);
    }

    @Override
    protected float getU1() {
        return ((160 % 64) / 64.0f + 0.015625f);
    }

    @Override
    protected float getV0() {
        return ((160 / 64) / 64.0f);
    }

    @Override
    protected float getV1() {
        return ((160 / 64) / 64.0f + 0.015625f);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return TCSprites.additive();
    }
}