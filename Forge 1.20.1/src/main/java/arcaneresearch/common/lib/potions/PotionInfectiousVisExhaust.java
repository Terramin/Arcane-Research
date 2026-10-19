package arcaneresearch.common.lib.potions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import arcaneresearch.init.ModEffects;
import java.util.List;
public class PotionInfectiousVisExhaust extends MobEffect {
    public PotionInfectiousVisExhaust() {
        super(MobEffectCategory.HARMFUL, 0x557755);
    }
    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
        if (target.level().isClientSide) return;
        Level level = target.level();
        AABB searchBox = target.getBoundingBox().inflate(4.0, 4.0, 4.0);
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, searchBox);
        for (LivingEntity entity : nearbyEntities) {
            if (entity == target) continue;
            if (!entity.hasEffect(ModEffects.INFECTIOUS_VIS_EXHAUST.get())) {
                if (amplifier > 0) {
                    entity.addEffect(new MobEffectInstance(
                            ModEffects.INFECTIOUS_VIS_EXHAUST.get(),
                            6000,
                            amplifier - 1,
                            false,
                            true
                    ));
                } else {
                    entity.addEffect(new MobEffectInstance(
                            ModEffects.VIS_EXHAUST.get(),
                            6000,
                            0,
                            false,
                            true
                    ));
                }
            }
        }
    }
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 40 == 0;
    }
}
