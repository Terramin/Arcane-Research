package thaumictinker.gear;

import java.util.List;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


 



 
@Mod.EventBusSubscriber(modid = thaumictinker.TtCore.MODID)
public final class GearEvents {

    public static final float BLOCK_DAMAGE = 3.0F;
    public static final float STRIKE_DAMAGE = 2.0F;
    private static int handleNext;

    private GearEvents() {
    }

    public static void reset() {
        handleNext = 0;
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        DamageSource source = event.getSource();
        if (!source.is(DamageTypes.PLAYER_ATTACK) || !(source.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof BloodSwordItem) || !BloodSwordItem.isActivated(stack)) {
            return;
        }
        reset();
        List<ItemStack> souls = SoulDrops.soulsFor(event.getEntity());
        if (souls == null) {
            return;
        }
        
        event.getDrops().clear();
        for (ItemStack soul : souls) {
            ItemEntity drop = new ItemEntity(event.getEntity().level(),
                    event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), soul);
            drop.setPickUpDelay(10);
            event.getDrops().add(drop);
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingAttackEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        boolean handle = handleNext == 0;
        if (!handle) {
            handleNext--;
        }
        if (entity instanceof Player player && handle && player.isUsingItem()
                && player.getUseItem().getItem() instanceof BloodSwordItem) {
            event.setCanceled(true);
            handleNext = 3;
            player.hurt(entity.damageSources().magic(), BLOCK_DAMAGE);
        }
        if (handle) {
            Entity direct = event.getSource().getDirectEntity();
            if (direct instanceof LivingEntity attacker
                    && attacker.getMainHandItem().getItem() instanceof BloodSwordItem) {
                attacker.hurt(attacker.damageSources().magic(), STRIKE_DAMAGE);
            }
        }
    }
}
