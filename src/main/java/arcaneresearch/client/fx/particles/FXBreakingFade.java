package arcaneresearch.client.fx.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class FXBreakingFade extends ArcaneresearchParticle {
    private static final float BOOM_BOOST = 0.964f;
    private static final float MIN_LEN = 1.0e-6f;

    private final ItemStack itemStack;
    private final Vector3f renderScratch = new Vector3f();

    public FXBreakingFade(ClientLevel level, double x, double y, double z, Item item) {
        this(level, x, y, z, item, 0);
    }

    public FXBreakingFade(ClientLevel level, double x, double y, double z, Item item, int meta) {
        this(level, x, y, z, 0, 0, 0, item, meta);
    }

    public FXBreakingFade(ClientLevel level, double x, double y, double z,
                          double vx, double vy, double vz, Item item, int meta) {
        super(level, x, y, z, vx, vy, vz);

        if (item == null) {
            this.itemStack = ItemStack.EMPTY;
            this.remove();
            return;
        }

        this.itemStack = new ItemStack(item);

        try {
            this.setSprite(Minecraft.getInstance().getItemRenderer()
                    .getModel(itemStack, level, null, 0)
                    .getParticleIcon());
        } catch (Exception e) {
            this.remove();
            return;
        }

        this.gravity = 1.0f;
        this.quadSize = 0.1f;
        this.lifetime = 20;
        this.alpha = 1.0f;

        this.xd = vx + (this.random.nextFloat() - 0.5f) * 0.2f;
        this.yd = vy + this.random.nextFloat() * 0.2f;
        this.zd = vz + (this.random.nextFloat() - 0.5f) * 0.2f;
    }

    public FXBreakingFade setParticleMaxAge(int age) {
        this.lifetime = age;
        return this;
    }

    public FXBreakingFade setParticleGravity(float g) {
        this.gravity = g;
        return this;
    }

    public FXBreakingFade setSpeed(double vx, double vy, double vz) {
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        return this;
    }

    public void boom() {
        float f = (this.random.nextFloat() + this.random.nextFloat() + 1.0f) * 0.15f;

        float len = Mth.sqrt((float) (this.xd * this.xd + this.yd * this.yd + this.zd * this.zd));

        if (len <= MIN_LEN) return;

        float scale = f * BOOM_BOOST;

        this.xd = this.xd / len * scale;
        this.yd = this.yd / len * scale + 0.1f;
        this.zd = this.zd / len * scale;
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

        this.yd -= 0.04 * this.gravity;
        this.move(this.xd, this.yd, this.zd);

        this.xd *= 0.98;
        this.yd *= 0.98;
        this.zd *= 0.98;

        if (this.onGround) {
            this.xd *= 0.7;
            this.zd *= 0.7;
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        float fade = this.lifetime > 0
                ? 1.0f - (float) this.age / (float) this.lifetime
                : 0.0f;

        Vec3 cameraPos = camera.getPosition();

        float x = (float) (Mth.lerp(partialTicks, this.xo, this.x) - cameraPos.x());
        float y = (float) (Mth.lerp(partialTicks, this.yo, this.y) - cameraPos.y());
        float z = (float) (Mth.lerp(partialTicks, this.zo, this.z) - cameraPos.z());

        Quaternionf quaternion = camera.rotation();

        float size = this.quadSize;

        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();

        int light = this.getLightColor(partialTicks);

        float cr = this.rCol;
        float cg = this.gCol;
        float cb = this.bCol;
        float renderAlpha = this.alpha * fade;

        Vector3f v = renderScratch;

        v.set(-1.0F, -1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u1, v1).color(cr, cg, cb, renderAlpha).uv2(light).endVertex();

        v.set(-1.0F, 1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u1, v0).color(cr, cg, cb, renderAlpha).uv2(light).endVertex();

        v.set(1.0F, 1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u0, v0).color(cr, cg, cb, renderAlpha).uv2(light).endVertex();

        v.set(1.0F, -1.0F, 0.0F).rotate(quaternion).mul(size).add(x, y, z);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u0, v1).color(cr, cg, cb, renderAlpha).uv2(light).endVertex();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }
}