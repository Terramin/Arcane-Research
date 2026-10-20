package arcaneresearch.common.entities.monster.mods;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.client.fx.FXDispatcher;

public class ChampionModSpined implements IChampionModifierEffect {

    @Override
    public float performEffect(LivingEntity champion, LivingEntity target, DamageSource source, float amount) {

        if (target != null && !source.is(DamageTypeTags.IS_PROJECTILE) &&
                !source.is(DamageTypeTags.BYPASSES_ARMOR)) {

            float reflectedDamage = amount * 0.25f;
            if (reflectedDamage > 0.5f) {
                target.hurt(champion.damageSources().thorns(champion), reflectedDamage);
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
        int p = 704 + champion.level().getRandom().nextInt(4) * 3;
        FXDispatcher.INSTANCE.drawGenericParticles(champion.getBoundingBox().minX + w,
                champion.getBoundingBox().minY + h, champion.getBoundingBox().minZ + d,
                0.0, 0.0, 0.0, 0.5f + champion.level().getRandom().nextFloat() * 0.2f,
                0.1f + champion.level().getRandom().nextFloat() * 0.2f, 0.1f + champion.level().getRandom().nextFloat() * 0.2f,
                0.7f, false, p, 3, 1, 3, 0, 1.2f + champion.level().getRandom().nextFloat() * 0.3f, 0.0f, 0);
    }
}