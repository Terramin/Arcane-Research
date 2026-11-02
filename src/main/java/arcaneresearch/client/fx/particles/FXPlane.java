package arcaneresearch.client.fx.particles;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class FXPlane extends TextureSheetParticle {
    private static final ResourceLocation PARTICLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("arcaneresearch", "textures/misc/particles.png");

    private static final float INV_32 = 1.0f / 32.0f;
    private static final float V0 = 10.0f / 32.0f;
    private static final float V1 = 11.0f / 32.0f;

    private final Quaternionf staticRotation = new Quaternionf();
    private final Matrix4f renderMatrix = new Matrix4f();

    public FXPlane(ClientLevel level, double x, double y, double z,
                   double targetX, double targetY, double targetZ, int lifetime) {
        super(level, x, y, z, 0, 0, 0);

        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;
        this.gravity = 0.0f;
        this.lifetime = lifetime;
        this.setSize(0.01f, 0.01f);
        this.quadSize = 1.0f;
        this.alpha = 0.0f;

        if (lifetime <= 0) {
            this.remove();
            return;
        }

        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;

        this.xd = dx / lifetime;
        this.yd = dy / lifetime;
        this.zd = dz / lifetime;

        double horizontalDist = Mth.sqrt((float) (dx * dx + dz * dz));

        float angleYaw = 0.0f;
        float anglePitch = 0.0f;

        if (horizontalDist >= 1.0E-7) {
            angleYaw = (float) (Mth.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
            anglePitch = (float) (-(Mth.atan2(dy, horizontalDist) * 180.0 / Math.PI));
        }

        float randomAngle = (float) (random.nextGaussian() * 20.0);

        staticRotation.rotateY((-angleYaw + 90.0f) * Mth.DEG_TO_RAD);
        staticRotation.rotateZ((anglePitch + 90.0f) * Mth.DEG_TO_RAD);
        staticRotation.rotateY(randomAngle * Mth.DEG_TO_RAD);
    }

    @Override
    public void tick() {
        this.xo = x;
        this.yo = y;
        this.zo = z;

        if (this.lifetime <= 0) {
            this.remove();
            return;
        }

        float threshold = lifetime / 5.0f;

        if (age <= threshold) {
            this.alpha = age / threshold;
        } else {
            this.alpha = (lifetime - age) / (float) lifetime;
        }

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        x += xd;
        y += yd;
        z += zd;
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTicks) {
        renderPlane(camera, partialTicks);
    }

    protected void renderPlane(Camera camera, float partialTicks) {
        if (this.lifetime <= 0) return;

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, PARTICLE_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();

        Vec3 camPos = camera.getPosition();

        float px = (float) (Mth.lerp(partialTicks, xo, x) - camPos.x());
        float py = (float) (Mth.lerp(partialTicks, yo, y) - camPos.y());
        float pz = (float) (Mth.lerp(partialTicks, zo, z) - camPos.z());

        int frame = 22 + Math.round((age + partialTicks) / lifetime * 8.0f);
        frame = Math.min(frame, 30);

        float u0 = frame * INV_32;
        float u1 = u0 + INV_32;

        float size = quadSize * (0.5f + (age + partialTicks) / lifetime);
        float renderAlpha = alpha / 2.0f;

        renderMatrix.identity();
        renderMatrix.translate(px, py, pz);
        renderMatrix.rotate(staticRotation);

        float halfSize = size * 0.5f;

        float cr = this.rCol;
        float cg = this.gCol;
        float cb = this.bCol;

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.vertex(renderMatrix, -halfSize, halfSize, 0).uv(u1, V1).color(cr, cg, cb, renderAlpha).endVertex();
        builder.vertex(renderMatrix, halfSize, halfSize, 0).uv(u1, V0).color(cr, cg, cb, renderAlpha).endVertex();
        builder.vertex(renderMatrix, halfSize, -halfSize, 0).uv(u0, V0).color(cr, cg, cb, renderAlpha).endVertex();
        builder.vertex(renderMatrix, -halfSize, -halfSize, 0).uv(u0, V1).color(cr, cg, cb, renderAlpha).endVertex();
        tesselator.end();

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public boolean shouldCull() {
        return false;
    }

    public void setColor(float r, float g, float b) {
        this.rCol = r;
        this.gCol = g;
        this.bCol = b;
    }

    public void setGravity(float value) {
        this.gravity = value;
    }
}