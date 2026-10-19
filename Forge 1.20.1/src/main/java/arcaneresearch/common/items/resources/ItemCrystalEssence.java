package arcaneresearch.common.items.resources;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IEssentiaContainerItem;
import javax.annotation.Nullable;
public class ItemCrystalEssence extends Item implements IEssentiaContainerItem {
    protected final int baseAmount;
    public ItemCrystalEssence() {
        super(new Item.Properties().stacksTo(64));
        this.baseAmount = 1;
    }
    public ItemCrystalEssence(int baseAmount) {
        super(new Item.Properties().stacksTo(64));
        this.baseAmount = baseAmount;
    }
    public static ItemStack createStack(Item item, Aspect aspect, int count) {
        ItemStack stack = new ItemStack(item, count);
        if (item instanceof ItemCrystalEssence crystalItem) {
            crystalItem.setAspects(stack, new AspectList().add(aspect, crystalItem.baseAmount));
        }
        return stack;
    }
    @Nullable
    public static Aspect getAspect(ItemStack stack) {
        if (stack.getItem() instanceof ItemCrystalEssence crystalItem) {
            AspectList aspects = crystalItem.getAspects(stack);
            if (aspects != null && aspects.size() > 0) {
                return aspects.getAspects()[0];
            }
        }
        return null;
    }
    public static boolean isSameAspect(ItemStack stack1, ItemStack stack2) {
        Aspect a1 = getAspect(stack1);
        Aspect a2 = getAspect(stack2);
        if (a1 == null || a2 == null) return false;
        return a1.equals(a2);
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
    @Override
    public Component getName(ItemStack stack) {
        AspectList aspects = getAspects(stack);
        if (aspects != null && aspects.size() > 0) {
            Aspect aspect = aspects.getAspects()[0];
            return Component.translatable(this.getDescriptionId(stack), aspect.getName());
        }
        return super.getName(stack);
    }
}
