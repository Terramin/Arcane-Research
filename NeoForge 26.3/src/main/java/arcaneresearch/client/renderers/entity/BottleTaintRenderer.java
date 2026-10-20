package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.common.entities.projectile.EntityBottleTaint;
import arcaneresearch.init.ModItems;

// NEOFORGE_PORT (26.3): spinning bottle via pre-resolved ItemStackRenderState
// (old ItemRenderer.renderStatic gone); yaw/spin via State.
public class BottleTaintRenderer extends EntityRenderer<EntityBottleTaint, BottleTaintRenderer.State> {

    public static class State extends EntityRenderState {
        public float yaw;
        public float spin;
        public final ItemStackRenderState itemState = new ItemStackRenderState();
    }

    private final ItemModelResolver itemModelResolver;
    private volatile ItemStack cachedItem;

    public BottleTaintRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    private ItemStack getItemStack() {
        if (cachedItem == null) {
            cachedItem = new ItemStack(ModItems.BOTTLE_TAINT.get());
        }
        return cachedItem;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EntityBottleTaint entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        state.spin = (entity.tickCount + partialTicks) * 20.0F;
        this.itemModelResolver.updateForNonLiving(state.itemState, getItemStack(),
                ItemDisplayContext.GROUND, entity);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(-90.0F - state.yaw));
        poseStack.rotate(com.mojang.math.Axis.ZP.rotationDegrees(state.spin));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        state.itemState.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}