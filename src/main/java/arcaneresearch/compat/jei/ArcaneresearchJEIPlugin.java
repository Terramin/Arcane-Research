package arcaneresearch.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.client.gui.screens.FocalManipulatorScreen;
import arcaneresearch.client.gui.screens.ResearchTableScreen;
import arcaneresearch.common.lib.crafting.ArcaneWandDynamicRecipe;
import arcaneresearch.common.lib.crafting.CrucibleRecipeType;
import arcaneresearch.common.lib.crafting.InfusionRecipeType;
import arcaneresearch.common.menu.ArcaneWorkbenchMenu;
import arcaneresearch.compat.jei.arcane.ArcaneWorkbenchCategory;
import arcaneresearch.compat.jei.aspect.AspectCompoundCategory;
import arcaneresearch.compat.jei.aspect.AspectIngredient;
import arcaneresearch.compat.jei.aspect.AspectIngredientRenderer;
import arcaneresearch.compat.jei.aspect.AspectSourcesCategory;
import arcaneresearch.compat.jei.crucible.CrucibleCategory;
import arcaneresearch.compat.jei.gui.ResearchGuiHandlers;
import arcaneresearch.compat.jei.infusion.InfusionCategory;
import arcaneresearch.compat.jei.multiblock.MultiblockCategory;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModItems;
import arcaneresearch.init.ModMenuTypes;
import arcaneresearch.init.ModRecipeTypes;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JeiPlugin
@OnlyIn(Dist.CLIENT)
public class ArcaneresearchJEIPlugin implements IModPlugin {

