package arcaneresearch.common.lib.wands;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import arcaneresearch.common.lib.utils.EntityUtils;
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
}
