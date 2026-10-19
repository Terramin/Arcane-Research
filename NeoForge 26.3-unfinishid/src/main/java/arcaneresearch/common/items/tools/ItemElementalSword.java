package arcaneresearch.common.items.tools;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.common.lib.utils.EntityUtils;
import arcaneresearch.init.ModSounds;

import java.util.List;

public class ItemElementalSword extends Item {

    public ItemElementalSword(Item.Properties props) {
        // NEOFORGE_PORT: SwordItem REMOVED in 26.x -> plain Item with .sword(material, damage, speed).
        super(props
                .rarity(Rarity.RARE)
                .sword(ArcaneresearchMaterials.TOOLMAT_ELEMENTAL, 3, -2.4F));
    }

    // NEOFORGE_PORT: isValidRepairItem(ItemStack, ItemStack) REMOVED from Item in 26.x;
    // repair is now data-driven via the ToolMaterial repairItems tag (repairs_thaumium)
    // -> REPAIRABLE component, so no override is needed (or possible).
    // (Forge body was: repair.is(ModItems.THAUMIUM_INGOT.get()) || super...)

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        // NEOFORGE_PORT: UseAnim -> ItemUseAnimation (BLOCK kept 1:1).
        return ItemUseAnimation.BLOCK;
    }

    // NEOFORGE_PORT: тот же хук, что в Forge (SwordBlockAnimation.HAND_EXTENSION).
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(java.util.function.Consumer<net.neoforged.neoforge.client.extensions.common.IClientItemExtensions> consumer) {
        // NEOFORGE_PORT: пакет neoforged; хук BLOCK-стойки тот же.
        consumer.accept(arcaneresearch.client.renderers.item.SwordBlockAnimation.HAND_EXTENSION);
    }

    /**
     * Урон от удержания (каждую секунду) меняет NBT — дефолт считает это
     * сменой предмета и роняет руку (re-equip) каждую секунду: дёрганье
     * только от 1 лица, от 3 не видно. Игнорим урон, смотрим смену слота/предмета.
     */
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack,
            boolean slotChanged) {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        // NEOFORGE_PORT: getUseDuration now takes the using entity.
        return 72000;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // NEOFORGE_PORT: InteractionResultHolder removed -> InteractionResult.
        player.startUsingItem(hand);
        // Точный паритет reforged: CONSUME. SUCCESS на клиенте давал взмах
        // на каждый тик удержания ПКМ — от 3 лица читалось как анимация,
        // от 1 лица — бесконечное дёрганье. Удержание показывает BLOCK-стойку.
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseDuration) {
        int ticks = getUseDuration(stack, user) - remainingUseDuration;

        Vec3 motion = user.getDeltaMovement();
        if (motion.y < 0.0D) {
            user.setDeltaMovement(motion.x, motion.y / 1.2D, motion.z);
            user.fallDistance /= 1.2F;
        }
        motion = user.getDeltaMovement();
        double lift = motion.y + 0.08D;
        if (lift > 0.5D) {
            lift = 0.2D;
        }
        user.setDeltaMovement(motion.x, lift, motion.z);
        if (user instanceof ServerPlayer serverPlayer) {
            EntityUtils.resetFloatCounter(serverPlayer);
        }

        List<Entity> targets = level.getEntities(user, user.getBoundingBox().inflate(2.5D));
        if (!targets.isEmpty()) {
            for (Entity entity : targets) {
                if (!(entity instanceof Player) && entity instanceof LivingEntity
                        && !entity.isRemoved()
                        && (user.getVehicle() == null || user.getVehicle() != entity)) {
                    Vec3 p = user.position();
                    Vec3 t = entity.position();
                    double distance = p.distanceTo(t) + 0.1D;
                    Vec3 r = t.subtract(p);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(
                            r.x / 2.5D / distance, r.y / 2.5D / distance, r.z / 2.5D / distance));
                }
            }
        }
        if (level.isClientSide()) {
            doClientUseFX(level, user);
        } else if (ticks == 0 || ticks % 20 == 0) {
            level.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.WIND.get(),
                    SoundSource.PLAYERS, 0.5F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        }
        if (ticks % 20 == 0 && !level.isClientSide()) {
            // Прочность как в оригинале, но без broadcastBreakEvent:
            // свинг руки каждую секунду дёргал BLOCK-стойку в 1 лице.
            // NEOFORGE_PORT: consumer-хвост hurtAndBreak удалён — пустой пакет
            // недостижим, рвём напрямую через (amount, entity, hand).
            stack.hurtAndBreak(1, user, user.getUsedItemHand());
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void doClientUseFX(Level level, LivingEntity user) {
        int miny = (int) (user.getBoundingBox().minY - 2.0D);
        if (user.onGround()) {
            miny = Mth.floor(user.getBoundingBox().minY);
        }
        // От 1 лица спираль на уровне тела проходит сквозь камеру
        // (~150 квадов) — мерцание; опускаем её к ногам и уже.
        // От 3 лица и для наблюдателей — как в оригинале.
        double spiralY = user.getBoundingBox().minY + user.getBbHeight() / 2.0F;
        float spiralRad = 1.5F;
        if (FXDispatcher.isLocalFirstPerson(user)) {
            spiralY = user.getBoundingBox().minY + 0.2D;
            spiralRad = 0.8F;
        }
        for (int a = 0; a < 5; a++) {
            FXDispatcher.INSTANCE.smokeSpiral(user.getX(), spiralY, user.getZ(),
                    spiralRad, level.getRandom().nextInt(360), miny, 14540253);
        }
        if (user.onGround()) {
            float r1 = level.getRandom().nextFloat() * 360.0F;
            float mx = -Mth.sin(r1 / 180.0F * (float) Math.PI) / 5.0F;
            float mz = Mth.cos(r1 / 180.0F * (float) Math.PI) / 5.0F;
            level.addParticle(ParticleTypes.SMOKE, user.getX(),
                    user.getBoundingBox().minY + 0.1D, user.getZ(), mx, 0.0D, mz);
        }
    }
}
