package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.CrossbowModel;
import arcaneresearch.common.entities.construct.EntityTurretCrossbow;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class TurretCrossbowRenderer extends MobRenderer<EntityTurretCrossbow, CrossbowModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/crossbow.png");
    public TurretCrossbowRenderer(EntityRendererProvider.Context context) {
        super(context, new CrossbowModel(context.bakeLayer(CrossbowModel.LAYER_LOCATION)), 0.5F);
    }
    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull EntityTurretCrossbow entity) {
        return TEXTURE;
    }
    @Override
    public void render(EntityTurretCrossbow entity, float entityYaw, float partialTicks,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        entity.loadProgressForRender = entity.getLoadProgress(partialTicks);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
    @Override
    protected void scale(@NotNull EntityTurretCrossbow entity, @NotNull PoseStack poseStack, float partialTicks) {
        super.scale(entity, poseStack, partialTicks);
    }
    @Override
    protected boolean shouldShowName(@NotNull EntityTurretCrossbow entity) {
        return false;
    }
}
