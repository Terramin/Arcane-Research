package thaumictinker.necromancy;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import arcaneresearch.api.aspects.Aspect;

 


 
public class SoulAspectItem extends Item {

    public enum Form {
        BASE,
        CONDENSED,
        INFUSED
    }

    private static final String TABLET_TAG = "thaumictinker:tablet";

    private final Aspect aspect;
    private final Form form;

    public SoulAspectItem(Properties properties, Aspect aspect, Form form) {
        super(properties);
        this.aspect = aspect;
        this.form = form;
    }

    public Aspect getAspect() {
        return aspect;
    }

    public Form getForm() {
        return form;
    }

    public static boolean isSoul(ItemStack stack) {
        return stack.getItem() instanceof SoulAspectItem;
    }

    public static boolean isInfused(ItemStack stack) {
        return stack.getItem() instanceof SoulAspectItem item && item.form == Form.INFUSED;
    }

    public static Aspect aspectOf(ItemStack stack) {
        return ((SoulAspectItem) stack.getItem()).getAspect();
    }

    @Nullable
    public static BlockPos getLastTablet(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(TABLET_TAG)) {
            return null;
        }
        CompoundTag tag = stack.getTag().getCompound(TABLET_TAG);
        if (tag.contains("Y")) {
            return new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
        }
        return null;
    }

    public static void setLastTablet(ItemStack stack, @Nullable BlockPos pos) {
        if (stack.hasTag()) {
            stack.getTag().remove(TABLET_TAG);
        }
        if (pos == null) {
            return;
        }
        CompoundTag tag = new CompoundTag();
        tag.putInt("X", pos.getX());
        tag.putInt("Y", pos.getY());
        tag.putInt("Z", pos.getZ());
        stack.getOrCreateTag().put(TABLET_TAG, tag);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal(aspect.getName()));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return form == Form.INFUSED || super.isFoil(stack);
    }
}
