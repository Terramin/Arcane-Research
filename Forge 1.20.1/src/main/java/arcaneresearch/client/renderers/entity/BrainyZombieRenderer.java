package arcaneresearch.client.renderers.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.entities.monster.EntityBrainyZombie;
import arcaneresearch.common.entities.monster.EntityGiantBrainyZombie;
@OnlyIn(Dist.CLIENT)
public class BrainyZombieRenderer extends HumanoidMobRenderer<EntityBrainyZombie, ZombieModel<EntityBrainyZombie>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/entity/bzombie.png");
    public BrainyZombieRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
    }
    @Override
    public ResourceLocation getTextureLocation(EntityBrainyZombie entity) {
        return TEXTURE;
    }
    @Override
    protected void scale(EntityBrainyZombie entity, PoseStack poseStack, float partialTicks) {
        if (entity instanceof EntityGiantBrainyZombie giant) {
            float s = giant.getScale();
            poseStack.scale(s, s, s);
        }
        super.scale(entity, poseStack, partialTicks);
    }
}
