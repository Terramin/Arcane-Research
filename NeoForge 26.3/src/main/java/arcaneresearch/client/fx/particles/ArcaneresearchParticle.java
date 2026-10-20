package arcaneresearch.client.fx.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import arcaneresearch.client.fx.BeamEngine;
import net.minecraft.client.Camera;

// NEOFORGE_PORT: 1:1 from Forge 1.20.1 (fields/tick/setters kept).
// REQUIRED changes (verified via javap on MC 26.3):
//   TextureSheetParticle removed -> SingleQuadParticle (keeps rCol/gCol/bCol/alpha/roll/
//   oRoll/quadSize/sprite + tick fields xo/x/y/z/xd/move/onGround/age/lifetime/remove).
//   Custom render(VertexConsumer,Camera,float) removed -> base extract() submits the quad
//   (same camera-facing math lives in vanilla now).
//   getRenderType() -> getGroup() (SINGLE_QUADS; the old sheet types are gone, see FXGeneric).
//   Fullbright (old PARTICLE_SHEET_LIT via layer==1) -> getLightCoords override below.
public class ArcaneresearchParticle extends SingleQuadParticle {

    protected float startR, startG, startB;
    protected float endR, endG, endB;

    protected float startScale;
    protected float endScale;

    protected float rotationSpeed;
    protected float rotation;

    protected double slowDown = 0.98;
    protected float windX, windZ;
    protected boolean noClip = false;

    protected int spriteStart = 0;
    protected int spriteCount = 1;
    protected int spriteIncrement = 1;
    protected boolean spriteLoop = false;
    protected int gridSize = 64;

    protected int layer = 0;

    public ArcaneresearchParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z, null);
        this.startR = this.rCol;
        this.startG = this.gCol;
        this.startB = this.bCol;
        this.endR = this.rCol;
        this.endG = this.gCol;
        this.endB = this.bCol;
        this.startScale = this.quadSize;
        this.endScale = this.quadSize;
    }

    public ArcaneresearchParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
        super(level, x, y, z, vx, vy, vz, null);
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.startR = this.rCol;
        this.startG = this.gCol;
        this.startB = this.bCol;
        this.endR = this.rCol;
        this.endG = this.gCol;
        this.endB = this.bCol;
        this.startScale = this.quadSize;
        this.endScale = this.quadSize;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.oRoll = this.roll;
        this.roll += this.rotationSpeed;

        this.yd -= 0.04 * this.gravity;

        if (!noClip) {
            this.move(this.xd, this.yd, this.zd);
        } else {
            this.x += this.xd;
            this.y += this.yd;
            this.z += this.zd;
        }

        this.xd *= this.slowDown;
        this.yd *= this.slowDown;
        this.zd *= this.slowDown;

        this.xd += this.windX;
        this.zd += this.windZ;

        if (this.onGround && slowDown != 1.0) {
            this.xd *= 0.7;
            this.zd *= 0.7;
        }

        float progress = (float) this.age / (float) this.lifetime;
        this.rCol = Mth.lerp(progress, this.startR, this.endR);
        this.gCol = Mth.lerp(progress, this.startG, this.endG);
        this.bCol = Mth.lerp(progress, this.startB, this.endB);

        this.quadSize = Mth.lerp(progress, this.startScale, this.endScale);
    }

    // NEOFORGE_PORT: quad submission moved to SingleQuadParticle.extract (same math in vanilla).
    // Fullbright for any non-zero layer (Forge base: layer == 0 -> TRANSLUCENT
    // world-lit, else PARTICLE_SHEET_LIT).
    @Override
    protected int getLightCoords(float partialTicks) {
        if (layer != 0) {
            return 0xF000F0;
        }
        return super.getLightCoords(partialTicks);
    }

    @Override
    public ParticleRenderType getGroup() {
        return ParticleRenderType.SINGLE_QUADS;
    }

    // NEOFORGE_PORT: fallback full-sheet UVs (concrete subclasses override per-cell);
    // never NPEs on the unused null sprite.
    @Override
    protected float getU0() {
        return 0.0f;
    }

    @Override
    protected float getU1() {
        return 1.0f;
    }

    @Override
    protected float getV0() {
        return 0.0f;
    }

    @Override
    protected float getV1() {
        return 1.0f;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        // NEOFORGE_PORT: per-layer dispatch restored (was collapsed to translucent:
        // every glow particle rendered alpha-blended with a dark fringe).
        // layerFor maps 0/4 -> additive, 2/3 -> depth-tested fallbacks, else normal.
        return TCSprites.layerFor(layer);
    }

    public ArcaneresearchParticle setTCColor(float r, float g, float b) {
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

    public ArcaneresearchParticle setColor(float r1, float g1, float b1, float r2, float g2, float b2) {
        this.rCol = r1;
        this.gCol = g1;
        this.bCol = b1;
        this.startR = r1;
        this.startG = g1;
        this.startB = b1;
        this.endR = r2;
        this.endG = g2;
        this.endB = b2;
        return this;
    }

    public ArcaneresearchParticle setTCAlpha(float alpha) {
        this.alpha = alpha;
        return this;
    }

    public ArcaneresearchParticle setTCAlphaRange(float startAlpha, float endAlpha) {
        this.alpha = startAlpha;

        return this;
    }

    public ArcaneresearchParticle setTCScale(float scale) {
        this.quadSize = scale;
        this.startScale = scale;
        this.endScale = scale;
        return this;
    }

    public ArcaneresearchParticle setTCScaleRange(float startScale, float endScale) {
        this.quadSize = startScale;
        this.startScale = startScale;
        this.endScale = endScale;
        return this;
    }

    public ArcaneresearchParticle setTCLifetime(int lifetime) {
        this.lifetime = lifetime;
        return this;
    }

    public ArcaneresearchParticle setTCGravity(float gravity) {
        this.gravity = gravity;
        return this;
    }

    public ArcaneresearchParticle setTCRotationSpeed(float speed) {
        this.rotationSpeed = speed;
        return this;
    }

    public ArcaneresearchParticle setTCSlowDown(double slowDown) {
        this.slowDown = slowDown;
        return this;
    }

    public ArcaneresearchParticle setTCNoClip(boolean noClip) {
        this.noClip = noClip;
        return this;
    }

    public ArcaneresearchParticle setTCLayer(int layer) {
        this.layer = layer;
        return this;
    }

    public ArcaneresearchParticle setTCWind(float windX, float windZ) {
        this.windX = windX;
        this.windZ = windZ;
        return this;
    }

    /** Camera-facing quad corners for BeamEngine-style custom emission (old render math). */
    protected Vector3f[] quadCorners(float partialTicks, Quaternionf quaternion, float size, Vec3 cameraPos) {
        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());
        Vector3f[] vertices = new Vector3f[]{
                new Vector3f(-1.0F, -1.0F, 0.0F),
                new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, -1.0F, 0.0F)
        };
        for (Vector3f vertex : vertices) {
            vertex.rotate(quaternion);
            vertex.mul(size);
            vertex.add(x, y, z);
        }
        return vertices;
    }
}