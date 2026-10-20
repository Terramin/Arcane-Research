package arcaneresearch.common.lib.potions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
public class PotionBlurredVision extends MobEffect {
    public PotionBlurredVision() {
        super(MobEffectCategory.HARMFUL, 0x888888);
    }
    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
    }
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
