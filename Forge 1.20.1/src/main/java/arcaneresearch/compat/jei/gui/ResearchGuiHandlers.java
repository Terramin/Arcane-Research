package arcaneresearch.compat.jei.gui;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.renderer.Rect2i;
import arcaneresearch.client.gui.screens.FocalManipulatorScreen;
import arcaneresearch.client.gui.screens.ResearchTableScreen;
import java.util.List;
@net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class ResearchGuiHandlers {
    private ResearchGuiHandlers() {
    }
    public static class ResearchTableHandler implements IGuiContainerHandler<ResearchTableScreen> {
        @Override
        public List<Rect2i> getGuiExtraAreas(ResearchTableScreen guiContainer) {
            return List.of();
        }
    }
    public static class FocalManipulatorHandler implements IGuiContainerHandler<FocalManipulatorScreen> {
        @Override
        public List<Rect2i> getGuiExtraAreas(FocalManipulatorScreen guiContainer) {
            return List.of();
        }
    }
}
