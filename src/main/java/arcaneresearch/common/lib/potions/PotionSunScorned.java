package arcaneresearch.common.lib.potions;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class PotionSunScorned extends MobEffect {

    public PotionSunScorned() {
        super(MobEffectCategory.HARMFUL, 0xFF8800);
    }

    @Override
    public boolean applyEffectTick(net.minecraft.server.level.ServerLevel serverLevel, LivingEntity target, int amplifier) {
        

        Level level = target.level();
        float brightness = target.getLightLevelDependentMagicValue();
        BlockPos pos = BlockPos.containing(
                Mth.floor(target.getX()),
                Mth.floor(target.getY()),
                Mth.floor(target.getZ())
        );

        if (brightness > 0.5f
                && level.getRandom().nextFloat() * 30.0f < (brightness - 0.4f) * 2.0f
                && level.canSeeSky(pos)) {
            // NEOFORGE_PORT (26.3): Forge setSecondsOnFire(4) = only-if-longer + fire-protection
            // dampening; 26.x equivalent is igniteForSeconds (BURNING_TIME attribute), not a raw set.
            target.igniteForSeconds(4.0F);
        } else if (brightness < 0.25f && level.getRandom().nextFloat() > brightness * 2.0f) {
            target.heal(1.0f);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 40 == 0;
    }
}
