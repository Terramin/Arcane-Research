package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.CrossbowModel;
import arcaneresearch.common.entities.construct.EntityTurretCrossbow;
@OnlyIn(Dist.CLIENT)
public class TurretCrossbowRenderer extends MobRenderer<EntityTurretCrossbow, CrossbowModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/entity/crossbow.png");
    public TurretCrossbowRenderer(EntityRendererProvider.Context context) {
        super(context, new CrossbowModel(context.bakeLayer(CrossbowModel.LAYER_LOCATION)), 0.5F);
    }
    @Override
    public ResourceLocation getTextureLocation(EntityTurretCrossbow entity) {
        return TEXTURE;
    }
    @Override
    public void render(EntityTurretCrossbow entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        entity.loadProgressForRender = entity.getLoadProgress(partialTicks);
        entity.yBodyRot = 0.0F;
        entity.yBodyRotO = 0.0F;
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
    @Override
    protected void scale(EntityTurretCrossbow entity, PoseStack poseStack, float partialTicks) {
        super.scale(entity, poseStack, partialTicks);
    }
    @Override
    protected boolean shouldShowName(EntityTurretCrossbow entity) {
        return false;
    }
}
