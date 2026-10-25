package arcaneresearch.discovered.client;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.discovered.table.DiscoveredTableSetup;
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class DiscoveredTableClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            if (DiscoveredTableSetup.getMenuType() != null) {
                MenuScreens.register(DiscoveredTableSetup.getMenuType(), CognitionNoteTableScreen::new);
            }
            if (DiscoveredTableSetup.getBlock() != null) {
                ItemBlockRenderTypes.setRenderLayer(DiscoveredTableSetup.getBlock(), RenderType.cutout());
            }
            if (DiscoveredTableSetup.getSingleTable() != null) {
                ItemBlockRenderTypes.setRenderLayer(DiscoveredTableSetup.getSingleTable(), RenderType.cutout());
            }
        });
    }
}
