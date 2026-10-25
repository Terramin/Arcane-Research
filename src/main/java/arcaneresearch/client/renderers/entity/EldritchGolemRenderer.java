package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.EldritchGolemModel;
import arcaneresearch.common.entities.monster.boss.EntityEldritchGolem;
@OnlyIn(Dist.CLIENT)
public class EldritchGolemRenderer extends MobRenderer<EntityEldritchGolem, EldritchGolemModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/entity/eldritch_golem.png");
    public EldritchGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchGolemModel(context.bakeLayer(EldritchGolemModel.LAYER_LOCATION)), 0.7F);
    }
    @Override
    public ResourceLocation getTextureLocation(EntityEldritchGolem entity) {
        return TEXTURE;
    }
    @Override
    protected void scale(EntityEldritchGolem entity, PoseStack poseStack, float partialTicks) {
        poseStack.scale(1.7F, 1.7F, 1.7F);
    }
}
