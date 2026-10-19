package arcaneresearch.client.fx.particles;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
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
import org.joml.Matrix4f;
@OnlyIn(Dist.CLIENT)
public class FXPlane extends TextureSheetParticle {
    private static final ResourceLocation PARTICLE_TEXTURE = new ResourceLocation("arcaneresearch", "textures/misc/particles.png");
    private float angle;
    private float angleYaw;
    private float anglePitch;
    private int particleTextureIndexX = 22;
    private int particleTextureIndexY = 10;
    public FXPlane(ClientLevel level, double x, double y, double z,
                   double targetX, double targetY, double targetZ, int lifetime) {
        super(level, x, y, z, 0, 0, 0);
        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;
        this.gravity = 0.0f;
        this.lifetime = lifetime;
        this.setSize(0.01f, 0.01f);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.quadSize = 1.0f;
        this.alpha = 0.0f;
        double dx = targetX - x;
        double dy = targetY - y;
        double dz = targetZ - z;
        this.xd = dx / lifetime;
        this.yd = dy / lifetime;
        this.zd = dz / lifetime;
        double horizontalDist = Mth.sqrt((float)(dx * dx + dz * dz));
        this.angleYaw = 0.0f;
        this.anglePitch = 0.0f;
        if (horizontalDist >= 1.0E-7) {
            this.angleYaw = (float)(Mth.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
            this.anglePitch = (float)(-(Mth.atan2(dy, horizontalDist) * 180.0 / Math.PI));
        }
        this.angle = (float)(random.nextGaussian() * 20.0);
    }
    @Override
    public void tick() {
        this.xo = x;
        this.yo = y;
        this.zo = z;
        float threshold = lifetime / 5.0f;
        if (age <= threshold) {
            alpha = age / threshold;
        } else {
            alpha = (lifetime - age) / (float)lifetime;
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
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        renderPlane(camera, partialTicks);
    }
    protected void renderPlane(Camera camera, float partialTicks) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, PARTICLE_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        Vec3 camPos = camera.getPosition();
        float px = (float)(Mth.lerp(partialTicks, xo, x) - camPos.x());
        float py = (float)(Mth.lerp(partialTicks, yo, y) - camPos.y());
        float pz = (float)(Mth.lerp(partialTicks, zo, z) - camPos.z());
        int frame = 22 + Math.round((age + partialTicks) / lifetime * 8.0f);
        frame = Math.min(frame, 30);
        float u0 = frame / 32.0f;
        float u1 = u0 + 0.03125f;
        float v0 = particleTextureIndexY / 32.0f;
        float v1 = v0 + 0.03125f;
        float size = quadSize * (0.5f + (age + partialTicks) / lifetime);
        float renderAlpha = alpha / 2.0f;
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(px, py, pz);
        poseStack.mulPose(Axis.YP.rotationDegrees(-angleYaw + 90.0f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(anglePitch + 90.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        Matrix4f matrix = poseStack.last().pose();
        float halfSize = size * 0.5f;
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.vertex(matrix, -halfSize, halfSize, 0)
                .uv(u1, v1).color(rCol, gCol, bCol, renderAlpha).endVertex();
        builder.vertex(matrix, halfSize, halfSize, 0)
                .uv(u1, v0).color(rCol, gCol, bCol, renderAlpha).endVertex();
        builder.vertex(matrix, halfSize, -halfSize, 0)
                .uv(u0, v0).color(rCol, gCol, bCol, renderAlpha).endVertex();
        builder.vertex(matrix, -halfSize, -halfSize, 0)
                .uv(u0, v1).color(rCol, gCol, bCol, renderAlpha).endVertex();
        tesselator.end();
        poseStack.popPose();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
    @Override
    public ParticleRenderType getRenderType() {
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
