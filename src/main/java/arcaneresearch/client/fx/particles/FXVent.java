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
public class FXVent extends TextureSheetParticle {
    protected static final int GRID_SIZE = 64;

    private static final int FULLBRIGHT = 0xF000F0;
    private static final float UV_STEP = 0.015625f;
    private static final float SIZE_FACTOR = 0.3f;
    private static final double WANDER_SCALE = 0.0075;

    protected float targetScale;

    private final Vector3f renderScratch = new Vector3f();

    public FXVent(ClientLevel level, double x, double y, double z,
                  double vx, double vy, double vz, int color) {
        super(level, x, y, z, vx, vy, vz);

        this.targetScale = 1.0f;
        this.setSize(0.02f, 0.02f);
        this.quadSize = this.random.nextFloat() * 0.1f + 0.05f;

        this.rCol = ((color >> 16) & 0xFF) / 255.0f;
        this.gCol = ((color >> 8) & 0xFF) / 255.0f;
        this.bCol = (color & 0xFF) / 255.0f;

        setHeading(vx, vy, vz, 0.125f, 5.0f);

        Minecraft mc = Minecraft.getInstance();
        Entity viewEntity = mc.getCameraEntity();
        int visibleDistance = mc.options.graphicsMode().get().getId() > 0 ? 50 : 25;

        if (viewEntity != null && viewEntity.distanceToSqr(x, y, z) > (double) visibleDistance * visibleDistance) {
            this.lifetime = 0;
        }

        this.alpha = 1.0f;
    }

    public void setHeading(double vx, double vy, double vz, float speed, float spread) {
        double length = Math.sqrt(vx * vx + vy * vy + vz * vz);

        if (length > 0) {
            double inv = 1.0 / length;
            vx *= inv;
            vy *= inv;
            vz *= inv;
        }

        double wander = WANDER_SCALE * spread;

        vx += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * wander;
        vy += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * wander;
        vz += this.random.nextGaussian() * (this.random.nextBoolean() ? -1 : 1) * wander;

        this.xd = vx * speed;
        this.yd = vy * speed;
        this.zd = vz * speed;
    }

    public void setScale(float scale) {
        this.quadSize *= scale;
        this.targetScale *= scale;
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

        if (this.quadSize >= this.targetScale) {
            this.remove();
            return;
        }

        this.yd += 0.0025;

        this.move(this.xd, this.yd, this.zd);

        this.xd *= 0.85;
        this.yd *= 0.85;
        this.zd *= 0.85;

        if (this.quadSize < this.targetScale) {
            this.quadSize *= 1.15f;
        }

        if (this.quadSize > this.targetScale) {
            this.quadSize = this.targetScale;
        }

        if (this.onGround) {
            this.xd *= 0.7;
            this.zd *= 0.7;
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        float invTarget = this.targetScale > 0.0f ? 1.0f / this.targetScale : 0.0f;

        Vec3 cameraPos = camera.getPosition();

        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        int spriteIndex = (int) (1.0f + this.quadSize * invTarget * 4.0f);

        float u0 = (spriteIndex % 16) / 64.0f;
        float u1 = u0 + UV_STEP;
        float v0 = (spriteIndex / 16) / 64.0f;
        float v1 = v0 + UV_STEP;

        float size = SIZE_FACTOR * this.quadSize;
        float displayAlpha = this.alpha * ((this.targetScale - this.quadSize) * invTarget);
        displayAlpha = Mth.clamp(displayAlpha, 0.0f, 1.0f);

        int light = this.getLightColor(partialTicks);

        Quaternionf quaternion = camera.rotation();

        float cr = this.rCol;
        float cg = this.gCol;
        float cb = this.bCol;

        Vector3f v = renderScratch;

        v.set(-1.0F, -1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u1, v1).color(cr, cg, cb, displayAlpha).uv2(light).endVertex();

        v.set(-1.0F, 1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u1, v0).color(cr, cg, cb, displayAlpha).uv2(light).endVertex();

        v.set(1.0F, 1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u0, v0).color(cr, cg, cb, displayAlpha).uv2(light).endVertex();

        v.set(1.0F, -1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u0, v1).color(cr, cg, cb, displayAlpha).uv2(light).endVertex();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return FXGeneric.TC_NORMAL;
    }

    public void setRGB(float r, float g, float b) {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
    }
}