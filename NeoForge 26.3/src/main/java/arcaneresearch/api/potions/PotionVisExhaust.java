package arcaneresearch.api.potions;

import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class PotionVisExhaust extends MobEffect {

    public static MobEffect instance = null;

    private int statusIconIndex = -1;
    static final Identifier rl = Identifier.fromNamespaceAndPath("arcaneresearch", "textures/misc/potions.png");

    public PotionVisExhaust() {
        super(MobEffectCategory.HARMFUL, 0x555577);
    }

    public PotionVisExhaust(boolean isBadEffect, int liquidColor) {
        super(isBadEffect ? MobEffectCategory.HARMFUL : MobEffectCategory.BENEFICIAL, liquidColor);
    }

    // NEOFORGE_PORT: see PotionFluxTaint (tick hooks renamed + boolean result).
    @Override
    public boolean applyEffectTick(net.minecraft.server.level.ServerLevel level, LivingEntity target, int amplifier) {
        return true; // NEOFORGE_PORT (26.3): false would remove the effect (Forge hook was a void no-op).
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}
