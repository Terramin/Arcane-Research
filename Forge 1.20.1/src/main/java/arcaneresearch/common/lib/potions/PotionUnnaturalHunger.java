package arcaneresearch.common.lib.potions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
public class PotionUnnaturalHunger extends MobEffect {
    public PotionUnnaturalHunger() {
        super(MobEffectCategory.HARMFUL, 0x884422);
    }
    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
        if (target.level().isClientSide) return;
        if (target instanceof Player player) {
            player.causeFoodExhaustion(0.025f * (amplifier + 1));
        }
    }
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
