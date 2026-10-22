package arcaneresearch.common.items.consumables;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.init.ModBlocks;
import arcaneresearch.init.ModEffects;
import arcaneresearch.init.ModSounds;

public class ItemSanitySoap extends Item {
    public ItemSanitySoap(Item.Properties props) {
        super(props);
    }

    
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 100;
    }

    
    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    
    
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        int ticks = getUseDuration(stack, entity) - remainingUseDuration;
        if (ticks > 95) {
            entity.stopUsingItem();
        }
        if (level.isClientSide()) {
            doUseFX(level, entity);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void doUseFX(Level level, LivingEntity entity) {
        if (level.getRandom().nextFloat() < 0.2F) {
            level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 0.1F,
                    1.5F + level.getRandom().nextFloat() * 0.2F, false);
        }
        for (int a = 0; a < 10; a++) {
            FXDispatcher.INSTANCE.crucibleBubble(
                    (float) entity.getX() - 0.5F + level.getRandom().nextFloat(),
                    (float) entity.getBoundingBox().minY
                            + level.getRandom().nextFloat() * entity.getBbHeight(),
                    (float) entity.getZ() - 0.5F + level.getRandom().nextFloat(),
                    1.0F, 0.8F, 0.9F);
        }
    }

    
    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        int held = getUseDuration(stack, entity) - timeLeft;
        if (held > 95 && entity instanceof Player player) {
            stack.shrink(1);
            if (!level.isClientSide()) {
                IPlayerWarp warp = ArcaneresearchCapabilities.getWarp(player);
                if (warp != null) {
                    int amt = 1;
                    
                    
                    if (player.hasEffect(ModEffects.WARP_WARD)) amt++;
                    int i = Mth.floor(player.getX());
                    int j = Mth.floor(player.getY());
                    int k = Mth.floor(player.getZ());
                    if (level.getBlockState(new BlockPos(i, j, k)).is(ModBlocks.PURIFYING_FLUID.get())) amt++;
                    if (warp.get(IPlayerWarp.EnumWarpType.NORMAL) > 0) {
                        ArcaneresearchApi.internalMethods.addWarpToPlayer(player, -amt,
                                IPlayerWarp.EnumWarpType.NORMAL);
                    }
                    if (warp.get(IPlayerWarp.EnumWarpType.TEMPORARY) > 0) {
                        ArcaneresearchApi.internalMethods.addWarpToPlayer(player,
                                -warp.get(IPlayerWarp.EnumWarpType.TEMPORARY),
                                IPlayerWarp.EnumWarpType.TEMPORARY);
                    }
                }
            } else {
                doReleaseFX(level, player);
            }
            return true;
        }
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    private void doReleaseFX(Level level, Player player) {
        level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                ModSounds.CRAFT_START.get(), SoundSource.PLAYERS, 0.25F, 1.0F, false);
        for (int a = 0; a < 40; a++) {
            FXDispatcher.INSTANCE.crucibleBubble(
                    (float) player.getX() - 0.5F + level.getRandom().nextFloat() * 1.5F,
                    (float) player.getBoundingBox().minY
                            + level.getRandom().nextFloat() * player.getBbHeight(),
                    (float) player.getZ() - 0.5F + level.getRandom().nextFloat() * 1.5F,
                    1.0F, 0.7F, 0.9F);
        }
    }
}
