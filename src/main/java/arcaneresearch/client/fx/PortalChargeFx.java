package arcaneresearch.client.fx;

import arcaneresearch.common.entities.EntityPortal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
public final class PortalChargeFx {
    private static final Map<UUID, Integer> CHARGE = new HashMap<>();

    private PortalChargeFx() {
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.isPaused()) {
            return;
        }
        Set<UUID> inside = new HashSet<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof EntityPortal portal)) {
                continue;
            }
            if (portal.isClosing() || portal.tickCount < EntityPortal.OPEN_ANIMATION_TICKS) {
                continue;
            }
            AABB slab = EntityPortal.screenBounds(
                    portal.getX(), portal.getY(), portal.getZ(),
                    portal.getPortalYaw(0f), portal.getPortalWidth(), portal.getPortalHeight());
            for (Player player : level.players()) {
                if (!player.isAlive() || player.isSpectator()) {
                    continue;
                }
                if (player.getBoundingBox().intersects(slab)) {
                    inside.add(player.getUUID());
                }
            }
        }
        for (UUID id : inside) {
            CHARGE.merge(id, 1, Integer::sum);
        }
        CHARGE.keySet().removeIf(u -> !inside.contains(u));
    }

    public static float getAmount(UUID id) {
        if (id == null) {
            return 0.0F;
        }
        int ticks = CHARGE.getOrDefault(id, 0);
        if (ticks <= 0) {
            return 0.0F;
        }
        float progress = Math.min(ticks / (float) EntityPortal.STAND_TELEPORT_TICKS, 1.0F);
        return 0.25F + 0.75F * progress;
    }
}
