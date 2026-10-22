package arcaneresearch.client.renderers.item;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import arcaneresearch.Arcaneresearch;



@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class SpecialItemModels {

    @SubscribeEvent
    public static void registerSpecialRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "node"),
                NodeSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "node_device"),
                NodeDeviceSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "hungry_chest"),
                HungryChestSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "caster_focus"),
                CasterFocusSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "wand"),
                WandSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "wand_staff"),
                WandSpecialRenderer.StaffUnbaked.MAP_CODEC);
        event.register(
                Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "eldritch_portal"),
                EldritchPortalSpecialRenderer.Unbaked.MAP_CODEC);
    }
}