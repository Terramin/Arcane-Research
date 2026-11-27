package arcaneresearch.compat.jei.multiblock;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.crafting.Part;
import arcaneresearch.client.gui.BlueprintBlockAccess;
import arcaneresearch.compat.jei.JeiResearch;
import arcaneresearch.compat.jei.ArcaneresearchJEIPlugin;
import arcaneresearch.init.ModItems;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * EN: JEI recipe category for multiblock blueprints. Renders the structure as an
 *     animated 3D preview with the required parts listed underneath.
 * RU: Категория рецептов JEI для чертежей многоблочных структур. Рисует структуру
 *     как анимированное 3D-превью со списком необходимых частей снизу.
 */
@net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
public class MultiblockCategory implements IRecipeCategory<ArcaneresearchApi.BluePrint> {
    /** EN: Legacy category id, kept for resource compatibility. RU: Старый id категории для совместимости ресурсов. */
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "multiblock");
    private static final int WIDTH = 150;
    private static final int HEIGHT = 150;
    private static final int CX = 75;
    private static final int CY = 62;
    private static final float ROT_X = 25.0f;
    private static final float ROT_Y = -45.0f;
    private static final int PARTS_Y = 124;

    /** EN: Category icon (Salis Mundus). RU: Иконка категории (Salis Mundus). */
    private final IDrawable icon;
    /** EN: Localized category title. RU: Локализованное название категории. */
    private final Component title;
    /** EN: Recipe whose block access was cached last. RU: Рецепт, для которого последний раз создан доступ к блокам. */
    private ArcaneresearchApi.BluePrint currentRecipe;
    /** EN: Cached 3D block access for the displayed blueprint. RU: Кэш доступа к блокам для отображаемого чертежа. */
    private BlueprintBlockAccess access;

    /**
     * EN: Creates the category, using Salis Mundus as its icon.
     * RU: Создаёт категорию, используя Salis Mundus в качестве иконки.
     *
     * @param guiHelper JEI helper used to turn the icon item into a drawable / хелпер JEI для создания иконки
     */
    public MultiblockCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.SALIS_MUNDUS.get()));
        this.title = Component.translatable("gui.arcaneresearch.multiblock");
    }

    /**
     * {@inheritDoc}
     *
     * @return the JEI recipe type for multiblock blueprints / тип рецептов JEI для чертежей
     */
    @Override
    public @NotNull RecipeType<ArcaneresearchApi.BluePrint> getRecipeType() {
        return ArcaneresearchJEIPlugin.MULTIBLOCK_TYPE;
    }

    /**
     * {@inheritDoc}
     *
     * @return localized category title / локализованное название категории
     */
    @Override
    public @NotNull Component getTitle() {
        return title;
    }

    /**
     * {@inheritDoc}
     *
     * @return category width in pixels / ширина категории в пикселях
     */
    @Override
    public int getWidth() {
        return WIDTH;
    }

    /**
     * {@inheritDoc}
     *
     * @return category height in pixels / высота категории в пикселях
     */
    @Override
    public int getHeight() {
        return HEIGHT;
    }

    /**
     * {@inheritDoc}
     *
     * @return category icon / иконка категории
     */
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    /**
     * EN: Adds the blueprint's ingredient list as input slots along the bottom and caches
     *     the 3D block access for the recipe.
     * RU: Добавляет ингредиенты чертежа как слоты ввода внизу и кэширует 3D-доступ к блокам.
     *
     * @param builder JEI recipe layout builder / построитель разметки рецепта JEI
     * @param recipe  blueprint being displayed / отображаемый чертёж
     * @param focuses JEI focus group / группа фокуса JEI
     */
    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, ArcaneresearchApi.BluePrint recipe, @NotNull IFocusGroup focuses) {
        this.currentRecipe = recipe;
        Part[][][] parts = recipe.getParts();
        this.access = (parts == null) ? null : new BlueprintBlockAccess(parts);
        ItemStack[] ingredients = recipe.getIngredientList();
        if (ingredients != null && ingredients.length > 0) {
            List<ItemStack> list = new ArrayList<>();
            for (ItemStack stack : ingredients) {
                if (stack != null && !stack.isEmpty()) {
                    list.add(stack.copy());
                }
            }
            int n = list.size();
            int sx = (WIDTH - (n * 18 + (n - 1) * 4)) / 2;
            for (int i = 0; i < n; i++) {
                builder.addSlot(RecipeIngredientRole.INPUT, sx + i * 22, PARTS_Y)
                        .addItemStack(list.get(i));
            }
        }
    }

    /**
     * EN: Draws the animated 3D structure preview and the research badge.
     *     Parts are re-read every frame because {@link #setRecipe} already assigned
     *     {@code currentRecipe}/{@code access}; a cached local array would stay null
     *     and the blueprint would never be drawn.
     * RU: Рисует анимированное 3D-превью структуры и метку исследования. Части читаются
     *     каждый кадр, потому что {@link #setRecipe} уже выставил {@code currentRecipe}/{@code access};
     *     локальный кэш остался бы null и чертёж никогда не отрисовывался.
     *
     * @param recipe        blueprint being displayed / отображаемый чертёж
     * @param recipeSlotsView JEI recipe slots / слоты рецепта JEI
     * @param guiGraphics   rendering helper / помощник отрисовки
     * @param mouseX        mouse X within the recipe GUI / координата X мыши
     * @param mouseY        mouse Y within the recipe GUI / координата Y мыши
     */
    @Override
    public void draw(ArcaneresearchApi.@NotNull BluePrint recipe, @NotNull IRecipeSlotsView recipeSlotsView,
                     @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe == null || guiGraphics == null) {
            return;
        }
        Part[][][] parts = recipe.getParts();
        if (access == null || currentRecipe != recipe) {
            currentRecipe = recipe;
            access = (parts == null) ? null : new BlueprintBlockAccess(parts);
        }
        if (access == null || parts == null) {
            JeiResearch.drawTopRight(guiGraphics, recipe.getResearch(), WIDTH);
            return;
        }

        int ySize = parts.length;
        Part[][] layer0 = ySize > 0 ? parts[0] : null;
        Part[] row0 = (layer0 != null && layer0.length > 0) ? layer0[0] : null;
        int xSize = layer0 == null ? 0 : layer0.length;
        int zSize = row0 == null ? 0 : row0.length;
        if (ySize == 0 || xSize == 0 || zSize == 0) {
            JeiResearch.drawTopRight(guiGraphics, recipe.getResearch(), WIDTH);
            return;
        }

        int height = access.getHeight();
        if (height < 0) {
            height = 0;
        }
        access.sliceLine = (int) ((System.currentTimeMillis() / 1000L) % (height + 1L));

        float scale = Math.max(6f, 26 - Math.max(ySize, Math.max(xSize, zSize)) * 2);

        var pose = guiGraphics.pose();
        var buffer = guiGraphics.bufferSource();
        pose.pushPose();
        try {
            pose.translate(CX, CY, 200);
            pose.scale(scale, -scale, scale);
            pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(ROT_X));
            pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(ROT_Y));
            pose.translate(-xSize / 2f, -ySize / 2f, -zSize / 2f);

            for (int h = 0; h < ySize; ++h) {
                boolean hidden = access.isHiddenBySlice(h);
                Part[][] mirror = parts[ySize - 1 - h];
                if (mirror == null) {
                    continue;
                }
                int rowCount = Math.min(xSize, mirror.length);
                for (int l = 0; l < rowCount; ++l) {
                    Part[] row = mirror[l];
                    if (row == null) {
                        continue;
                    }
                    int colCount = Math.min(zSize, row.length);
                    for (int w = 0; w < colCount; ++w) {
                        Part part = row[w];
                        BlockState state = access.getBlockState(new BlockPos(l, h, w));
                        pose.pushPose();
                        try {
                            pose.translate(l, h, w);
                            BlueprintBlockAccess.renderBlueprintState(pose, buffer, state, 15728880);
                            if (!hidden) {
                                BlueprintBlockAccess.renderNodeOverlay(pose, buffer, part);
                            }
                        } catch (Exception ignored) {
                        } finally {
                            pose.popPose();
                        }
                    }
                }
            }
        } finally {
            pose.popPose();
        }

        guiGraphics.flush();
        JeiResearch.drawTopRight(guiGraphics, recipe.getResearch(), WIDTH);
    }

    /**
     * EN: Adds the research requirement tooltip for the recipe.
     * RU: Добавляет подсказку о требуемом исследовании для рецепта.
     *
     * @param tooltip        JEI tooltip builder / построитель подсказки JEI
     * @param recipe         blueprint being displayed / отображаемый чертёж
     * @param recipeSlotsView JEI recipe slots / слоты рецепта JEI
     * @param mouseX         mouse X within the recipe GUI / координата X мыши
     * @param mouseY         mouse Y within the recipe GUI / координата Y мыши
     */
    @Override
    public void getTooltip(ITooltipBuilder tooltip, ArcaneresearchApi.BluePrint recipe, @NotNull IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        tooltip.addAll(JeiResearch.tooltip(recipe.getResearch(), mouseX, mouseY, WIDTH));
    }
}
