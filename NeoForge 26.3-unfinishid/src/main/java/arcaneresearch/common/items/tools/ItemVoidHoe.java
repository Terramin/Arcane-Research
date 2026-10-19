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

public class ItemVoidHoe extends Item implements IWarpingGear {

    public ItemVoidHoe(Item.Properties props) {
        // NEOFORGE_PORT: HoeItem REMOVED in 26.x -> plain Item with .hoe(material, damage, speed).
        super(props
                .hoe(ArcaneresearchMaterials.TOOLMAT_VOID, -3, -0.5F));
    }

    // NEOFORGE_PORT: isValidRepairItem(ItemStack, ItemStack) REMOVED from Item in 26.x;
    // repair is now data-driven via the ToolMaterial repairItems tag (repairs_void)
    // -> REPAIRABLE component, so no override is needed (or possible).

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);

        if (stack.isDamaged() && entity != null && entity.tickCount % 20 == 0 && entity instanceof LivingEntity) {
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
        // NEOFORGE_PORT: MinecraftServer.isPvpAllowed moved to ServerLevel.isPvpAllowed in 26.x.
        // Non-server levels keep the old getServer() == null -> true default.
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
