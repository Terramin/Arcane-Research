package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.SpellBatModel;
import arcaneresearch.common.entities.monster.EntitySpellBat;
@OnlyIn(Dist.CLIENT)
public class SpellBatRenderer extends MobRenderer<EntitySpellBat, SpellBatModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/entity/spellbat.png");
    public SpellBatRenderer(EntityRendererProvider.Context context) {
        super(context, new SpellBatModel(context.bakeLayer(SpellBatModel.LAYER_LOCATION)), 0.25F);
    }
    @Override
    public ResourceLocation getTextureLocation(EntitySpellBat entity) {
        return TEXTURE;
    }
    @Override
    protected RenderType getRenderType(EntitySpellBat entity, boolean bodyVisible, boolean translucent,
                                       boolean glowing) {
        return RenderType.entityTranslucent(getTextureLocation(entity));
    }
    @Override
    protected void scale(EntitySpellBat entity, PoseStack poseStack, float partialTicks) {
        poseStack.scale(0.35F, 0.35F, 0.35F);
    }
    @Override
    protected void setupRotations(EntitySpellBat entity, PoseStack poseStack, float ageInTicks,
                                  float rotationYaw, float partialTicks) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
        poseStack.translate(0.0F, -0.1F, 0.0F);
    }
    @Override
    protected int getBlockLightLevel(EntitySpellBat entity, BlockPos pos) {
        return 15;
    }
    @Override
    protected int getSkyLightLevel(EntitySpellBat entity, BlockPos pos) {
        return 15;
    }
}
