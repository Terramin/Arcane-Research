package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.entity.EldritchGuardianModel;
import arcaneresearch.common.entities.monster.EntityEldritchGuardian;
import arcaneresearch.common.world.maze.MazeFeature;
@OnlyIn(Dist.CLIENT)
public class EldritchGuardianRenderer extends MobRenderer<EntityEldritchGuardian, EldritchGuardianModel<EntityEldritchGuardian>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/entity/eldritch_guardian.png");
    public EldritchGuardianRenderer(EntityRendererProvider.Context context) {
        super(context, new EldritchGuardianModel<>(context.bakeLayer(EldritchGuardianModel.LAYER_LOCATION)), 0.5F);
    }
    @Override
    public ResourceLocation getTextureLocation(EntityEldritchGuardian entity) {
        return TEXTURE;
    }
    @Override
    protected RenderType getRenderType(EntityEldritchGuardian entity, boolean bodyVisible,
                                       boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(getTextureLocation(entity));
    }
    @Override
    public void render(EntityEldritchGuardian entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.model.renderAlpha = ghostAlpha(entity, true);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
    static float ghostAlpha(net.minecraft.world.entity.Entity entity, boolean opaqueInOuterLands) {
        try {
            if (opaqueInOuterLands && entity.level().dimension().equals(MazeFeature.DIMENSION)) {
                return 1.0f;
            }
        } catch (Exception ignored) {
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return 1.0f;
        }
        float visRange = entity.level().getDifficulty() == Difficulty.HARD ? 576.0f : 1024.0f;
        float fadeStart = 256.0f;
        double distSq = entity.distanceToSqr(player.getX(), player.getY(), player.getZ());
        if (distSq < fadeStart) {
            return 0.6f;
        }
        return (1.0f - Math.min(visRange - fadeStart, (float) distSq - fadeStart)
                / (visRange - fadeStart)) * 0.6f;
    }
}
