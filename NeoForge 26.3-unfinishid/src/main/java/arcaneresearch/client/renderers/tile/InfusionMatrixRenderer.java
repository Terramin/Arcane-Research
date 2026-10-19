package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.tiles.crafting.TileInfusionMatrix;

import java.util.Random;

// NEOFORGE_PORT (26.3 render-state rework): Forge render() body 1:1
// (8 wobbling corner cubes + emissive overlay + halo spikes).
public class InfusionMatrixRenderer implements BlockEntityRenderer<TileInfusionMatrix, InfusionMatrixRenderer.State> {

    private static final Identifier TEXTURE_NORMAL =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/infuser_normal.png");
    private static final Identifier TEXTURE_ANCIENT =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/infuser_ancient.png");
    private static final Identifier TEXTURE_ELDRITCH =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/infuser_eldritch.png");

    public static class State extends BlockEntityRenderState {
        public float ticks;
        public float startUp;
        public boolean active;
        public float instability;
        public Identifier texture = TEXTURE_NORMAL;
        public boolean crafting;
        public int craftCount;
        public int haloSpikes = 20;
    }

    public InfusionMatrixRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileInfusionMatrix tile, State state, float partialTicks,
            Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.ticks = Minecraft.getInstance().player != null ?
                Minecraft.getInstance().player.tickCount + partialTicks : 0;
        state.startUp = tile.startUp;
        state.active = tile.active;
        state.instability = Math.min(6.0f, 1.0f + (tile.stability < 0 ? -tile.stability * 0.66f : 1.0f) *
                (Math.min(tile.craftCount, 50) / 50.0f));

        Identifier texture = TEXTURE_NORMAL;
        if (tile.getLevel() != null) {
            net.minecraft.world.level.block.state.BlockState pillarState = tile.getLevel()
                    .getBlockState(tile.getBlockPos().offset(-1, -2, -1));
            if (arcaneresearch.init.ModBlocks.ANCIENT_PILLAR != null
                    && pillarState.is(arcaneresearch.init.ModBlocks.ANCIENT_PILLAR.get())) {
                texture = TEXTURE_ANCIENT;
            } else if (arcaneresearch.init.ModBlocks.ELDRITCH_PILLAR != null
                    && pillarState.is(arcaneresearch.init.ModBlocks.ELDRITCH_PILLAR.get())) {
                texture = TEXTURE_ELDRITCH;
            }
        }
        state.texture = texture;
        state.crafting = tile.crafting;
        state.craftCount = tile.craftCount;
        try {
            state.haloSpikes = Minecraft.getInstance().options.graphicsPreset().get().ordinal() >= 1 ? 20 : 10;
        } catch (Exception ignored) {
            state.haloSpikes = 20;
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
            CameraRenderState cameraState) {
        float ticks = state.ticks;

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);

        if (state.startUp > 0) {
            poseStack.rotate(Axis.YP.rotationDegrees(ticks % 360.0f * state.startUp));
            poseStack.rotate(Axis.XP.rotationDegrees(35.0f * state.startUp));
            poseStack.rotate(Axis.ZP.rotationDegrees(45.0f * state.startUp));
        }

        RenderType cutoutType = TCRenderTypes.objSolid(state.texture);
        int light = state.lightCoords;

        for (int a = 0; a < 2; a++) {
            for (int b = 0; b < 2; b++) {
                for (int c = 0; c < 2; c++) {

                    float wobbleX = 0, wobbleY = 0, wobbleZ = 0;
                    if (state.active) {
                        wobbleX = Mth.sin((ticks + a * 10) / 15.0f) * 0.01f * state.startUp * state.instability;
                        wobbleY = Mth.sin((ticks + b * 10) / 14.0f) * 0.01f * state.startUp * state.instability;
                        wobbleZ = Mth.sin((ticks + c * 10) / 13.0f) * 0.01f * state.startUp * state.instability;
                    }

                    int signA = (a == 0) ? -1 : 1;
                    int signB = (b == 0) ? -1 : 1;
                    int signC = (c == 0) ? -1 : 1;

                    poseStack.pushPose();
                    poseStack.translate(wobbleX + signA * 0.25f, wobbleY + signB * 0.25f, wobbleZ + signC * 0.25f);

                    if (a > 0) poseStack.rotate(Axis.XP.rotationDegrees(90.0f));
                    if (b > 0) poseStack.rotate(Axis.YP.rotationDegrees(90.0f));
                    if (c > 0) poseStack.rotate(Axis.ZP.rotationDegrees(90.0f));

                    poseStack.scale(0.45f, 0.45f, 0.45f);
                    renderCube(poseStack, collector, cutoutType, light, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f, 0);
                    poseStack.popPose();
                }
            }
        }

