package arcaneresearch.api.potions;

import net.minecraft.resources.Identifier;
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
    static final Identifier rl = Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/potions.png");

    public PotionFluxTaint() {
        super(MobEffectCategory.HARMFUL, 0x6600AA);
    }

    public PotionFluxTaint(boolean isBadEffect, int liquidColor) {
        super(isBadEffect ? MobEffectCategory.HARMFUL : MobEffectCategory.BENEFICIAL, liquidColor);
    }

    // NEOFORGE_PORT: applyEffectTick(LivingEntity,int) -> applyEffectTick(ServerLevel,LivingEntity,int)
    // returning boolean; hurt(DamageSource,float) -> hurtServer(ServerLevel,DamageSource,float).
    @Override
    public boolean applyEffectTick(net.minecraft.server.level.ServerLevel level, LivingEntity target, int amplifier) {
        if (target.level().isClientSide()) return false;

        if (target instanceof ITaintedMob) {
            target.heal(1);
            return true;
        }

        if (ChampionModifier.getChampionId(target) == 13) {
            target.heal(1);
            return true;
        }

        if (!target.isInvertedHealAndHarm()
                && (target.getMaxHealth() > 1 || target instanceof Player)) {
            return target.hurtServer(level, DamageSourceArcaneresearch.createTaint(target.level()), 1);
        }
        return false;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int k = 40 >> amplifier;
        return k > 0 ? duration % k == 0 : true;
    }
}
