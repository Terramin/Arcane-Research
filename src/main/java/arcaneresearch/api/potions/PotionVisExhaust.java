package arcaneresearch.api.potions;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class PotionVisExhaust extends MobEffect {
    public PotionVisExhaust() {
        super(MobEffectCategory.HARMFUL, 0x555577);
    }

    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
