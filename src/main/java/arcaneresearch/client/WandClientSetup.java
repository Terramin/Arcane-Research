package arcaneresearch.client;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.gui.screens.WandFocusPouchScreen;
import arcaneresearch.common.menu.WandMenuTypes;
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class WandClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(
                    WandMenuTypes.WAND_FOCUS_POUCH.get(), WandFocusPouchScreen::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(
                    arcaneresearch.common.entities.WandEntities.EMBER.get(),
                    arcaneresearch.client.renderers.entity.NoProjectileRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(
                    arcaneresearch.common.entities.WandEntities.EXPLOSIVE_ORB.get(),
                    arcaneresearch.client.renderers.entity.NoProjectileRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(
                    arcaneresearch.common.entities.WandEntities.PRIMAL_ORB.get(),
                    arcaneresearch.client.renderers.entity.NoProjectileRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(
                    arcaneresearch.common.entities.WandEntities.FROST_SHARD.get(),
                    net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(
                    arcaneresearch.common.entities.WandEntities.SHOCK_ORB.get(),
                    net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(
                    arcaneresearch.common.entities.WandEntities.PRIMAL_ORB.get(),
                    net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(
                    arcaneresearch.common.entities.WandEntities.PECH_BLAST.get(),
                    net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
            net.minecraft.client.renderer.entity.EntityRenderers.register(
                    arcaneresearch.common.entities.WandEntities.HELLBAT.get(),
                    arcaneresearch.client.renderers.entity.FireBatRenderer::new);
            net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                    arcaneresearch.common.blocks.aura.AuraBlockEntities.AURA_NODE.get(),
                    arcaneresearch.client.renderers.tile.AuraNodeRenderer::new);
            net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(
                    arcaneresearch.common.blocks.aura.AuraBlockEntities.JAR_NODE.get(),
                    arcaneresearch.client.renderers.tile.JarNodeRenderer::new);
        });
    }
}
