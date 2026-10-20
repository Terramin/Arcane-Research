package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.ISealDisplayer;
import arcaneresearch.api.golems.parts.GolemMaterial;
import arcaneresearch.client.models.entity.GolemModel;
import arcaneresearch.common.golems.EntityArcaneresearchGolem;
import arcaneresearch.common.lib.utils.EntityUtils;
@OnlyIn(Dist.CLIENT)
public class GolemRenderer extends MobRenderer<EntityArcaneresearchGolem, GolemModel> {
    private static final ResourceLocation DEFAULT_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/entity/golems/mat_wood.png");
    private final GolemObjLayer objLayer;
    public GolemRenderer(EntityRendererProvider.Context context) {
        super(context, new GolemModel(context.bakeLayer(GolemModel.LAYER_LOCATION)), 0.3F);
        this.objLayer = new GolemObjLayer(this);
    }
    private static boolean holdsSealDisplayer(Player player) {
        return player.getMainHandItem().getItem() instanceof ISealDisplayer
                || player.getOffhandItem().getItem() instanceof ISealDisplayer;
    }
    @Override
    public ResourceLocation getTextureLocation(EntityArcaneresearchGolem entity) {
        if (entity.getProperties() != null && entity.getProperties().getMaterial() != null) {
            GolemMaterial material = entity.getProperties().getMaterial();
            if (material.texture != null) {
                return material.texture;
            }
        }
        return DEFAULT_TEXTURE;
    }
    @Override
    public void render(EntityArcaneresearchGolem entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Player player = Minecraft.getInstance().player;
        boolean visible = !entity.isInvisible();
        boolean translucent = !visible && player != null && !entity.isInvisibleTo(player);
        if (visible || translucent) {
            if (translucent) {
                objLayer.setGhost(1.0F, 1.0F, 1.0F, 0.15F, false);
            }
            renderObjParts(entity, partialTicks, poseStack, buffer, packedLight);
            objLayer.clearGhost();
        }
        if (player != null && player.isShiftKeyDown() && holdsSealDisplayer(player)
                && !EntityUtils.canEntityBeSeen(player, entity.getX(),
                entity.getY() + entity.getBbHeight() / 2.0, entity.getZ())) {
            objLayer.setGhost(0.25F, 0.25F, 0.25F, 0.25F, true);
            renderObjParts(entity, partialTicks, poseStack, buffer, packedLight);
            objLayer.clearGhost();
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
    private void renderObjParts(EntityArcaneresearchGolem entity, float partialTicks,
                                PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float limbSwing = entity.walkAnimation.position(partialTicks);
        float limbSwingAmount = entity.walkAnimation.speed(partialTicks);
        float ageInTicks = entity.tickCount + partialTicks;
        float bodyYaw = Mth.lerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
        float netHeadYaw = Mth.wrapDegrees(Mth.lerp(partialTicks,
                entity.yHeadRotO, entity.yHeadRot) - bodyYaw);
        float headPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        poseStack.pushPose();
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F - bodyYaw));
        objLayer.render(poseStack, buffer, packedLight, entity, limbSwing,
                limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        poseStack.popPose();
    }
}
