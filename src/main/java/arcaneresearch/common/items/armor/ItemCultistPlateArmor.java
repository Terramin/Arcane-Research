package arcaneresearch.common.items.armor;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorType;
import arcaneresearch.api.ArcaneresearchArmorMaterials;
import arcaneresearch.api.items.IRunicArmor;




public class ItemCultistPlateArmor extends Item implements IRunicArmor {

     


 
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    
    private final ArmorType armorType;

    public ItemCultistPlateArmor(Item.Properties props, ArmorType type) {
        
        super(props.humanoidArmor(ArcaneresearchArmorMaterials.ARMORMAT_CULTIST_PLATE, type)
                
                
                .durability(arcaneresearch.api.ArcaneresearchMaterials.forgeArmorDurability(
                        ArcaneresearchArmorMaterials.ARMORMAT_CULTIST_PLATE, type))
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON));
        this.armorType = type;
    }

    public static ItemCultistPlateArmor createHelmet(Item.Properties props) {
        return new ItemCultistPlateArmor(props, ArmorType.HELMET);
    }

    public static ItemCultistPlateArmor createChestplate(Item.Properties props) {
        return new ItemCultistPlateArmor(props, ArmorType.CHESTPLATE);
    }

    public static ItemCultistPlateArmor createLeggings(Item.Properties props) {
        return new ItemCultistPlateArmor(props, ArmorType.LEGGINGS);
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    
    
    
    

    

    
}
