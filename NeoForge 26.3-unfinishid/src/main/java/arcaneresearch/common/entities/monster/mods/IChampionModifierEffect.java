package arcaneresearch.common.entities.monster.mods;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;

public interface IChampionModifierEffect {

    float performEffect(LivingEntity champion, LivingEntity target, DamageSource source, float amount);

    void showFX(LivingEntity champion);

    default void preRender(LivingEntity champion) {

    }
}