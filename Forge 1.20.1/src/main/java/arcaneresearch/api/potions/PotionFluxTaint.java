package arcaneresearch.api.potions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import arcaneresearch.api.damagesource.DamageSourceArcaneresearch;
import arcaneresearch.api.entities.ITaintedMob;
import arcaneresearch.common.entities.monster.mods.ChampionModifier;
public class PotionFluxTaint extends MobEffect {
    public static MobEffect instance = null;
    private int statusIconIndex = -1;
    static final ResourceLocation rl = new ResourceLocation("arcaneresearch", "textures/misc/potions.png");
    public PotionFluxTaint() {
        super(MobEffectCategory.HARMFUL, 0x6600AA);
    }
    public PotionFluxTaint(boolean isBadEffect, int liquidColor) {
        super(isBadEffect ? MobEffectCategory.HARMFUL : MobEffectCategory.BENEFICIAL, liquidColor);
    }
    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
        if (target.level().isClientSide) return;
        if (target instanceof ITaintedMob) {
            target.heal(1);
            return;
        }
        if (ChampionModifier.getChampionId(target) == 13) {
            target.heal(1);
            return;
        }
        if (!target.isInvertedHealAndHarm()
                && (target.getMaxHealth() > 1 || target instanceof Player)) {
            target.hurt(DamageSourceArcaneresearch.createTaint(target.level()), 1);
        }
    }
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int k = 40 >> amplifier;
        return k > 0 ? duration % k == 0 : true;
    }
}
