package arcaneresearch.common.lib.utils;

import arcaneresearch.Arcaneresearch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;


@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public final class RegistryNbtClient {
    private RegistryNbtClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        RegistryNbt.clientLookup = () -> {
            ClientLevel level = Minecraft.getInstance().level;
            return level != null ? level.registryAccess() : null;
        };
    }
}
