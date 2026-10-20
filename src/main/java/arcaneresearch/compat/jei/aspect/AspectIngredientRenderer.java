package arcaneresearch.compat.jei.aspect;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.client.lib.AspectRenderer;
import javax.annotation.Nullable;
import java.util.List;
@net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
public class AspectIngredientRenderer implements IIngredientRenderer<AspectList> {
    @Override
    public void render(GuiGraphics guiGraphics, @Nullable AspectList ingredient) {
        Aspect aspect = AspectIngredient.first(ingredient);
        if (aspect != null) {
            AspectRenderer.drawAspect(guiGraphics, 0, 0, aspect);
        }
    }
    @Override
    public List<Component> getTooltip(AspectList ingredient, TooltipFlag tooltipFlag) {
        Aspect aspect = AspectIngredient.first(ingredient);
        if (aspect == null) {
            return List.of();
        }
        return List.of(
                Component.literal(aspect.getName()).withStyle(ChatFormatting.AQUA),
                Component.literal(aspect.getLocalizedDescription()).withStyle(ChatFormatting.GRAY)
        );
    }
}
