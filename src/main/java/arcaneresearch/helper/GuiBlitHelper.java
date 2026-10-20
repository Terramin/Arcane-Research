package arcaneresearch.helper;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Единая точка blit-вызовов GUI (порт из Forge 1.20.1: 9-аргументный
 * {@code GuiGraphics.blit} -&gt; {@code GuiGraphicsExtractor.blit} с явным
 * пайплайном {@code RenderPipelines.GUI_TEXTURED}).
 *
 * <p>Сигнатура для вызывателей та же (тексельщики {@code u/v}, размер PNG
 * {@code texW/texH} обязателен), меняется только приёмник: экран рисует через
 * {@code extractBackground(..., GuiGraphicsExtractor, ...)}.</p>
 */
@OnlyIn(Dist.CLIENT)
public final class GuiBlitHelper {

    private GuiBlitHelper() {
    }

    /**
     * Прямоугольник {@code w*h} из атласа {@code texW*texH} со сдвигом
     * {@code (u, v)} в текселях.
     */
    public static void blit(GuiGraphicsExtractor graphics, Identifier tex,
            int x, int y, float u, float v, int w, int h, int texW, int texH) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, u, v, w, h, texW, texH);
    }
}
