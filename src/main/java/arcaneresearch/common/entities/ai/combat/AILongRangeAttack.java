package arcaneresearch.common.entities.ai.combat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
public class AILongRangeAttack extends RangedAttackGoal {
    private final Mob mob;
    private final double minDistance;
    public AILongRangeAttack(RangedAttackMob attacker, double minDistance, double moveSpeed,
                             int attackIntervalMin, int attackIntervalMax, float attackRadius) {
        super(attacker, moveSpeed, attackIntervalMin, attackIntervalMax, attackRadius);
        this.mob = (Mob) attacker;
        this.minDistance = minDistance;
    }
    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }
        LivingEntity target = mob.getTarget();
        if (target == null) {
            return false;
        }
        if (!target.isAlive()) {
            mob.setTarget(null);
            return false;
        }
        return mob.distanceToSqr(target) >= minDistance * minDistance;
    }
}
