package arcaneresearch.common.golems.ai;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.common.golems.EntityArcaneresearchGolem;
import java.util.EnumSet;
public class AIGotoHome extends Goal {
    protected EntityArcaneresearchGolem golem;
    protected int idleCounter;
    private double movePosX;
    private double movePosY;
    private double movePosZ;
    public AIGotoHome(EntityArcaneresearchGolem golem) {
        this.golem = golem;
        this.idleCounter = 10;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }
    @Override
    public boolean canUse() {
        if (idleCounter > 0) {
            --idleCounter;
            return false;
        }
        idleCounter = 50;
        if (!golem.hasRestriction()) {
            return false;
        }
        BlockPos homePos = golem.getRestrictCenter();
        double distSq = golem.distanceToSqr(Vec3.atCenterOf(homePos));
        if (distSq < 5.0) {
            return false;
        }
        if (distSq <= 1024.0) {
            movePosX = homePos.getX();
            movePosY = homePos.getY();
            movePosZ = homePos.getZ();
            return true;
        }
        Vec3 homeVec = Vec3.atCenterOf(homePos);
        Vec3 randomPos = DefaultRandomPos.getPosTowards(golem, 16, 7, homeVec, Math.PI);
        if (randomPos == null) {
            return false;
        }
        movePosX = randomPos.x;
        movePosY = randomPos.y;
        movePosZ = randomPos.z;
        return true;
    }
    @Override
    public void start() {
        golem.getNavigation().moveTo(movePosX, movePosY, movePosZ, golem.getGolemMoveSpeed());
    }
    @Override
    public boolean canContinueToUse() {
        if (golem.getTask() != null) {
            return false;
        }
        if (golem.getNavigation().isDone()) {
            return false;
        }
        if (!golem.hasRestriction()) {
            return false;
        }
        BlockPos homePos = golem.getRestrictCenter();
        double distSq = golem.distanceToSqr(Vec3.atCenterOf(homePos));
        return distSq > 3.0;
    }
    @Override
    public void stop() {
        idleCounter = 50;
        golem.getNavigation().stop();
    }
}
