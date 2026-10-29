package arcaneresearch.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier; 
import net.minecraft.world.item.ItemStack;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.common.lib.crafting.CrucibleRecipeType;
import arcaneresearch.common.lib.crafting.InfusionRecipeType;
import arcaneresearch.compat.jei.arcane.ArcaneWorkbenchCategory;
import arcaneresearch.compat.jei.aspect.AspectCompoundCategory;
import arcaneresearch.compat.jei.aspect.AspectIngredient;
import arcaneresearch.compat.jei.aspect.AspectIngredientRenderer;
import arcaneresearch.compat.jei.aspect.AspectSourcesCategory;
import arcaneresearch.compat.jei.crucible.CrucibleCategory;
import arcaneresearch.compat.jei.infusion.InfusionCategory;
import arcaneresearch.compat.jei.multiblock.MultiblockCategory;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModItems;
import arcaneresearch.init.ModRecipeTypes;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class ArcaneresearchJEIPlugin implements IModPlugin {

    public static final Identifier PLUGIN_ID = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "jei_plugin");

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

    
    
    
    
    private static volatile java.util.Map<Object, Identifier> recipeIds = java.util.Map.of();

    public static Identifier recipeId(Object recipe) {
        if (recipe == null) {
            return null;
        }
        Identifier id = recipeIds.get(recipe);
        if (id == null && recipe instanceof IArcaneRecipe arcane) {
            id = arcane.getId();
        }
        return id;
    }

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerIngredients(@NotNull IModIngredientRegistration registration) {
        try {
            var all = AspectIngredient.createAll();
            if (all == null || all.isEmpty()) {
                return;
            }
            registration.register(AspectIngredient.TYPE, all,
                    new AspectIngredient.Helper(), new AspectIngredientRenderer(),
                    arcaneresearch.api.aspects.AspectList.CODEC);
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

        
        
        
        
        java.util.Collection<net.minecraft.world.item.crafting.RecipeHolder<?>> holders =
                arcaneresearch.common.lib.crafting.ClientRecipeCache.values();

        java.util.Map<Object, Identifier> ids = new java.util.IdentityHashMap<>();
        for (net.minecraft.world.item.crafting.RecipeHolder<?> holder : holders) {
            if (holder == null) {
                continue;
            }
            ids.put(holder.value(), holder.id().identifier());
        }
        recipeIds = ids;

        List<IArcaneRecipe> arcaneRecipes = new java.util.ArrayList<>();
        for (net.minecraft.world.item.crafting.RecipeHolder<?> holder : holders) {
            if (holder == null) {
                continue;
            }
            Object value = holder.value();
            if (value instanceof IArcaneRecipe arcane && !arcaneRecipes.contains(arcane)) {
                arcaneRecipes.add(arcane);
            }
        }

        try {
            var pairs = arcaneresearch.common.lib.crafting.ArcaneWandDynamicRecipe.allPairs();
            if (pairs != null) {
                for (var r : pairs) {
                    if (r != null) {
                        arcaneRecipes.add(r);
                    }
                }
            }
        } catch (Exception e) {
            Arcaneresearch.LOGGER.warn("JEI: failed to list wand recipes", e);
        }

        registration.addRecipes(ARCANE_TYPE, arcaneRecipes);

        List<CrucibleRecipeType> crucibleRecipes = new java.util.ArrayList<>();
        List<InfusionRecipeType> infusionRecipes = new java.util.ArrayList<>();
        for (net.minecraft.world.item.crafting.RecipeHolder<?> holder : holders) {
            if (holder == null) {
                continue;
            }
            Object value = holder.value();
            if (value instanceof CrucibleRecipeType crucible) {
                crucibleRecipes.add(crucible);
            } else if (value instanceof InfusionRecipeType infusion) {
                infusionRecipes.add(infusion);
            }
        }

        registration.addRecipes(CRUCIBLE_TYPE, crucibleRecipes);
        registration.addRecipes(INFUSION_TYPE, infusionRecipes);

        List<ArcaneresearchApi.BluePrint> blueprints = new java.util.ArrayList<>();
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

        registration.addRecipes(MULTIBLOCK_TYPE, blueprints);

        var compoundRecipes = AspectCompoundCategory.recipes();
        if (compoundRecipes != null) {
            registration.addRecipes(ASPECT_COMPOUND_TYPE, compoundRecipes);
        }

        var sources = AspectSourcesCategory.buildIndex();
        if (sources != null) {
            registration.addRecipes(ASPECT_SOURCES_TYPE, sources);
        }

        Arcaneresearch.LOGGER.info("JEI: Registered {} arcane, {} crucible, {} infusion, {} multiblock, {} aspects",
                arcaneRecipes.size(), crucibleRecipes.size(), infusionRecipes.size(), blueprints.size(),
                sources == null ? 0 : sources.size());

        ResearchGate.track(arcaneRecipes, crucibleRecipes, infusionRecipes, blueprints);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        ResearchGate.attachRuntime(jeiRuntime);
    }

    @Override
    public void registerRecipeTransferHandlers(@NotNull IRecipeTransferRegistration registration) {
        var menuType = arcaneresearch.init.ModMenuTypes.ARCANE_WORKBENCH.get();
        registration.addRecipeTransferHandler(
                arcaneresearch.common.menu.ArcaneWorkbenchMenu.class,
                menuType,
                ARCANE_TYPE, 1, 9, 16, 36);
    }

    @Override
    public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {

        registration.addGuiContainerHandler(arcaneresearch.client.gui.screens.ResearchTableScreen.class,
                new arcaneresearch.compat.jei.gui.ResearchGuiHandlers.ResearchTableHandler());
        registration.addGuiContainerHandler(arcaneresearch.client.gui.screens.FocalManipulatorScreen.class,
                new arcaneresearch.compat.jei.gui.ResearchGuiHandlers.FocalManipulatorHandler());
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        addCatalystSafe(registration, ModBlocks.ARCANE_WORKBENCH, ARCANE_TYPE);
        addCatalystSafe(registration, ModBlocks.CRUCIBLE, CRUCIBLE_TYPE);
        addCatalystSafe(registration, ModBlocks.INFUSION_MATRIX, INFUSION_TYPE);
        addCatalystSafe(registration, arcaneresearch.init.ModItems.SALIS_MUNDUS, MULTIBLOCK_TYPE);
        addCatalystSafe(registration, ModItems.THAUMONOMICON, ASPECT_COMPOUND_TYPE);
        addCatalystSafe(registration, ModItems.THAUMONOMICON, ASPECT_SOURCES_TYPE);
    }

    private static void addCatalystSafe(IRecipeCatalystRegistration reg, Object holder, RecipeType<?> type) {
        if (reg == null || holder == null || type == null) {
            return;
        }
        try {
            Object value = null;
            if (holder instanceof java.util.function.Supplier<?> supplier) {
                value = supplier.get();
            } else {
                value = holder;
            }
            if (value instanceof net.minecraft.world.level.ItemLike item) {
                reg.addRecipeCatalyst(new ItemStack(item), type);
            }
        } catch (Exception ignored) {
        }
    }
}
