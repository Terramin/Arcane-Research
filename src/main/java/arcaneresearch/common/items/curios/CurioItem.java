package arcaneresearch.common.items.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.api.items.IVisDiscountGear;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class CurioItem extends Item implements ICurioItem, IVisDiscountGear, IRunicArmor {
    private final String variant;
    private final CurioSlot slot;
    private final int visDiscount;

    public CurioItem(String variant, CurioSlot slot, Rarity rarity, int visDiscount) {
        super(new Properties()
                .stacksTo(1)
                .rarity(rarity));
        this.variant = variant;
        this.slot = slot;
        this.visDiscount = visDiscount;
    }

    public static CurioItem amuletMundane() {
        return new CurioItem("amulet_mundane", CurioSlot.AMULET, Rarity.COMMON, 0);
    }

    public static CurioItem ringMundane() {
        return new CurioItem("ring_mundane", CurioSlot.RING, Rarity.COMMON, 0);
    }

    public static CurioItem girdleMundane() {
        return new CurioItem("girdle_mundane", CurioSlot.BELT, Rarity.COMMON, 0);
    }

    public static CurioItem ringApprentice() {
        return new CurioItem("ring_apprentice", CurioSlot.RING, Rarity.UNCOMMON, 5);
    }

    public static CurioItem amuletFancy() {
        return new CurioItem("amulet_fancy", CurioSlot.AMULET, Rarity.UNCOMMON, 0);
    }

    public static CurioItem ringFancy() {
        return new CurioItem("ring_fancy", CurioSlot.RING, Rarity.UNCOMMON, 0);
    }

    public static CurioItem girdleFancy() {
        return new CurioItem("girdle_fancy", CurioSlot.BELT, Rarity.UNCOMMON, 0);
    }

    @Override
    protected @NotNull String getOrCreateDescriptionId() {
        return "item.arcaneresearch.curios." + variant;
    }

    public CurioSlot getSlot() {
        return slot;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return switch (slot) {
            case AMULET -> slotContext.identifier().equals("necklace");
            case RING -> slotContext.identifier().equals("ring");
            case BELT -> slotContext.identifier().equals("belt");
        };
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player) {
        return visDiscount;
    }

    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    public enum CurioSlot {
        AMULET,
        RING,
        BELT
    }
}
