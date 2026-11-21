package arcaneresearch.common.items.tools.voidmetal;

import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.api.items.IWarpingGear;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class VoidSwordItem extends Item implements IWarpingGear, VoidTool {
    public VoidSwordItem(Item.Properties props) {
        super(VoidTool.createProperties(props).sword(ArcaneresearchMaterials.TOOLMAT_VOID, 3, -2.4F));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        VoidTool.repairOverTime(stack, entity);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel sl && !sl.isPvpAllowed()) {
            return;
        }
        target.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80, 0));
        super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public int getWarp(ItemStack itemstack, Player player) {
        return 1;
    }
}