        if (state.active) {
            RenderType glowType = TCRenderTypes.additiveTextured(state.texture);

            for (int a = 0; a < 2; a++) {
                for (int b = 0; b < 2; b++) {
                    for (int c = 0; c < 2; c++) {
                        float wobbleX = Mth.sin((ticks + a * 10) / 15.0f) * 0.01f * state.startUp * state.instability;
                        float wobbleY = Mth.sin((ticks + b * 10) / 14.0f) * 0.01f * state.startUp * state.instability;
                        float wobbleZ = Mth.sin((ticks + c * 10) / 13.0f) * 0.01f * state.startUp * state.instability;

                        int signA = (a == 0) ? -1 : 1;
                        int signB = (b == 0) ? -1 : 1;
                        int signC = (c == 0) ? -1 : 1;

                        poseStack.pushPose();
                        poseStack.translate(wobbleX + signA * 0.25f, wobbleY + signB * 0.25f, wobbleZ + signC * 0.25f);

                        if (a > 0) poseStack.rotate(Axis.XP.rotationDegrees(90.0f));
                        if (b > 0) poseStack.rotate(Axis.YP.rotationDegrees(90.0f));
                        if (c > 0) poseStack.rotate(Axis.ZP.rotationDegrees(90.0f));

                        poseStack.scale(0.45f, 0.45f, 0.45f);

                        float pulse = (Mth.sin((ticks + a * 2 + b * 3 + c * 4) / 4.0f) * 0.1f + 0.2f) * state.startUp;
                        renderCube(poseStack, collector, glowType, 0x00F000F0, OverlayTexture.NO_OVERLAY, 0.8f, 0.1f, 1.0f, pulse, 32);
                        poseStack.popPose();
                    }
                }
            }
        }

        poseStack.popPose();

