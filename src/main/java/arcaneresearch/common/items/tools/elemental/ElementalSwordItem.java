package arcaneresearch.common.items.tools.elemental;

import arcaneresearch.api.ArcaneresearchMaterials;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.client.renderers.item.SwordBlockAnimation;
import arcaneresearch.common.lib.utils.EntityUtils;
import arcaneresearch.init.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class ElementalSwordItem extends SwordItem implements ElementalTool {
    public ElementalSwordItem() {
        super(ArcaneresearchMaterials.TOOLMAT_ELEMENTAL, 3, -2.4F,
                ElementalTool.createProperties());
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack toRepair, ItemStack repair) {
        return ElementalTool.canRepair(repair) || super.isValidRepairItem(toRepair, repair);
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SwordBlockAnimation.HAND_EXTENSION);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack,
                                               boolean slotChanged) {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 72000;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(@NotNull Level level, LivingEntity user, @NotNull ItemStack stack, int remainingUseDuration) {
        int ticks = getUseDuration(stack) - remainingUseDuration;
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
                if (entity instanceof LivingEntity living && !(entity instanceof Player)
                        && !living.isRemoved() && user.getVehicle() != entity) {
                    Vec3 p = user.position();
                    Vec3 t = living.position();
                    double distance = p.distanceTo(t) + 0.1D;
                    Vec3 r = t.subtract(p);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(
                            r.x / 2.5D / distance, r.y / 2.5D / distance, r.z / 2.5D / distance));
                }
            }
        }
        if (level.isClientSide()) {
            doClientUseFX(level, user);
        } else if (ticks % 20 == 0) {
            level.playSound(null, user.getX(), user.getY(), user.getZ(), ModSounds.WIND.get(),
                    SoundSource.PLAYERS, 0.5F, 0.9F + level.random.nextFloat() * 0.2F);
        }
        if (ticks % 20 == 0 && !level.isClientSide()) {
            stack.hurtAndBreak(1, user, (e) -> {
            });
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void doClientUseFX(Level level, LivingEntity user) {
        int miny = (int) (user.getBoundingBox().minY - 2.0D);
        if (user.onGround()) {
            miny = Mth.floor(user.getBoundingBox().minY);
        }
        double spiralY = user.getBoundingBox().minY + user.getBbHeight() / 2.0F;
        float spiralRad = 1.5F;
        if (FXDispatcher.isLocalFirstPerson(user)) {
            spiralY = user.getBoundingBox().minY + 0.2D;
            spiralRad = 0.8F;
        }
        for (int a = 0; a < 5; a++) {
            FXDispatcher.INSTANCE.smokeSpiral(user.getX(), spiralY, user.getZ(),
                    spiralRad, level.random.nextInt(360), miny, 14540253);
        }
        if (user.onGround()) {
            float r1 = level.random.nextFloat() * 360.0F;
            float mx = -Mth.sin(r1 / 180.0F * (float) Math.PI) / 5.0F;
            float mz = Mth.cos(r1 / 180.0F * (float) Math.PI) / 5.0F;
            level.addParticle(ParticleTypes.SMOKE, user.getX(),
                    user.getBoundingBox().minY + 0.1D, user.getZ(), mx, 0.0D, mz);
        }
    }
}
