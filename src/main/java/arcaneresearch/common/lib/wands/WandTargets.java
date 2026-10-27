package arcaneresearch.common.lib.wands;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import arcaneresearch.common.lib.utils.EntityUtils;

import javax.annotation.Nullable;

public class WandTargets {
    private WandTargets() {
    }

    public static Entity getPointedEntity(Level level, Entity entity, double minrange, double range, float padding) {
        return EntityUtils.getPointedEntity(level, entity, minrange, range, padding, false);
    }

    public static Entity getPointedEntity(Level level, Entity entity, double range, Class<? extends LivingEntity> cls) {
        Entity hit = EntityUtils.getPointedEntity(level, entity, 0.0D, range, 1.1F, false);
        if (hit != null && cls.isInstance(hit)) {
            return hit;
        }
        return null;
    }

    /**
     * Same as {@link #getPointedEntity(Level, Entity, double, double, float)}, but if the aim
     * ray finds nothing it falls back to the nearest living entity inside a short forward cone
     * around the caster. Standing right next to a mob (or a small mob sitting under the
     * crosshair) used to make the eye ray miss, so the cast dealt no damage even though the
     * target was touching the player. Both sides call this, so the server damage target and
     * the client-side bolt endpoint stay in sync.
     */
    @Nullable
    public static Entity getPointedEntityOrClose(Level level, Entity entity, double minrange, double range,
                                                 float padding, double closeRange, double minDot) {
        Entity pointed = getPointedEntity(level, entity, minrange, range, padding);
        if (pointed != null || closeRange <= 0.0D) {
            return pointed;
        }
        Vec3 eyePos = new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ());
        Vec3 look = entity.getLookAngle();
        AABB area = entity.getBoundingBox().inflate(closeRange);
        Entity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity candidate : level.getEntities(entity, area)) {
            if (!(candidate instanceof LivingEntity) || !candidate.isPickable() || candidate.isRemoved()) {
                continue;
            }
            if (eyePos.distanceTo(candidate.position()) < minrange) {
                continue;
            }
            Vec3 center = new Vec3(candidate.getX(),
                    candidate.getY() + candidate.getBbHeight() * 0.5D, candidate.getZ());
            Vec3 to = center.subtract(eyePos);
            double dist = to.length();
            if (dist < 1.0E-4D || dist > closeRange) {
                continue;
            }
            if (to.scale(1.0D / dist).dot(look) < minDot) {
                continue;
            }
            if (level.clip(new ClipContext(eyePos, center, ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE, entity)).getType() != HitResult.Type.MISS) {
                continue;
            }
            if (dist < bestDist) {
                bestDist = dist;
                best = candidate;
            }
        }
        return best;
    }
}
