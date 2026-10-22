package thaumictinker.client.awakenedarmor;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import thaumictinker.TtCore;
import thaumictinker.awakenedarmor.AwakenedArmor;
import thaumictinker.net.TtNet;

 





 
@Mod.EventBusSubscriber(modid = TtCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AwakenedArmorClient {

    public static final String CATEGORY = "key.categories.thaumictinker";

    public static final KeyMapping TOGGLE = new KeyMapping(
            "key.thaumictinker.toggle_armor", InputConstants.KEY_U, CATEGORY);

    private AwakenedArmorClient() {
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE);
        
        MinecraftForge.EVENT_BUS.register(TickHandler.class);
    }

      
    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new WingsLayer(renderer));
            }
        }
    }

      
    public static final class TickHandler {

        private TickHandler() {
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;
            while (TOGGLE.consumeClick()) {
                if (player != null && minecraft.screen == null && AwakenedArmor.wearsAny(player)) {
                    TtNet.sendToggleArmor(!AwakenedArmor.isEnabled(player));
                }
            }
        }
    }
}
