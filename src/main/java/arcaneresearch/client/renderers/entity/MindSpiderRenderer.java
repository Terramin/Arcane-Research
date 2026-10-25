package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SpiderEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.common.entities.monster.EntityMindSpider;
@OnlyIn(Dist.CLIENT)
public class MindSpiderRenderer extends MobRenderer<EntityMindSpider, SpiderModel<EntityMindSpider>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/spider/spider.png");
    public MindSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiderModel<>(context.bakeLayer(ModelLayers.SPIDER)), 0.5F);
        this.addLayer(new SpiderEyesLayer<>(this));
    }
    @Override
    public ResourceLocation getTextureLocation(EntityMindSpider entity) {
        return TEXTURE;
    }
    @Override
    public void render(EntityMindSpider entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        String viewer = entity.getViewer();
        Minecraft mc = Minecraft.getInstance();
        if (viewer.isEmpty() || (mc.player != null && viewer.equals(mc.player.getName().getString()))) {
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        }
    }
    @Override
    protected void scale(EntityMindSpider entity, PoseStack poseStack, float partialTicks) {
        poseStack.scale(0.6F, 0.6F, 0.6F);
    }
}
