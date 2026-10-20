package arcaneresearch.common.container;

import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class InventoryFake extends SimpleContainer {
    public InventoryFake(int size) {
        super(size);
    }

    public InventoryFake(NonNullList<ItemStack> inv) {
        super(inv.size());
        for (int i = 0; i < inv.size(); i++) {
            setItem(i, inv.get(i));
        }
    }

    public InventoryFake(ItemStack... stacks) {
        super(stacks.length);
        for (int i = 0; i < stacks.length; i++) {
            setItem(i, stacks[i]);
        }
    }

    public InventoryFake(ArrayList<ItemStack> inv) {
        this((List<ItemStack>) inv);
    }

    public InventoryFake(List<ItemStack> inv) {
        super(inv.size());
        for (int i = 0; i < inv.size(); i++) {
            setItem(i, inv.get(i));
        }
    }
}
