package arcaneresearch.common.items.armor;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorType;
import arcaneresearch.api.ArcaneresearchArmorMaterials;
import arcaneresearch.api.items.IRunicArmor;




public class ItemCultistLeaderArmor extends Item implements IRunicArmor {

     


 
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    
    private final ArmorType armorType;

    public ItemCultistLeaderArmor(Item.Properties props, ArmorType type) {
        
        super(props.humanoidArmor(ArcaneresearchArmorMaterials.ARMORMAT_CULTIST_LEADER, type)
                
                
                .durability(arcaneresearch.api.ArcaneresearchMaterials.forgeArmorDurability(
                        ArcaneresearchArmorMaterials.ARMORMAT_CULTIST_LEADER, type))
                .stacksTo(1)
                .rarity(Rarity.RARE));
        this.armorType = type;
    }

    public static ItemCultistLeaderArmor createHelmet(Item.Properties props) {
        return new ItemCultistLeaderArmor(props, ArmorType.HELMET);
    }

    public static ItemCultistLeaderArmor createChestplate(Item.Properties props) {
        return new ItemCultistLeaderArmor(props, ArmorType.CHESTPLATE);
    }

    public static ItemCultistLeaderArmor createLeggings(Item.Properties props) {
        return new ItemCultistLeaderArmor(props, ArmorType.LEGGINGS);
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    
    
    
    

    

    
}
