package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SpiderEyesLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.common.entities.monster.EntityMindSpider;

// NEOFORGE_PORT (26.3): vanilla SpiderModel/SpiderEyesLayer (moved packages,
// non-generic model). Viewer-conditional visibility via shouldRender (was
// render() gate); scale via hook.
public class MindSpiderRenderer extends MobRenderer<EntityMindSpider, LivingEntityRenderState, SpiderModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("minecraft", "textures/entity/spider/spider.png");

    public MindSpiderRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiderModel(context.bakeLayer(ModelLayers.SPIDER)), 0.5F);
        this.addLayer(new SpiderEyesLayer<>(this));
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public boolean shouldRender(EntityMindSpider entity, net.minecraft.client.renderer.culling.Frustum frustum,
                                double x, double y, double z, float partialTicks) {
        if (!super.shouldRender(entity, frustum, x, y, z, partialTicks)) {
            return false;
        }
        String viewer = entity.getViewer();
        Minecraft mc = Minecraft.getInstance();
        return viewer.isEmpty()
                || (mc.player != null && viewer.equals(mc.player.getName().getString()));
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void scale(LivingEntityRenderState state, PoseStack poseStack) {
        poseStack.scale(0.6F, 0.6F, 0.6F);
    }
}