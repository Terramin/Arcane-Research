package arcaneresearch.common.lib.potions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import arcaneresearch.common.blocks.world.taint.BlockFluxGoo;
import arcaneresearch.init.ModBlocks;

public class PotionThaumarhia extends MobEffect {

    public PotionThaumarhia() {
        super(MobEffectCategory.HARMFUL, 0x9900FF);
    }

    @Override
    public boolean applyEffectTick(net.minecraft.server.level.ServerLevel serverLevel, LivingEntity target, int amplifier) {
        

        Level level = target.level();
        BlockPos pos = target.blockPosition();

        if (level.getRandom().nextInt(15) == 0 && level.isEmptyBlock(pos)) {
            level.setBlockAndUpdate(pos, BlockFluxGoo.withLevel(ModBlocks.FLUX_GOO.get(), 2 + level.getRandom().nextInt(3)));
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}
