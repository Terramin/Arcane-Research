package arcaneresearch.common.items.armor;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorType;
import arcaneresearch.api.ArcaneresearchArmorMaterials;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.api.items.IWarpingGear;




public class ItemVoidArmor extends Item implements IWarpingGear, IRunicArmor {

     


 
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    
    private final ArmorType armorType;

    public ItemVoidArmor(Item.Properties props, ArmorType type) {
        
        super(props.humanoidArmor(ArcaneresearchArmorMaterials.ARMORMAT_VOID, type)
                
                
                .durability(arcaneresearch.api.ArcaneresearchMaterials.forgeArmorDurability(
                        ArcaneresearchArmorMaterials.ARMORMAT_VOID, type))
                .rarity(Rarity.UNCOMMON));
        this.armorType = type;
    }

    public static ItemVoidArmor createHelmet(Item.Properties props) {
        return new ItemVoidArmor(props, ArmorType.HELMET);
    }

    public static ItemVoidArmor createChestplate(Item.Properties props) {
        return new ItemVoidArmor(props, ArmorType.CHESTPLATE);
    }

    public static ItemVoidArmor createLeggings(Item.Properties props) {
        return new ItemVoidArmor(props, ArmorType.LEGGINGS);
    }

    public static ItemVoidArmor createBoots(Item.Properties props) {
        return new ItemVoidArmor(props, ArmorType.BOOTS);
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    
    
    
    

    @Override
    
    
    
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);

        if (stack.isDamaged() && entity != null
                && entity.tickCount % 20 == 0
                
                
                && entity instanceof Player) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    

    @Override
    public int getWarp(ItemStack itemstack, Player player) {
        return 1;
    }

    
}
