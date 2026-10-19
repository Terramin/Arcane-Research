package arcaneresearch.client.fx.particles;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import org.joml.Quaternionf;

import arcaneresearch.Arcaneresearch;

// NEOFORGE_PORT: 1:1 logic from Forge 1.20.1 (fields/animation/setters kept).
// REQUIRED changes (verified via javap on MC 26.3 + NeoForge docs):
//   TextureSheetParticle removed -> SingleQuadParticle.
//   Custom TC_* ParticleRenderTypes removed (record, fixed set) -> getLayer() with
//     Layer.bySprite (additive now real via TCSprites.ADDITIVE_PARTICLE;
//     no-depth variants still fall back to translucent, TODO visual).
//   Grid UVs (spriteIndex/gridSize over the raw sheet) remapped into the stitched sheet
//     sprite bounds (TCSprites); flipped/angled behavior preserved.
//   level.getDayTime removed -> getGameTime (same wind-direction variety).
public class FXGeneric extends SingleQuadParticle {

    protected boolean doneFrames = false;
    protected boolean flipped = false;

    protected double windX = 0;
    protected double windZ = 0;

    protected int layer = 0;

    protected float startR, startG, startB;
    protected float endR, endG, endB;

    protected boolean loop = false;
    protected int startParticle = 0;
    protected int numParticles = 1;
    protected int particleInc = 1;
    protected int[] finalFrames = null;

    protected float[] scaleKeys = new float[]{1.0f};
    protected float[] scaleFrames = new float[]{0.0f};

    protected float[] alphaKeys = new float[]{1.0f};
    protected float[] alphaFrames = new float[]{0.0f};

    protected double slowDown = 0.98;
    protected float randomX = 0, randomY = 0, randomZ = 0;

    protected float rotationSpeed = 0.0f;

    protected boolean angled = false;
    protected float angleYaw = 0;
    protected float anglePitch = 0;

    protected int gridSize = 64;

    protected int spriteIndexX = 0;
    protected int spriteIndexY = 0;

    protected net.minecraft.client.particle.SpriteSet sprites;

    public static final Identifier TC_TEXTURE =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/particles.png");

    // NEOFORGE_PORT: custom ParticleRenderTypes removed (fixed set in 26.x);
    // same layer dispatch, now returning the sheet Layer (see TCSprites).
    public static SingleQuadParticle.Layer renderTypeForLayer(int layer) {
        return TCSprites.layerFor(layer);
    }

    public FXGeneric(ClientLevel level, double x, double y, double z) {
        // NEOFORGE_PORT: null sprite (UVs come from grid-index getU/V overrides + sheet Layer).
        super(level, x, y, z, null);
        init();
    }

    public FXGeneric(ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
        super(level, x, y, z, vx, vy, vz, null);
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        init();
    }

    private void init() {
        this.setSize(0.1f, 0.1f);
        this.startR = this.rCol;
        this.startG = this.gCol;
        this.startB = this.bCol;
        this.endR = this.rCol;
        this.endG = this.gCol;
        this.endB = this.bCol;
    }

    protected void calculateFrames() {
        doneFrames = true;

        if (alphaKeys == null) {
            alphaKeys = new float[]{1.0f};
        }
        alphaFrames = new float[this.lifetime + 1];
        float inc = (alphaKeys.length - 1) / (float) this.lifetime;
        float is = 0.0f;
        for (int a = 0; a <= this.lifetime; ++a) {
            int isF = Mth.floor(is);
            float diff = (isF < alphaKeys.length - 1) ? (alphaKeys[isF + 1] - alphaKeys[isF]) : 0.0f;
            float pa = is - isF;
            alphaFrames[a] = alphaKeys[isF] + diff * pa;
            is += inc;
        }

        if (scaleKeys == null) {
            scaleKeys = new float[]{1.0f};
        }
        scaleFrames = new float[this.lifetime + 1];
        inc = (scaleKeys.length - 1) / (float) this.lifetime;
        is = 0.0f;
        for (int a = 0; a <= this.lifetime; ++a) {
            int isF = Mth.floor(is);
            float diff = (isF < scaleKeys.length - 1) ? (scaleKeys[isF + 1] - scaleKeys[isF]) : 0.0f;
            float pa = is - isF;
            scaleFrames[a] = scaleKeys[isF] + diff * pa;
            is += inc;
        }
    }

