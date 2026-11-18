package arcaneresearch.common.items.curios;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import arcaneresearch.api.items.IRunicArmor;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.SlotContext;

public class RunicCurioItem extends CurioItem implements IRunicArmor {
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

    public RunicCurioItem(String id, CurioSlot slot, Rarity rarity, int charge, Proc proc) {
        super(id, slot, rarity, 0);
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

    public static RunicCurioItem amulet() {
        return new RunicCurioItem("runic_amulet", CurioSlot.AMULET, Rarity.UNCOMMON, 8, Proc.NONE);
    }

    public static RunicCurioItem amuletEmergency() {
        return new RunicCurioItem("runic_amulet_emergency", CurioSlot.AMULET, Rarity.RARE, 7,
                Proc.EMERGENCY);
    }

    public static RunicCurioItem ringLesser() {
        return new RunicCurioItem("runic_ring_lesser", CurioSlot.RING, Rarity.UNCOMMON, 1, Proc.NONE);
    }

    public static RunicCurioItem ring() {
        return new RunicCurioItem("runic_ring", CurioSlot.RING, Rarity.RARE, 5, Proc.NONE);
    }

    public static RunicCurioItem ringCharged() {
        return new RunicCurioItem("runic_ring_charged", CurioSlot.RING, Rarity.RARE, 4, Proc.CHARGED);
    }

    public static RunicCurioItem ringRegen() {
        return new RunicCurioItem("runic_ring_regen", CurioSlot.RING, Rarity.RARE, 4, Proc.HEALING);
    }

    public static RunicCurioItem girdle() {
        return new RunicCurioItem("runic_girdle", CurioSlot.BELT, Rarity.RARE, 10, Proc.NONE);
    }

    public static RunicCurioItem girdleKinetic() {
        return new RunicCurioItem("runic_girdle_kinetic", CurioSlot.BELT, Rarity.RARE, 9,
                Proc.KINETIC);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return super.canEquip(slotContext, stack);
    }

    public static boolean isRunicCurio(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof RunicCurioItem;
    }
}
