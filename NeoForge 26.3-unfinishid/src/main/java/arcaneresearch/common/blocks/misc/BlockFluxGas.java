package arcaneresearch.common.blocks.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import arcaneresearch.api.entities.ITaintedMob;
import arcaneresearch.init.ModEffects;

import java.util.function.Supplier;

/**
 * Flux gas, ported from TC4 BlockFluxGas. A rising finite fluid: it floats
 * upward, dissipates, and afflicts living things it touches with vis
 * exhaustion or hunger. No bucket (TC4 had none either).
 */
public class BlockFluxGas extends LiquidBlock {

    public BlockFluxGas(BlockBehaviour.Properties props, Supplier<FlowingFluid> fluid) {
        super(fluid.get(), props
                .mapColor(MapColor.COLOR_PURPLE)
                .replaceable()
                .noCollision()
                .strength(100.0f)
                .pushReaction(PushReaction.POPPED)
                .noLootTable()
                .liquid()
                .sound(SoundType.EMPTY)
                .randomTicks());
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isClientSide()) {
            return;
        }
        int meta = state.getValue(LEVEL);
        BlockPos above = pos.above();
        if (level.isEmptyBlock(above) && above.getY() < level.getMaxY()) {
            int rising = Math.min(8, meta + 1);
            level.setBlock(above, defaultBlockState().setValue(LEVEL, rising), 3);
            if (meta >= 7) {
                level.removeBlock(pos, false);
            } else {
                level.setBlock(pos, state.setValue(LEVEL, meta + 1), 3);
            }
            return;
        }
        if (random.nextInt(20) == 0) {
            if (meta >= 7) {
                level.removeBlock(pos, false);
            } else {
                level.setBlock(pos, state.setValue(LEVEL, meta + 1), 3);
            }
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                net.minecraft.world.entity.InsideBlockEffectApplier applier, boolean runningEffects) {
        super.entityInside(state, level, pos, entity, applier, runningEffects);
        if (level.isClientSide() || level.getRandom().nextInt(10) != 0
                || !(entity instanceof LivingEntity living)
                || entity instanceof ITaintedMob
                || living.isInvertedHealAndHarm()
                || living.hasEffect(ModEffects.VIS_EXHAUST)
                || living.hasEffect(MobEffects.HUNGER)) {
            return;
        }
        int meta = state.getValue(LEVEL);
        if (level.getRandom().nextBoolean()) {
            living.addEffect(new MobEffectInstance(ModEffects.VIS_EXHAUST, 1200, meta / 3, true, true));
        } else {
            living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 80 + meta * 20, 0, false, true));
        }
        if (meta > 0) {
            level.setBlock(pos, state.setValue(LEVEL, meta - 1), 3);
        } else {
            level.removeBlock(pos, false);
        }
    }
}
