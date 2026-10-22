package arcaneresearch.helper;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

 







 
@OnlyIn(Dist.CLIENT)
public final class GuiBlitHelper {

    private GuiBlitHelper() {
    }

     


 
    public static void blit(GuiGraphicsExtractor graphics, Identifier tex,
            int x, int y, float u, float v, int w, int h, int texW, int texH) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, u, v, w, h, texW, texH);
    }
}
