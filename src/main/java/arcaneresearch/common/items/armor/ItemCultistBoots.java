package arcaneresearch.common.items.armor;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.api.items.IVisDiscountGear;
import arcaneresearch.api.items.IWarpingGear;




public class ItemCultistBoots extends Item implements IVisDiscountGear, IWarpingGear, IRunicArmor {

     


 
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    public ItemCultistBoots(Item.Properties props) {
        
        super(props.humanoidArmor(ArmorMaterials.IRON, ArmorType.BOOTS)
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON));
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
