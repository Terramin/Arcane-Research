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

/**
 * Порт {@code thaumcraft.common.items.ItemWispEssence} (1.7.10, TC 4.2.3.5) —
 * мудровая эссенция: предмет-носитель аспекта, выпадающий с мудров.
 * <li>NBT-аспект (по одному на стак), как у кристалла эссенции —
 * {@code IEssentiaContainerItem};</li>
 * <li>в 1.7.10 креативный список даёт по стаку на каждый аспект
 * (func_150895_a: {@code add(tag, 2), т.е. 2 эссенции);</li>
 * <li>иконка без NBT переливается цветами всех аспектов каждые 0.5 с
 * (func_82790_a — этапор у регистра тинта в TintHelper клиент-сайд).</li>
 */
public class ItemWispEssence extends Item implements IEssentiaContainerItem {

    /** Паритет 1.7.10: мудровая эссенция несёт 2 эссенции аспекта. */
    public static final int WISP_AMOUNT = 2;

    // NEOFORGE_PORT (26.x): registerItem passes the property factory (with the item id);
    // the no-arg constructor form is kept as a convenience overload.
    public ItemWispEssence(Item.Properties props) {
        super(props.stacksTo(64));
    }

    public ItemWispEssence() {
        this(new Item.Properties());
    }

    /** Стак с заданным аспектом (паритет креативного func_150895_a). */
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

    // NEOFORGE_PORT: Forge appendHoverText(ItemStack, Level, List, Flag) 1:1 logic kept
    // below (plain method); 26.x vanilla calls the 5-arg variant, which delegates to it
    // (same pattern as ItemFocus).
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        AspectList aspects = getAspects(stack);
        if (aspects != null && aspects.size() > 0) {
            Aspect aspect = aspects.getAspects()[0];
            tooltip.add(Component.literal(aspect.getName() + " x" + aspects.getAmount(aspect))
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    // NEOFORGE_PORT: 26.x tooltip signature (TooltipContext/TooltipDisplay/Consumer).
    // Same content as Forge (aspect name + amount).
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

    // NEOFORGE_PORT: stack root-tag storage -> DataComponents.CUSTOM_DATA
    // (same NBT shape; CustomData.of/copyTag round-trip).
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

    // NEOFORGE_PORT: stack root-tag storage -> DataComponents.CUSTOM_DATA
    @Override
    public void setAspects(ItemStack stack, AspectList aspects) {
        // NEOFORGE_PORT (26.3): Forge wrote into the existing root tag (other keys kept) ->
        // CustomData.update merges instead of replacing the whole component.
        CustomData.update(DataComponents.CUSTOM_DATA, stack, aspects::writeToNBT);
    }

    @Override
    public boolean ignoreContainedAspects() {
        return false;
    }
}
