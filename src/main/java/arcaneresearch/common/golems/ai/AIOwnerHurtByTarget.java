package arcaneresearch.common.golems.ai;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import arcaneresearch.common.entities.construct.EntityOwnedConstruct;
import java.util.EnumSet;
public class AIOwnerHurtByTarget extends TargetGoal {
    private final EntityOwnedConstruct ownedEntity;
    private LivingEntity ownerAttacker;
    private int timestamp;
    public AIOwnerHurtByTarget(EntityOwnedConstruct entity) {
        super(entity, false);
        this.ownedEntity = entity;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }
    @Override
    public boolean canUse() {
        if (!ownedEntity.isOwned()) {
            return false;
        }
        LivingEntity owner = ownedEntity.getOwner();
        if (owner == null) {
            return false;
        }
        ownerAttacker = owner.getLastHurtByMob();
        int lastHurtTime = owner.getLastHurtByMobTimestamp();
        return lastHurtTime != timestamp
                && AINearestValidTarget.isGoodTarget(mob, ownerAttacker, false, false);
    }
    @Override
    public void start() {
        mob.setTarget(ownerAttacker);
        LivingEntity owner = ownedEntity.getOwner();
        if (owner != null) {
            timestamp = owner.getLastHurtByMobTimestamp();
        }
        super.start();
    }
}
