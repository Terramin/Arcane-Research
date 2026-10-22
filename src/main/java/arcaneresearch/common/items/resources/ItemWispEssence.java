package arcaneresearch.common.items.resources;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IEssentiaContainerItem;

import javax.annotation.Nullable;
import java.util.List;

 








 
public class ItemWispEssence extends Item implements IEssentiaContainerItem {

      
    public static final int WISP_AMOUNT = 2;

    
    
    public ItemWispEssence(Item.Properties props) {
        super(props.stacksTo(64));
    }

    public ItemWispEssence() {
        this(new Item.Properties());
    }

      
    public static ItemStack createStack(Item item, Aspect aspect, int count) {
        ItemStack stack = new ItemStack(item, count);
        if (item instanceof ItemWispEssence wisp) {
            wisp.setAspects(stack, new AspectList().add(aspect, WISP_AMOUNT));
        }
        return stack;
    }

    @Nullable
    public static Aspect getAspect(ItemStack stack) {
        if (stack.getItem() instanceof ItemWispEssence wisp) {
            AspectList aspects = wisp.getAspects(stack);
            if (aspects != null && aspects.size() > 0) {
                return aspects.getAspects()[0];
            }
        }
        return null;
    }

    @Override
    public Component getName(ItemStack stack) {
        AspectList aspects = getAspects(stack);
        if (aspects != null && aspects.size() > 0) {
            Aspect aspect = aspects.getAspects()[0];
            return Component.translatable(this.getDescriptionId(), aspect.getName());
        }
        return super.getName(stack);
    }

    
    
    
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        AspectList aspects = getAspects(stack);
        if (aspects != null && aspects.size() > 0) {
            Aspect aspect = aspects.getAspects()[0];
            tooltip.add(Component.literal(aspect.getName() + " x" + aspects.getAmount(aspect))
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                net.minecraft.world.item.component.TooltipDisplay display,
                                java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        java.util.List<Component> lines = new java.util.ArrayList<>();
        appendHoverText(stack, context.level(), lines, flag);
        for (Component line : lines) {
            tooltip.accept(line);
        }
    }

    
    
    @Override
    public AspectList getAspects(ItemStack stack) {
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
        
        
        CustomData.update(DataComponents.CUSTOM_DATA, stack, aspects::writeToNBT);
    }

    @Override
    public boolean ignoreContainedAspects() {
        return false;
    }
}
