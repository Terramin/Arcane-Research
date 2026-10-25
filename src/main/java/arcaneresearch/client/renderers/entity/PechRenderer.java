package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.PechModel;
import arcaneresearch.client.renderers.entity.mob.PechHeldItemLayer;
import arcaneresearch.common.entities.monster.EntityPech;
@OnlyIn(Dist.CLIENT)
public class PechRenderer extends MobRenderer<EntityPech, PechModel> {
    private static final ResourceLocation TEXTURE_FORAGER =
            new ResourceLocation(Arcaneresearch.MODID, "textures/entity/pech_forage.png");
    private static final ResourceLocation TEXTURE_MAGE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/entity/pech_thaum.png");
    private static final ResourceLocation TEXTURE_STALKER =
            new ResourceLocation(Arcaneresearch.MODID, "textures/entity/pech_stalker.png");
    public PechRenderer(EntityRendererProvider.Context context) {
        super(context, new PechModel(context.bakeLayer(PechModel.LAYER_LOCATION)), 0.4F);
        this.addLayer(new PechHeldItemLayer(this, context.getItemInHandRenderer()));
    }
    @Override
    public ResourceLocation getTextureLocation(EntityPech entity) {
        return switch (entity.getPechType()) {
            case EntityPech.TYPE_MAGE -> TEXTURE_MAGE;
            case EntityPech.TYPE_FORAGER -> TEXTURE_STALKER;
            default -> TEXTURE_FORAGER;
        };
    }
    @Override
    public void render(EntityPech entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        double yOffset = 0.0;
        if (entity.isShiftKeyDown()) {
            yOffset = -0.125;
        }
        poseStack.pushPose();
        poseStack.translate(0.0, yOffset, 0.0);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
