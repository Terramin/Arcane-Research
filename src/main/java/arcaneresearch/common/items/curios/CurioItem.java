package arcaneresearch.common.items.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.api.items.IVisDiscountGear;
import org.jetbrains.annotations.NotNull;

public class CurioItem extends EquippableCurioItem implements IVisDiscountGear, IRunicArmor {
    private final String variant;
    private final CurioSlot slot;
    private final int visDiscount;

    public CurioItem(String variant, CurioSlot slot, Rarity rarity, int visDiscount) {
        super(new Item.Properties().rarity(rarity), slot);
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
    public int getVisDiscount(ItemStack stack, Player player) {
        return visDiscount;
    }

    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

}
