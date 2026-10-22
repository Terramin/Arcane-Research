package arcaneresearch.common.items.armor;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorType;
import arcaneresearch.api.ArcaneresearchArmorMaterials;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.api.items.IVisDiscountGear;
import arcaneresearch.api.items.IWarpingGear;




public class ItemCultistRobeArmor extends Item implements IVisDiscountGear, IWarpingGear, IRunicArmor {

     


 
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    
    private final ArmorType armorType;

    public ItemCultistRobeArmor(Item.Properties props, ArmorType type) {
        
        super(props.humanoidArmor(ArcaneresearchArmorMaterials.ARMORMAT_CULTIST_ROBE, type)
                
                
                .durability(arcaneresearch.api.ArcaneresearchMaterials.forgeArmorDurability(
                        ArcaneresearchArmorMaterials.ARMORMAT_CULTIST_ROBE, type))
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON));
        this.armorType = type;
    }

    public static ItemCultistRobeArmor createHelmet(Item.Properties props) {
        return new ItemCultistRobeArmor(props, ArmorType.HELMET);
    }

    public static ItemCultistRobeArmor createChestplate(Item.Properties props) {
        return new ItemCultistRobeArmor(props, ArmorType.CHESTPLATE);
    }

    public static ItemCultistRobeArmor createLeggings(Item.Properties props) {
        return new ItemCultistRobeArmor(props, ArmorType.LEGGINGS);
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    
    
    
    

    

    @Override
    public int getVisDiscount(ItemStack stack, Player player) {
        return 1;
    }

    @Override
    public int getWarp(ItemStack itemstack, Player player) {
        return 1;
    }

    
}
