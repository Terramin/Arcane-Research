package arcaneresearch.common.entities.monster.mods;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.client.fx.FXDispatcher;

public class ChampionModBold implements IChampionModifierEffect {

    @Override
    public float performEffect(LivingEntity champion, LivingEntity target, DamageSource source, float amount) {

        return 0.0f;
    }

    @Override
    public void showFX(LivingEntity champion) {

        if (champion.level().getRandom().nextBoolean()) {
            return;
        }
        float w = champion.level().getRandom().nextFloat() * champion.getBbWidth();
        float d = champion.level().getRandom().nextFloat() * champion.getBbWidth();
        float h = champion.level().getRandom().nextFloat() * champion.getBbHeight() / 3.0f;
        FXDispatcher.INSTANCE.spark(champion.getBoundingBox().minX + w, champion.getBoundingBox().minY + h,
                champion.getBoundingBox().minZ + d, 0.2f, 0.3f - champion.level().getRandom().nextFloat() * 0.1f,
                0.0f, 0.8f + champion.level().getRandom().nextFloat() * 0.2f, 1.0f);
    }
}