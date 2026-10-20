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

/**
 * Костяной лук, порт TC4 {@code ItemBowBone}: быстрая натяжка (полный
 * заряд за 10 тиков вместо 20, автоспуск на 18), стрела быстрее
 * (f * 2.5 вместо f * 2.0) и +0.5 урона вместо крита.
 */
public class ItemBoneBow extends BowItem {

    public ItemBoneBow(Item.Properties props) {
        // NEOFORGE_PORT: registerItem passes props; durability + enchantable here
        // (getEnchantmentValue удалён в 26.x -> enchantable(3), то же значение Forge).
        // NEOFORGE_PORT: Forge isValidRepairItem (repair.is(Items.BONE); BowItem has no own
        // repair material) -> REPAIRABLE component.
        super(props.durability(512).enchantable(3).repairable(net.minecraft.world.item.Items.BONE));
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        // Паритет TC4: натяжка дольше 18 тиков не держится — автоспуск.
        // ВАЖНО: именно releaseUsingItem (выстрел), а не stopUsingItem:
        // в 1.20.1 stopUsingItem только гасит использование без выстрела
        // (в 1.7.10 он и стрелял) — иначе вечная холостая натяжка.
        if (entity instanceof Player player
                && getUseDuration(stack, entity) - remainingUseDuration > 18) {
            player.releaseUsingItem();
        }
    }

    /** NEOFORGE_PORT: ForgeEventFactory.onArrowLoose 1:1 (post + canceled -> -1, else charge). */
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

    /**
     * Паритет TC4 {@code ItemBowBone}: заряд j / 10, скорость f * 2.5,
     * урон +0.5 вместо крита. Событие ArrowLoose постится как в ванилле —
     * подписчик первородных стрел перехватывает выстрел.
     */
    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        // NEOFORGE_PORT: releaseUsing now returns boolean (true = shot handled).
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
            // NEOFORGE_PORT: getBaseDamage/setKnockback/setSecondsOnFire удалены.
            // База ванильной стрелы 2.0; TC4-бонус +0.5 и сила лука считаются от неё
            // (Forge читал getBaseDamage() после createArrow — то же значение 2.0).
            // Панч/огонь ванилла теперь переносит с лука сама (weapon-стек в
            // createArrow) — ручные setKnockback/setSecondsOnFire не нужны;
            // огонь от FLAME даёт сам выстрел через ванильные эффекты.
            // NEOFORGE_PORT (26.3): no manual Power bonus here - Power is now the enchantment's
            // minecraft:damage effect (1.0 + 0.5 per level above first = Forge's 0.5*lvl + 0.5),
            // applied by AbstractArrow on hit from firedFromWeapon (this bow, passed to
            // createArrow). Adding it here as well counted Power twice.
            double arrowBase = 2.0D + 0.5D;
            arrow.setBaseDamage(arrowBase);
            // NEOFORGE_PORT: broadcastBreakEvent consumer overload REMOVED;
            // hurtAndBreak(int, LivingEntity, InteractionHand) is the replacement.
            stack.hurtAndBreak(1, player, player.getUsedItemHand());
            if (noConsume || player.getAbilities().instabuild
                    && (ammo.is(Items.SPECTRAL_ARROW) || ammo.is(Items.TIPPED_ARROW))) {
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
            level.addFreshEntity(arrow);
            // NEOFORGE_PORT (26.3): Forge set arrow.setSecondsOnFire(100) for Flame; Flame is now a
            // projectile_spawned enchantment effect (ignite 100 s), which vanilla only runs via
            // Projectile.spawnProjectile -> applyOnProjectileSpawned. Run the bow's spawn effects.
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
        // NEOFORGE_PORT: InteractionResultHolder removed -> InteractionResult.
        return super.use(level, player, hand);
    }
}
