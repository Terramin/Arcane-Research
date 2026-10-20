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

/**
 * Первородные стрелы в полёте, порт TC4 {@code EventHandlerEntity.bowShot}:
 * если в инвентаре есть первородные стрелы — выстрел заменяется на
 * {@link EntityPrimalArrow} (тип из первого найденного стака) и ванильный
 * выстрел отменяется. Заряд: костяной лук j / 10, остальные j / 20.
 */
@EventBusSubscriber(modid = Arcaneresearch.MODID)
public class PrimalArrowEvents {


    private static Holder<Enchantment> ench(Level level, ResourceKey<Enchantment> key) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        // NEOFORGE_PORT: bus FORGE -> NeoForge.EVENT_BUS (default GAME bus of the subscriber).
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
        // NEOFORGE_PORT: AbstractArrow.getBaseDamage удалён (только setBaseDamage);
        // база после ctor известна: 2.1*mult (см. EntityPrimalArrow). Бонусы считаем
        // от неё же: костяной лук +0.5 (вместо крита), иначе крит при полном заряде.
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
            // NEOFORGE_PORT: setKnockback удалён -> поле EntityPrimalArrow.punchLevel
            // (применяется в onHitEntity тем же толчком 0.6/уровень).
            shot.punchLevel = punchLevel;
        }
        if (EnchantmentHelper.getItemEnchantmentLevel(flameH, bow) > 0) {
            // NEOFORGE_PORT: setSecondsOnFire(100) -> igniteForSeconds(100).
            // NEOFORGE_PORT (26.3): setSecondsOnFire took SECONDS (100 s = 2000 ticks, vanilla Flame).
            shot.igniteForSeconds(100.0F);
        }
        // NEOFORGE_PORT: broadcastBreakEvent consumer overload REMOVED.
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

    /**
     * Паритет TC4 ArrowNockEvent: любой лук натягивается, если есть
     * первородные стрелы (ванилла без обычных стрел отказывает).
     * Событие постится до проверки патронов — подменяем исход consume.
     */
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
        // NEOFORGE_PORT: InteractionResultHolder removed -> InteractionResult.
        event.setAction(net.minecraft.world.InteractionResult.CONSUME);
    }

    /**
     * NEOFORGE_PORT (26.3): BowItem.releaseUsing now returns BEFORE posting ArrowLooseEvent when
     * player.getProjectile(bow) is empty (Forge 1.20.1 posted it with hasAmmo=false), so with only
     * primal arrows a vanilla bow would nock (onArrowNock) but never fire. Report the primal stack
     * as the bow's ammo in that case; onArrowLoose then cancels the vanilla shot exactly as before
     * (it fires whenever a primal stack exists and power >= 0.1; below that vanilla also aborts).
     */
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

    /** Первый стак первородных стрел: основной инвентарь, затем вторая рука. */
    private static ItemStack findPrimalArrow(Player player) {
        // NEOFORGE_PORT: Inventory.items now private -> slot iteration (same stacks).
        // NEOFORGE_PORT (26.3): only the 36 main slots (Forge `items`); getContainerSize() also spans armor/offhand.
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
