package arcaneresearch.client.renderers.tile;
import arcaneresearch.common.tiles.aura.EldritchAltarTile;

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
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.init.ModItems;

 






 
public class EldritchAltarRenderer implements BlockEntityRenderer<EldritchAltarTile, EldritchAltarRenderer.State> {

    private static final Identifier CAP_ALTAR =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/block/obelisk_cap_altar.png");
    private static final Identifier CAP_OUTER =
            Identifier.fromNamespaceAndPath("arcaneresearch", "textures/models/obelisk_cap_2.png");

    public static class State extends BlockEntityRenderState {
        public final ItemStackRenderState eye = new ItemStackRenderState();
        public int eyes;
        public boolean capOuter;
    }

    private final ItemModelResolver itemModelResolver;

    public EldritchAltarRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EldritchAltarTile tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        int eyes = 0;
        try {
            eyes = tile.getEyes();
        } catch (Exception ignored) {
        }
        state.eyes = eyes;
        if (eyes > 0) {
            try {
                ItemStack eye = new ItemStack(ModItems.ELDRITCH_EYE.get());
                itemModelResolver.updateForTopItem(state.eye, eye, ItemDisplayContext.FIXED,
                        tile.getLevel(), null, 0);
            } catch (Exception ignored) {
                state.eyes = 0;
            }
        }
        boolean outer = false;
        try {
            if (tile.getLevel() != null
                    && tile.getLevel().dimension().identifier().getPath().contains("outer")) {
                outer = true;
            }
        } catch (Exception ignored) {
        }
        state.capOuter = outer;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        
        
        
        Identifier capTexture = state.capOuter ? CAP_OUTER : CAP_ALTAR;
        ObeliskCapModels.renderCapSubmit(poseStack, collector, capTexture,
                state.lightCoords, OverlayTexture.NO_OVERLAY);

        if (state.eyes <= 0) {
            return;
        }
        for (int a = 0; a < Math.min(state.eyes, 4); a++) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.0D, 0.5D);
            poseStack.rotate(Axis.YP.rotationDegrees(a * 90.0F));
            poseStack.translate(0.46D, 0.2D, 0.0D);
            poseStack.rotate(Axis.YP.rotationDegrees(90.0F));
            poseStack.rotate(Axis.XP.rotationDegrees(-18.0F));
            
            
            poseStack.scale(0.5F, 0.5F, 0.5F);
            state.eye.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    
    
    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox(EldritchAltarTile blockEntity) {
        return blockEntity.getRenderBoundingBox();
    }
}
