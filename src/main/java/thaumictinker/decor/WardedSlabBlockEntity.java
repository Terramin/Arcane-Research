package thaumictinker.decor;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import arcaneresearch.common.blocks.aura.AuraBlocks;
import arcaneresearch.init.ModBlocks;

 








 
public class WardedSlabBlockEntity extends BlockEntity {
    private int count;

    public WardedSlabBlockEntity(BlockPos pos, BlockState state) {
        super(DecorFeature.WARDED_SLAB, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockEntity entity) {
        if (entity instanceof WardedSlabBlockEntity blockEntity) {
            blockEntity.tick(level, pos);
        }
    }

    private void tick(Level level, BlockPos pos) {
        if (count == 0) {
            count = level.getRandom().nextInt(100);
        }
        if (count % 5 == 0 && !level.hasNeighborSignal(pos)) {
            AABB area = new AABB(pos.getX(), pos.getY() - 1, pos.getZ(), pos.getX() + 1, pos.getY() + 3, pos.getZ() + 1).inflate(0.1, 0.1, 0.1);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area);
            for (LivingEntity target : targets) {
                if (!target.onGround() && !(target instanceof Player)) {
                    float angle = (target.getYRot() + 180.0F) * (float) Math.PI / 180.0F;
                    target.push(-Mth.sin(angle) * 0.2F, -0.1, Mth.cos(angle) * 0.2F);
                }
            }
        }
        count++;
        if (count % 100 == 0) {
            placeAura(level, pos.above());
            placeAura(level, pos.above(2));
        }
    }

    private static void placeAura(Level level, BlockPos target) {
        BlockState existing = level.getBlockState(target);
        if (!existing.is(ModBlocks.EFFECT_GLIMMER.get()) && existing.canBeReplaced()) {
            level.setBlock(target, AuraBlocks.AIRY.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }
}
