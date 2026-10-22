package arcaneresearch.common.items.baubles;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.common.items.baubles.ItemBauble.BaubleSlot;
import top.theillusivec4.curios.api.SlotContext;




 



 
public class ItemRunicBauble extends ItemBauble implements IRunicArmor {

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

    public ItemRunicBauble(Item.Properties props, String id, BaubleSlot slot, Rarity rarity, int charge, Proc proc) {
        super(props, id, slot, rarity, 0);
        this.id = id;
        this.charge = charge;
        this.proc = proc;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.arcaneresearch." + id);
    }

    @Override
    public int getRunicCharge(ItemStack stack) {
        return charge;
    }

    public Proc getProc(ItemStack stack) {
        return proc;
    }

    public static ItemRunicBauble amulet(Item.Properties props) {
        return new ItemRunicBauble(props, "runic_amulet", BaubleSlot.AMULET, Rarity.UNCOMMON, 8, Proc.NONE);
    }

    public static ItemRunicBauble amuletEmergency(Item.Properties props) {
        return new ItemRunicBauble(props, "runic_amulet_emergency", BaubleSlot.AMULET, Rarity.RARE, 7,
                Proc.EMERGENCY);
    }

    public static ItemRunicBauble ringLesser(Item.Properties props) {
        return new ItemRunicBauble(props, "runic_ring_lesser", BaubleSlot.RING, Rarity.UNCOMMON, 1, Proc.NONE);
    }

    public static ItemRunicBauble ring(Item.Properties props) {
        return new ItemRunicBauble(props, "runic_ring", BaubleSlot.RING, Rarity.RARE, 5, Proc.NONE);
    }

    public static ItemRunicBauble ringCharged(Item.Properties props) {
        return new ItemRunicBauble(props, "runic_ring_charged", BaubleSlot.RING, Rarity.RARE, 4, Proc.CHARGED);
    }

    public static ItemRunicBauble ringRegen(Item.Properties props) {
        return new ItemRunicBauble(props, "runic_ring_regen", BaubleSlot.RING, Rarity.RARE, 4, Proc.HEALING);
    }

    public static ItemRunicBauble girdle(Item.Properties props) {
        return new ItemRunicBauble(props, "runic_girdle", BaubleSlot.BELT, Rarity.RARE, 10, Proc.NONE);
    }

    public static ItemRunicBauble girdleKinetic(Item.Properties props) {
        return new ItemRunicBauble(props, "runic_girdle_kinetic", BaubleSlot.BELT, Rarity.RARE, 9,
                Proc.KINETIC);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return super.canEquip(slotContext, stack);
    }

    public static boolean isRunicBauble(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemRunicBauble;
    }
}
