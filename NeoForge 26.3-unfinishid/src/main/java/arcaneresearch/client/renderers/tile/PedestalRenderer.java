package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.common.tiles.crafting.TilePedestal;

// NEOFORGE_PORT (26.3 render-state rework): Forge render() body 1:1
// (item height/rotation/scale); item via ItemStackRenderState.submit.
public class PedestalRenderer implements BlockEntityRenderer<TilePedestal, PedestalRenderer.State> {

    public static class State extends BlockEntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public boolean hasItem;
        public double itemHeight = 1.28;
        public float rotation;
    }

    private final ItemModelResolver itemModelResolver;

    public PedestalRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TilePedestal tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        net.minecraft.world.item.ItemStack stack = tile.getItem(0);
        state.hasItem = !stack.isEmpty();
        if (state.hasItem) {
            itemModelResolver.updateForTopItem(state.item, stack, ItemDisplayContext.FIXED,
                    tile.getLevel(), null, 0);
        }
        double itemHeight = 1.28;
        if (tile.getBlockState().getBlock()
                instanceof arcaneresearch.common.blocks.devices.BlockPedestal pedestalBlock) {
            itemHeight = pedestalBlock.getItemRenderHeight();
        }
        state.itemHeight = itemHeight;
        long time = tile.getLevel() != null ? tile.getLevel().getGameTime() : 0;
        state.rotation = (time + partialTicks) % 360.0F;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        if (!state.hasItem) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5, state.itemHeight, 0.5);
        poseStack.rotate(Axis.YP.rotationDegrees(state.rotation));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}