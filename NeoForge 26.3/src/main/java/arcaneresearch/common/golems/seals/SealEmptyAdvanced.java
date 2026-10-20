package arcaneresearch.common.golems.seals;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.seals.ISealConfigToggles;

import java.util.ArrayList;

public class SealEmptyAdvanced extends SealEmpty implements ISealConfigToggles {

    private Identifier iconAdvanced;

    public SealEmptyAdvanced() {
        super();
        iconAdvanced = Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "item/seals/seal_empty_advanced");
    }

    @Override
    public String getKey() {
        return "arcaneresearch:empty_advanced";
    }

    @Override
    public int getFilterSize() {
        return 9;
    }

    @Override
    public NonNullList<ItemStack> getInv(int c) {
        if (getToggles()[4].getValue() && !isBlacklist()) {
            ArrayList<ItemStack> w = new ArrayList<>();
            for (ItemStack s : super.getInv()) {
                if (s != null && !s.isEmpty()) {
                    w.add(s);
                }
            }
            if (!w.isEmpty()) {
                int i = Math.abs(c % w.size());
                NonNullList<ItemStack> single = NonNullList.withSize(1, ItemStack.EMPTY);
                single.set(0, w.get(i));
                return single;
            }
        }
        return super.getInv();
    }

    @Override
    public NonNullList<ItemStack> getInv() {
        return super.getInv();
    }

    @Override
    public Identifier getSealIcon() {
        return iconAdvanced;
    }

    @Override
    public int[] getGuiCategories() {
        return new int[]{1, 3, 0, 4};
    }

    @Override
    public SealToggle[] getToggles() {
        return props;
    }

    @Override
    public void setToggle(int index, boolean value) {
        if (index >= 0 && index < props.length) {
            props[index].setValue(value);
        }
    }

    @Override
    public EnumGolemTrait[] getRequiredTags() {
        return new EnumGolemTrait[]{EnumGolemTrait.SMART};
    }
}
