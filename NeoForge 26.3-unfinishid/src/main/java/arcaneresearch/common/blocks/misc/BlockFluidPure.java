package arcaneresearch.common.blocks.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.capabilities.IPlayerWarp;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.init.ModEffects;

import java.util.function.Supplier;

public class BlockFluidPure extends LiquidBlock {

    public BlockFluidPure(BlockBehaviour.Properties props, Supplier<FlowingFluid> fluid) {
        super(fluid.get(), props
                .mapColor(MapColor.SNOW)
                .replaceable()
                .noCollision()
                .strength(100.0f)
                .pushReaction(PushReaction.POPPED)
                .noLootTable()
                .liquid()
                .sound(SoundType.EMPTY)
                .lightLevel(state -> 5)
                .randomTicks());
    }

    private static float getQuantaPercentage(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof LiquidBlock)) {
            return 0.0f;
        }

        int lvl = state.getValue(LiquidBlock.LEVEL);
        return lvl >= 8 ? 0.25f : (8 - lvl) / 8.0f;
    }

    private static boolean isSourceBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof LiquidBlock && state.getValue(LiquidBlock.LEVEL) == 0;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                net.minecraft.world.entity.InsideBlockEffectApplier applier, boolean runningEffects) {
        super.entityInside(state, level, pos, entity, applier, runningEffects);

        float quantaPct = getQuantaPercentage(level, pos);
        float slow = 1.0f - quantaPct / 2.0f;
        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(motion.x * slow, motion.y, motion.z * slow);

        if (!level.isClientSide() && isSourceBlock(level, pos) && entity instanceof Player player) {
            if (!player.hasEffect(ModEffects.WARP_WARD)) {
                IPlayerWarp warpCap = ArcaneresearchCapabilities.getWarp(player);
                int permWarp = warpCap != null ? warpCap.get(IPlayerWarp.EnumWarpType.PERMANENT) : 0;
                int div = 1;
                if (permWarp > 0) {
                    div = (int) Math.sqrt(permWarp);
                    if (div < 1) {
                        div = 1;
                    }
                }
                int duration = Math.min(32000, 200000 / div);
                player.addEffect(new MobEffectInstance(ModEffects.WARP_WARD, duration, 0, true, true));
                level.removeBlock(pos, false);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {

        if (random.nextInt(10) == 0) {
            int meta = state.getValue(LEVEL);
            float height = 0.125f * (8 - Math.min(8, meta));
            FXDispatcher.INSTANCE.sparkle(
                    (float) (pos.getX() + random.nextFloat()),
                    (float) (pos.getY() + height),
                    (float) (pos.getZ() + random.nextFloat()),
                    1.0f, 1.0f, 1.0f);
        }
        if (random.nextInt(50) == 0) {
            double x = pos.getX() + random.nextFloat();
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + random.nextFloat();
            level.playLocalSound(x, y, z, SoundEvents.LAVA_POP, SoundSource.BLOCKS,
                    0.1f + random.nextFloat() * 0.1f, 0.9f + random.nextFloat() * 0.15f, false);
        }
    }
}