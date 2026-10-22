package arcaneresearch.common.items.tools;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.state.BlockState;
import arcaneresearch.api.ArcaneresearchToolMaterials;
import arcaneresearch.api.items.IWarpingGear;

public class ItemPrimalCrusher extends Item implements IWarpingGear {

    
    
    
    
    public static final ToolMaterial PRIMAL_VOID_TIER = ArcaneresearchToolMaterials.TOOLMAT_PRIMAL_VOID;

    public ItemPrimalCrusher(Item.Properties props) {
        
        
        super(props
                .stacksTo(20)
                .pickaxe(PRIMAL_VOID_TIER, 3.5F, -2.8F));
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.FLOWERS) || state.is(BlockTags.SAPLINGS)
                
                
                || state.is(BlockTags.CROPS)
                || state.is(BlockTags.REPLACEABLE_BY_TREES)) {
            return false;
        }
        
        
        return !state.is(PRIMAL_VOID_TIER.incorrectBlocksForDrops());
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {

        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return PRIMAL_VOID_TIER.speed();
        }
        return super.getDestroySpeed(stack, state);
    }

    
    
    

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);

        if (stack.isDamaged() && entity != null && entity.tickCount % 20 == 0
                
                
                && entity instanceof Player) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public int getWarp(ItemStack stack, Player player) {
        return 2;
    }
}
