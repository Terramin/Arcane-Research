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
public class FXSlimyBubble extends TextureSheetParticle {
    private static final float UV_STEP = 0.015625f;

    private final Vector3f renderScratch = new Vector3f();

    private int particle = 144;

    public FXSlimyBubble(ClientLevel level, double x, double y, double z, float scale) {
        super(level, x, y, z, 0, 0, 0);

        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;
        this.gravity = 0.0f;
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

        if (age < 7) {
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
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 camPos = camera.getPosition();

        float px = (float) (Mth.lerp(partialTicks, xo, x) - camPos.x());
        float py = (float) (Mth.lerp(partialTicks, yo, y) - camPos.y());
        float pz = (float) (Mth.lerp(partialTicks, zo, z) - camPos.z());

        Quaternionf rotation = camera.rotation();

        int p = this.particle;
        float u0 = (p % 16) / 64.0f;
        float u1 = u0 + UV_STEP;
        float v0 = (p / 16) / 64.0f;
        float v1 = v0 + UV_STEP;

        int light = getLightColor(partialTicks);

        float size = this.quadSize;
        float cr = this.rCol;
        float cg = this.gCol;
        float cb = this.bCol;
        float ca = this.alpha;

        Vector3f v = renderScratch;

        v.set(-1.0f, -1.0f, 0.0f).rotate(rotation).mul(size).add(px, py, pz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u1, v1).color(cr, cg, cb, ca).uv2(light).endVertex();

        v.set(-1.0f, 1.0f, 0.0f).rotate(rotation).mul(size).add(px, py, pz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u1, v0).color(cr, cg, cb, ca).uv2(light).endVertex();

        v.set(1.0f, 1.0f, 0.0f).rotate(rotation).mul(size).add(px, py, pz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u0, v0).color(cr, cg, cb, ca).uv2(light).endVertex();

        v.set(1.0f, -1.0f, 0.0f).rotate(rotation).mul(size).add(px, py, pz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(u0, v1).color(cr, cg, cb, ca).uv2(light).endVertex();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return FXGeneric.TC_NORMAL;
    }
}