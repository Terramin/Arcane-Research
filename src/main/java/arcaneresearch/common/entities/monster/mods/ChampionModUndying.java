package arcaneresearch.common.entities.monster.mods;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.client.fx.FXDispatcher;

public class ChampionModUndying implements IChampionModifierEffect {

    @Override
    public float performEffect(LivingEntity champion, LivingEntity target, DamageSource source, float amount) {

        if (!champion.level().isClientSide() && champion.tickCount % 20 == 0) {
            float currentHealth = champion.getHealth();
            float maxHealth = champion.getMaxHealth();
            if (currentHealth < maxHealth) {
                champion.setHealth(Math.min(currentHealth + 0.5f, maxHealth));
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
                0.0, 0.03, 0.0, 0.1f + champion.level().getRandom().nextFloat() * 0.1f,
                0.8f + champion.level().getRandom().nextFloat() * 0.2f, 0.1f + champion.level().getRandom().nextFloat() * 0.1f,
                0.9f, true, 69, 4, 1, 4 + champion.level().getRandom().nextInt(4), 0,
                0.5f + champion.level().getRandom().nextFloat() * 0.2f, 0.0f, 0);
    }
}