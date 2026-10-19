package arcaneresearch.common.items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IEssentiaContainerItem;
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
    public AspectList getAspects(ItemStack stack) {
        if (stack.hasTag()) {
            AspectList aspects = new AspectList();
            aspects.readFromNBT(stack.getTag());
            return aspects.size() > 0 ? aspects : null;
        }
        return null;
    }
    @Override
    public void setAspects(ItemStack stack, AspectList aspects) {
        if (!stack.hasTag()) {
            stack.setTag(new CompoundTag());
        }
        aspects.writeToNBT(stack.getTag());
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
