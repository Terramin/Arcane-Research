package arcaneresearch.client.lib.events;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber; 
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.client.fx.other.MistEffect;
import arcaneresearch.client.lib.events.handler.ParticleHandler;
import arcaneresearch.common.lib.events.EssentiaHandler;

@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class ClientEvents {

    private static int tickCount = 0;

    
    @SubscribeEvent
    public static void onClientTickPre(ClientTickEvent.Pre event) {
        tickEssentiaStreams();

        ParticleHandler.tick();

        HudHandler.tickWarpVignette();

        KeyHandler.onClientTick(event);

        
        
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            arcaneresearch.common.items.tools.ItemThaumometer.clientTick(mc.level, mc.player);
        }

        MistEffect.tick();
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        HudHandler.tickKnowledgeGains();
        FogHandler.tick();
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
    
    public static void onLivingTick(net.neoforged.neoforge.event.tick.EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof net.minecraft.world.entity.LivingEntity living)) {
            return;
        }
        if (living instanceof net.minecraft.world.entity.player.Player) {
            return;
        }
        if (!living.level().isClientSide() || !living.isAlive()) {
            return;
        }
        int t = arcaneresearch.common.entities.monster.mods.ChampionModifier.getChampionId(living);
        if (t >= 0 && t < arcaneresearch.common.entities.monster.mods.ChampionModifier.MODS.length
                && arcaneresearch.common.entities.monster.mods.ChampionModifier.MODS[t] != null
                && arcaneresearch.common.entities.monster.mods.ChampionModifier.MODS[t].effect != null) {
                arcaneresearch.common.entities.monster.mods.ChampionModifier.MODS[t].effect.showFX(living);
        }
    }
}