        if (state.crafting && state.craftCount > 0) {
            renderHalo(poseStack, collector, state.craftCount, state.haloSpikes);
        }
    }

    private void renderCube(PoseStack poseStack, SubmitNodeCollector collector, RenderType type,
                            int packedLight, int packedOverlay,
                            float r, float g, float b, float a, int vOff) {
        float size = 0.5f;
        float t = 64.0f;

        emitQuad(poseStack, collector, type,
                -size, size, -size, size, size, size, 0, 1, 0, r, g, b, a, packedLight, packedOverlay,
                16 / t, vOff / t, 32 / t, (vOff + 16) / t);

        emitQuad(poseStack, collector, type,
                -size, -size, size, size, -size, -size, 0, -1, 0, r, g, b, a, packedLight, packedOverlay,
                32 / t, vOff / t, 48 / t, (vOff + 16) / t);

        emitQuad(poseStack, collector, type,
                size, -size, -size, -size, size, -size, 0, 0, -1, r, g, b, a, packedLight, packedOverlay,
                16 / t, (vOff + 16) / t, 32 / t, (vOff + 32) / t);

        emitQuad(poseStack, collector, type,
                -size, -size, size, size, size, size, 0, 0, 1, r, g, b, a, packedLight, packedOverlay,
                48 / t, (vOff + 16) / t, 64 / t, (vOff + 32) / t);

        emitQuad(poseStack, collector, type,
                -size, -size, -size, -size, size, size, -1, 0, 0, r, g, b, a, packedLight, packedOverlay,
                0 / t, (vOff + 16) / t, 16 / t, (vOff + 32) / t);

        emitQuad(poseStack, collector, type,
                size, -size, size, size, size, -size, 1, 0, 0, r, g, b, a, packedLight, packedOverlay,
                32 / t, (vOff + 16) / t, 48 / t, (vOff + 32) / t);
    }

    private static void emitQuad(PoseStack poseStack, SubmitNodeCollector collector, RenderType type,
                                 float x1, float y1, float z1, float x2, float y2, float z2,
                                 float nx, float ny, float nz, float r, float g, float b, float a,
                                 int light, int overlay,
                                 float u0, float v0, float u1, float v1) {
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            if (ny != 0) {
                quad(consumer, pose, x1, y1, z1, u0, v0, r, g, b, a, light, overlay, nx, ny, nz);
                quad(consumer, pose, x1, y1, z2, u0, v1, r, g, b, a, light, overlay, nx, ny, nz);
                quad(consumer, pose, x2, y1, z2, u1, v1, r, g, b, a, light, overlay, nx, ny, nz);
                quad(consumer, pose, x2, y1, z1, u1, v0, r, g, b, a, light, overlay, nx, ny, nz);
            } else if (nz != 0) {
                quad(consumer, pose, x1, y1, z1, u0, v1, r, g, b, a, light, overlay, nx, ny, nz);
                quad(consumer, pose, x2, y1, z1, u1, v1, r, g, b, a, light, overlay, nx, ny, nz);
                quad(consumer, pose, x2, y2, z1, u1, v0, r, g, b, a, light, overlay, nx, ny, nz);
                quad(consumer, pose, x1, y2, z1, u0, v0, r, g, b, a, light, overlay, nx, ny, nz);
            } else {
                quad(consumer, pose, x1, y1, z1, u0, v1, r, g, b, a, light, overlay, nx, ny, nz);
                quad(consumer, pose, x1, y1, z2, u1, v1, r, g, b, a, light, overlay, nx, ny, nz);
                quad(consumer, pose, x1, y2, z2, u1, v0, r, g, b, a, light, overlay, nx, ny, nz);
                quad(consumer, pose, x1, y2, z1, u0, v0, r, g, b, a, light, overlay, nx, ny, nz);
            }
        });
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose,
                             float x, float y, float z, float u, float v,
                             float r, float g, float b, float a, int light, int overlay,
                             float nx, float ny, float nz) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a).setUv(u, v)
                .setOverlay(overlay).setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    private void renderHalo(PoseStack poseStack, SubmitNodeCollector collector, int craftCount, int numSpikes) {
        Random random = new Random(245L);

        float intensity = craftCount / 500.0f;

        // NEOFORGE_PORT: Tesselator/POSITION_COLOR gone; same spikes as fans
        // (apex + 3 rim verts = identical 3 triangles) via the color-fan layer.
        RenderType type = TCRenderTypes.orbTendrils();

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);

        for (int i = 0; i < numSpikes; i++) {
            poseStack.pushPose();

            poseStack.rotate(Axis.XP.rotationDegrees(random.nextFloat() * 360.0f));
            poseStack.rotate(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f));
            poseStack.rotate(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0f));
            poseStack.rotate(Axis.XP.rotationDegrees(random.nextFloat() * 360.0f));
            poseStack.rotate(Axis.YP.rotationDegrees(random.nextFloat() * 360.0f));
            poseStack.rotate(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0f + intensity * 360.0f));

            float length = (random.nextFloat() * 20.0f + 5.0f) / 20.0f * (Math.min(craftCount, 50) / 50.0f);
            float width = (random.nextFloat() * 2.0f + 1.0f) / 20.0f * (Math.min(craftCount, 50) / 50.0f);

            float alpha = 1.0f - intensity;
            float ax = -0.866f * width, ay = length, az = -0.5f * width;
            float bx = 0.866f * width, by = length, bz = -0.5f * width;
            float cx = 0, cy = length, cz = width;
            collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
                fan(consumer, pose, 0, 0, 0, 1, 1, 1, alpha);
                fan(consumer, pose, ax, ay, az, 1, 0, 1, 0);
                fan(consumer, pose, bx, by, bz, 1, 0, 1, 0);
                fan(consumer, pose, cx, cy, cz, 1, 0, 1, 0);
            });

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static void fan(VertexConsumer consumer, PoseStack.Pose pose,
                            float x, float y, float z, float r, float g, float b, float a) {
        consumer.addVertex(pose, x, y, z).setColor(r, g, b, a);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}