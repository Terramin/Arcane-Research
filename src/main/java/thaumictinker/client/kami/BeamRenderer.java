package thaumictinker.client.kami;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import thaumictinker.kami.foci.ShadowbeamFocus;

 


 
public class BeamRenderer extends EntityRenderer<ShadowbeamFocus.Beam> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("thaumictinker", "textures/misc/white.png");

    public BeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ShadowbeamFocus.Beam entity) {
        return TEXTURE;
    }
}
