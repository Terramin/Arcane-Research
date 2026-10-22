package thaumictinker.client.kami;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import thaumictinker.kami.foci.KamiFociFeature;
import thaumictinker.kami.foci.XpDrainFocus;

 


 
@Mod.EventBusSubscriber(modid = thaumictinker.TtCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class KamiFociClient {

    private KamiFociClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
                EntityRenderers.register(KamiFociFeature.BEAM_TYPE, BeamRenderer::new));
        XpDrainFocus.useClientTicks(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            return minecraft.player == null ? 0 : minecraft.player.tickCount;
        });
    }
}
