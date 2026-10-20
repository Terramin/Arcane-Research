package arcaneresearch.common.menu.slot;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.CommonHooks;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.api.crafting.IArcaneWorkbench;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import arcaneresearch.common.menu.ArcaneWorkbenchMenu;
import arcaneresearch.common.tiles.crafting.TileArcaneWorkbench;

public class ArcaneWorkbenchResultSlot extends Slot {

    private final CraftingContainer craftMatrix;
    private final Player player;
    private final TileArcaneWorkbench tile;
    private int amountCrafted;

    public ArcaneWorkbenchResultSlot(TileArcaneWorkbench tile, Player player,
                                     CraftingContainer craftMatrix, Container resultContainer, int slot, int x, int y) {
        super(resultContainer, slot, x, y);
        this.tile = tile;
        this.player = player;
        this.craftMatrix = craftMatrix;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {

        return false;
    }

    @Override
    public ItemStack remove(int amount) {
        if (hasItem()) {
            amountCrafted += Math.min(amount, getItem().getCount());
        }
        return super.remove(amount);
    }

    @Override
    protected void onQuickCraft(ItemStack stack, int amount) {
        amountCrafted += amount;
        checkTakeAchievements(stack);
    }

    @Override
    protected void onSwapCraft(int numItems) {
        amountCrafted += numItems;
    }

    @Override
    protected void checkTakeAchievements(ItemStack stack) {
        if (amountCrafted > 0) {
            stack.onCraftedBy(player, amountCrafted);
            net.neoforged.neoforge.event.EventHooks.firePlayerCraftingEvent(player, stack, craftMatrix);
        }

        Container container = this.container;
        // NEOFORGE_PORT: ResultContainer exposes the used recipe via RecipeCraftingHolder
        // (holders, not values); awardRecipes takes Collection<RecipeHolder<?>>.
        if (container instanceof net.minecraft.world.inventory.RecipeCraftingHolder recipeHolder) {
            var recipe = recipeHolder.getRecipeUsed();
            if (recipe != null && !recipe.value().isSpecial()) {
                player.awardRecipes(java.util.Collections.singleton(recipe));
                recipeHolder.setRecipeUsed(null);
            }
        }

        amountCrafted = 0;
    }

    @Override
    public void onTake(Player thePlayer, ItemStack stack) {
        checkTakeAchievements(stack);

        IArcaneRecipe arcaneRecipe = ArcaneresearchCraftingManager.findMatchingArcaneRecipe(craftMatrix, thePlayer);

        CommonHooks.setCraftingPlayer(thePlayer);

        // NEOFORGE_PORT: IArcaneRecipe#getRemainingItems returns List<ItemStack> in
        // 26.x; vanilla getRecipeFor returns Optional<RecipeHolder<CraftingRecipe>>
        // (holder access via .value()) and takes CraftingInput, not CraftingContainer.
        java.util.List<ItemStack> remainingItems;
        if (arcaneRecipe != null && craftMatrix instanceof IArcaneWorkbench workbench) {

            remainingItems = arcaneRecipe.getRemainingItems(workbench);

            int visCost = ArcaneWorkbenchMenu.getDiscountedVisCost(arcaneRecipe, thePlayer);
            if (visCost > 0) {
                tile.updateAura();
                tile.spendAura(visCost);
            }

            AspectList crystals = arcaneRecipe.getCrystals();
            if (crystals != null && crystals.size() > 0) {
                consumeCrystals(crystals);
            }
        } else if (arcaneRecipe != null) {

            remainingItems = NonNullList.withSize(craftMatrix.getContainerSize(), ItemStack.EMPTY);
        } else {

            var level = thePlayer.level();
            var gridCopy = ArcaneWorkbenchMenu.gridOnlyCopy(
                    (AbstractContainerMenu) thePlayer.containerMenu, craftMatrix);
            var gridInput = gridCopy.asCraftInput();
            // NEOFORGE_PORT: Level.getRecipeManager removed in 26.3 -> server RecipeManager.
            var recipeOpt = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,
                    gridInput, level);

            if (recipeOpt.isPresent()) {
                remainingItems = recipeOpt.get().value().getRemainingItems(gridInput);
            } else {
                remainingItems = NonNullList.withSize(craftMatrix.getContainerSize(), ItemStack.EMPTY);
            }
        }

        CommonHooks.setCraftingPlayer(null);

        for (int i = 0; i < Math.min(9, remainingItems.size()); i++) {
            ItemStack slotStack = craftMatrix.getItem(i);
            ItemStack remaining = remainingItems.get(i);

            if (!slotStack.isEmpty()) {
                craftMatrix.removeItem(i, 1);
                slotStack = craftMatrix.getItem(i);
            }

            if (!remaining.isEmpty()) {
                if (slotStack.isEmpty()) {
                    craftMatrix.setItem(i, remaining);
                } else if (ItemStack.isSameItemSameComponents(slotStack, remaining)) {
                    remaining.grow(slotStack.getCount());
                    craftMatrix.setItem(i, remaining);
                } else if (!player.getInventory().add(remaining)) {
                    player.drop(remaining, false, net.minecraft.util.Prediction.SERVER_ONLY);
                }
            }
        }
    }

    private void consumeCrystals(AspectList crystals) {
        for (Aspect aspect : crystals.getAspects()) {
            int required = crystals.getAmount(aspect);
            ItemStack targetCrystal = ArcaneresearchApiHelper.makeCrystal(aspect, required);

            for (int slot = 9; slot < 15; slot++) {
                ItemStack slotStack = craftMatrix.getItem(slot);
                if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(targetCrystal, slotStack)) {
                    int toRemove = Math.min(required, slotStack.getCount());
                    craftMatrix.removeItem(slot, toRemove);
                    required -= toRemove;
                    if (required <= 0) break;
                }
            }
        }
    }
}
