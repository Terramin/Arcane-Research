package arcaneresearch.common.lib.potions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
public class PotionDeathGaze extends MobEffect {
    public PotionDeathGaze() {
        super(MobEffectCategory.HARMFUL, 0x220022);
    }
    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
    }
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
