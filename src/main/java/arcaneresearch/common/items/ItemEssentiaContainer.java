package arcaneresearch.common.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IEssentiaContainerItem;
import org.jetbrains.annotations.NotNull;

public class ItemEssentiaContainer extends ItemTC implements IEssentiaContainerItem {
    protected final int capacity;

    public ItemEssentiaContainer(Properties properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    public ItemEssentiaContainer(int capacity) {
        super(new Properties().stacksTo(64));
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public AspectList getAspects(@NotNull ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return null;
        }
        AspectList aspects = new AspectList();
        aspects.readFromNBT(tag);
        return aspects.size() > 0 ? aspects : null;
    }

    @Override
    public void setAspects(ItemStack stack, AspectList aspects) {
        if (stack == null || stack.isEmpty() || aspects == null) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        aspects.writeToNBT(tag);
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
            Aspect[] arr = aspects.getAspects();
            if (arr != null && arr.length > 0) {
                return arr[0];
            }
        }
        return null;
    }

    public int getAmount(ItemStack stack) {
        AspectList aspects = getAspects(stack);
        if (aspects != null && aspects.size() > 0) {
            Aspect[] arr = aspects.getAspects();
            if (arr != null && arr.length > 0) {
                Aspect aspect = arr[0];
                if (aspect != null) {
                    return aspects.getAmount(aspect);
                }
            }
        }
        return 0;
    }

    public ItemStack createFilled(Aspect aspect, int amount) {
        if (aspect == null) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(this);
        setAspects(stack, new AspectList().add(aspect, amount));
        return stack;
    }

    public ItemStack createFull(Aspect aspect) {
        if (aspect == null) {
            return ItemStack.EMPTY;
        }
        return createFilled(aspect, capacity);
    }
}