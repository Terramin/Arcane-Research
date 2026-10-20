package arcaneresearch.common.golems.client;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
@OnlyIn(Dist.CLIENT)
@Deprecated
public class RenderArcaneresearchGolem extends arcaneresearch.client.renderers.entity.GolemRenderer {
    public RenderArcaneresearchGolem(EntityRendererProvider.Context context) {
        super(context);
    }
}
