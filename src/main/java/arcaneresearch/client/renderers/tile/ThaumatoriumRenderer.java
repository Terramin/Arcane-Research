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
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.crafting.CrucibleRecipe;
import arcaneresearch.common.tiles.crafting.TileThaumatorium;



public class ThaumatoriumRenderer implements BlockEntityRenderer<TileThaumatorium, ThaumatoriumRenderer.State> {

    public static class State extends BlockEntityRenderState {
        public final ItemStackRenderState display = new ItemStackRenderState();
        public boolean hasDisplay;
        public Direction facing = Direction.NORTH;
        public float time;
    }

    private final ItemModelResolver itemModelResolver;

    public ThaumatoriumRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileThaumatorium tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.hasDisplay = false;
        if (tile == null || tile.getLevel() == null) return;
        if (tile.recipeHash == null || tile.recipeHash.isEmpty()) return;

        Direction facing = Direction.NORTH;
        try {
            if (tile.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                facing = tile.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            }
        } catch (Exception ignored) {
        }
        state.facing = facing;

        int ticks = 0;
        if (Minecraft.getInstance().getCameraEntity() != null) {
            ticks = Minecraft.getInstance().getCameraEntity().tickCount;
        }
        state.time = ticks + partialTicks;
        int stack = (ticks / 20) % tile.recipeHash.size();
        CrucibleRecipe recipe = ArcaneresearchApi.getCrucibleRecipeFromHash(tile.recipeHash.get(stack));
        if (recipe == null) return;

        ItemStack display = recipe.getRecipeOutput().copy();
        display.setCount(1);
        if (display.isEmpty()) return;
        itemModelResolver.updateForTopItem(state.display, display, ItemDisplayContext.FIXED,
                tile.getLevel(), null, 0);
        state.hasDisplay = true;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        if (!state.hasDisplay) {
            return;
        }
        float time = state.time;
        poseStack.pushPose();
        poseStack.translate(0.5f + state.facing.getStepX() / 1.99f,
                1.125f + Mth.sin(time * 0.1f) * 0.06f,
                0.5f + state.facing.getStepZ() / 1.99f);
        float rotation = switch (state.facing) {
            case EAST -> 90.0f;
            case WEST -> 270.0f;
            case NORTH -> 180.0f;
            case SOUTH -> 0.0f;
            default -> 0.0f;
        };
        poseStack.rotate(Axis.YP.rotationDegrees(rotation));
        poseStack.rotate(Axis.YP.rotationDegrees((time * 3.0f) % 360.0f));
        poseStack.scale(0.75f, 0.75f, 0.75f);
        state.display.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    
    
    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(TileThaumatorium blockEntity) {
        return blockEntity.getRenderBoundingBox();
    }
}
