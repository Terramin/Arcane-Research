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
public class FXCrucibleBubble extends TextureSheetParticle {
    private static final int SPRITE = 160;
    private static final float UV_STEP = 0.015625f;

    private static final float U0 = (SPRITE % 64) / 64.0f;
    private static final float U1 = U0 + UV_STEP;
    private static final float V0 = (SPRITE / 64) / 64.0f;
    private static final float V1 = V0 + UV_STEP;

    private static final float TWO_PI = (float) (Math.PI * 2.0);
    private static final float WOBBLE_FORCE = 0.001f;
    private static final float WOBBLE_Z_FACTOR = 0.7f;

    private final Vector3f renderScratch = new Vector3f();

    private float wobblePhase;
    private float wobbleSpeed;

    public FXCrucibleBubble(ClientLevel level, double x, double y, double z,
                            float r, float g, float b) {
        super(level, x, y, z, 0, 0, 0);

        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
        this.alpha = 0.7f;
        this.quadSize = 0.05f + random.nextFloat() * 0.03f;

        this.xd = (random.nextFloat() - 0.5f) * 0.01;
        this.yd = 0.01 + random.nextFloat() * 0.015;
        this.zd = (random.nextFloat() - 0.5f) * 0.01;

        this.lifetime = 20 + random.nextInt(20);

        this.wobblePhase = random.nextFloat() * TWO_PI;
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

        xd += Mth.sin(wobblePhase) * WOBBLE_FORCE;
        zd += Mth.cos(wobblePhase * WOBBLE_Z_FACTOR) * WOBBLE_FORCE;

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
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        Vec3 camPos = camera.getPosition();

        float px = (float) (Mth.lerp(partialTicks, xo, x) - camPos.x());
        float py = (float) (Mth.lerp(partialTicks, yo, y) - camPos.y());
        float pz = (float) (Mth.lerp(partialTicks, zo, z) - camPos.z());

        Quaternionf rotation = camera.rotation();

        float cr = this.rCol;
        float cg = this.gCol;
        float cb = this.bCol;
        float ca = this.alpha;
        float size = this.quadSize;

        int light = getLightColor(partialTicks);

        Vector3f v = renderScratch;

        v.set(-1.0f, -1.0f, 0.0f).rotate(rotation).mul(size).add(px, py, pz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(U1, V1).color(cr, cg, cb, ca).uv2(light).endVertex();

        v.set(-1.0f, 1.0f, 0.0f).rotate(rotation).mul(size).add(px, py, pz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(U1, V0).color(cr, cg, cb, ca).uv2(light).endVertex();

        v.set(1.0f, 1.0f, 0.0f).rotate(rotation).mul(size).add(px, py, pz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(U0, V0).color(cr, cg, cb, ca).uv2(light).endVertex();

        v.set(1.0f, -1.0f, 0.0f).rotate(rotation).mul(size).add(px, py, pz);
        buffer.vertex(v.x(), v.y(), v.z()).uv(U0, V1).color(cr, cg, cb, ca).uv2(light).endVertex();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return FXGeneric.TC_ADDITIVE;
    }
}