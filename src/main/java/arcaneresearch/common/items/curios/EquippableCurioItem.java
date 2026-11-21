package arcaneresearch.common.items.curios;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

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

    public String getSlotId() {
        return slotId;
    }
}
