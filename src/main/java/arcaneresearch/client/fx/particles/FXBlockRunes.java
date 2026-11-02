package arcaneresearch.client.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class FXBlockRunes extends TextureSheetParticle {
    protected static final int GRID_SIZE = 64;

    private static final int FULLBRIGHT = 0xF000F0;
    private static final float HALF_PI = (float) (Math.PI / 2.0);
    private static final float UV_STEP = 1.0f / 64.0f;
    private static final float V_START = 0.09375f;
    private static final float RUNE_COLUMNS = 16.0f;

    protected double offsetX;
    protected double offsetY;
    protected float rotation;
    protected int runeIndex;

    private final Quaternionf scratchQuat = new Quaternionf();
    private final Vector3f scratchVec = new Vector3f();

    public FXBlockRunes(ClientLevel level, double x, double y, double z,
                        float r, float g, float b, int duration) {
        super(level, x, y, z, 0, 0, 0);

        if (r == 0.0f) r = 1.0f;

        this.rCol = r;
        this.gCol = g;
        this.bCol = b;

        this.gravity = 0.0f;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;

        this.lifetime = 3 * duration;
        this.setSize(0.01f, 0.01f);

        this.rotation = this.random.nextInt(4) * 90.0f;
        this.runeIndex = 224 + this.random.nextInt(16);

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

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 cameraPos = camera.getPosition();

        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        float u0 = (this.runeIndex % 16) / 64.0f;
        float u1 = u0 + UV_STEP;
        float v0 = V_START;
        float v1 = v0 + UV_STEP;

        float size = 0.3f * this.quadSize;
        float half = 0.5f * size;

        float displayAlpha = this.alpha / 2.0f;

        float cr = this.rCol;
        float cg = this.gCol;
        float cb = this.bCol;

        scratchQuat.identity();
        scratchQuat.rotateY(this.rotation * Mth.DEG_TO_RAD);
        scratchQuat.rotateZ(HALF_PI);

        float cx = x + (float) this.offsetX;
        float cy = y + (float) this.offsetY;
        float cz = z - 0.51f;

        Vector3f v = scratchVec;

        v.set(-half, half, 0.0f).rotate(scratchQuat).add(cx, cy, cz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u1, v1).color(cr, cg, cb, displayAlpha).uv2(FULLBRIGHT).endVertex();

        v.set(half, half, 0.0f).rotate(scratchQuat).add(cx, cy, cz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u1, v0).color(cr, cg, cb, displayAlpha).uv2(FULLBRIGHT).endVertex();

        v.set(half, -half, 0.0f).rotate(scratchQuat).add(cx, cy, cz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u0, v0).color(cr, cg, cb, displayAlpha).uv2(FULLBRIGHT).endVertex();

        v.set(-half, -half, 0.0f).rotate(scratchQuat).add(cx, cy, cz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u0, v1).color(cr, cg, cb, displayAlpha).uv2(FULLBRIGHT).endVertex();
    }

    @Override
    public int getLightColor(float partialTick) {
        return FULLBRIGHT;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return FXGeneric.TC_NORMAL;
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