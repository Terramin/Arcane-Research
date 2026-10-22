package arcaneresearch.common.golems.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.common.golems.EntityArcaneresearchGolem;
import arcaneresearch.common.golems.tasks.TaskHandler;

import java.util.ArrayList;

public class AIGotoBlock extends AIGoto {

    public AIGotoBlock(EntityArcaneresearchGolem golem) {
        super(golem, (byte) 0);
    }

    @Override
    public void tick() {
        super.tick();

        Task task = golem.getTask();
        if (golem.getLookControl() != null && task != null) {
            golem.getLookControl().setLookAt(
                    task.getPos().getX() + 0.5,
                    task.getPos().getY() + 0.5,
                    task.getPos().getZ() + 0.5,
                    10.0f,
                    (float) golem.getMaxHeadXRot()
            );
        }
    }

    @Override
    protected void moveTo() {
        Task task = golem.getTask();
        if (task == null) return;

        if (targetBlock != null) {
            golem.getNavigation().moveTo(
                    targetBlock.getX() + 0.5,
                    targetBlock.getY() + 0.5,
                    targetBlock.getZ() + 0.5,
                    golem.getGolemMoveSpeed()
            );
        } else {
            golem.getNavigation().moveTo(
                    task.getPos().getX() + 0.5,
                    task.getPos().getY() + 0.5,
                    task.getPos().getZ() + 0.5,
                    golem.getGolemMoveSpeed()
            );
        }
    }

    @Override
    protected boolean findDestination() {
        ArrayList<Task> tasks = TaskHandler.getBlockTasksSorted(
                golem.level().dimension(),
                golem.getUUID(),
                golem
        );

        for (Task task : tasks) {
            if (areGolemTagsValidForTask(task) &&
                    task.canGolemPerformTask(golem) &&
                    golem.isWithinHome(task.getPos())    &&
                    isValidDestination(golem.level(), task.getPos()) &&
                    canEasilyReach(task.getPos())) {

                targetBlock = getAdjacentSpace(task.getPos());
                golem.setTask(task);
                task.setReserved(true);

                if (arcaneresearch.common.config.ModConfig.CONFIG_GRAPHICS.showGolemEmotes)
                    golem.level().broadcastEntityEvent(golem, (byte) 5);
                return true;
            }
        }

        return false;
    }

    private BlockPos getAdjacentSpace(BlockPos pos) {
        double closestDist = Double.MAX_VALUE;
        BlockPos closest = null;

        for (Direction face : Direction.Plane.HORIZONTAL) {
            BlockPos adjacent = pos.relative(face);
            BlockState state = golem.level().getBlockState(adjacent);

            
            if (!(state.isSolid() && !state.is(net.minecraft.world.level.block.Blocks.COBWEB)
                    && !state.is(net.minecraft.world.level.block.Blocks.BAMBOO_SAPLING))) {
                double dist = adjacent.distToCenterSqr(golem.getX(), golem.getY(), golem.getZ());
                if (dist < closestDist) {
                    closest = adjacent;
                    closestDist = dist;
                }
            }
        }

        return closest;
    }

    private boolean canEasilyReach(BlockPos pos) {
        if (golem.distanceToSqr(Vec3.atCenterOf(pos)) < minDist) {
            return true;
        }

        Path path = golem.getNavigation().createPath(pos, 0);
        if (path == null) {
            return false;
        }

        Node finalNode = path.getEndNode();
        if (finalNode == null) {
            return false;
        }

        int dx = finalNode.x - Mth.floor(pos.getX());
        int dy = finalNode.y - Mth.floor(pos.getY());
        int dz = finalNode.z - Mth.floor(pos.getZ());

        if (dx == 0 && dz == 0 && dy == 2) {
            dy--;
        }

        return dx * dx + dy * dy + dz * dz < 2.25;
    }
}
