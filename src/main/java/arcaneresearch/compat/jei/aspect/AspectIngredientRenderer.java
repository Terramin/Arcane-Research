package arcaneresearch.compat.jei.aspect;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.client.lib.AspectRenderer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
public class AspectIngredientRenderer implements IIngredientRenderer<AspectList> {

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, @Nullable AspectList ingredient) {
        if (ingredient == null) {
            return;
        }
        Aspect aspect = AspectIngredient.first(ingredient);
        if (aspect != null) {
            AspectRenderer.drawAspect(guiGraphics, 0, 0, aspect);
        }
    }

    @Override
    @SuppressWarnings("removal")
    public @NotNull List<Component> getTooltip(@NotNull AspectList ingredient, @NotNull TooltipFlag tooltipFlag) {
        return buildTooltip(ingredient);
    }

    @Override
    public void getTooltip(@NotNull ITooltipBuilder tooltip, @NotNull AspectList ingredient, @Nullable Player player, @NotNull TooltipFlag tooltipFlag) {
        for (Component line : buildTooltip(ingredient)) {
            tooltip.add(line);
        }
    }

    private static List<Component> buildTooltip(AspectList ingredient) {
        if (ingredient == null) {
            return List.of();
        }
        Aspect aspect = AspectIngredient.first(ingredient);
        if (aspect == null) {
            return List.of();
        }
        List<Component> lines = new ArrayList<>(2);
        String name = aspect.getName();
        if (name != null && !name.isEmpty()) {
            lines.add(Component.literal(name).withStyle(ChatFormatting.AQUA));
        }
        String desc = aspect.getLocalizedDescription();
        if (desc != null && !desc.isEmpty()) {
            lines.add(Component.literal(desc).withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }
}