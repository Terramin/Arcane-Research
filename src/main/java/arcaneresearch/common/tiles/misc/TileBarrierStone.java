package arcaneresearch.common.tiles.misc;

import arcaneresearch.common.tiles.TileArcaneresearch;
import arcaneresearch.init.ModBlockEntities;
import arcaneresearch.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TileBarrierStone extends TileArcaneresearch {

    private int tickCount = 0;

    public TileBarrierStone(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BARRIER_STONE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TileBarrierStone tile) {
        if (tile.tickCount == 0) {
            tile.tickCount = level.random.nextInt(100);
        }

        if (tile.tickCount % 5 == 0 && !tile.isGettingPower()) {
            AABB area = new AABB(
                    pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + 1, pos.getY() + 3, pos.getZ() + 1
            ).inflate(0.1);

            List<LivingEntity> entities = level.getEntitiesOfClass(
                    LivingEntity.class,
                    area,
                    e -> !e.onGround() && !(e instanceof Player)
            );

            for (LivingEntity entity : entities) {
                float radians = (entity.getYRot() + 180.0f) * ((float) Math.PI / 180.0f);
                Vec3 push = new Vec3(
                        -Math.sin(radians) * 0.2,
                        -0.1,
                        Math.cos(radians) * 0.2
                );
                entity.setDeltaMovement(entity.getDeltaMovement().add(push));
            }
        }

        if (++tile.tickCount % 100 == 0) {
            Block barrierBlock = ModBlocks.BARRIER.get();
            BlockState barrierState = barrierBlock.defaultBlockState();

            BlockPos pos1 = pos.above(1);
            BlockState state1 = level.getBlockState(pos1);
            if (!state1.is(barrierBlock) && state1.isAir()) {
                level.setBlock(pos1, barrierState, Block.UPDATE_ALL);
            }

            BlockPos pos2 = pos.above(2);
            BlockState state2 = level.getBlockState(pos2);
            if (!state2.is(barrierBlock) && state2.isAir()) {
                level.setBlock(pos2, barrierState, Block.UPDATE_ALL);
            }
        }
    }

    public boolean isGettingPower() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }
}