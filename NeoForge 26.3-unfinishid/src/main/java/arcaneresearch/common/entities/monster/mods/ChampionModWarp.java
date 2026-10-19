package arcaneresearch.common.entities.monster.mods;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.client.fx.FXDispatcher;

public class ChampionModWarp implements IChampionModifierEffect {

    @Override
    public float performEffect(LivingEntity champion, LivingEntity target, DamageSource source, float amount) {

        if (target instanceof Player player && !player.level().isClientSide()) {
            IPlayerWarp warp = ArcaneresearchCapabilities.getWarp(player);
            if (warp != null && player.level().getRandom().nextInt(3) == 0) {
                warp.add(IPlayerWarp.EnumWarpType.TEMPORARY, 1 + player.level().getRandom().nextInt(3));
            }
        }
        return amount;
    }

    @Override
    public void showFX(LivingEntity champion) {

        if (champion.level().getRandom().nextBoolean()) {
            return;
        }
        float w = champion.level().getRandom().nextFloat() * champion.getBbWidth();
        float d = champion.level().getRandom().nextFloat() * champion.getBbWidth();
        float h = champion.level().getRandom().nextFloat() * champion.getBbHeight();
        FXDispatcher.INSTANCE.drawGenericParticles(champion.getBoundingBox().minX + w,
                champion.getBoundingBox().minY + h, champion.getBoundingBox().minZ + d,
                0.0, 0.0, 0.0, 0.8f + champion.level().getRandom().nextFloat() * 0.2f, 0.0f,
                0.9f + champion.level().getRandom().nextFloat() * 0.1f,
                0.7f, true, 264, 8, 1, 10 + champion.level().getRandom().nextInt(4), 0,
                0.6f + champion.level().getRandom().nextFloat() * 0.4f, 0.0f, 0);
    }
}