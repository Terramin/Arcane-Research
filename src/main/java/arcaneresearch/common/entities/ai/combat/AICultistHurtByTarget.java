package arcaneresearch.common.entities.ai.combat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.phys.AABB;
import arcaneresearch.common.entities.monster.cult.EntityCultist;
import java.util.List;
public class AICultistHurtByTarget extends HurtByTargetGoal {
    private final boolean callsHelp;
    public AICultistHurtByTarget(PathfinderMob mob, boolean callsHelp) {
        this(mob, callsHelp, EntityCultist.class);
    }
    public AICultistHurtByTarget(PathfinderMob mob, boolean callsHelp, Class<?>... toIgnoreDamage) {
        super(mob, toIgnoreDamage);
        this.callsHelp = callsHelp;
    }
    @Override
    public void start() {
        super.start();
        if (callsHelp) {
            alertOthers();
        }
    }
    @Override
    protected void alertOthers() {
        double range = getFollowDistance();
        AABB box = mob.getBoundingBox().inflate(range, 10.0, range);
        List<EntityCultist> cultists = mob.level().getEntitiesOfClass(EntityCultist.class, box);
        LivingEntity revengeTarget = mob.getLastHurtByMob();
        for (EntityCultist cultist : cultists) {
            if (mob == cultist || cultist.getTarget() != null) {
                continue;
            }
            if (revengeTarget != null && cultist.isAlliedTo(revengeTarget)) {
                continue;
            }
            setEntityAttackTarget(cultist, revengeTarget);
        }
    }
    protected void setEntityAttackTarget(Mob mob, LivingEntity target) {
        mob.setTarget(target);
    }
}
