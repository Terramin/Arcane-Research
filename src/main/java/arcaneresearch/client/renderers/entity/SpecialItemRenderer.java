package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier; 
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.entities.EntitySpecialItem;






public class SpecialItemRenderer extends EntityRenderer<ItemEntity, SpecialItemRenderer.State> {

    public static class State extends EntityRenderState {
        public float bob;
        public float spin;
        public float age;
        public float growScale;
        public int tendrils;
        public final ItemStackRenderState itemState = new ItemStackRenderState();
    }

    private static final Identifier BLANK =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/blank.png");

    private final ItemModelResolver itemModelResolver;
    private final RandomSource random = RandomSource.create(187L);

    public SpecialItemRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ItemEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        float bobOffs = entity instanceof EntitySpecialItem special ? special.bobOffs : 0.0F;
        state.bob = Mth.sin((entity.getAge() + partialTicks) / 10.0F + bobOffs) * 0.1F + 0.1F;
        state.spin = (entity.getAge() + partialTicks) * 2.0F;
        state.age = entity.getAge() / 500.0F;
        state.growScale = Math.min(entity.getAge(), 10) / 10.0F;
        state.tendrils = Minecraft.getInstance().options.graphicsPreset().get()
                != net.minecraft.client.GraphicsPreset.FAST ? 10 : 5;
        this.itemModelResolver.updateForNonLiving(state.itemState, entity.getItem(),
                ItemDisplayContext.GROUND, entity);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0D, state.bob + 0.25D, 0.0D);

        random.setSeed(187L);
        RenderType tendrils = TCRenderTypes.orbTendrils();
        float scale = state.growScale;
        for (int i = 0; i < state.tendrils; i++) {
            float rx1 = random.nextFloat() * 360.0F;
            float ry1 = random.nextFloat() * 360.0F;
            float rz1 = random.nextFloat() * 360.0F;
            float rx2 = random.nextFloat() * 360.0F;
            float ry2 = random.nextFloat() * 360.0F;
            float rz2 = random.nextFloat() * 360.0F + state.age * 360.0F;
            float length = (random.nextFloat() * 20.0F + 5.0F) / 30.0F * scale;
            float width = (random.nextFloat() * 2.0F + 1.0F) / 30.0F * scale;

            poseStack.pushPose();
            poseStack.rotate(com.mojang.math.Axis.XP.rotationDegrees(rx1));
            poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(ry1));
            poseStack.rotate(com.mojang.math.Axis.ZP.rotationDegrees(rz1));
            poseStack.rotate(com.mojang.math.Axis.XP.rotationDegrees(rx2));
            poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(ry2));
            poseStack.rotate(com.mojang.math.Axis.ZP.rotationDegrees(rz2));
            collector.submitCustomGeometry(poseStack, tendrils,
                    (pose, consumer) -> renderTendril(pose, consumer, length, width));
            poseStack.popPose();
        }

        poseStack.pushPose();
        poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(state.spin));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        state.itemState.submit(poseStack, collector, 0xF000F0, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
        poseStack.popPose();

        super.submit(state, poseStack, collector, camera);
    }

    private static void renderTendril(PoseStack.Pose pose, VertexConsumer consumer, float length, float width) {
        consumer.addVertex(pose, 0.0F, 0.0F, 0.0F).setColor(255, 255, 255, 255);
        consumer.addVertex(pose, (float) (-0.866 * width), length, (float) (-0.5 * width)).setColor(255, 0, 255, 0);
        consumer.addVertex(pose, (float) (0.866 * width), length, (float) (-0.5 * width)).setColor(255, 0, 255, 0);
        consumer.addVertex(pose, 0.0F, length, width).setColor(255, 0, 255, 0);
        consumer.addVertex(pose, (float) (-0.866 * width), length, (float) (-0.5 * width)).setColor(255, 0, 255, 0);
    }
}