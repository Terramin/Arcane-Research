package arcaneresearch.common.items.casters;

import arcaneresearch.api.wands.IFocusHost;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class CasterAim {
    private static final double FORWARD = 0.6D;
    private static final double DOWN = 0.4D;

    private CasterAim() {
    }

    public static boolean isCasterHeld(Entity entity) {
        if (!(entity instanceof Player player)) {
            return false;
        }
        return (player.getMainHandItem().getItem() instanceof IFocusHost host && host.isCaster())
                || (player.getOffhandItem().getItem() instanceof IFocusHost host2 && host2.isCaster());
    }

    public static Vec3 origin(Entity entity) {
        Vec3 look = entity.getViewVector(1.0F);
        Vec3 right = new Vec3(-look.z, 0.0D, look.x);
        if (right.lengthSqr() < 1.0E-6D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }
        Vec3 up = right.cross(look).normalize();
        return entity.getEyePosition().add(look.scale(FORWARD)).add(up.scale(-DOWN));
    }
}
