package arcaneresearch.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
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

    // NEOFORGE_PORT (26.3): JEI 15 defaulted IRecipeCategory.getRegistryName(recipe) to
    // Recipe.getId() (output-slot "Recipe ID" tooltip line). 26.x recipe objects no longer
    // carry their key and JEI only reads RecipeHolder ids, so the holder keys are kept here
    // and the arcane/crucible/infusion categories return them from getIdentifier.
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
    public void registerIngredients(IModIngredientRegistration registration) {
        // NEOFORGE_PORT: JEI 26.3 register requires Codec<V> (5th arg); AspectList.CODEC added for parity.
        registration.register(AspectIngredient.TYPE, AspectIngredient.createAll(),
                new AspectIngredient.Helper(), new AspectIngredientRenderer(),
                arcaneresearch.api.aspects.AspectList.CODEC);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
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
    public void registerRecipes(IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) {
            return;
        }

        // NEOFORGE_PORT: client levels expose no recipe query API in 26.3
        // (Level.getRecipeManager + RecipeManager.recipeMap both gone; ClientLevel
        // .recipeAccess() is RecipeAccess without getRecipes). JEI runs on the client,
        // so recipes come from ClientRecipeCache (RecipesReceivedEvent RecipeMap).
        java.util.Collection<net.minecraft.world.item.crafting.RecipeHolder<?>> holders =
                arcaneresearch.common.lib.crafting.ClientRecipeCache.values();

        java.util.Map<Object, Identifier> ids = new java.util.IdentityHashMap<>();
        for (net.minecraft.world.item.crafting.RecipeHolder<?> holder : holders) {
            ids.put(holder.value(), holder.id().identifier());
        }
        recipeIds = ids;

        List<IArcaneRecipe> arcaneRecipes = new java.util.ArrayList<>(holders
                .stream()
                .map(net.minecraft.world.item.crafting.RecipeHolder::value)
                .filter(IArcaneRecipe.class::isInstance)
                .map(IArcaneRecipe.class::cast)
                .toList());

        try {
            arcaneRecipes.addAll(arcaneresearch.common.lib.crafting.ArcaneWandDynamicRecipe.allPairs());
        } catch (Exception e) {
            Arcaneresearch.LOGGER.warn("JEI: failed to list wand recipes", e);
        }

        registration.addRecipes(ARCANE_TYPE, arcaneRecipes);

        List<CrucibleRecipeType> crucibleRecipes = holders
                .stream()
                .map(net.minecraft.world.item.crafting.RecipeHolder::value)
                .filter(CrucibleRecipeType.class::isInstance)
                .map(CrucibleRecipeType.class::cast)
                .toList();

        registration.addRecipes(CRUCIBLE_TYPE, crucibleRecipes);

        List<InfusionRecipeType> infusionRecipes = holders
                .stream()
                .map(net.minecraft.world.item.crafting.RecipeHolder::value)
                .filter(InfusionRecipeType.class::isInstance)
                .map(InfusionRecipeType.class::cast)
                .toList();

        registration.addRecipes(INFUSION_TYPE, infusionRecipes);

        List<ArcaneresearchApi.BluePrint> blueprints = ArcaneresearchApi.getCraftingRecipes().values().stream()
                .filter(r -> r instanceof ArcaneresearchApi.BluePrint)
                .map(r -> (ArcaneresearchApi.BluePrint) r)
                .toList();

        registration.addRecipes(MULTIBLOCK_TYPE, blueprints);

        registration.addRecipes(ASPECT_COMPOUND_TYPE, AspectCompoundCategory.recipes());

        List<AspectSourcesCategory.Sources> sources = AspectSourcesCategory.buildIndex();
        registration.addRecipes(ASPECT_SOURCES_TYPE, sources);

        Arcaneresearch.LOGGER.info("JEI: Registered {} arcane, {} crucible, {} infusion, {} multiblock, {} aspects",
                arcaneRecipes.size(), crucibleRecipes.size(), infusionRecipes.size(), blueprints.size(), sources.size());

        ResearchGate.track(arcaneRecipes, crucibleRecipes, infusionRecipes, blueprints);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        ResearchGate.attachRuntime(jeiRuntime);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {

        registration.addRecipeTransferHandler(
                arcaneresearch.common.menu.ArcaneWorkbenchMenu.class,
                arcaneresearch.init.ModMenuTypes.ARCANE_WORKBENCH.get(),
                ARCANE_TYPE, 1, 9, 16, 36);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {

        registration.addGuiContainerHandler(arcaneresearch.client.gui.screens.ResearchTableScreen.class,
                new arcaneresearch.compat.jei.gui.ResearchGuiHandlers.ResearchTableHandler());
        registration.addGuiContainerHandler(arcaneresearch.client.gui.screens.FocalManipulatorScreen.class,
                new arcaneresearch.compat.jei.gui.ResearchGuiHandlers.FocalManipulatorHandler());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ARCANE_WORKBENCH.get()), ARCANE_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CRUCIBLE.get()), CRUCIBLE_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.INFUSION_MATRIX.get()), INFUSION_TYPE);

        registration.addRecipeCatalyst(new ItemStack(arcaneresearch.init.ModItems.SALIS_MUNDUS.get()), MULTIBLOCK_TYPE);

        registration.addRecipeCatalyst(new ItemStack(ModItems.THAUMONOMICON.get()), ASPECT_COMPOUND_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.THAUMONOMICON.get()), ASPECT_SOURCES_TYPE);
    }
}
