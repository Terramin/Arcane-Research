package arcaneresearch.client.renderers.tile;
import arcaneresearch.common.tiles.aura.EldritchCapTile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/**
 * Пьедестал-кап, порт TC4 TileEldritchCapRenderer (в ClientProxy он
 * модификаторнее зарегистрирован на TileEldritchCap): сам блок meta 3
 * ISBRH не рисовался — модель obelisk_cap.obj ("Cap") рисует TESR с
 * текстурой obelisk_cap.png (obelisk_cap_2.png во Внешних землях).
 * Лежит в arcaneresearch.arcaneresearch.client.renderers.tile.
 *
 * NEOFORGE_PORT (26.3 render-state rework, паритет EldritchAltarRenderer):
 * render(tile, partialTicks, poseStack, buffer, light, overlay) -&gt;
 * createRenderState/extractRenderState/submit; MultiBufferSource удалён —
 * модель уходит через ObeliskCapModels.renderCapSubmit.
 */
public class EldritchCapRenderer implements BlockEntityRenderer<EldritchCapTile, EldritchCapRenderer.State> {

    private static final Identifier CAP =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/models/obelisk_cap.png");
    private static final Identifier CAP_OUTER =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/models/obelisk_cap_2.png");

    public static class State extends BlockEntityRenderState {
        public boolean capOuter;
    }

    public EldritchCapRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EldritchCapTile tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        if (tile.getLevel() == null) {
            return;
        }
        boolean outer = false;
        try {
            // TC4: именование "outer" в пути дименшна — текстуры _2.
            if (tile.getLevel().dimension().identifier().getPath().contains("outer")) {
                outer = true;
            }
        } catch (Exception ignored) {
        }
        state.capOuter = outer;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        // TC4: лайтмапа позиции тайла (func_145838_q).
        Identifier texture = state.capOuter ? CAP_OUTER : CAP;
        ObeliskCapModels.renderCapSubmit(poseStack, collector, texture,
                state.lightCoords, OverlayTexture.NO_OVERLAY);
    }

    // NEOFORGE_PORT: render bounds moved from BlockEntity to the renderer in 26.x
    // (IBlockEntityRendererExtension); forward the tile's Forge-parity culling box.
    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(EldritchCapTile blockEntity) {
        return blockEntity.getRenderBoundingBox();
    }
}