    @Override
    public void tick() {
        if (!doneFrames) {
            calculateFrames();
        }

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.oRoll = this.roll;
        this.roll += 3.1415927f * this.rotationSpeed * 2.0f;

        this.yd -= 0.04 * this.gravity;

        this.move(this.xd, this.yd, this.zd);

        this.xd *= this.slowDown;
        this.yd *= this.slowDown;
        this.zd *= this.slowDown;

        if (randomX != 0 || randomY != 0 || randomZ != 0) {
            this.xd += this.random.nextGaussian() * randomX;
            this.yd += this.random.nextGaussian() * randomY;
            this.zd += this.random.nextGaussian() * randomZ;
        }

        this.xd += this.windX;
        this.zd += this.windZ;

        if (this.onGround && slowDown != 1.0) {
            this.xd *= 0.7;
            this.zd *= 0.7;
        }

        if (alphaFrames != null && alphaFrames.length > 0) {
            this.alpha = alphaFrames[Math.min(this.age, alphaFrames.length - 1)];
        }

        if (scaleFrames != null && scaleFrames.length > 0) {
            this.quadSize = scaleFrames[Math.min(this.age, scaleFrames.length - 1)];
        }

        float progress = (float) this.age / (float) this.lifetime;
        this.rCol = Mth.lerp(progress, this.startR, this.endR);
        this.gCol = Mth.lerp(progress, this.startG, this.endG);
        this.bCol = Mth.lerp(progress, this.startB, this.endB);

        updateSpriteIndex();
    }

    protected void updateSpriteIndex() {
        int index;
        if (loop) {
            index = startParticle + (this.age / particleInc) % numParticles;
        } else {
            float fs = this.age / (float) this.lifetime;
            index = (int) (startParticle + Math.min(numParticles * fs, numParticles - 1));
        }

        if (finalFrames != null && finalFrames.length > 0 && this.age > this.lifetime - finalFrames.length) {
            int frame = this.lifetime - this.age;
            if (frame < 0) frame = 0;
            if (frame < finalFrames.length) {
                index = finalFrames[frame];
            }
        }

        setParticleTextureIndex(index);
    }

    public void setParticleTextureIndex(int index) {
        if (index < 0) index = 0;
        this.spriteIndexX = index % gridSize;
        this.spriteIndexY = index / gridSize;
    }

    // NEOFORGE_PORT: grid fractions remapped into the stitched sheet sprite (same art).
    @Override
    protected float getU0() {
        float f = (float) spriteIndexX / (float) gridSize;
        if (flipped) {
            f = ((float) spriteIndexX + 1.0f) / (float) gridSize;
        }
        return (f);
    }

    @Override
    protected float getU1() {
        float f = ((float) spriteIndexX + 1.0f) / (float) gridSize;
        if (flipped) {
            f = (float) spriteIndexX / (float) gridSize;
        }
        return (f);
    }

    @Override
    protected float getV0() {
        return ((float) spriteIndexY / (float) gridSize);
    }

    @Override
    protected float getV1() {
        return (((float) spriteIndexY + 1.0f) / (float) gridSize);
    }

