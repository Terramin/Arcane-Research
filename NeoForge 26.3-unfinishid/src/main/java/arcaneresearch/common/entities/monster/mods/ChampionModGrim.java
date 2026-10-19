package arcaneresearch.common.entities.monster.mods;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.client.fx.FXDispatcher;

public class ChampionModGrim implements IChampionModifierEffect {

    @Override
    public float performEffect(LivingEntity champion, LivingEntity target, DamageSource source, float amount) {

        if (target != null && !target.level().isClientSide()) {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
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
                0.0, -0.02, 0.0, champion.level().getRandom().nextFloat() * 0.2f,
                champion.level().getRandom().nextFloat() * 0.2f, champion.level().getRandom().nextFloat() * 0.2f,
                0.8f, false, 640, 10, 1, 8 + champion.level().getRandom().nextInt(4), 0,
                0.6f + champion.level().getRandom().nextFloat() * 0.4f, 0.0f, 0);
    }
}