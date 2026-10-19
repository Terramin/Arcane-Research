package arcaneresearch.common.blocks.misc;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
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
public class BlockFluxGas extends LiquidBlock {
    public BlockFluxGas(Supplier<FlowingFluid> fluid) {
        super(fluid, BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .replaceable()
                .noCollission()
                .strength(100.0f)
                .pushReaction(PushReaction.DESTROY)
                .noLootTable()
                .liquid()
                .sound(SoundType.EMPTY)
                .randomTicks());
    }
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.isClientSide) {
            return;
        }
        int meta = state.getValue(LEVEL);
        BlockPos above = pos.above();
        if (level.isEmptyBlock(above) && above.getY() < level.getMaxBuildHeight()) {
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
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (level.isClientSide || level.random.nextInt(10) != 0
                || !(entity instanceof LivingEntity living)
                || entity instanceof ITaintedMob
                || living.getMobType() == MobType.UNDEAD
                || living.hasEffect(ModEffects.VIS_EXHAUST.get())
                || living.hasEffect(MobEffects.HUNGER)) {
            return;
        }
        int meta = state.getValue(LEVEL);
        if (level.random.nextBoolean()) {
            living.addEffect(new MobEffectInstance(ModEffects.VIS_EXHAUST.get(), 1200, meta / 3, true, true));
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
