package arcaneresearch.common.items.armor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
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
import arcaneresearch.api.items.IRunicArmor;
import arcaneresearch.api.items.IVisDiscountGear;







public class ItemRobeArmor extends Item implements IVisDiscountGear, IRunicArmor {

     


 
    @Override
    public int getRunicCharge(ItemStack stack) {
        return 0;
    }

    private static final int DEFAULT_COLOR = 0x6A4C00;

    
    private final ArmorType armorType;

    public ItemRobeArmor(Item.Properties props, ArmorType type) {
        
        super(props.humanoidArmor(ArcaneresearchArmorMaterials.ARMORMAT_SPECIAL, type)
                
                
                .durability(arcaneresearch.api.ArcaneresearchMaterials.forgeArmorDurability(
                        ArcaneresearchArmorMaterials.ARMORMAT_SPECIAL, type))
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON));
        this.armorType = type;
    }

    public static ItemRobeArmor createChest(Item.Properties props) {
        return new ItemRobeArmor(props, ArmorType.CHESTPLATE);
    }

    public static ItemRobeArmor createLegs(Item.Properties props) {
        return new ItemRobeArmor(props, ArmorType.LEGGINGS);
    }

    public static ItemRobeArmor createBoots(Item.Properties props) {
        return new ItemRobeArmor(props, ArmorType.BOOTS);
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    @Override
    public int getVisDiscount(ItemStack stack, Player player) {

        return this.armorType == ArmorType.BOOTS ? 2 : 3;
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
