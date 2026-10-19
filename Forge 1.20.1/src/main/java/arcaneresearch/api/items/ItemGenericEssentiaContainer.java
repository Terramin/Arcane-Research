package arcaneresearch.api.items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Items;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IEssentiaContainerItem;
public class ItemGenericEssentiaContainer extends Item implements IEssentiaContainerItem {
    protected final int baseAmount;
    protected int base;
    public ItemGenericEssentiaContainer(int baseAmount, Properties properties) {
        super(properties);
        this.baseAmount = baseAmount;
        this.base = baseAmount;
    }
    public ItemGenericEssentiaContainer(int base) {
        this(base, new Properties().stacksTo(64));
    }
    public void addFilledVariants(java.util.function.Consumer<ItemStack> output) {
        for (Aspect tag : Aspect.aspects.values()) {
            ItemStack stack = new ItemStack(this);
            setAspects(stack, new AspectList().add(tag, this.base));
            output.accept(stack);
        }
    }
    @Override
    public AspectList getAspects(ItemStack itemstack) {
        if (itemstack.hasTag()) {
            AspectList aspects = new AspectList();
            aspects.readFromNBT(itemstack.getTag());
            return aspects.size() > 0 ? aspects : null;
        }
        return null;
    }
    @Override
    public void setAspects(ItemStack itemstack, AspectList aspects) {
        if (!itemstack.hasTag()) {
            itemstack.setTag(new CompoundTag());
        }
        aspects.writeToNBT(itemstack.getTag());
    }
    @Override
    public boolean ignoreContainedAspects() {
        return false;
    }
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && !stack.hasTag()) {
            Aspect[] displayAspects = Aspect.aspects.values().toArray(new Aspect[0]);
            setAspects(stack, new AspectList().add(displayAspects[level.random.nextInt(displayAspects.length)], baseAmount));
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }
    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide && !stack.hasTag()) {
            Aspect[] displayAspects = Aspect.aspects.values().toArray(new Aspect[0]);
            setAspects(stack, new AspectList().add(displayAspects[level.random.nextInt(displayAspects.length)], baseAmount));
        }
        super.onCraftedBy(stack, level, player);
    }
}