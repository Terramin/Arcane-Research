package arcaneresearch.client.lib.events;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.fx.PortalChargeFx;
import net.minecraft.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class PlayerGlitchFx {
    private static final long STEP_MS = 40L;
    private static final Map<Integer, float[]> JITTER = new HashMap<>();

    private PlayerGlitchFx() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            PortalChargeFx.tick();
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
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
        JITTER.put(player.getId(), new float[]{dx, dy});
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        float[] jitter = JITTER.remove(player.getId());
        if (jitter != null) {
            PoseStack ps = event.getPoseStack();
            ps.translate(-jitter[0], -jitter[1], 0.0F);
        }
    }
}
