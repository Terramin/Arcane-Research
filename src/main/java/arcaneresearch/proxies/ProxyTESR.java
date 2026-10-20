package arcaneresearch.proxies;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import arcaneresearch.init.ModBlockEntities;

public class ProxyTESR {
    public void setupTESR(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.CRUCIBLE.get(),
                arcaneresearch.client.renderers.tile.CrucibleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DIOPTRA.get(),
                arcaneresearch.client.renderers.tile.DioptraRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PEDESTAL.get(),
                arcaneresearch.client.renderers.tile.PedestalRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RECHARGE_PEDESTAL.get(),
                arcaneresearch.client.renderers.tile.RechargePedestalRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FOCAL_MANIPULATOR.get(),
                arcaneresearch.client.renderers.tile.FocalManipulatorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HUNGRY_CHEST.get(),
                arcaneresearch.client.renderers.tile.HungryChestRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TUBE_ONEWAY.get(),
                arcaneresearch.client.renderers.tile.TubeOnewayRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TUBE_VALVE.get(),
                arcaneresearch.client.renderers.tile.TubeValveRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TUBE_BUFFER.get(),
                arcaneresearch.client.renderers.tile.TubeBufferRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.JAR.get(),
                arcaneresearch.client.renderers.tile.JarRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.JAR_VOID.get(),
                arcaneresearch.client.renderers.tile.JarRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BELLOWS.get(),
                arcaneresearch.client.renderers.tile.BellowsRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ALEMBIC.get(),
                arcaneresearch.client.renderers.tile.AlembicRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.INFUSION_MATRIX.get(),
                arcaneresearch.client.renderers.tile.InfusionMatrixRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RESEARCH_TABLE.get(),
                arcaneresearch.client.renderers.tile.ResearchTableRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.THAUMATORIUM.get(),
                arcaneresearch.client.renderers.tile.ThaumatoriumRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CENTRIFUGE.get(),
                arcaneresearch.client.renderers.tile.CentrifugeRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.MIRROR_ITEM.get(),
                arcaneresearch.client.renderers.tile.MirrorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MIRROR_ESSENTIA.get(),
                arcaneresearch.client.renderers.tile.EssentiaMirrorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GOLEM_BUILDER.get(),
                arcaneresearch.client.renderers.tile.GolemBuilderRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PATTERN_CRAFTER.get(),
                arcaneresearch.client.renderers.tile.PatternCrafterRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VOID_SIPHON.get(),
                arcaneresearch.client.renderers.tile.VoidSiphonRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BANNER.get(),
                arcaneresearch.client.renderers.tile.BannerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HOLE.get(),
                arcaneresearch.client.renderers.tile.HoleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.JAR_BRAIN.get(),
                arcaneresearch.client.renderers.tile.JarBrainRenderer::new);
        event.registerBlockEntityRenderer(
                arcaneresearch.common.blocks.aura.AuraBlockEntities.AURA_NODE.get(),
                arcaneresearch.client.renderers.tile.AuraNodeRenderer::new);
        event.registerBlockEntityRenderer(
                arcaneresearch.common.blocks.aura.AuraBlockEntities.JAR_NODE.get(),
                arcaneresearch.client.renderers.tile.JarNodeRenderer::new);
        event.registerBlockEntityRenderer(
                arcaneresearch.common.blocks.aura.AuraBlockEntities.ENERGIZED_NODE.get(),
                arcaneresearch.client.renderers.tile.EnergizedNodeRenderer::new);
        event.registerBlockEntityRenderer(
                arcaneresearch.common.blocks.aura.AuraBlockEntities.NODE_CONVERTER.get(),
                arcaneresearch.client.renderers.tile.NodeConverterRenderer::new);
        event.registerBlockEntityRenderer(
                arcaneresearch.common.blocks.aura.AuraBlockEntities.STABILIZER_TILE.get(),
                arcaneresearch.client.renderers.tile.StabilizerRenderer::new);
        event.registerBlockEntityRenderer(
                arcaneresearch.common.blocks.aura.AuraBlockEntities.TRANSDUCER_TILE.get(),
                arcaneresearch.client.renderers.tile.TransducerRenderer::new);
        event.registerBlockEntityRenderer(
                arcaneresearch.common.blocks.aura.AuraBlockEntities.ELDRITCH_ALTAR.get(),
                arcaneresearch.client.renderers.tile.EldritchAltarRenderer::new);
        event.registerBlockEntityRenderer(
                arcaneresearch.common.blocks.aura.AuraBlockEntities.ELDRITCH_PORTAL.get(),
                arcaneresearch.client.renderers.tile.EldritchPortalRenderer::new);
        event.registerBlockEntityRenderer(
                arcaneresearch.common.blocks.aura.AuraBlockEntities.ELDRITCH_OBELISK.get(),
                arcaneresearch.client.renderers.tile.EldritchObeliskRenderer::new);
        event.registerBlockEntityRenderer(
                arcaneresearch.common.blocks.aura.AuraBlockEntities.ELDRITCH_CRAB.get(),
                arcaneresearch.client.renderers.tile.EldritchCrabRenderer::new);
    }
}