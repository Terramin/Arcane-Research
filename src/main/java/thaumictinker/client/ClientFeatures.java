package thaumictinker.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import thaumictinker.TtCore;
import thaumictinker.crops.CropsFeature;
import thaumictinker.elements.ElementsFeature;
import thaumictinker.enchanting.EnchantingFeature;
import thaumictinker.funnel.FunnelFeature;
import thaumictinker.gas.GasFeature;

 







 
@Mod.EventBusSubscriber(modid = TtCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientFeatures {

    private ClientFeatures() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ClientFeatures::registerRenderLayers);
    }

    private static void registerRenderLayers() {
        
        ItemBlockRenderTypes.setRenderLayer(CropsFeature.INFUSED_GRAIN, RenderType.cutout());
        
        ItemBlockRenderTypes.setRenderLayer(ElementsFeature.AER_FIRE, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ElementsFeature.AQUA_FIRE, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ElementsFeature.ORDO_FIRE, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ElementsFeature.PERDITIO_FIRE, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ElementsFeature.TERRA_FIRE, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ElementsFeature.IGNIS_FIRE, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ElementsFeature.FORCEFIELD, RenderType.cutout());
        
        ItemBlockRenderTypes.setRenderLayer(FunnelFeature.FUNNEL, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(EnchantingFeature.ENCHANTER, RenderType.cutout());
        
        ItemBlockRenderTypes.setRenderLayer(GasFeature.GASEOUS_LIGHT, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(GasFeature.GASEOUS_SHADOW, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(GasFeature.NITOR_GAS, RenderType.cutout());
    }
}
