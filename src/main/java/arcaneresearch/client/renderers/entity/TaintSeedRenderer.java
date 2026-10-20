package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.TaintSeedModel;
import arcaneresearch.common.entities.monster.tainted.EntityTaintSeed;
@OnlyIn(Dist.CLIENT)
public class TaintSeedRenderer extends MobRenderer<EntityTaintSeed, TaintSeedModel<EntityTaintSeed>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/entity/taintseed.png");
    public TaintSeedRenderer(EntityRendererProvider.Context context) {
        super(context, new TaintSeedModel<>(context.bakeLayer(TaintSeedModel.LAYER_LOCATION)), 0.4F);
    }
    @Override
    public ResourceLocation getTextureLocation(EntityTaintSeed entity) {
        return TEXTURE;
    }
    @Override
    protected void scale(EntityTaintSeed entity, PoseStack poseStack, float partialTicks) {
        this.shadowRadius = entity instanceof arcaneresearch.common.entities.monster.tainted.EntityTaintSeedPrime
                ? 0.6F : 0.5F;
        float height = entity.getBbHeight();
        float hc = height * 10.0f;
        float grow = 0.0f;
        if (entity.tickCount < hc) {
            grow = (hc - entity.tickCount) / hc * height;
        }
        float s = height / 2.0f;
        float offset = (((height == 3.0f) ? 0.6f : 1.2f) + grow) + 1.501f * (s - 1.0f);
        poseStack.translate(0.0f, offset, 0.0f);
        float age = entity.tickCount + partialTicks;
        float pulse = 1.0f + 0.03f * (float) Math.sin(age / 12.0f);
        poseStack.scale(s * pulse, s, s * pulse);
    }
}
