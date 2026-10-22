package arcaneresearch.common.items.armor;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorType;
import arcaneresearch.api.ArcaneresearchArmorMaterials;
import arcaneresearch.api.items.IRunicArmor;




public class ItemThaumiumArmor extends Item implements IRunicArmor {

     


 
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    
    private final ArmorType armorType;

    public ItemThaumiumArmor(Item.Properties props, ArmorType type) {
        
        super(props.humanoidArmor(ArcaneresearchArmorMaterials.ARMORMAT_THAUMIUM, type)
                
                
                .durability(arcaneresearch.api.ArcaneresearchMaterials.forgeArmorDurability(
                        ArcaneresearchArmorMaterials.ARMORMAT_THAUMIUM, type))
                .rarity(Rarity.UNCOMMON));
        this.armorType = type;
    }

    public static ItemThaumiumArmor createHelmet(Item.Properties props) {
        return new ItemThaumiumArmor(props, ArmorType.HELMET);
    }

    public static ItemThaumiumArmor createChestplate(Item.Properties props) {
        return new ItemThaumiumArmor(props, ArmorType.CHESTPLATE);
    }

    public static ItemThaumiumArmor createLeggings(Item.Properties props) {
        return new ItemThaumiumArmor(props, ArmorType.LEGGINGS);
    }

    public static ItemThaumiumArmor createBoots(Item.Properties props) {
        return new ItemThaumiumArmor(props, ArmorType.BOOTS);
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    
    
    
    

    

    
}
