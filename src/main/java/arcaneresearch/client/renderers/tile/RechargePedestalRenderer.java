package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.common.tiles.devices.TileRechargePedestal;

// NEOFORGE_PORT (26.3 render-state rework): Forge render() body 1:1
// (spinning levitating item).
public class RechargePedestalRenderer implements BlockEntityRenderer<TileRechargePedestal, RechargePedestalRenderer.State> {

    public static class State extends BlockEntityRenderState {
        public final ItemStackRenderState renderStack = new ItemStackRenderState();
        public boolean hasStack;
        public float ticks;
    }

    private final ItemModelResolver itemModelResolver;

    public RechargePedestalRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileRechargePedestal tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.hasStack = false;
        if (tile.getLevel() == null) return;
        ItemStack stack = tile.getSyncedStackInSlot(0);
        if (stack.isEmpty()) return;

        float ticks;
        if (Minecraft.getInstance().player != null) {
            ticks = Minecraft.getInstance().player.tickCount + partialTicks;
        } else {
            ticks = tile.getLevel().getGameTime() + partialTicks;
        }
        state.ticks = ticks;

        ItemStack renderStack = stack.copy();
        renderStack.setCount(1);
        itemModelResolver.updateForTopItem(state.renderStack, renderStack, ItemDisplayContext.GROUND,
                tile.getLevel(), null, 0);
        state.hasStack = true;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        if (!state.hasStack) {
            return;
        }
        float ticks = state.ticks;
        // NEOFORGE_PORT: BakedModel GROUND scale gone with ItemRenderer;
        // standard GROUND display scale (0.5) preserved.
        float bob = Mth.sin(ticks / 10.0f) * 0.1f + 0.1f;
        float lift = bob + 0.25f * 0.5f + 0.05f;

        poseStack.pushPose();

        poseStack.translate(0.5, 0.75, 0.5);
        poseStack.scale(1.5f, 1.5f, 1.5f);

        poseStack.rotate(Axis.YP.rotationDegrees(ticks % 360.0f));
        poseStack.translate(0.0, lift, 0.0);

        state.renderStack.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }

    // NEOFORGE_PORT: render bounds moved from BlockEntity to the renderer in 26.x
    // (IBlockEntityRendererExtension); forward the tile's Forge-parity culling box.
    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(TileRechargePedestal blockEntity) {
        return blockEntity.getRenderBoundingBox();
    }
}
