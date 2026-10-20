package arcaneresearch.common.golems.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;

import java.util.EnumSet;

public class AIArrowAttack extends Goal {

    private final Mob host;
    private final RangedAttackMob attacker;
    private final double speed;
    private final int minTime;
    private final int maxTime;
    private final float range;
    private final float maxDistSq;
    private int seeTime;
    private int attackTime = -1;

    public AIArrowAttack(RangedAttackMob attacker, double speed, int minTime, int maxTime, float range) {
        this.attacker = attacker;
        this.speed = speed;
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.range = range;
        this.maxDistSq = range * range;
        this.seeTime = 0;
        if (!(attacker instanceof Mob)) {
            throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
        }
        this.host = (Mob) attacker;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return host.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() || !host.getNavigation().isDone();
    }

    @Override
    public void stop() {
        seeTime = 0;
        attackTime = -1;
    }

    @Override
    public void tick() {
        LivingEntity target = host.getTarget();
        if (target == null) {
            return;
        }
        double distSq = host.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ());
        boolean canSee = host.getSensing().hasLineOfSight(target);
        if (canSee) {
            seeTime++;
        } else {
            seeTime = 0;
        }
        if (distSq <= maxDistSq && seeTime >= 20) {
            host.getNavigation().stop();
        } else {
            host.getNavigation().moveTo(target, speed);
        }
        host.getLookControl().setLookAt(target, 10.0f, 30.0f);
        if (--attackTime == 0) {
            if (distSq > maxDistSq || !canSee) {
                return;
            }
            float f = Mth.sqrt((float) distSq) / range;
            f = Mth.clamp(f, 0.1f, 1.0f);
            attacker.performRangedAttack(target, f);
            attackTime = Mth.floor(f * (maxTime - minTime) + minTime);
        } else if (attackTime < 0) {
            float f = Mth.sqrt((float) distSq) / range;
            attackTime = Mth.floor(f * (maxTime - minTime) + minTime);
        }
    }
}
