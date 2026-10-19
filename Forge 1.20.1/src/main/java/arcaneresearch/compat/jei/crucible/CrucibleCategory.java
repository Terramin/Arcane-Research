package arcaneresearch.compat.jei.crucible;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.common.lib.crafting.CrucibleRecipeType;
import arcaneresearch.compat.jei.JeiResearch;
import arcaneresearch.compat.jei.ArcaneresearchJEIPlugin;
import arcaneresearch.init.ModBlocks;
import java.util.ArrayList;
import java.util.List;
@net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
public class CrucibleCategory implements IRecipeCategory<CrucibleRecipeType> {
    public static final ResourceLocation UID = new ResourceLocation(Arcaneresearch.MODID, "crucible");
    private static final ResourceLocation OVERLAY_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/gui/gui_researchbook_overlay.png");
    private static final int WIDTH = 140;
    private static final int HEIGHT = 130;
    private static final int OX = 70;
    private static final int OY = 60;
    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;
    public CrucibleCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.CRUCIBLE.get()));
        this.title = Component.translatable("gui.arcaneresearch.crucible");
    }
    private static List<AspectSpot> layoutAspects(AspectList aspects) {
        List<AspectSpot> spots = new ArrayList<>();
        if (aspects == null || aspects.size() == 0) {
            return spots;
        }
        int total = 0;
        int rows = (aspects.size() - 1) / 3;
        int shift = (3 - aspects.size() % 3) * 10;
        int sx = OX - 28;
        int sy = OY + 8 - 10 * rows;
        for (Aspect tag : aspects.getAspectsSortedByName()) {
            int m = 0;
            if (total / 3 >= rows && (rows > 1 || aspects.size() < 3)) {
                m = 1;
            }
            int vx = sx + total % 3 * 20 + shift * m;
            int vy = sy + total / 3 * 20;
            spots.add(new AspectSpot(tag, aspects.getAmount(tag), vx, vy));
            total++;
        }
        return spots;
    }
    @Override
    public RecipeType<CrucibleRecipeType> getRecipeType() {
        return ArcaneresearchJEIPlugin.CRUCIBLE_TYPE;
    }
    @Override
    public Component getTitle() {
        return title;
    }
    @Override
    public IDrawable getBackground() {
        return background;
    }
    @Override
    public IDrawable getIcon() {
        return icon;
    }
    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CrucibleRecipeType recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, OX - 65, OY - 57)
                .addIngredients(recipe.getCatalyst());
        RegistryAccess registryAccess = Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.registryAccess()
                : RegistryAccess.EMPTY;
        builder.addSlot(RecipeIngredientRole.OUTPUT, OX - 9, OY - 51)
                .addItemStack(recipe.getResultItem(registryAccess));
    }
    @Override
    public void draw(CrucibleRecipeType recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(OX, OY, 0);
        guiGraphics.pose().scale(2f, 2f, 1f);
        guiGraphics.blit(OVERLAY_TEXTURE, -28, -29, 0, 3, 56, 17);
        guiGraphics.blit(OVERLAY_TEXTURE, -28, -12, 0, 20, 56, 48);
        guiGraphics.blit(OVERLAY_TEXTURE, -25, -26, 100, 84, 11, 13);
        guiGraphics.pose().popPose();
        for (AspectSpot spot : layoutAspects(recipe.getAspects())) {
            AspectRenderer.drawAspect(guiGraphics, spot.x(), spot.y(), spot.aspect(), spot.amount());
        }
        JeiResearch.drawTopRight(guiGraphics, recipe.getResearch(), WIDTH);
    }
    @Override
    public List<Component> getTooltipStrings(CrucibleRecipeType recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        for (AspectSpot spot : layoutAspects(recipe.getAspects())) {
            if (AspectRenderer.isMouseOverAspect(spot.x(), spot.y(), (int) mouseX, (int) mouseY)) {
                return AspectRenderer.getAspectTooltip(spot.aspect(), spot.amount());
            }
        }
        return JeiResearch.tooltip(recipe.getResearch(), mouseX, mouseY, WIDTH);
    }
    private record AspectSpot(Aspect aspect, int amount, int x, int y) {
    }
}
