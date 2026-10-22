package arcaneresearch.common.items.tools;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.api.items.IWarpingGear;

import java.util.function.Consumer;

public class ItemVoidSword extends Item implements IWarpingGear {

    public ItemVoidSword(Item.Properties props) {
        
        super(props
                .sword(ArcaneresearchMaterials.TOOLMAT_VOID, 3, -2.4F));
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
                && (!(target instanceof Player) || !(attacker instanceof Player) || isPvPEnabled(attacker.level()))) {
            try {
                target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60, 0));
            } catch (Exception ignored) {
            }
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

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("enchantment.special.sapless").withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
