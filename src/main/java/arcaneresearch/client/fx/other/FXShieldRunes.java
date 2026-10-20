package arcaneresearch.client.fx.other;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import arcaneresearch.Arcaneresearch;
@OnlyIn(Dist.CLIENT)
public class FXShieldRunes extends Particle {
    private static final ResourceLocation[] RIPPLE_TEXTURES = new ResourceLocation[15];
    private static final ResourceLocation[] HEMIS_TEXTURES = new ResourceLocation[15];
    static {
        for (int i = 0; i < 15; i++) {
            RIPPLE_TEXTURES[i] = new ResourceLocation(Arcaneresearch.MODID, "textures/models/ripple" + (i + 1) + ".png");
            HEMIS_TEXTURES[i] = new ResourceLocation(Arcaneresearch.MODID, "textures/models/hemis" + (i + 1) + ".png");
        }
    }
    private final Entity target;
    private final float yaw;
    private final float pitch;
    public FXShieldRunes(ClientLevel level, double x, double y, double z, Entity target, int maxAge, float yaw, float pitch) {
        super(level, x, y, z, 0.0, 0.0, 0.0);
        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;
        this.gravity = 0.0f;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.lifetime = maxAge + this.random.nextInt(maxAge / 2);
        this.target = target;
        this.yaw = yaw;
        this.pitch = pitch;
        if (target != null) {
            this.xo = this.x = target.getX();
            this.yo = this.y = (target.getBoundingBox().minY + target.getBoundingBox().maxY) / 2.0;
            this.zo = this.z = target.getZ();
        }
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
        if (this.target != null) {
            this.x = this.target.getX();
            this.y = (this.target.getBoundingBox().minY + this.target.getBoundingBox().maxY) / 2.0;
            this.z = this.target.getZ();
        }
    }
    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera camera, float partialTicks) {
        float fade = (this.age + partialTicks) / this.lifetime;
        int frame = Math.min(15, (int)(14.0f * fade) + 1);
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 1);
        ResourceLocation texture;
        float b = 1.0f;
        if (this.target != null && this.target instanceof Monster
                && !(this.target instanceof arcaneresearch.common.entities.monster.cult.EntityCultist)) {
            texture = RIPPLE_TEXTURES[frame - 1];
            b = 0.5f;
        } else {
            texture = HEMIS_TEXTURES[frame - 1];
        }
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, texture);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);
        float px = (float)(Mth.lerp(partialTicks, this.xo, this.x) - camera.getPosition().x);
        float py = (float)(Mth.lerp(partialTicks, this.yo, this.y) - camera.getPosition().y);
        float pz = (float)(Mth.lerp(partialTicks, this.zo, this.z) - camera.getPosition().z);
        float th = (this.target == null) ? 2.0f : this.target.getBbHeight();
        PoseStack poseStack = new PoseStack();
        poseStack.translate(px, py, pz);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - this.yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-this.pitch));
        poseStack.scale(0.2f * th, 0.2f * th, 0.2f * th);
        float a = Math.min(1.0f, (1.0f - fade) * 3.0f);
        if (this.target == null) {
            HemisModel.render(poseStack, builder, 0.65f, 0.1f, 0.5f, a);
        } else {
            HemisModel.render(poseStack, builder, b, b, b, a);
        }
        Tesselator.getInstance().end();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
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
