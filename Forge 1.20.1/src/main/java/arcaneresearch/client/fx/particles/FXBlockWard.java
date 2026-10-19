package arcaneresearch.client.fx.particles;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
@OnlyIn(Dist.CLIENT)
public class FXBlockWard extends TextureSheetParticle {
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[15];
    static {
        for (int i = 0; i < 15; i++) {
            TEXTURES[i] = new ResourceLocation("arcaneresearch", "textures/models/hemis" + (i + 1) + ".png");
        }
    }
    private final Direction side;
    private final int rotation;
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;
    public FXBlockWard(ClientLevel level, double x, double y, double z,
                       Direction side, float hitX, float hitY, float hitZ) {
        super(level, x, y, z, 0, 0, 0);
        this.side = side;
        this.gravity = 0.0f;
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.lifetime = 12 + random.nextInt(5);
        this.setSize(0.01f, 0.01f);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.quadSize = (float)(1.4 + random.nextGaussian() * 0.3);
        this.rotation = random.nextInt(360);
        this.offsetX = side.getStepX() != 0 ? 0 : Mth.clamp(hitX - 0.6f + random.nextFloat() * 0.2f, -0.4f, 0.4f);
        this.offsetY = side.getStepY() != 0 ? 0 : Mth.clamp(hitY - 0.6f + random.nextFloat() * 0.2f, -0.4f, 0.4f);
        this.offsetZ = side.getStepZ() != 0 ? 0 : Mth.clamp(hitZ - 0.6f + random.nextFloat() * 0.2f, -0.4f, 0.4f);
        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;
        this.alpha = 0.0f;
    }
    @Override
    public void tick() {
        this.xo = x;
        this.yo = y;
        this.zo = z;
        float threshold = lifetime / 5.0f;
        if (age <= threshold) {
            this.alpha = age / threshold;
        } else {
            this.alpha = (lifetime - age) / (float)lifetime;
        }
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
        this.yd -= 0.04 * this.gravity;
        this.x += this.xd;
        this.y += this.yd;
        this.z += this.zd;
    }
    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        renderWard(camera, partialTicks);
    }
    protected void renderWard(Camera camera, float partialTicks) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        float fade = (age + partialTicks) / lifetime;
        int frame = Math.min(14, (int)(15.0f * fade));
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, TEXTURES[frame]);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 1);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        Vec3 camPos = camera.getPosition();
        float px = (float)(Mth.lerp(partialTicks, xo, x) - camPos.x() + offsetX);
        float py = (float)(Mth.lerp(partialTicks, yo, y) - camPos.y() + offsetY);
        float pz = (float)(Mth.lerp(partialTicks, zo, z) - camPos.z() + offsetZ);
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(px, py, pz);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0f * side.getStepY()));
        poseStack.mulPose(Axis.XN.rotationDegrees(90.0f * side.getStepX()));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0f * side.getStepZ()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
        if (side.getStepZ() > 0) {
            poseStack.translate(0, 0, 0.505);
            poseStack.mulPose(Axis.YN.rotationDegrees(180.0f));
        } else if (side.getStepZ() < 0) {
            poseStack.translate(0, 0, -0.505);
        } else if (side.getStepX() > 0) {
            poseStack.translate(0.505, 0, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
        } else if (side.getStepX() < 0) {
            poseStack.translate(-0.505, 0, 0);
            poseStack.mulPose(Axis.YN.rotationDegrees(90.0f));
        } else if (side.getStepY() > 0) {
            poseStack.translate(0, 0.505, 0);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
        } else if (side.getStepY() < 0) {
            poseStack.translate(0, -0.505, 0);
            poseStack.mulPose(Axis.XN.rotationDegrees(90.0f));
        }
        Matrix4f matrix = poseStack.last().pose();
        float size = quadSize * 0.5f;
        float renderAlpha = alpha / 2.0f;
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.vertex(matrix, -size, size, 0)
                .uv(0, 1).color(rCol, gCol, bCol, renderAlpha).endVertex();
        builder.vertex(matrix, size, size, 0)
                .uv(1, 1).color(rCol, gCol, bCol, renderAlpha).endVertex();
        builder.vertex(matrix, size, -size, 0)
                .uv(1, 0).color(rCol, gCol, bCol, renderAlpha).endVertex();
        builder.vertex(matrix, -size, -size, 0)
                .uv(0, 0).color(rCol, gCol, bCol, renderAlpha).endVertex();
        tesselator.end();
        poseStack.popPose();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
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
