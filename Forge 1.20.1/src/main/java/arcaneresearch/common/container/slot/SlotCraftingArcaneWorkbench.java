package arcaneresearch.common.container.slot;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.crafting.IArcaneRecipe;
import arcaneresearch.api.crafting.IArcaneWorkbench;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import arcaneresearch.common.menu.ArcaneWorkbenchMenu;
import arcaneresearch.common.tiles.crafting.TileArcaneWorkbench;
@Deprecated
public class SlotCraftingArcaneWorkbench extends Slot {
    protected CraftingContainer craftMatrix;
    protected Player player;
    protected int amountCrafted;
    protected TileArcaneWorkbench tile;
    public SlotCraftingArcaneWorkbench(TileArcaneWorkbench tile, Player player,
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
            stack.onCraftedBy(player.level(), player, amountCrafted);
            net.minecraftforge.event.ForgeEventFactory.firePlayerCraftingEvent(player, stack, craftMatrix);
        }
        Container result = this.container;
        if (result instanceof RecipeHolder recipeHolder) {
            var recipe = recipeHolder.getRecipeUsed();
            if (recipe != null && !recipe.isSpecial()) {
                player.awardRecipes(java.util.Collections.singleton(recipe));
                recipeHolder.setRecipeUsed(null);
            }
        }
        amountCrafted = 0;
    }
    @Override
    public void onTake(Player thePlayer, ItemStack stack) {
        checkTakeAchievements(stack);
        IArcaneRecipe recipe = ArcaneresearchCraftingManager.findMatchingArcaneRecipe(craftMatrix, thePlayer);
        ForgeHooks.setCraftingPlayer(thePlayer);
        NonNullList<ItemStack> remaining;
        if (recipe != null && craftMatrix instanceof IArcaneWorkbench workbench) {
            remaining = recipe.getRemainingItems(workbench);
            int vis = ArcaneWorkbenchMenu.getDiscountedVisCost(recipe, thePlayer);
            if (vis > 0) {
                tile.updateAura();
                tile.spendAura(vis);
            }
            AspectList crystals = recipe.getCrystals();
            if (crystals != null && crystals.size() > 0) {
                consumeCrystals(crystals);
            }
        } else {
            var level = thePlayer.level();
            var gridCopy = ArcaneWorkbenchMenu.gridOnlyCopy(
                    (AbstractContainerMenu) thePlayer.containerMenu, craftMatrix);
            var recipeOpt = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, gridCopy, level);
            if (recipeOpt.isPresent()) {
                CraftingRecipe vanillaRecipe = recipeOpt.get();
                remaining = vanillaRecipe.getRemainingItems(gridCopy);
            } else {
                remaining = NonNullList.withSize(craftMatrix.getContainerSize(), ItemStack.EMPTY);
            }
        }
        ForgeHooks.setCraftingPlayer(null);
        for (int i = 0; i < Math.min(9, remaining.size()); ++i) {
            ItemStack slotStack = craftMatrix.getItem(i);
            ItemStack leftover = remaining.get(i);
            if (!slotStack.isEmpty()) {
                craftMatrix.removeItem(i, 1);
                slotStack = craftMatrix.getItem(i);
            }
            if (!leftover.isEmpty()) {
                if (slotStack.isEmpty()) {
                    craftMatrix.setItem(i, leftover);
                } else if (ItemStack.isSameItemSameTags(slotStack, leftover)) {
                    leftover.grow(slotStack.getCount());
                    craftMatrix.setItem(i, leftover);
                } else if (!player.getInventory().add(leftover)) {
                    player.drop(leftover, false);
                }
            }
        }
    }
    protected void consumeCrystals(AspectList crystals) {
        for (Aspect aspect : crystals.getAspects()) {
            int required = crystals.getAmount(aspect);
            ItemStack wanted = ArcaneresearchApiHelper.makeCrystal(aspect, required);
            for (int slot = 9; slot < 15; ++slot) {
                ItemStack stack = craftMatrix.getItem(slot);
                if (!stack.isEmpty() && ItemStack.isSameItemSameTags(wanted, stack)) {
                    int toRemove = Math.min(required, stack.getCount());
                    craftMatrix.removeItem(slot, toRemove);
                    required -= toRemove;
                    if (required <= 0) {
                        break;
                    }
                }
            }
        }
    }
}
