package arcaneresearch.common.items.curios;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.NotNull;

public class RunicCurioItem extends CurioItem {
    public enum Proc {
        NONE,
        CHARGED,
        HEALING,
        KINETIC,
        EMERGENCY
    }

    private final int charge;
    private final Proc proc;
    private final String id;

    public RunicCurioItem(Item.Properties props, String id, CurioSlot slot, Rarity rarity, int charge, Proc proc) {
        super(props, id, slot, rarity, 0);
        this.id = id;
        this.charge = charge;
        this.proc = proc;
    }

    @Override
    protected @NotNull String getOrCreateDescriptionId() {
        return "item.arcaneresearch." + id;
    }

    @Override
    public int getRunicCharge(ItemStack stack) {
        return charge;
    }

    public Proc getProc(ItemStack stack) {
        return proc;
    }

    public static RunicCurioItem amulet(Item.Properties props) {
        return new RunicCurioItem(props, "runic_amulet", CurioSlot.AMULET, Rarity.UNCOMMON, 8, Proc.NONE);
    }

    public static RunicCurioItem amuletEmergency(Item.Properties props) {
        return new RunicCurioItem(props, "runic_amulet_emergency", CurioSlot.AMULET, Rarity.RARE, 7,
                Proc.EMERGENCY);
    }

    public static RunicCurioItem ringLesser(Item.Properties props) {
        return new RunicCurioItem(props, "runic_ring_lesser", CurioSlot.RING, Rarity.UNCOMMON, 1, Proc.NONE);
    }

    public static RunicCurioItem ring(Item.Properties props) {
        return new RunicCurioItem(props, "runic_ring", CurioSlot.RING, Rarity.RARE, 5, Proc.NONE);
    }

    public static RunicCurioItem ringCharged(Item.Properties props) {
        return new RunicCurioItem(props, "runic_ring_charged", CurioSlot.RING, Rarity.RARE, 4, Proc.CHARGED);
    }

    public static RunicCurioItem ringRegen(Item.Properties props) {
        return new RunicCurioItem(props, "runic_ring_regen", CurioSlot.RING, Rarity.RARE, 4, Proc.HEALING);
    }

    public static RunicCurioItem girdle(Item.Properties props) {
        return new RunicCurioItem(props, "runic_girdle", CurioSlot.BELT, Rarity.RARE, 10, Proc.NONE);
    }

    public static RunicCurioItem girdleKinetic(Item.Properties props) {
        return new RunicCurioItem(props, "runic_girdle_kinetic", CurioSlot.BELT, Rarity.RARE, 9,
                Proc.KINETIC);
    }

    public static boolean isRunicCurio(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof RunicCurioItem;
    }
}
