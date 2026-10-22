package arcaneresearch.common.items.baubles;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.api.items.IVisDiscountGear;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ItemBauble extends Item implements ICurioItem, IVisDiscountGear, IRunicArmor {

    private final String variant;
    private final BaubleSlot slot;
    private final int visDiscount;
    public ItemBauble(Item.Properties props, String variant, BaubleSlot slot, Rarity rarity, int visDiscount) {
        super(props
                .stacksTo(1)
                .rarity(rarity));
        this.variant = variant;
        this.slot = slot;
        this.visDiscount = visDiscount;
    }

    public static ItemBauble amuletMundane(Item.Properties props) {
        return new ItemBauble(props, "amulet_mundane", BaubleSlot.AMULET, Rarity.COMMON, 0);
    }

    public static ItemBauble ringMundane(Item.Properties props) {
        return new ItemBauble(props, "ring_mundane", BaubleSlot.RING, Rarity.COMMON, 0);
    }

    public static ItemBauble girdleMundane(Item.Properties props) {
        return new ItemBauble(props, "girdle_mundane", BaubleSlot.BELT, Rarity.COMMON, 0);
    }

    public static ItemBauble ringApprentice(Item.Properties props) {
        return new ItemBauble(props, "ring_apprentice", BaubleSlot.RING, Rarity.UNCOMMON, 5);
    }

    public static ItemBauble amuletFancy(Item.Properties props) {
        return new ItemBauble(props, "amulet_fancy", BaubleSlot.AMULET, Rarity.UNCOMMON, 0);
    }

    public static ItemBauble ringFancy(Item.Properties props) {
        return new ItemBauble(props, "ring_fancy", BaubleSlot.RING, Rarity.UNCOMMON, 0);
    }

    public static ItemBauble girdleFancy(Item.Properties props) {
        return new ItemBauble(props, "girdle_fancy", BaubleSlot.BELT, Rarity.UNCOMMON, 0);
    }

    
    
    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.arcaneresearch.baubles." + variant);
    }

    public BaubleSlot getSlot() {
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

    public enum BaubleSlot {
        AMULET,
        RING,
        BELT
    }
}
