package arcaneresearch.client.lib.events;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier; 
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.init.ModEffects;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;








public final class TCShaderHandler {

    public static final int SHADER_DESAT = 0;
    public static final int SHADER_BLUR = 1;
    public static final int SHADER_HUNGER = 2;
    public static final int SHADER_SUNSCORNED = 3;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier[] SHADERS = {
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "desaturatetc"),
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "blurtc"),
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "hunger"),
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "sunscorned")
    };

    private static final Map<UUID, Identifier> APPLIED = new ConcurrentHashMap<>();

    private TCShaderHandler() {
    }

    public static void tickServer(ServerPlayer player) {
        Identifier want = null;
        if (!arcaneresearch.common.config.ModConfig.CONFIG_GRAPHICS.disableShaders) {
            if (player.hasEffect(ModEffects.DEATH_GAZE)) {
                want = SHADERS[SHADER_DESAT];
            } else if (player.hasEffect(ModEffects.BLURRED_VISION)) {
                want = SHADERS[SHADER_BLUR];
            } else if (player.hasEffect(ModEffects.UNNATURAL_HUNGER)) {
                want = SHADERS[SHADER_HUNGER];
            } else if (player.hasEffect(ModEffects.SUN_SCORNED)) {
                want = SHADERS[SHADER_SUNSCORNED];
            }
        }

        UUID uuid = player.getUUID();
        Identifier last = APPLIED.get(uuid);
        if (Objects.equals(want, last)
                && (want == null || player.getPostEffects().contains(want))) {
            return;
        }
        for (Identifier id : SHADERS) {
            if (!id.equals(want) && player.getPostEffects().contains(id)) {
                player.removePostEffect(id);
            }
        }
        if (want != null) {
            if (player.addPostEffect(want)) {
                LOGGER.info("[Arcaneresearch] post shader on: {}", want);
            }
            APPLIED.put(uuid, want);
        } else {
            if (last != null) {
                LOGGER.info("[Arcaneresearch] post shader off (was {})", last);
            }
            APPLIED.remove(uuid);
        }
    }

    public static void onPlayerLoggedOut(UUID uuid) {
        APPLIED.remove(uuid);
    }
}