    public static final ResourceLocation PLUGIN_ID =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "jei_plugin");

    public static final RecipeType<IArcaneRecipe> ARCANE_TYPE =
            RecipeType.create(Arcaneresearch.MODID, "arcane_workbench", IArcaneRecipe.class);
    public static final RecipeType<CrucibleRecipeType> CRUCIBLE_TYPE =
            RecipeType.create(Arcaneresearch.MODID, "crucible", CrucibleRecipeType.class);
    public static final RecipeType<InfusionRecipeType> INFUSION_TYPE =
            RecipeType.create(Arcaneresearch.MODID, "infusion", InfusionRecipeType.class);
    public static final RecipeType<ArcaneresearchApi.BluePrint> MULTIBLOCK_TYPE =
            RecipeType.create(Arcaneresearch.MODID, "multiblock", ArcaneresearchApi.BluePrint.class);
    public static final RecipeType<Aspect> ASPECT_COMPOUND_TYPE =
            RecipeType.create(Arcaneresearch.MODID, "aspect_compound", Aspect.class);
    public static final RecipeType<AspectSourcesCategory.Sources> ASPECT_SOURCES_TYPE =
            RecipeType.create(Arcaneresearch.MODID, "aspect_sources", AspectSourcesCategory.Sources.class);

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerIngredients(@NotNull IModIngredientRegistration registration) {
        try {
            var all = AspectIngredient.createAll();
            if (all.isEmpty()) {
                return;
            }
            registration.register(AspectIngredient.TYPE, all,
                    new AspectIngredient.Helper(), new AspectIngredientRenderer());
        } catch (Exception e) {
            Arcaneresearch.LOGGER.error("JEI: failed to register aspects", e);
        }
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        IJeiHelpers jeiHelpers = registration.getJeiHelpers();
        IGuiHelper guiHelper = jeiHelpers.getGuiHelper();
        registration.addRecipeCategories(new ArcaneWorkbenchCategory(guiHelper));
        registration.addRecipeCategories(new CrucibleCategory(guiHelper));
        registration.addRecipeCategories(new InfusionCategory(guiHelper));
        registration.addRecipeCategories(new MultiblockCategory(guiHelper));
        registration.addRecipeCategories(new AspectCompoundCategory(guiHelper));
        registration.addRecipeCategories(new AspectSourcesCategory(guiHelper));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        RecipeManager recipeManager = mc.level.getRecipeManager();

        List<IArcaneRecipe> arcaneRecipes = new ArrayList<>();
        try {
            var pairs = ArcaneWandDynamicRecipe.allPairs();
            for (var r : pairs) {
                if (r != null) {
                    arcaneRecipes.add(r);
                }
            }
        } catch (Exception e) {
            Arcaneresearch.LOGGER.warn("JEI: failed to list wand recipes", e);
        }

        List<CrucibleRecipeType> crucibleRecipes;
        var crucibleType = ModRecipeTypes.CRUCIBLE.get();
        crucibleRecipes = recipeManager
                .getAllRecipesFor(crucibleType)
                .stream()
                .filter(r -> r instanceof CrucibleRecipeType)
                .map(r -> (CrucibleRecipeType) r)
                .toList();

        List<InfusionRecipeType> infusionRecipes;
        var infusionType = ModRecipeTypes.INFUSION.get();
        infusionRecipes = recipeManager
                .getAllRecipesFor(infusionType)
                .stream()
                .filter(r -> r instanceof InfusionRecipeType)
                .map(r -> (InfusionRecipeType) r)
                .toList();

        List<ArcaneresearchApi.BluePrint> blueprints = new ArrayList<>();
        try {
            Map<?, ?> crafting = ArcaneresearchApi.getCraftingRecipes();
            if (crafting != null) {
                for (Object r : crafting.values()) {
                    if (r instanceof ArcaneresearchApi.BluePrint bp) {
                        blueprints.add(bp);
                    }
                }
            }
        } catch (Exception e) {
            Arcaneresearch.LOGGER.warn("JEI: failed to list blueprints", e);
        }

        registration.addRecipes(ARCANE_TYPE, arcaneRecipes);
        registration.addRecipes(CRUCIBLE_TYPE, crucibleRecipes);
        registration.addRecipes(INFUSION_TYPE, infusionRecipes);
        registration.addRecipes(MULTIBLOCK_TYPE, blueprints);

        var compoundRecipes = AspectCompoundCategory.recipes();
        if (compoundRecipes != null) {
            registration.addRecipes(ASPECT_COMPOUND_TYPE, compoundRecipes);
        }

        var sources = AspectSourcesCategory.buildIndex();
        registration.addRecipes(ASPECT_SOURCES_TYPE, sources);

        Arcaneresearch.LOGGER.info("JEI: Registered {} arcane, {} crucible, {} infusion, {} multiblock, {} aspects",
                arcaneRecipes.size(), crucibleRecipes.size(), infusionRecipes.size(),
                blueprints.size(), sources.size());

        try {
            ResearchGate.track(arcaneRecipes, crucibleRecipes, infusionRecipes, blueprints);
        } catch (Exception e) {
            Arcaneresearch.LOGGER.error("JEI: ResearchGate.track failed", e);
        }
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        ResearchGate.attachRuntime(jeiRuntime);
    }

    @Override
    public void registerRecipeTransferHandlers(@NotNull IRecipeTransferRegistration registration) {
        var menuType = ModMenuTypes.ARCANE_WORKBENCH.get();
        registration.addRecipeTransferHandler(
                ArcaneWorkbenchMenu.class,
                menuType,
                ARCANE_TYPE,
                1, 9,
                16, 36);
    }

    @Override
    public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(ResearchTableScreen.class,
                new ResearchGuiHandlers.ResearchTableHandler());
        registration.addGuiContainerHandler(FocalManipulatorScreen.class,
                new ResearchGuiHandlers.FocalManipulatorHandler());
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        addCatalystSafe(registration, ModBlocks.ARCANE_WORKBENCH, ARCANE_TYPE);
        addCatalystSafe(registration, ModBlocks.CRUCIBLE, CRUCIBLE_TYPE);
        addCatalystSafe(registration, ModBlocks.INFUSION_MATRIX, INFUSION_TYPE);
        addCatalystSafe(registration, ModItems.SALIS_MUNDUS, MULTIBLOCK_TYPE);
        addCatalystSafe(registration, ModItems.THAUMONOMICON, ASPECT_COMPOUND_TYPE);
        addCatalystSafe(registration, ModItems.THAUMONOMICON, ASPECT_SOURCES_TYPE);
    }

    private static void addCatalystSafe(IRecipeCatalystRegistration reg, RegistryObject<?> holder, RecipeType<?> type) {
        if (reg == null || holder == null || type == null) {
            return;
        }
        try {
            Object value = holder.get();
            if (value instanceof ItemLike item) {
                reg.addRecipeCatalyst(new ItemStack(item), type);
            }
        } catch (Exception ignored) {
        }
    }
}