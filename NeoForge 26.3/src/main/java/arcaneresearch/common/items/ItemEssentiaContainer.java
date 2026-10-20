package arcaneresearch.common.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IEssentiaContainerItem;

public class ItemEssentiaContainer extends ItemTC implements IEssentiaContainerItem {

    protected final int capacity;

    // NEOFORGE_PORT: bare `Properties` -> Item.Properties (26.x explicit nesting).
    public ItemEssentiaContainer(Item.Properties properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    public ItemEssentiaContainer(int capacity) {
        super(new Item.Properties().stacksTo(64));
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public AspectList getAspects(ItemStack stack) {
        // NEOFORGE_PORT: stack root-tag storage -> DataComponents.CUSTOM_DATA
        // (same NBT shape; CustomData.of/copyTag round-trip).
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            AspectList aspects = new AspectList();
            aspects.readFromNBT(data.copyTag());
            return aspects.size() > 0 ? aspects : null;
        }
        return null;
    }

    @Override
    public void setAspects(ItemStack stack, AspectList aspects) {
        // NEOFORGE_PORT (26.3): Forge wrote into the EXISTING root tag (other keys kept);
        // CustomData.update merges the same way instead of replacing the whole component.
        CustomData.update(DataComponents.CUSTOM_DATA, stack, aspects::writeToNBT);
    }

    @Override
    public boolean ignoreContainedAspects() {
        return false;
    }

    public boolean isEmpty(ItemStack stack) {
        AspectList aspects = getAspects(stack);
        return aspects == null || aspects.size() == 0;
    }

    public Aspect getAspect(ItemStack stack) {
        AspectList aspects = getAspects(stack);
        if (aspects != null && aspects.size() > 0) {
            return aspects.getAspects()[0];
        }
        return null;
    }

    public int getAmount(ItemStack stack) {
        AspectList aspects = getAspects(stack);
        if (aspects != null && aspects.size() > 0) {
            Aspect aspect = aspects.getAspects()[0];
            return aspects.getAmount(aspect);
        }
        return 0;
    }

    public ItemStack createFilled(Aspect aspect, int amount) {
        ItemStack stack = new ItemStack(this);
        setAspects(stack, new AspectList().add(aspect, amount));
        return stack;
    }

    public ItemStack createFull(Aspect aspect) {
        return createFilled(aspect, capacity);
    }
}
