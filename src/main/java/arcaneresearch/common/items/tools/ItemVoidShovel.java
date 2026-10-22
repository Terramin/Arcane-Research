package arcaneresearch.common.items.tools;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.api.items.IWarpingGear;

public class ItemVoidShovel extends Item implements IWarpingGear {

    public ItemVoidShovel(Item.Properties props) {
        
        super(props
                .shovel(ArcaneresearchMaterials.TOOLMAT_VOID, 1.5F, -3.0F));
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
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {

        if (!attacker.level().isClientSide()
                && (!(target instanceof Player) || isPvPEnabled(attacker.level()))) {
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 0));
        }
        super.hurtEnemy(stack, target, attacker);
    }

    private boolean isPvPEnabled(Level level) {
        
        
        if (!(level instanceof net.minecraft.server.level.ServerLevel pvpLevel)) {
            return true;
        }
        return pvpLevel.isPvpAllowed();
    }

    @Override
    public int getWarp(ItemStack itemstack, Player player) {
        return 1;
    }
}
