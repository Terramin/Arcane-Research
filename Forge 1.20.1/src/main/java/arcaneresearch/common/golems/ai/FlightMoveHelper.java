package arcaneresearch.common.golems.ai;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;
public class FlightMoveHelper extends MoveControl {
    public FlightMoveHelper(Mob mob) {
        super(mob);
    }
    @Override
    public void tick() {
        if (operation == Operation.MOVE_TO && !mob.getNavigation().isDone()) {
            operation = Operation.WAIT;
            double dx = wantedX - mob.getX();
            double dy = wantedY - mob.getY();
            double dz = wantedZ - mob.getZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist < 1.0E-5) {
                mob.setSpeed(0.0f);
                return;
            }
            double verticalFactor = dy / dist;
            float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0f;
            float yawDiff = Mth.wrapDegrees(yaw - mob.getYRot());
            mob.setYRot(mob.getYRot() + Mth.clamp(yawDiff, -30.0f, 30.0f));
            mob.yBodyRot = mob.getYRot();
            float baseSpeed = (float) (speedModifier * mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
            mob.setSpeed(mob.getSpeed() + (baseSpeed - mob.getSpeed()) * 0.125f);
            double bob = Math.sin((mob.tickCount + mob.getId()) * 0.5) * 0.05;
            double yawRad = mob.getYRot() * Math.PI / 180.0;
            double driftX = bob * Math.cos(yawRad);
            double driftZ = bob * Math.sin(yawRad);
            double bobY = Math.sin((mob.tickCount + mob.getId()) * 0.75) * 0.05;
            Vec3 motion = mob.getDeltaMovement();
            double mx = motion.x + driftX;
            double mz = motion.z + driftZ;
            double my = motion.y + bobY * (Math.sin(yawRad) + Math.cos(yawRad)) * 0.25
                    + mob.getSpeed() * verticalFactor * 0.1;
            mob.setDeltaMovement(mx, my, mz);
            double lookX = mob.getX() + dx / dist * 2.0;
            double lookY = mob.getEyeY() + verticalFactor * 1.0;
            double lookZ = mob.getZ() + dz / dist * 2.0;
            if (mob.getLookControl().isLookingAtTarget()) {
                lookX = mob.getLookControl().getWantedX() + (lookX - mob.getLookControl().getWantedX()) * 0.125;
                lookY = mob.getLookControl().getWantedY() + (lookY - mob.getLookControl().getWantedY()) * 0.125;
                lookZ = mob.getLookControl().getWantedZ() + (lookZ - mob.getLookControl().getWantedZ()) * 0.125;
            }
            mob.getLookControl().setLookAt(lookX, lookY, lookZ, 10.0f, 40.0f);
        } else {
            mob.setSpeed(0.0f);
        }
    }
}
