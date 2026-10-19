package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.EldritchCrabModel;
import arcaneresearch.common.entities.monster.EntityEldritchCrab;
@OnlyIn(Dist.CLIENT)
public class EldritchCrabRenderer extends MobRenderer<EntityEldritchCrab, EldritchCrabModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/entity/crab.png");
    public EldritchCrabRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchCrabModel(context.bakeLayer(EldritchCrabModel.LAYER_LOCATION)), 0.5F);
    }
    @Override
    public ResourceLocation getTextureLocation(EntityEldritchCrab entity) {
        return TEXTURE;
    }
    @Override
    protected void scale(EntityEldritchCrab entity, PoseStack poseStack, float partialTicks) {
        poseStack.scale(0.8F, 0.8F, 0.8F);
    }
}
