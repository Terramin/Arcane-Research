package arcaneresearch.common.lib.potions;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class PotionWarpWard extends MobEffect {
    public PotionWarpWard() {
        super(MobEffectCategory.BENEFICIAL, 0xFFFFDD);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity target, int amplifier) {
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
