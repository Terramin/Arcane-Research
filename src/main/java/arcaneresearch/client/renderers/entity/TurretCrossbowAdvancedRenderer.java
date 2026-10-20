package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.entities.construct.EntityTurretCrossbowAdvanced;
@OnlyIn(Dist.CLIENT)
public class TurretCrossbowAdvancedRenderer extends EntityRenderer<EntityTurretCrossbowAdvanced> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/entity/crossbow_advanced.png");
    private static final ResourceLocation MODEL =
            new ResourceLocation(Arcaneresearch.MODID, "models/obj/crossbow_advanced.obj");
    public TurretCrossbowAdvancedRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }
    @Override
    public ResourceLocation getTextureLocation(EntityTurretCrossbowAdvanced entity) {
        return TEXTURE;
    }
    @Override
    public void render(EntityTurretCrossbowAdvanced turret, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(turret, entityYaw, partialTicks, poseStack, buffer, packedLight);
        TurretObjModel model = TurretObjModel.load(MODEL);
        if (model == null) {
            return;
        }
        turret.loadProgressForRender = turret.getLoadProgress(partialTicks);
        float loadProgress = turret.loadProgressForRender;
        float swing = turret.getAttackAnim(partialTicks);
        VertexConsumer consumer = buffer.getBuffer(TCRenderTypes.objSolid(TEXTURE));
        float r = 1.0f, g = 1.0f, b = 1.0f;
        if (turret.hurtTime > 0) {
            r = 1.0f;
            g = 0.5f;
            b = 0.5f;
        }
        poseStack.pushPose();
        poseStack.translate(0.0, 0.75, 0.0);
        poseStack.pushPose();
        if (turret.isPassenger() && turret.getVehicle() instanceof AbstractMinecart) {
            poseStack.scale(0.66f, 0.75f, 0.66f);
        }
        model.renderGroup(poseStack, consumer, "legs", packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0f);
        poseStack.popPose();
        if (turret.hurtTime > 0) {
            float jiggle = turret.hurtTime / 500.0f;
            poseStack.translate(turret.getRandom().nextGaussian() * jiggle,
                    turret.getRandom().nextGaussian() * jiggle,
                    turret.getRandom().nextGaussian() * jiggle);
        }
        float yawHead = Mth.lerp(partialTicks, turret.yHeadRotO, turret.yHeadRot);
        float pitch = Mth.lerp(partialTicks, turret.xRotO, turret.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(-yawHead));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        model.renderGroup(poseStack, consumer, "mech", packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0f);
        model.renderGroup(poseStack, consumer, "box", packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0f);
        model.renderGroup(poseStack, consumer, "shield", packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0f);
        model.renderGroup(poseStack, consumer, "brain", packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0f);
        poseStack.pushPose();
        poseStack.translate(0.0, 0.0,
                Mth.sin(Mth.sqrt(loadProgress) * (float) Math.PI * 2.0f) / 12.0f);
        model.renderGroup(poseStack, consumer, "loader", packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0f);
        poseStack.popPose();
        float sp = 0.0f;
        if (swing > 0.0f) {
            sp = Mth.sin(Mth.sqrt(swing) * (float) Math.PI * 2.0f) * 20.0f;
        }
        poseStack.translate(0.0, 0.0, 0.375);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(sp));
        model.renderGroup(poseStack, consumer, "bow1", packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0f);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-sp));
        model.renderGroup(poseStack, consumer, "bow2", packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0f);
        poseStack.popPose();
        poseStack.popPose();
    }
    @Override
    protected boolean shouldShowName(EntityTurretCrossbowAdvanced entity) {
        return false;
    }
}
