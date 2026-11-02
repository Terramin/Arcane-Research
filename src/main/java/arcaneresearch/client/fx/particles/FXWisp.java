package arcaneresearch.client.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class FXWisp extends TextureSheetParticle {
    protected static final int GRID_SIZE = 64;

    private static final int FULLBRIGHT = 0xF000F0;
    private static final float UV_STEP = 0.015625f;
    private static final float SIZE_FACTOR = 0.5f;
    private static final float RENDER_ALPHA = 0.2f;
    private static final float MAX_DIST_SQ = 1024.0f;
    private static final float V_START = 0.046875f;
    private static final float INITIAL_QUAD_SIZE_FACTOR = 0.4f;

    private final Vector3f renderScratch = new Vector3f();

    protected Entity target;
    protected int blendMode = 1;
    protected int spriteIndexX = 0;
    protected int spriteIndexY = 0;

    public FXWisp(ClientLevel level, double x, double y, double z, Entity target) {
        super(level, x, y, z, 0, 0, 0);

        this.target = target;
        this.xd = this.random.nextGaussian() * 0.03;
        this.yd = -0.05;
        this.zd = this.random.nextGaussian() * 0.03;

        this.quadSize *= INITIAL_QUAD_SIZE_FACTOR;
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

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTicks) {
        Entity viewEntity = Minecraft.getInstance().getCameraEntity();
        if (viewEntity == null) return;

        float ageScale = 1.0f - (float) this.age / (float) this.lifetime;
        double dist = viewEntity.distanceToSqr(this.x, this.y, this.z);
        float distFade = (float) (1.0 - Math.min(MAX_DIST_SQ, dist) / MAX_DIST_SQ);
        float finalAlpha = RENDER_ALPHA * ageScale * distFade;

        if (finalAlpha <= 0) return;

        Vec3 cameraPos = camera.getPosition();

        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        float size = SIZE_FACTOR * this.quadSize;

        int frame = this.age % 13;
        float u0 = frame / 64.0f;
        float u1 = u0 + UV_STEP;
        float v0 = V_START;
        float v1 = v0 + UV_STEP;

        Quaternionf quaternion = camera.rotation();

        float cr = this.rCol;
        float cg = this.gCol;
        float cb = this.bCol;

        Vector3f v = renderScratch;

        v.set(-1.0F, -1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u1, v1).color(cr, cg, cb, finalAlpha).uv2(FULLBRIGHT).endVertex();

        v.set(-1.0F, 1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u1, v0).color(cr, cg, cb, finalAlpha).uv2(FULLBRIGHT).endVertex();

        v.set(1.0F, 1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u0, v0).color(cr, cg, cb, finalAlpha).uv2(FULLBRIGHT).endVertex();

        v.set(1.0F, -1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u0, v1).color(cr, cg, cb, finalAlpha).uv2(FULLBRIGHT).endVertex();
    }

    @Override
    public int getLightColor(float partialTick) {
        return FULLBRIGHT;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return blendMode == 1 ? FXGeneric.TC_ADDITIVE : FXGeneric.TC_NORMAL;
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