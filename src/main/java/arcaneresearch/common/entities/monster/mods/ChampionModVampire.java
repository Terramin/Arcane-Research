package arcaneresearch.common.entities.monster.mods;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.client.fx.FXDispatcher;

public class ChampionModVampire implements IChampionModifierEffect {

    @Override
    public float performEffect(LivingEntity champion, LivingEntity target, DamageSource source, float amount) {

        if (target != null && !champion.level().isClientSide() && amount > 0) {
            float healAmount = amount * 0.25f;
            champion.heal(healAmount);
        }
        return amount;
    }

    @Override
    public void showFX(LivingEntity champion) {

        if (champion.level().getRandom().nextFloat() > 0.2f) {
            return;
        }
        float w = champion.level().getRandom().nextFloat() * champion.getBbWidth();
        float d = champion.level().getRandom().nextFloat() * champion.getBbWidth();
        float h = champion.level().getRandom().nextFloat() * champion.getBbHeight();
        FXDispatcher.INSTANCE.drawGenericParticles(champion.getBoundingBox().minX + w,
                champion.getBoundingBox().minY + h, champion.getBoundingBox().minZ + d,
                0.0, 0.0, 0.0, 0.9f + champion.level().getRandom().nextFloat() * 0.1f, 0.0f, 0.0f,
                0.9f, false, 579, 4, 1, 4 + champion.level().getRandom().nextInt(4), 0,
                0.5f + champion.level().getRandom().nextFloat() * 0.2f, 0.0f, 0);
    }
}