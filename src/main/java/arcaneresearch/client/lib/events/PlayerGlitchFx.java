package arcaneresearch.client.lib.events;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.fx.PortalChargeFx;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public final class PlayerGlitchFx {
    private static final long STEP_MS = 40L;
    private static final Map<Integer, float[]> JITTER = new HashMap<>();

    private PlayerGlitchFx() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        PortalChargeFx.tick();
    }

    private static Player playerFor(AvatarRenderState state) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        return level.getEntity(state.id) instanceof Player player ? player : null;
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre<?> event) {
        AvatarRenderState state = event.getRenderState();
        Player player = playerFor(state);
        if (player == null) {
            return;
        }
        float amount = PortalChargeFx.getAmount(player.getUUID());
        if (amount <= 0.01F) {
            return;
        }
        long step = Util.getMillis() / STEP_MS;
        Random random = new Random(((long) player.getId() << 12) ^ step);
        if (random.nextFloat() > amount * 0.8F) {
            return;
        }
        float dx = (random.nextFloat() - 0.5F) * 0.12F * amount;
        float dy = (random.nextFloat() - 0.5F) * 0.08F * amount;
        event.getPoseStack().translate(dx, dy, 0.0F);
        JITTER.put(state.id, new float[]{dx, dy});
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post<?> event) {
        float[] jitter = JITTER.remove(event.getRenderState().id);
        if (jitter != null) {
            PoseStack ps = event.getPoseStack();
            ps.translate(-jitter[0], -jitter[1], 0.0F);
        }
    }
}
