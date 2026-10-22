package arcaneresearch.common.items.tools;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;

 



 
public class ItemBoneBow extends BowItem {

    public ItemBoneBow(Item.Properties props) {
        
        
        
        
        super(props.durability(512).enchantable(3).repairable(net.minecraft.world.item.Items.BONE));
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        
        
        
        
        if (entity instanceof Player player
                && getUseDuration(stack, entity) - remainingUseDuration > 18) {
            player.releaseUsingItem();
        }
    }

      
    private static int fireArrowLoose(ItemStack stack, Level level, Player player, int charge, boolean hasAmmo) {
        ArrowLooseEvent event = new ArrowLooseEvent(player, stack, level, charge, hasAmmo);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            return -1;
        }
        return event.getCharge();
    }

    private static Holder<Enchantment> ench(Level level, ResourceKey<Enchantment> key) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

     



 
    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        
        if (!(entity instanceof Player player)) {
            return false;
        }
        Holder<Enchantment> infinity = ench(level, Enchantments.INFINITY);
        Holder<Enchantment> punch = ench(level, Enchantments.PUNCH);
        Holder<Enchantment> flame = ench(level, Enchantments.FLAME);
        boolean infinite = player.getAbilities().instabuild
                || EnchantmentHelper.getItemEnchantmentLevel(infinity, stack) > 0;
        ItemStack ammo = player.getProjectile(stack);
        int charge = this.getUseDuration(stack, entity) - timeLeft;
        charge = fireArrowLoose(stack, level, player, charge, !ammo.isEmpty());
        if (charge < 0) {
            return false;
        }
        float powerF = charge / 10.0F;
        powerF = (powerF * powerF + powerF * 2.0F) / 3.0F;
        if (powerF < 0.1F) {
            return false;
        }
        if (powerF > 1.0F) {
            powerF = 1.0F;
        }
        boolean noConsume = infinite && ammo.is(Items.ARROW);
        if (!level.isClientSide()) {
            ArrowItem arrowItem = ammo.getItem() instanceof ArrowItem arrow ? arrow : (ArrowItem) Items.ARROW;
            AbstractArrow arrow = arrowItem.createArrow(level, ammo, player, stack);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, powerF * 2.5F, 1.0F);
            
            
            
            
            
            
            
            
            
            
            double arrowBase = 2.0D + 0.5D;
            arrow.setBaseDamage(arrowBase);
            
            
            stack.hurtAndBreak(1, player, player.getUsedItemHand());
            if (noConsume || player.getAbilities().instabuild
                    && (ammo.is(Items.SPECTRAL_ARROW) || ammo.is(Items.TIPPED_ARROW))) {
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
            level.addFreshEntity(arrow);
            
            
            
            EnchantmentHelper.onProjectileSpawned((net.minecraft.server.level.ServerLevel) level, stack, arrow,
                    brokenItem -> {
                    });
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS, 1.0F,
                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + powerF * 0.5F);
        if (!noConsume && !player.getAbilities().instabuild) {
            ammo.shrink(1);
            if (ammo.isEmpty()) {
                player.getInventory().removeItem(ammo);
            }
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return true;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        
        return super.use(level, player, hand);
    }
}
