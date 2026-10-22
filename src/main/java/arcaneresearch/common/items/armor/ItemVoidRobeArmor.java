package arcaneresearch.common.items.armor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.api.ArcaneresearchArmorMaterials;
import arcaneresearch.api.items.IGoggles;
import arcaneresearch.api.items.IRevealer;
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.api.items.IVisDiscountGear;
import arcaneresearch.api.items.IWarpingGear;







public class ItemVoidRobeArmor extends Item
        implements IVisDiscountGear, IGoggles, IRevealer, IWarpingGear, IRunicArmor {

     


 
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    private static final int DEFAULT_COLOR = 6961280;

    
    private final ArmorType armorType;

    public ItemVoidRobeArmor(Item.Properties props, ArmorType type) {
        
        super(props.humanoidArmor(ArcaneresearchArmorMaterials.ARMORMAT_VOIDROBE, type)
                
                
                .durability(arcaneresearch.api.ArcaneresearchMaterials.forgeArmorDurability(
                        ArcaneresearchArmorMaterials.ARMORMAT_VOIDROBE, type))
                .stacksTo(1)
                .rarity(Rarity.EPIC));
        this.armorType = type;
    }

    public static ItemVoidRobeArmor createHelmet(Item.Properties props) {
        return new ItemVoidRobeArmor(props, ArmorType.HELMET);
    }

    public static ItemVoidRobeArmor createChestplate(Item.Properties props) {
        return new ItemVoidRobeArmor(props, ArmorType.CHESTPLATE);
    }

    public static ItemVoidRobeArmor createLeggings(Item.Properties props) {
        return new ItemVoidRobeArmor(props, ArmorType.LEGGINGS);
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    
    
    
    

    

    @Override
    
    
    
    
    
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);

        
        
        if (stack.isDamaged() && entity.tickCount % 20 == 0
                && entity instanceof Player living) {
            stack.setDamageValue(stack.getDamageValue() - 1);
            for (EquipmentSlot armorSlot : new EquipmentSlot[]{
                    EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                if (living.getItemBySlot(armorSlot) == stack && stack.isDamaged()) {
                    stack.setDamageValue(stack.getDamageValue() - 1);
                    break;
                }
            }
        }
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player) {
        return 5;
    }

    @Override
    public boolean showNodes(ItemStack itemstack, LivingEntity player) {
        return this.armorType == ArmorType.HELMET;
    }

    @Override
    public boolean showIngamePopups(ItemStack itemstack, LivingEntity player) {
        return this.armorType == ArmorType.HELMET;
    }

    @Override
    public int getWarp(ItemStack itemstack, Player player) {
        return 3;
    }

    

    public int getColor(ItemStack stack) {
        return DyedItemColor.getOrDefault(stack, DEFAULT_COLOR);
    }

    public boolean hasCustomColor(ItemStack stack) {
        return stack.has(DataComponents.DYED_COLOR);
    }

    public void clearColor(ItemStack stack) {
        stack.remove(DataComponents.DYED_COLOR);
    }

    public void setColor(ItemStack stack, int color) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (state.is(Blocks.WATER_CAULDRON)) {
            ItemStack stack = context.getItemInHand();
            if (hasCustomColor(stack)) {
                if (!level.isClientSide()) {
                    clearColor(stack);
                    LayeredCauldronBlock.lowerFillLevel(state, level, pos);
                }
                
                
                return (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
            }
        }

        return super.useOn(context);
    }
}
