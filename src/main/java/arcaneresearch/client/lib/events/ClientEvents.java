package arcaneresearch.client.lib.events;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.client.fx.other.MistEffect;
import arcaneresearch.client.lib.events.handler.ParticleHandler;
import arcaneresearch.common.lib.events.EssentiaHandler;
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {
    private static int tickCount = 0;
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            tickEssentiaStreams();
            ParticleHandler.tick();
            HudHandler.tickWarpVignette();
            TCShaderHandler.tick();
        }
        KeyHandler.onClientTick(event);
        if (event.phase == TickEvent.Phase.END) {
            HudHandler.tickKnowledgeGains();
            FogHandler.tick();
        }
        MistEffect.tick();
    }
    private static void tickEssentiaStreams() {
        if (EssentiaHandler.sourceFX.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        tickCount++;
        for (String fxk : EssentiaHandler.sourceFX.keySet().toArray(new String[0])) {
            EssentiaHandler.EssentiaSourceFX fx = EssentiaHandler.sourceFX.get(fxk);
            if (fx == null) {
                continue;
            }
            net.minecraft.core.BlockPos dest = fx.start;
            if (mc.level.getBlockEntity(dest)
                    instanceof arcaneresearch.common.tiles.crafting.TileInfusionMatrix) {
                dest = dest.below();
            }
            FXDispatcher.INSTANCE.essentiaTrailFx(fx.end, dest,
                    tickCount, fx.color, 0.1f, fx.ext);
            EssentiaHandler.sourceFX.remove(fxk);
        }
    }
    @SubscribeEvent
    public static void onLivingTick(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent event) {
        net.minecraft.world.entity.LivingEntity living = event.getEntity();
        if (living instanceof net.minecraft.world.entity.player.Player) {
            return;
        }
        if (!living.level().isClientSide || !living.isAlive()) {
            return;
        }
        int t = arcaneresearch.common.entities.monster.mods.ChampionModifier.getChampionId(living);
        if (t >= 0 && t < arcaneresearch.common.entities.monster.mods.ChampionModifier.MODS.length
                && arcaneresearch.common.entities.monster.mods.ChampionModifier.MODS[t] != null
                && arcaneresearch.common.entities.monster.mods.ChampionModifier.MODS[t].effect != null) {
            arcaneresearch.common.entities.monster.mods.ChampionModifier.MODS[t].effect.showFX(living);
        }
    }
    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
    }
}
