package arcaneresearch.common.golems.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import arcaneresearch.common.entities.construct.EntityOwnedConstruct;

import java.util.EnumSet;

public class AIOwnerHurtTarget extends TargetGoal {

    private final EntityOwnedConstruct ownedEntity;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public AIOwnerHurtTarget(EntityOwnedConstruct entity) {
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

        ownerLastHurt = owner.getLastHurtMob();
        int lastHurtTime = owner.getLastHurtMobTimestamp();

        return lastHurtTime != timestamp
                && AINearestValidTarget.isGoodTarget(mob, ownerLastHurt, false, false);
    }

    @Override
    public void start() {
        mob.setTarget(ownerLastHurt);

        LivingEntity owner = ownedEntity.getOwner();
        if (owner != null) {
            timestamp = owner.getLastHurtMobTimestamp();
        }

        super.start();
    }
}
