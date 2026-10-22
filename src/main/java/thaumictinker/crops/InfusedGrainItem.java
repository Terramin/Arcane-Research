package thaumictinker.crops;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IEssentiaContainerItem;

 


 
public class InfusedGrainItem extends Item implements IEssentiaContainerItem {

    private final Aspect aspect;

    public InfusedGrainItem(Aspect aspect, Properties properties) {
        super(properties);
        this.aspect = aspect;
    }

    public Aspect getAspect() {
        return aspect;
    }

    @Override
    public AspectList getAspects(ItemStack stack) {
        return new AspectList().add(aspect, 1).add(Aspect.PLANT, 1);
    }

    @Override
    public void setAspects(ItemStack stack, AspectList aspects) {
    }

    @Override
    public boolean ignoreContainedAspects() {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(aspect.getLocalizedDescription()));
    }
}
