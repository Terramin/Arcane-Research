package arcaneresearch.common.lib.events;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import net.neoforged.neoforge.event.entity.player.ArrowNockEvent;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.entities.projectile.EntityPrimalArrow;
import arcaneresearch.common.items.tools.ItemBoneBow;
import arcaneresearch.common.items.tools.ItemPrimalArrow;

 




 
@EventBusSubscriber(modid = Arcaneresearch.MODID)
public class PrimalArrowEvents {


    private static Holder<Enchantment> ench(Level level, ResourceKey<Enchantment> key) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        
        Player player = event.getEntity();
        if (player == null) {
            return;
        }
        ItemStack primal = findPrimalArrow(player);
        if (primal == null || primal.isEmpty()) {
            return;
        }
        int type = primal.getItem() instanceof ItemPrimalArrow arrow ? arrow.getType() : 0;
        Level level = player.level();
        ItemStack bow = event.getBow();
        int charge = event.getCharge();
        boolean bonebow = bow.getItem() instanceof ItemBoneBow;

        float power = bonebow ? charge / 10.0F : charge / 20.0F;
        power = (power * power + power * 2.0F) / 3.0F;
        if (power < 0.1F) {
            return;
        }
        if (power > 1.0F) {
            power = 1.0F;
        }
        float dam = bonebow ? 2.5F : 2.0F;

        EntityPrimalArrow shot = new EntityPrimalArrow(level, player, power * dam, type);
        
        
        
        double base = 2.1D * EntityPrimalArrow.damageMult(type);
        if (bonebow) {
            base += 0.5D;
        } else if (power == 1.0F) {
            shot.setCritArrow(true);
        }
        Holder<Enchantment> powerH = ench(level, Enchantments.POWER);
        Holder<Enchantment> punchH = ench(level, Enchantments.PUNCH);
        Holder<Enchantment> flameH = ench(level, Enchantments.FLAME);
        Holder<Enchantment> infinityH = ench(level, Enchantments.INFINITY);
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(powerH, bow);
        if (powerLevel > 0) {
            base += (double) powerLevel * 0.5D + 0.5D;
        }
        shot.setBaseDamage(base);
        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(punchH, bow);
        if (type == ItemPrimalArrow.EARTH) {
            punchLevel++;
        }
        if (punchLevel > 0) {
            
            
            shot.punchLevel = punchLevel;
        }
        if (EnchantmentHelper.getItemEnchantmentLevel(flameH, bow) > 0) {
            
            
            shot.igniteForSeconds(100.0F);
        }
        
        bow.hurtAndBreak(1, player, player.getUsedItemHand());
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS, 1.0F,
                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);

        boolean creative = player.getAbilities().instabuild;
        boolean keep = creative;
        if (!keep && EnchantmentHelper.getItemEnchantmentLevel(infinityH, bow) > 0
                && level.getRandom().nextFloat() < 0.33F) {
            keep = true;
        }
        if (!keep) {
            primal.shrink(1);
        }
        if (!level.isClientSide()) {
            level.addFreshEntity(shot);
        }
        event.setCanceled(true);
    }

     



 
    @SubscribeEvent
    public static void onArrowNock(ArrowNockEvent event) {
        if (event.getAction() != null) {
            return;
        }
        if (!(event.getBow().getItem() instanceof BowItem)) {
            return;
        }
        Player player = event.getEntity();
        boolean vanillaOk = player.getAbilities().instabuild || event.hasAmmo();
        if (vanillaOk || !holdsPrimalArrow(player)) {
            return;
        }
        player.startUsingItem(event.getHand());
        
        event.setAction(net.minecraft.world.InteractionResult.CONSUME);
    }

     





 
    @SubscribeEvent
    public static void onGetProjectile(net.neoforged.neoforge.event.entity.living.LivingGetProjectileEvent event) {
        if (!event.getProjectileItemStack().isEmpty()) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)
                || !(event.getProjectileWeaponItemStack().getItem() instanceof BowItem)) {
            return;
        }
        ItemStack primal = findPrimalArrow(player);
        if (primal != null) {
            event.setProjectileItemStack(primal);
        }
    }

      
    private static ItemStack findPrimalArrow(Player player) {
        
        
        net.minecraft.world.entity.player.Inventory inv = player.getInventory();
        for (int i = 0; i < net.minecraft.world.entity.player.Inventory.INVENTORY_SIZE; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemPrimalArrow) {
                return stack;
            }
        }
        ItemStack off = player.getOffhandItem();
        if (!off.isEmpty() && off.getItem() instanceof ItemPrimalArrow) {
            return off;
        }
        return null;
    }

    public static boolean holdsPrimalArrow(Player player) {
        return findPrimalArrow(player) != null;
    }
}
