package arcaneresearch.common.golems.ai;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.api.golems.EnumGolemTrait;
import arcaneresearch.api.golems.seals.ISealEntity;
import arcaneresearch.api.golems.tasks.Task;
import arcaneresearch.common.golems.EntityArcaneresearchGolem;
import arcaneresearch.common.golems.seals.SealHandler;
import arcaneresearch.common.golems.tasks.TaskHandler;
import java.util.Arrays;
import java.util.EnumSet;
public abstract class AIGoto extends Goal {
    protected EntityArcaneresearchGolem golem;
    protected int taskCounter;
    protected byte type;
    protected int cooldown;
    protected double minDist;
    protected BlockPos targetBlock;
    protected int pause;
    private BlockPos prevRamble;
    public AIGoto(EntityArcaneresearchGolem golem, byte type) {
        this.golem = golem;
        this.type = type;
        this.taskCounter = -1;
        this.minDist = 4.0;
        this.pause = 0;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }
    @Override
    public boolean canUse() {
        if (golem.level().isClientSide) {
            return false;
        }
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        cooldown = 5;
        if (golem.getTask() != null && !golem.getTask().isSuspended()) {
            return false;
        }
        targetBlock = null;
        boolean start = findDestination();
        if (start && golem.getTask() != null && golem.getTask().getSealPos() != null) {
            ISealEntity sealEntity = SealHandler.getSealEntity(golem.level().dimension(), golem.getTask().getSealPos());
            if (sealEntity != null) {
                sealEntity.getSeal().onTaskStarted(golem.level(), golem, golem.getTask());
            }
        }
        return start;
    }
    @Override
    public void start() {
        moveTo();
        taskCounter = 0;
    }
    protected abstract void moveTo();
    @Override
    public boolean canContinueToUse() {
        return taskCounter >= 0 &&
                taskCounter <= 1000 &&
                golem.getTask() != null &&
                !golem.getTask().isSuspended() &&
                isValidDestination(golem.level(), golem.getTask().getPos());
    }
    @Override
    public void tick() {
        Task task = golem.getTask();
        if (task == null) return;
        if (pause-- <= 0) {
            double dist;
            if (task.getType() == 0) {
                BlockPos targetPos = (targetBlock != null) ? targetBlock : task.getPos();
                dist = golem.distanceToSqr(Vec3.atCenterOf(targetPos));
            } else {
                dist = golem.distanceToSqr(task.getEntity());
            }
            if (dist > minDist) {
                task.setCompletion(false);
                taskCounter++;
                if (taskCounter % 5 == 0) {
                    BlockPos currentPos = golem.blockPosition();
                    if (prevRamble != null && prevRamble.equals(currentPos)) {
                        Vec3 targetVec = Vec3.atCenterOf(task.getPos());
                        Vec3 randomPos = DefaultRandomPos.getPosTowards(golem, 6, 4, targetVec, Math.PI);
                        if (randomPos != null) {
                            golem.getNavigation().moveTo(randomPos.x + 0.5, randomPos.y + 0.5, randomPos.z + 0.5,
                                    golem.getGolemMoveSpeed());
                        }
                    } else {
                        moveTo();
                    }
                    prevRamble = currentPos;
                }
            } else {
                TaskHandler.completeTask(task, golem);
                if (task.isCompleted()) {
                    if (taskCounter >= 0) {
                        taskCounter = 0;
                    }
                    pause = 0;
                } else {
                    pause = 10;
                    taskCounter++;
                }
                taskCounter--;
            }
        }
    }
    @Override
    public void stop() {
        Task task = golem.getTask();
        if (task != null) {
            if (!task.isCompleted() && task.isReserved()) {
                if (arcaneresearch.common.config.ModConfig.CONFIG_GRAPHICS.showGolemEmotes)
                    golem.level().broadcastEntityEvent(golem, (byte) 6);
            }
            if (task.isCompleted() && !task.isSuspended()) {
                task.setSuspended(true);
            }
            task.setReserved(false);
        }
    }
    protected abstract boolean findDestination();
    protected boolean isValidDestination(Level level, BlockPos pos) {
        return true;
    }
    protected boolean areGolemTagsValidForTask(Task task) {
        ISealEntity sealEntity = SealHandler.getSealEntity(golem.level().dimension(), task.getSealPos());
        if (sealEntity == null || sealEntity.getSeal() == null) {
            return true;
        }
        java.util.UUID ownerId = golem.getOwnerUUID();
        if (sealEntity.isLocked() && (ownerId == null
                || sealEntity.getOwner() == null
                || !ownerId.toString().equals(sealEntity.getOwner()))) {
            return false;
        }
        EnumGolemTrait[] required = sealEntity.getSeal().getRequiredTags();
        if (required != null && !golem.getProperties().getTraits().containsAll(Arrays.asList(required))) {
            return false;
        }
        EnumGolemTrait[] forbidden = sealEntity.getSeal().getForbiddenTags();
        if (forbidden != null) {
            for (EnumGolemTrait trait : forbidden) {
                if (golem.getProperties().getTraits().contains(trait)) {
                    return false;
                }
            }
        }
        return true;
    }
}
