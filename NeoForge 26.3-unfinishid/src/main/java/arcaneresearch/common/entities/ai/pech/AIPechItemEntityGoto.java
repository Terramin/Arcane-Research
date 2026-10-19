package arcaneresearch.common.entities.ai.pech;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import arcaneresearch.common.entities.monster.EntityPech;

import java.util.EnumSet;
import java.util.List;

public class AIPechItemEntityGoto extends Goal {

    private final EntityPech pech;
    private final float maxTargetDistance = 16.0f;
    private Entity targetEntity;
    private int cooldown;
    private int failedPathFindingPenalty;

    public AIPechItemEntityGoto(EntityPech pech) {
        this.pech = pech;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (--cooldown > 0) {
            return false;
        }
        targetEntity = null;
        double range = Double.MAX_VALUE;
        AABB box = pech.getBoundingBox().inflate(maxTargetDistance);
        List<Entity> targets = pech.level().getEntities(pech, box,
                e -> e instanceof ItemEntity item

                        && !item.getPersistentData().getBooleanOr("PechDrop", false)
                        && pech.canPickup(item.getItem()));
        for (Entity e : targets) {
            double distance = e.distanceToSqr(pech);
            if (distance >= range || distance > maxTargetDistance * maxTargetDistance) {
                continue;
            }
            range = distance;
            targetEntity = e;
        }
        return targetEntity != null;
    }

    @Override
    public boolean canContinueToUse() {
        return targetEntity != null && targetEntity.isAlive()
                && (!pech.getNavigation().isDone()
                && targetEntity.distanceToSqr(pech) < maxTargetDistance * maxTargetDistance);
    }

    @Override
    public void stop() {
        targetEntity = null;
    }

    @Override
    public void start() {
        double speed = pech.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.5;
        pech.getNavigation().moveTo(targetEntity, speed);
        cooldown = 0;
    }

    @Override
    public void tick() {
        if (targetEntity == null) {
            return;
        }
        pech.getLookControl().setLookAt(targetEntity, 30.0f, 30.0f);
        if (pech.getSensing().hasLineOfSight(targetEntity) && --cooldown <= 0) {
            cooldown = failedPathFindingPenalty + 4 + pech.getRandom().nextInt(4);
            double speed = pech.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.5;
            if (pech.getNavigation().moveTo(targetEntity, speed)) {
                failedPathFindingPenalty = 0;
            } else {
                failedPathFindingPenalty += 10;
            }
        }
        double distance = pech.distanceToSqr(targetEntity.getX(),
                targetEntity.getBoundingBox().minY, targetEntity.getZ());
        if (distance <= 1.5 && targetEntity instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            int before = stack.getCount();
            ItemStack remaining = pech.pickupItem(stack);
            if (remaining == null || remaining.isEmpty() || remaining.getCount() <= 0) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(remaining);
            }
            if (remaining == null || remaining.isEmpty() || remaining.getCount() != before) {
                pech.level().playSound(null, targetEntity.getX(), targetEntity.getY(), targetEntity.getZ(),
                        SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.2f,
                        ((pech.getRandom().nextFloat() - pech.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
            }
            cooldown = 0;
        }
    }
}
