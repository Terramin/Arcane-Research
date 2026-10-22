package arcaneresearch.api.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
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
        
        CustomData data = itemstack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            AspectList aspects = new AspectList();
            aspects.readFromNBT(data.copyTag());
            return aspects.size() > 0 ? aspects : null;
        }
        return null;
    }

    @Override
    public void setAspects(ItemStack itemstack, AspectList aspects) {
        
        
        CustomData.update(DataComponents.CUSTOM_DATA, itemstack, tag -> aspects.writeToNBT(tag));
    }

    @Override
    public boolean ignoreContainedAspects() {
        return false;
    }

    
    
    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.Entity entity, net.minecraft.world.entity.EquipmentSlot slot) {
        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            Aspect[] displayAspects = Aspect.aspects.values().toArray(new Aspect[0]);
            setAspects(stack, new AspectList().add(displayAspects[level.getRandom().nextInt(displayAspects.length)], baseAmount));
        }
        super.inventoryTick(stack, level, entity, slot);
    }

    
    @Override
    public void onCraftedBy(ItemStack stack, Player player) {
        Level level = player.level();
        if (!level.isClientSide() && !stack.has(DataComponents.CUSTOM_DATA)) {
            Aspect[] displayAspects = Aspect.aspects.values().toArray(new Aspect[0]);
            setAspects(stack, new AspectList().add(displayAspects[level.getRandom().nextInt(displayAspects.length)], baseAmount));
        }
        super.onCraftedBy(stack, player);
    }
}