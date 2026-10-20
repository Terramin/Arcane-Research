package arcaneresearch.common.lib.events;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.entities.projectile.EntityPrimalArrow;
import arcaneresearch.common.items.tools.ItemBoneBow;
import arcaneresearch.common.items.tools.ItemPrimalArrow;
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PrimalArrowEvents {
    private static int debugFires = 0;
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
        if (debugFires < 5) {
            debugFires++;
            Arcaneresearch.LOGGER.info("[PrimalFire] side={} type={} power={} dam={}",
                    level.isClientSide ? "client" : "server", type, power, dam);
        }
        if (bonebow) {
            shot.setBaseDamage(shot.getBaseDamage() + 0.5D);
        } else if (power == 1.0F) {
            shot.setCritArrow(true);
        }
        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, bow);
        if (powerLevel > 0) {
            shot.setBaseDamage(shot.getBaseDamage() + (double) powerLevel * 0.5D + 0.5D);
        }
        int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, bow);
        if (type == ItemPrimalArrow.EARTH) {
            punchLevel++;
        }
        if (punchLevel > 0) {
            shot.setKnockback(punchLevel);
        }
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, bow) > 0) {
            shot.setSecondsOnFire(100);
        }
        bow.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS, 1.0F,
                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);
        boolean creative = player.getAbilities().instabuild;
        boolean keep = creative;
        if (!keep && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, bow) > 0
                && level.getRandom().nextFloat() < 0.33F) {
            keep = true;
        }
        if (!keep) {
            primal.shrink(1);
        }
        if (!level.isClientSide) {
            level.addFreshEntity(shot);
        }
        event.setCanceled(true);
    }
    @SubscribeEvent
    public static void onArrowNock(ArrowNockEvent event) {
        if (event.getAction() != null) {
            return;
        }
        if (!(event.getBow().getItem() instanceof net.minecraft.world.item.BowItem)) {
            return;
        }
        Player player = event.getEntity();
        boolean vanillaOk = player.getAbilities().instabuild || event.hasAmmo();
        if (vanillaOk || !holdsPrimalArrow(player)) {
            return;
        }
        player.startUsingItem(event.getHand());
        event.setAction(net.minecraft.world.InteractionResultHolder.consume(event.getBow()));
    }
    private static ItemStack findPrimalArrow(Player player) {
        for (ItemStack stack : player.getInventory().items) {
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
