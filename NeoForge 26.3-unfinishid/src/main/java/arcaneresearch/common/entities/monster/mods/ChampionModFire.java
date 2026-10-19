package arcaneresearch.common.entities.monster.mods;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.client.fx.FXDispatcher;

public class ChampionModFire implements IChampionModifierEffect {

    @Override
    public float performEffect(LivingEntity champion, LivingEntity target, DamageSource source, float amount) {

        if (target != null && !target.fireImmune()) {
            target.setRemainingFireTicks((5) * 20);
        }
        return amount;
    }

    @Override
    public void showFX(LivingEntity champion) {

        float w = champion.level().getRandom().nextFloat() * champion.getBbWidth();
        float d = champion.level().getRandom().nextFloat() * champion.getBbWidth();
        float h = champion.level().getRandom().nextFloat() * champion.getBbHeight();
        FXDispatcher.INSTANCE.drawGenericParticles(champion.getBoundingBox().minX + w,
                champion.getBoundingBox().minY + h, champion.getBoundingBox().minZ + d,
                0.0, 0.03, 0.0, 0.9f + champion.level().getRandom().nextFloat() * 0.1f, 1.0f, 1.0f,
                0.7f, false, 640, 10, 1, 8 + champion.level().getRandom().nextInt(4), 0,
                0.7f + champion.level().getRandom().nextFloat() * 0.2f, 0.0f, 0);
    }
}