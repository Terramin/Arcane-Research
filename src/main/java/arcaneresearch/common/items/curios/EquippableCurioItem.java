package arcaneresearch.common.items.curios;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * Common base for every equippable Curio in this mod.
 *
 * <p>Centralizes the boilerplate shared by all curio items: a single-stack default,
 * slot matching against the Curios API slot identifier and the ability to be equipped
 * directly from the use action.</p>
 */
public abstract class EquippableCurioItem extends Item implements ICurioItem {

    private final String slotId;

    protected EquippableCurioItem(Item.Properties properties, CurioSlot slot) {
        super(properties.stacksTo(1));
        this.slotId = slot.slotId;
    }

    protected EquippableCurioItem(Item.Properties properties, String slotId) {
        super(properties.stacksTo(1));
        this.slotId = slotId;
    }

    @Override
    public boolean canEquip(@NotNull SlotContext slotContext, @NotNull ItemStack stack) {
        return slotId.equals(slotContext.identifier());
    }

    @Override
    public boolean canEquipFromUse(@NotNull SlotContext slotContext, @NotNull ItemStack stack) {
        return true;
    }

    /** Returns the Curios slot identifier this item is restricted to. */
    public String getSlotId() {
        return slotId;
    }
}
