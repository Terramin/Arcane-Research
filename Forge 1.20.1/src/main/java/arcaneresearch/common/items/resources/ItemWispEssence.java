package arcaneresearch.common.items.resources;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.IEssentiaContainerItem;
import javax.annotation.Nullable;
import java.util.List;
public class ItemWispEssence extends Item implements IEssentiaContainerItem {
    public static final int WISP_AMOUNT = 2;
    public ItemWispEssence() {
        super(new Item.Properties().stacksTo(64));
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
            return Component.translatable(this.getDescriptionId(stack), aspect.getName());
        }
        return super.getName(stack);
    }
    @Override
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
}
