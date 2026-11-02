package arcaneresearch.common.lib.wands;

import arcaneresearch.common.lib.utils.EntityUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class WandTargets {

    private WandTargets() {
    }

    public static Entity getPointedEntity(Level level, Entity entity, double minrange, double range, float padding) {
        return EntityUtils.getPointedEntity(level, entity, minrange, range, padding, false);
    }

    public static Entity getPointedEntity(Level level, Entity entity, double range,
                                          Class<? extends LivingEntity> cls) {
        if (cls == null || entity == null || level == null) return null;
        Entity hit = EntityUtils.getPointedEntity(level, entity, 0.0D, range, 1.1F, false);
        return cls.isInstance(hit) ? hit : null;
    }

    @Nullable
    public static Entity getPointedEntityOrClose(Level level, Entity entity, double minrange, double range,
                                                 float padding, double closeRange, double minDot) {
        if (level == null || entity == null) return null;

        Entity pointed = getPointedEntity(level, entity, minrange, range, padding);
        if (pointed != null || !(closeRange > 0.0D)) return pointed;

        final double eyeY = entity.getY() + entity.getEyeHeight();
        final Vec3 eyePos = new Vec3(entity.getX(), eyeY, entity.getZ());
        final Vec3 look = entity.getLookAngle();
        final AABB area = entity.getBoundingBox().inflate(closeRange);

        final double closeRangeSq = closeRange * closeRange;
        final double minrangeClamped = Math.max(0.0D, minrange);
        final double minrangeSq = minrangeClamped * minrangeClamped;

        final double eyeX = eyePos.x;
        final double eyeZ = eyePos.z;
        final double lookX = look.x;
        final double lookY = look.y;
        final double lookZ = look.z;

        Entity best = null;
        double bestDistSq = Double.MAX_VALUE;

        for (Entity candidate : level.getEntities(entity, area)) {
            if (!(candidate instanceof LivingEntity)) continue;
            if (!candidate.isPickable() || candidate.isRemoved()) continue;

            if (eyePos.distanceToSqr(candidate.position()) < minrangeSq) continue;

            double cx = candidate.getX();
            double cy = candidate.getY() + candidate.getBbHeight() * 0.5D;
            double cz = candidate.getZ();

            double dx = cx - eyeX;
            double dy = cy - eyeY;
            double dz = cz - eyeZ;

            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq < 1.0E-8D || distSq > closeRangeSq) continue;

            double dist = Math.sqrt(distSq);
            double dot = (dx * lookX + dy * lookY + dz * lookZ) / dist;
            if (dot < minDot) continue;

            Vec3 center = new Vec3(cx, cy, cz);
            if (level.clip(new ClipContext(eyePos, center,
                            ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))
                    .getType() != HitResult.Type.MISS) {
                continue;
            }

            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                best = candidate;
            }
        }

        return best;
    }
}