    // NEOFORGE_PORT: angled orientation (fixed yaw/pitch) preserved via custom quaternion;
    // base extract() handles the camera-facing case.
    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTicks) {
        if (!angled) {
            super.extract(state, camera, partialTicks);
            return;
        }
        Quaternionf quaternion = new Quaternionf();
        quaternion.rotateY((float) Math.toRadians(-angleYaw + 90.0f));
        quaternion.rotateX((float) Math.toRadians(anglePitch + 90.0f));
        if (this.roll != 0.0f) {
            float rollAngle = Mth.lerp(partialTicks, this.oRoll, this.roll);
            quaternion.rotateZ(rollAngle);
        }
        extractRotatedQuad(state, camera, quaternion, partialTicks);
    }

    @Override
    public ParticleRenderType getGroup() {
        return ParticleRenderType.SINGLE_QUADS;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        // NEOFORGE_PORT: TC sheet types collapsed (see ArcaneresearchParticle); kept per-layer
        // for the visual-verify pass.
        return TCSprites.layerFor(layer);
    }

    // NEOFORGE_PORT: Forge FXGeneric had NO getLightColor override (vanilla world
    // light always, even on layer 1) -> no override here either.

    @Override
    public void setColor(float r, float g, float b) {

        if (r > 1.0f) r /= 255.0f;
        if (g > 1.0f) g /= 255.0f;
        if (b > 1.0f) b /= 255.0f;

        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
        this.startR = r;
        this.startG = g;
        this.startB = b;
        this.endR = r;
        this.endG = g;
        this.endB = b;
    }

    public void setColorRange(float r1, float g1, float b1, float r2, float g2, float b2) {

        if (r1 > 1.0f) r1 /= 255.0f;
        if (g1 > 1.0f) g1 /= 255.0f;
        if (b1 > 1.0f) b1 /= 255.0f;
        if (r2 > 1.0f) r2 /= 255.0f;
        if (g2 > 1.0f) g2 /= 255.0f;
        if (b2 > 1.0f) b2 /= 255.0f;

        this.rCol = r1;
        this.gCol = g1;
        this.bCol = b1;
        this.startR = r1;
        this.startG = g1;
        this.startB = b1;
        this.endR = r2;
        this.endG = g2;
        this.endB = b2;
    }

    public void setAlphaF(float alpha) {
        this.alpha = alpha;
        this.alphaKeys = new float[]{alpha};
    }

    public void setAlphaKeyframes(float... alphaKeyframes) {
        this.alpha = alphaKeyframes[0];
        this.alphaKeys = alphaKeyframes;
    }

    public void setScale(float scale) {
        this.quadSize = scale;
        this.scaleKeys = new float[]{scale};
    }

    public void setScaleKeyframes(float... scaleKeyframes) {
        this.quadSize = scaleKeyframes[0];
        this.scaleKeys = scaleKeyframes;
    }

    public void setMaxAge(int maxAge) {
        this.lifetime = maxAge;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void setParticles(int startParticle, int numParticles, int particleInc) {
        this.startParticle = startParticle;
        this.numParticles = numParticles;
        this.particleInc = particleInc;
        setParticleTextureIndex(startParticle);
    }

    public void setParticle(int particleIndex) {
        this.startParticle = particleIndex;
        this.numParticles = 1;
        this.particleInc = 1;
        setParticleTextureIndex(particleIndex);
    }

    public void setRotationSpeed(float speed) {
        this.rotationSpeed = (float) (speed * 0.017453292519943);
    }

    public void setRotationSpeedWithStart(float startAngle, float speed) {
        this.roll = (float) (startAngle * Math.PI * 2.0);
        this.rotationSpeed = (float) (speed * 0.017453292519943);
    }

    public void setSlowDown(double slowDown) {
        this.slowDown = slowDown;
    }

    public void setRandomMovementScale(float x, float y, float z) {
        this.randomX = x;
        this.randomY = y;
        this.randomZ = z;
    }

    public void setWindStrength(double windStrength) {

        int m = (int) ((this.level.getLevelData().getGameTime() / 24000L) % 8);
        double angle = m * (40 + this.random.nextInt(10)) / 180.0f * Math.PI;
        this.windX = Math.cos(angle) * windStrength;
        this.windZ = Math.sin(angle) * windStrength;
    }

    public void setWind(double windX, double windZ) {
        this.windX = windX;
        this.windZ = windZ;
    }

    public void setFinalFrames(int... frames) {
        this.finalFrames = frames;
    }

    public void setAngles(float yaw, float pitch) {
        this.angleYaw = yaw;
        this.anglePitch = pitch;
        this.angled = true;
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
    }

    public void setGridSize(int size) {
        this.gridSize = size;
    }

    public void setNoClip(boolean noClip) {
        this.hasPhysics = !noClip;
    }

    public void setSprites(net.minecraft.client.particle.SpriteSet sprites) {
        this.sprites = sprites;
    }

    public boolean isFlipped() {
        return flipped;
    }
}