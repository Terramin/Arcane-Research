package arcaneresearch.common.items.tools;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
public class ItemBoneBow extends BowItem {
    private static int debugShots = 0;
    public ItemBoneBow() {
        super(new Item.Properties().durability(512));
    }
    @Override
    public int getEnchantmentValue() {
        return 3;
    }
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.is(Items.BONE) || super.isValidRepairItem(toRepair, repair);
    }
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (entity instanceof Player player
                && getUseDuration(stack) - remainingUseDuration > 18) {
            player.releaseUsingItem();
        }
    }
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }
        boolean infinite = player.getAbilities().instabuild
                || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.INFINITY_ARROWS, stack) > 0;
        ItemStack ammo = player.getProjectile(stack);
        int charge = this.getUseDuration(stack) - timeLeft;
        charge = ForgeEventFactory.onArrowLoose(stack, level, player, charge, !ammo.isEmpty());
        if (charge < 0) {
            return;
        }
        float power = charge / 10.0F;
        power = (power * power + power * 2.0F) / 3.0F;
        if (debugShots < 5) {
            debugShots++;
            arcaneresearch.Arcaneresearch.LOGGER.info(
                    "[BowRelease] side={} charge={} power={} ammo={}",
                    level.isClientSide ? "client" : "server", charge, power, ammo);
        }
        if (power < 0.1F) {
            return;
        }
        if (power > 1.0F) {
            power = 1.0F;
        }
        boolean noConsume = infinite && ammo.is(Items.ARROW);
        if (!level.isClientSide) {
            ArrowItem arrowItem = ammo.getItem() instanceof ArrowItem arrow ? arrow : (ArrowItem) Items.ARROW;
            AbstractArrow arrow = arrowItem.createArrow(level, ammo, player);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 2.5F, 1.0F);
            arrow.setBaseDamage(arrow.getBaseDamage() + 0.5D);
            int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
            if (powerLevel > 0) {
                arrow.setBaseDamage(arrow.getBaseDamage() + (double) powerLevel * 0.5D + 0.5D);
            }
            int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
            if (punchLevel > 0) {
                arrow.setKnockback(punchLevel);
            }
            if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, stack) > 0) {
                arrow.setSecondsOnFire(100);
            }
            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
            if (noConsume || player.getAbilities().instabuild
                    && (ammo.is(Items.SPECTRAL_ARROW) || ammo.is(Items.TIPPED_ARROW))) {
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
            level.addFreshEntity(arrow);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS, 1.0F,
                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + power * 0.5F);
        if (!noConsume && !player.getAbilities().instabuild) {
            ammo.shrink(1);
            if (ammo.isEmpty()) {
                player.getInventory().removeItem(ammo);
            }
        }
        player.awardStat(Stats.ITEM_USED.get(this));
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return super.use(level, player, hand);
    }
}
