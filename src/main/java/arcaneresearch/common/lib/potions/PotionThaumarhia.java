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
    public void applyEffectTick(LivingEntity target, int amplifier) {
        if (target.level().isClientSide) return;
        Level level = target.level();
        BlockPos pos = target.blockPosition();
        if (level.random.nextInt(15) == 0 && level.isEmptyBlock(pos)) {
            level.setBlockAndUpdate(pos, BlockFluxGoo.withLevel(ModBlocks.FLUX_GOO.get(), 2 + level.random.nextInt(3)));
        }
    }
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }
}
