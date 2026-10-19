package arcaneresearch.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber; // NEOFORGE_PORT: was Mod
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.gui.screens.WandFocusPouchScreen;
import arcaneresearch.common.menu.WandMenuTypes;

// NEOFORGE_PORT: FMLClientSetupEvent.enqueueWork(MenuScreens/EntityRenderers
// .register) -> RegisterMenuScreensEvent/RegisterRenderers (same pairs).
// ItemWand.initializeClient(Consumer) is DEAD in 26.x (Item.initializeClient
// removed): the first-person use animation (WandUseAnimation.HAND_EXTENSION,
// applyForgeHandTransform hook itself still exists) must be registered here
// via RegisterClientExtensionsEvent, otherwise the wand has no use animation.
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class WandClientSetup {
    @SubscribeEvent
    public static void onRegisterClientExtensions(
            net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event) {
        try {
            event.registerItem(
                    arcaneresearch.client.renderers.item.WandUseAnimation.HAND_EXTENSION,
                    arcaneresearch.common.items.wands.WandItems.WAND.get());
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(
                WandMenuTypes.WAND_FOCUS_POUCH.get(), WandFocusPouchScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                arcaneresearch.common.entities.WandEntities.EMBER.get(),
                arcaneresearch.client.renderers.entity.NoProjectileRenderer::new);
        event.registerEntityRenderer(
                arcaneresearch.common.entities.WandEntities.EXPLOSIVE_ORB.get(),
                arcaneresearch.client.renderers.entity.NoProjectileRenderer::new);
        event.registerEntityRenderer(
                arcaneresearch.common.entities.WandEntities.PRIMAL_ORB.get(),
                arcaneresearch.client.renderers.entity.NoProjectileRenderer::new);
        event.registerEntityRenderer(
                arcaneresearch.common.entities.WandEntities.FROST_SHARD.get(),
                net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        event.registerEntityRenderer(
                arcaneresearch.common.entities.WandEntities.SHOCK_ORB.get(),
                net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        event.registerEntityRenderer(
                arcaneresearch.common.entities.WandEntities.PRIMAL_ORB.get(),
                net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        event.registerEntityRenderer(
                arcaneresearch.common.entities.WandEntities.PECH_BLAST.get(),
                net.minecraft.client.renderer.entity.ThrownItemRenderer::new);

        event.registerEntityRenderer(
                arcaneresearch.common.entities.WandEntities.HELLBAT.get(),
                arcaneresearch.client.renderers.entity.FireBatRenderer::new);

        // NEOFORGE_PORT: Forge registered these via direct BlockEntityRenderers.register
        // here; 26.x equivalent is event.registerBlockEntityRenderer (same pairs).
        event.registerBlockEntityRenderer(
                arcaneresearch.common.blocks.aura.AuraBlockEntities.AURA_NODE.get(),
                arcaneresearch.client.renderers.tile.AuraNodeRenderer::new);
        event.registerBlockEntityRenderer(
                arcaneresearch.common.blocks.aura.AuraBlockEntities.JAR_NODE.get(),
                arcaneresearch.client.renderers.tile.JarNodeRenderer::new);
    }
}