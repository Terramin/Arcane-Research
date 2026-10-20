package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.FireBatModel;
import arcaneresearch.common.entities.monster.EntityFireBat;
@OnlyIn(Dist.CLIENT)
public class FireBatRenderer extends MobRenderer<EntityFireBat, FireBatModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/entity/firebat.png");
    public FireBatRenderer(EntityRendererProvider.Context context) {
        super(context, new FireBatModel(context.bakeLayer(FireBatModel.LAYER_LOCATION)), 0.25F);
    }
    @Override
    public ResourceLocation getTextureLocation(EntityFireBat entity) {
        return TEXTURE;
    }
    @Override
    protected void scale(EntityFireBat entity, PoseStack poseStack, float partialTicks) {
        poseStack.scale(0.35F, 0.35F, 0.35F);
    }
    @Override
    protected void setupRotations(EntityFireBat entity, PoseStack poseStack, float ageInTicks,
                                  float rotationYaw, float partialTicks) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
        if (!entity.getIsBatHanging()) {
            poseStack.translate(0.0F, Mth.cos(ageInTicks * 0.3F) * 0.1F, 0.0F);
        } else {
            poseStack.translate(0.0F, -0.1F, 0.0F);
        }
    }
}
