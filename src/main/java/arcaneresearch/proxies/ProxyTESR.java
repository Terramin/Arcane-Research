package arcaneresearch.proxies;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import arcaneresearch.init.ModBlockEntities;
@OnlyIn(Dist.CLIENT)
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
    }
}
