package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.EldritchGuardianModel;
import arcaneresearch.common.entities.monster.boss.EntityEldritchWarden;
@OnlyIn(Dist.CLIENT)
public class EldritchWardenRenderer extends MobRenderer<EntityEldritchWarden, EldritchGuardianModel<EntityEldritchWarden>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/entity/eldritch_warden.png");
    public EldritchWardenRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchGuardianModel<>(context.bakeLayer(EldritchGuardianModel.LAYER_LOCATION)), 0.6F);
    }
    @Override
    public ResourceLocation getTextureLocation(EntityEldritchWarden entity) {
        return TEXTURE;
    }
    @Override
    protected RenderType getRenderType(EntityEldritchWarden entity, boolean bodyVisible,
                                       boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(getTextureLocation(entity));
    }
    @Override
    protected void scale(EntityEldritchWarden entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.5F, 1.5F, 1.5F);
    }
    @Override
    public void render(EntityEldritchWarden entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.model.renderAlpha = EldritchGuardianRenderer.ghostAlpha(entity, true);
        if (entity.getSpawnTimer() > 0) {
            poseStack.translate(0.0F, -entity.getBbHeight() * (entity.getSpawnTimer() / 150.0F), 0.0F);
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
