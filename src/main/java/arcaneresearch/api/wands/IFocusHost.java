package arcaneresearch.api.wands;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;

public interface IFocusHost {
    int getMaxVis(ItemStack stack);

    /**
     * Capacity in hundredths of a vis — the unit used by the internal vis tags.
     * Wands keep the 1.5x sceptre bonus here so no half point is lost to early
     * truncation; casters and other hosts can keep using the integer default.
     */
    default int getMaxVisHundredths(ItemStack stack) {
        return getMaxVis(stack) * 100;
    }

    int getVis(ItemStack stack, Aspect aspect);

    AspectList getAllVis(ItemStack stack);

    AspectList getAspectsWithRoom(ItemStack stack);

    float getConsumptionModifier(ItemStack stack, Player player, Aspect aspect, boolean crafting);

    /**
     * The last flag means "simulate": check affordability without mutating the
     * stack. It is not a crafting-discount switch — pass {@code false} to
     * {@link #getConsumptionModifier} for that.
     */
    boolean consumeVis(ItemStack stack, Player player, Aspect aspect, int amount, boolean simulate);

    /**
     * @param simulate check only, never write NBT
     * @param crafting suppress focus/staff bonuses when computing the modifier
     */
    boolean consumeAllVis(ItemStack stack, Player player, AspectList cost, boolean simulate, boolean crafting);

    int addVis(ItemStack stack, Aspect aspect, int amount, boolean simulate);

    FocusItem getFocus(ItemStack stack);

    ItemStack getFocusStack(ItemStack stack);

    void setFocus(ItemStack wand, ItemStack focus);

    default ItemStack getPickedBlock(ItemStack stack) {
        return ItemStack.EMPTY;
    }

    default boolean isCaster() {
        return false;
    }

    int getFocusPotency(ItemStack stack);

    int getFocusTreasure(ItemStack stack);

    int getFocusFrugal(ItemStack stack);

    int getFocusEnlarge(ItemStack stack);

    int getFocusExtend(ItemStack stack);
}
