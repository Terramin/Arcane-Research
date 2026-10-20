package arcaneresearch.api.golems.seals;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface ISealConfigFilter {

    NonNullList<ItemStack> getInv();

    NonNullList<Integer> getSizes();

    int getFilterSize();

    ItemStack getFilterSlot(int index);

    int getFilterSlotSize(int index);

    void setFilterSlot(int index, ItemStack stack);

    void setFilterSlotSize(int index, int size);

    boolean isBlacklist();

    void setBlacklist(boolean blacklist);

    boolean hasStacksizeLimiters();
}
