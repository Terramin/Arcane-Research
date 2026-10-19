package arcaneresearch.client.renderers.entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
@OnlyIn(Dist.CLIENT)
public class InhabitedZombieRenderer extends ZombieRenderer {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/entity/czombie.png");
    public InhabitedZombieRenderer(EntityRendererProvider.Context context) {
        super(context);
    }
    @Override
    public ResourceLocation getTextureLocation(net.minecraft.world.entity.monster.Zombie entity) {
        return TEXTURE;
    }
}
