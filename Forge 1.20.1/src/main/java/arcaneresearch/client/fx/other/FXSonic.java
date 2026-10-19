package arcaneresearch.client.fx.other;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.fx.particles.ArcaneresearchParticle;
@OnlyIn(Dist.CLIENT)
public class FXSonic extends ArcaneresearchParticle {
    private static final ResourceLocation[] RIPPLE_TEXTURES = new ResourceLocation[15];
    static {
        for (int i = 0; i < 15; i++) {
            RIPPLE_TEXTURES[i] = new ResourceLocation(Arcaneresearch.MODID, "textures/models/ripple" + (i + 1) + ".png");
        }
    }
    private final Entity target;
    private final float yaw;
    private final float pitch;
    public FXSonic(ClientLevel level, double x, double y, double z, Entity target, int maxAge) {
        super(level, x, y, z);
        this.target = target;
        this.yaw = target.getYHeadRot();
        this.pitch = target.getXRot();
        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;
        this.gravity = 0.0f;
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.lifetime = maxAge + this.random.nextInt(maxAge / 2);
        this.quadSize = 1.0f;
        this.x = target.getX();
        this.xo = this.x;
        this.y = target.getY() + target.getEyeHeight();
        this.yo = this.y;
        this.z = target.getZ();
        this.zo = this.z;
        this.noClip = true;
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
        this.x = this.target.getX();
        this.y = this.target.getY() + this.target.getEyeHeight();
        this.z = this.target.getZ();
    }
    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        float fade = (this.age + partialTicks) / this.lifetime;
        int frame = Math.min(15, (int)(14.0f * fade) + 1);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, RIPPLE_TEXTURES[frame - 1]);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);
        float px = (float)(Mth.lerp(partialTicks, this.xo, this.x) - camera.getPosition().x);
        float py = (float)(Mth.lerp(partialTicks, this.yo, this.y) - camera.getPosition().y);
        float pz = (float)(Mth.lerp(partialTicks, this.zo, this.z) - camera.getPosition().z);
        float h = this.target.getBbHeight();
        float w = this.target.getBbWidth();
        com.mojang.blaze3d.vertex.PoseStack poseStack = new com.mojang.blaze3d.vertex.PoseStack();
        poseStack.translate(px, py, pz);
        poseStack.mulPose(Axis.YP.rotationDegrees(-this.yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(this.pitch));
        poseStack.translate(0.0, 0.0, 2.0f * h + w / 2.0f);
        poseStack.scale(0.25f * h, 0.25f * h, -1.0f * h);
        HemisModel.render(poseStack, builder, 0.5f, 0.5f, 0.5f, 1.0f);
        Tesselator.getInstance().end();
        RenderSystem.depthMask(true);
    }
    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }
    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }
    @Override
    public boolean shouldCull() {
        return false;
    }
}
