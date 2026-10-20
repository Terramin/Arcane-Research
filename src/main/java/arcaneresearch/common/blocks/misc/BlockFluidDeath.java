package arcaneresearch.common.blocks.misc;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.damagesource.DamageSourceArcaneresearch;
import arcaneresearch.client.fx.FXDispatcher;
import java.util.function.Supplier;
public class BlockFluidDeath extends LiquidBlock {
    public BlockFluidDeath(Supplier<FlowingFluid> fluid) {
        super(fluid, BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .replaceable()
                .noCollission()
                .strength(100.0f)
                .pushReaction(PushReaction.DESTROY)
                .noLootTable()
                .liquid()
                .sound(SoundType.EMPTY)
                .lightLevel(state -> 3)
                .randomTicks());
    }
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        int fluidLevel = state.getValue(LEVEL);
        float quantaPct = fluidLevel >= 8 ? 0.25f : (8 - fluidLevel) / 8.0f;
        float slow = 1.0f - quantaPct / 2.0f;
        Vec3 motion = entity.getDeltaMovement();
        entity.setDeltaMovement(motion.x * slow, motion.y, motion.z * slow);
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            float damage = fluidLevel >= 8 ? 2.0f : (8 - fluidLevel) / 2.0f + 1.0f;
            living.hurt(DamageSourceArcaneresearch.createDissolve(level), damage);
        }
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(20) == 0) {
            float h = random.nextFloat() * 0.075f;
            FXDispatcher.INSTANCE.slimyBubbleFX(
                    pos.getX() + random.nextFloat(),
                    pos.getY() + 0.6f + random.nextFloat() * 0.3f,
                    pos.getZ() + random.nextFloat(),
                    0.075f + h,
                    0.3f - random.nextFloat() * 0.1f,
                    0.0f,
                    0.4f + random.nextFloat() * 0.1f
            );
        }
        if (random.nextInt(50) == 0) {
            double x = pos.getX() + random.nextFloat();
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + random.nextFloat();
            level.playLocalSound(x, y, z, SoundEvents.LAVA_POP, SoundSource.BLOCKS,
                    0.1f + random.nextFloat() * 0.1f, 0.9f + random.nextFloat() * 0.15f, false);
        }
    }
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (state.getValue(LEVEL) != 0 && random.nextInt(200) == 0) {
            level.removeBlock(pos, false);
        }
    }
}
