package arcaneresearch.common.menu.slot;

import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.api.crafting.IArcaneWorkbench;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import arcaneresearch.common.menu.ArcaneWorkbenchMenu;
import arcaneresearch.common.tiles.crafting.TileArcaneWorkbench;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class ArcaneWorkbenchResultSlot extends Slot {

    private final CraftingContainer craftMatrix;
    private final Player player;
    private final TileArcaneWorkbench tile;
    private int amountCrafted;

    public ArcaneWorkbenchResultSlot(TileArcaneWorkbench tile, Player player,
                                     CraftingContainer craftMatrix, Container resultContainer,
                                     int slot, int x, int y) {
        super(resultContainer, slot, x, y);
        this.tile = tile;
        this.player = player;
        this.craftMatrix = craftMatrix;
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public @NotNull ItemStack remove(int amount) {
        if (amount <= 0) return ItemStack.EMPTY;
        if (hasItem()) {
            amountCrafted += Math.min(amount, getItem().getCount());
        }
        return super.remove(amount);
    }

    @Override
    protected void onQuickCraft(@NotNull ItemStack stack, int amount) {
        if (amount > 0) amountCrafted += amount;
        checkTakeAchievements(stack);
    }

    @Override
    protected void onSwapCraft(int numItems) {
        if (numItems > 0) amountCrafted += numItems;
    }

    @Override
    protected void checkTakeAchievements(@NotNull ItemStack stack) {
        if (amountCrafted > 0) {
            Level level = player.level();
            stack.onCraftedBy(level, player, amountCrafted);
            ForgeEventFactory.firePlayerCraftingEvent(player, stack, craftMatrix);
        }

        Container container = this.container;
        if (container instanceof RecipeHolder recipeHolder) {
            var recipe = recipeHolder.getRecipeUsed();
            if (recipe != null && !recipe.isSpecial()) {
                player.awardRecipes(Collections.singleton(recipe));
                recipeHolder.setRecipeUsed(null);
            }
        }
        amountCrafted = 0;
    }

    @Override
    public void onTake(@NotNull Player thePlayer, @NotNull ItemStack stack) {
        checkTakeAchievements(stack);

        IArcaneRecipe arcaneRecipe = ArcaneresearchCraftingManager.findMatchingArcaneRecipe(craftMatrix, thePlayer);
        final int matrixSize = craftMatrix.getContainerSize();

        NonNullList<ItemStack> remainingItems;
        ForgeHooks.setCraftingPlayer(thePlayer);
        try {
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
                remainingItems = NonNullList.withSize(matrixSize, ItemStack.EMPTY);
            } else {
                Level level = thePlayer.level();
                var gridCopy = ArcaneWorkbenchMenu.gridOnlyCopy(thePlayer.containerMenu, craftMatrix);
                var recipeOpt = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, gridCopy, level);
                if (recipeOpt.isPresent()) {
                    CraftingRecipe vanillaRecipe = recipeOpt.get();
                    remainingItems = vanillaRecipe.getRemainingItems(gridCopy);
                } else {
                    remainingItems = NonNullList.withSize(matrixSize, ItemStack.EMPTY);
                }
            }
        } finally {
            ForgeHooks.setCraftingPlayer(null);
        }

        if (remainingItems == null) {
            remainingItems = NonNullList.withSize(matrixSize, ItemStack.EMPTY);
        }

        final int limit = Math.min(9, Math.min(craftMatrix.getContainerSize(), remainingItems.size()));
        for (int i = 0; i < limit; i++) {
            ItemStack slotStack = craftMatrix.getItem(i);
            if (slotStack == null) slotStack = ItemStack.EMPTY;

            ItemStack remaining = remainingItems.get(i);
            if (remaining == null) remaining = ItemStack.EMPTY;

            if (!slotStack.isEmpty()) {
                craftMatrix.removeItem(i, 1);
                slotStack = craftMatrix.getItem(i);
                if (slotStack == null) slotStack = ItemStack.EMPTY;
            }

            if (remaining.isEmpty()) continue;

            if (slotStack.isEmpty()) {
                craftMatrix.setItem(i, remaining);
            } else if (ItemStack.isSameItemSameTags(slotStack, remaining)) {
                remaining.grow(slotStack.getCount());
                craftMatrix.setItem(i, remaining);
            } else if (!player.getInventory().add(remaining)) {
                player.drop(remaining, false);
            }
        }
    }

    private void consumeCrystals(AspectList crystals) {
        for (Aspect aspect : crystals.getAspects()) {
            if (aspect == null) continue;
            int required = crystals.getAmount(aspect);
            if (required <= 0) continue;

            ItemStack targetCrystal = ArcaneresearchApiHelper.makeCrystal(aspect, required);
            if (targetCrystal == null || targetCrystal.isEmpty()) continue;

            for (int slot = 9; slot < 15; slot++) {
                ItemStack slotStack = craftMatrix.getItem(slot);
                if (slotStack == null || slotStack.isEmpty()) continue;
                if (!ItemStack.isSameItemSameTags(targetCrystal, slotStack)) continue;

                int toRemove = Math.min(required, slotStack.getCount());
                craftMatrix.removeItem(slot, toRemove);
                required -= toRemove;
                if (required <= 0) break;
            }
        }
    }
}