package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.TaintacleModel;
import arcaneresearch.common.entities.monster.tainted.EntityTaintacle;
@OnlyIn(Dist.CLIENT)
public class TaintacleRenderer extends MobRenderer<EntityTaintacle, TaintacleModel<EntityTaintacle>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/entity/taintacle.png");
    public TaintacleRenderer(EntityRendererProvider.Context context) {
        super(context, new TaintacleModel<>(context.bakeLayer(TaintacleModel.LAYER_LOCATION)), 0.6F);
    }
    @Override
    public ResourceLocation getTextureLocation(EntityTaintacle entity) {
        return TEXTURE;
    }
    @Override
    protected void scale(EntityTaintacle entity, PoseStack poseStack, float partialTicks) {
        if (entity instanceof arcaneresearch.common.entities.monster.boss.EntityTaintacleGiant) {
            this.shadowRadius = 1.0F;
        } else if (entity instanceof arcaneresearch.common.entities.monster.tainted.EntityTaintacleSmall) {
            this.shadowRadius = 0.2F;
        } else {
            this.shadowRadius = 0.6F;
        }
        float height = entity.getBbHeight();
        float hc = height * 10.0f;
        float grow = 0.0f;
        if (entity.tickCount < hc) {
            grow = (hc - entity.tickCount) / hc * height;
        }
        float s = height / 3.0f;
        float off = ((height == 3.0f) ? 0.6f : 1.2f) + grow + 1.501f * (s - 1.0f);
        poseStack.translate(0.0f, off, 0.0f);
        poseStack.scale(s, s, s);
    }
}
