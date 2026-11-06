package arcaneresearch.common.items.consumables;

import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModEffects;
import arcaneresearch.init.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class ItemSanitySoap extends Item {

    private static final int USE_DURATION = 100;
    private static final int USE_THRESHOLD = 95;

    public ItemSanitySoap() {
        super(new Properties());
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack, int remainingUseDuration) {
        int ticks = USE_DURATION - remainingUseDuration;
        if (ticks > USE_THRESHOLD) {
            entity.stopUsingItem();
        }
        if (level.isClientSide) {
            doUseFX(level, entity);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void doUseFX(Level level, LivingEntity entity) {
        RandomSource random = level.random;

        if (random.nextFloat() < 0.2F) {
            level.playLocalSound(
                    entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.1F,
                    1.5F + random.nextFloat() * 0.2F, false
            );
        }

        AABB box = entity.getBoundingBox();
        double minY = box.minY;
        double height = entity.getBbHeight();
        double baseX = entity.getX() - 0.5F;
        double baseZ = entity.getZ() - 0.5F;

        for (int a = 0; a < 10; a++) {
            FXDispatcher.INSTANCE.crucibleBubble(
                    (float) (baseX + random.nextFloat()),
                    (float) (minY + random.nextFloat() * height),
                    (float) (baseZ + random.nextFloat()),
                    1.0F, 0.8F, 0.9F
            );
        }
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity, int timeLeft) {
        int held = USE_DURATION - timeLeft;
        if (held <= USE_THRESHOLD || !(entity instanceof Player player)) {
            return;
        }

        stack.shrink(1);

        if (!level.isClientSide) {
            IPlayerWarp warp = ArcaneresearchCapabilities.getWarp(player);
            if (warp == null) {
                return;
            }

            int amount = 1;

            MobEffect ward = ModEffects.WARP_WARD.get();
            if (player.hasEffect(ward)) {
                amount++;
            }

            BlockPos pos = player.blockPosition();
            Block purifying = ModBlocks.PURIFYING_FLUID.get();
            if (level.getBlockState(pos).is(purifying)) {
                amount++;
            }

            if (warp.get(IPlayerWarp.EnumWarpType.NORMAL) > 0) {
                ArcaneresearchApi.internalMethods.addWarpToPlayer(
                        player, -amount, IPlayerWarp.EnumWarpType.NORMAL
                );
            }

            int temporary = warp.get(IPlayerWarp.EnumWarpType.TEMPORARY);
            if (temporary > 0) {
                ArcaneresearchApi.internalMethods.addWarpToPlayer(
                        player, -temporary, IPlayerWarp.EnumWarpType.TEMPORARY
                );
            }
        } else {
            doReleaseFX(level, player);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void doReleaseFX(Level level, Player player) {
        SoundEvent sound = ModSounds.CRAFT_START.get();
        level.playLocalSound(
                player.getX(), player.getY(), player.getZ(),
                sound, SoundSource.PLAYERS, 0.25F, 1.0F, false
        );

        RandomSource random = level.random;
        AABB box = player.getBoundingBox();
        double minY = box.minY;
        double height = player.getBbHeight();
        double baseX = player.getX() - 0.5F;
        double baseZ = player.getZ() - 0.5F;

        for (int a = 0; a < 40; a++) {
            FXDispatcher.INSTANCE.crucibleBubble(
                    (float) (baseX + random.nextFloat() * 1.5F),
                    (float) (minY + random.nextFloat() * height),
                    (float) (baseZ + random.nextFloat() * 1.5F),
                    1.0F, 0.7F, 0.9F
            );
        }
    }
}