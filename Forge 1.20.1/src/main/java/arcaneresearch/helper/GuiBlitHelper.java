package arcaneresearch.helper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
@OnlyIn(Dist.CLIENT)
public final class GuiBlitHelper {
    private GuiBlitHelper() {
    }
    public static void blit(GuiGraphics graphics, ResourceLocation tex,
            int x, int y, float u, float v, int w, int h, int texW, int texH) {
        graphics.blit(tex, x, y, u, v, w, h, texW, texH);
    }
}
