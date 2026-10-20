package arcaneresearch.api.wands;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;

public interface IWand {
    int getMaxVis(ItemStack stack);

    int getVis(ItemStack stack, Aspect aspect);

    AspectList getAllVis(ItemStack stack);

    AspectList getAspectsWithRoom(ItemStack stack);

    float getConsumptionModifier(ItemStack stack, Player player, Aspect aspect, boolean crafting);

    boolean consumeVis(ItemStack stack, Player player, Aspect aspect, int amount, boolean simulate);

    boolean consumeAllVis(ItemStack stack, Player player, AspectList cost, boolean simulate, boolean crafting);

    int addVis(ItemStack stack, Aspect aspect, int amount, boolean simulate);

    ItemFocusBasic getFocus(ItemStack stack);

    ItemStack getFocusStack(ItemStack stack);

    void setFocus(ItemStack wand, ItemStack focus);

    WandRod getRod(ItemStack stack);

    boolean isStaff(ItemStack stack);

    boolean isSceptre(ItemStack stack);

    void setRod(ItemStack stack, WandRod rod);

    WandCap getCap(ItemStack stack);

    void setCap(ItemStack stack, WandCap cap);

    int getFocusPotency(ItemStack stack);

    int getFocusTreasure(ItemStack stack);

    int getFocusFrugal(ItemStack stack);

    int getFocusEnlarge(ItemStack stack);

    int getFocusExtend(ItemStack stack);
}
