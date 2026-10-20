package arcaneresearch.client.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import org.joml.Quaternionf;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.crafting.Part;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.minecraft.client.gui.navigation.ScreenRectangle;

/**
 * NEOFORGE_PORT: true 3D isometric structure preview (Thaumonomicon blueprint
 * + bookmark). The 1.20.1 Forge path was GuiGraphics.pose() * 3D ops +
 * BlockRenderDispatcher.renderSingleBlock into a GUI MultiBufferSource — both
 * are gone in 26.x (the GUI pose is a 2D Matrix3x2fStack). The equivalent
 * engine mechanism is the vanilla picture-in-picture system used by the
 * inventory entity/banner previews: submit a PictureInPictureRenderState from
 * the extractor and let a registered PictureInPictureRenderer draw it into an
 * offscreen texture with a full 3D PoseStack + SubmitNodeCollector
 * (RegisterPictureInPictureRenderersEvent, mod bus, client only).
 *
 * <p>Projection (pixel-exact with 1.20.1, whose chain was
 * translate(x, y-10, 200) * scale(s,-s,s) * rotX(25) * rotY(-45) * translate(-center)):
 * A = cos45 = 0.70710678, B = sin45*sin25 = 0.29889, C = cos25 = 0.90631,
 * viewer/depth direction (0.64086, 0.42262, 0.64086). In the pip eye space
 * (screen y grows down, so model y-up is flipped) the frame is realised as
 * S(s) * S(1,-1,-1) * rotX(25) * rotY(-45) * translate(-center); its
 * determinant is positive, so block face winding stays engine-correct, and
 * the depth row maps the same "towards viewer" direction as 1.20.1 did, so
 * the real depth buffer resolves overlapping blocks exactly like Forge did
 * with z=200 in front of the page graphics.
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public final class IsoStructurePip {

    private IsoStructurePip() {
    }

    /** cos45 */
    private static final float A = 0.70710678f;
    /** sin45*sin25 */
    private static final float B = 0.29889f;
    /** cos25 */
    private static final float C = 0.90631f;

    /**
     * Submits the isometric structure preview as a picture-in-picture state.
     *
     * @param parts raw structure parts (node swirl overlay); null = draw only
     *              the converted block states (bookmark preview behaviour)
     */
    public static void submit(GuiGraphicsExtractor graphics, BlueprintBlockAccess access,
                              @Nullable Part[][][] parts, int xSize, int ySize, int zSize,
                              int cx, int cy, float boxW, float boxH, float maxS) {
        State state = State.create(access, parts, xSize, ySize, zSize, cx, cy, boxW, boxH, maxS);
        if (state != null) {
            graphics.submitPictureInPictureRenderState(state);
        }
    }

    /** Same as {@link #submit} with a fixed GUI-pixels-per-block scale (1.20.1 parity, no box fit). */
    public static void submitFixedScale(GuiGraphicsExtractor graphics, BlueprintBlockAccess access,
                                        @Nullable Part[][][] parts, int xSize, int ySize, int zSize,
                                        int cx, int cy, float scale) {
        submit(graphics, access, parts, xSize, ySize, zSize, cx, cy, 0.0f, 0.0f, scale);
    }

    @SubscribeEvent
    static void onRegisterPipRenderers(RegisterPictureInPictureRenderersEvent event) {
        event.register(State.class, Renderer::new);
    }

    public static final class State implements PictureInPictureRenderState {
        final BlueprintBlockAccess access;
        @Nullable
        final Part[][][] parts;
        final int xSize, ySize, zSize;
        /** GUI pixels per block, fit computation identical to the 1.20.1-era logic. */
        final float scale;
        /** Eye-space shift that centres the projected footprint in the box. */
        final float shiftX, shiftY;
        final int x0, y0, x1, y1;

        private State(BlueprintBlockAccess access, @Nullable Part[][][] parts,
                      int xSize, int ySize, int zSize, int cx, int cy,
                      float scale, float shiftX, float shiftY, int x0, int y0, int x1, int y1) {
            this.access = access;
            this.parts = parts;
            this.xSize = xSize;
            this.ySize = ySize;
            this.zSize = zSize;
            this.scale = scale;
            this.shiftX = shiftX;
            this.shiftY = shiftY;
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
        }

        /**
         * Records the visible cells once: projected footprint via the 1.20.1
         * equations px = A*(l-w), py = B*(l+w) - C*h, then fits the Forge
         * scale (clamped to [3, maxS]) and derives the box centred so that
         * the projected structure origin stays anchored at (cx, cy) — the
         * same anchoring as Forge's translate(x, y-10, 200). Returns null if
         * nothing is visible (e.g. slice build-up has not started yet).
         */
        @Nullable
        static State create(BlueprintBlockAccess access, @Nullable Part[][][] parts,
                            int xSize, int ySize, int zSize,
                            int cx, int cy, float boxW, float boxH, float maxS) {
            if (access == null || xSize <= 0 || ySize <= 0 || zSize <= 0) {
                return null;
            }
            double minPx = Double.POSITIVE_INFINITY, maxPx = Double.NEGATIVE_INFINITY;
            double minPy = Double.POSITIVE_INFINITY, maxPy = Double.NEGATIVE_INFINITY;
            boolean any = false;
            for (int h = 0; h < ySize; h++) {
                for (int l = 0; l < xSize; l++) {
                    for (int w = 0; w < zSize; w++) {
                        BlockState state = access.getBlockState(new BlockPos(l, h, w));
                        // NEOFORGE_PORT: клетка аура-ноды чертежа пуста (AIR
                        // вместо стекла-заглушки), но её светящийся оверлей
                        // рисуется на этом же месте — учитываем её в
                        // footprint, иначе PiP-бокс клипает свечение по краям.
                        // Маппинг слоёв тот же, что в рендере ниже:
                        // structure[y] ← parts[ySize - 1 - y].
                        Part part = parts != null ? parts[ySize - 1 - h][l][w] : null;
                        boolean nodeOverlay = part != null && BlueprintBlockAccess.hasNodeOverlay(part);
                        if (state.isAir() && !nodeOverlay) {
                            continue;
                        }
                        any = true;
                        double px = (l - w) * A;
                        double py = (l + w) * B - h * C;
                        if (px < minPx) minPx = px;
                        if (px > maxPx) maxPx = px;
                        if (py < minPy) minPy = py;
                        if (py > maxPy) maxPy = py;
                    }
                }
            }
            if (!any) {
                return null;
            }
            // NEOFORGE_PORT (26.3): PiP-текстура жёстко клипает всё вне бокса.
            // Раньше бокс считался только по углам клеток с допуском +1, а
            // проекция куба гексагональна и выходит за углы: по px — ±A, по
            // py — в сумме на C+2B (≈1.5 высоты клетки в проекции). В
            // 1.20.1 Forge это не резалось (рисование шло свободно на экран),
            // в PiP поэтому обрезались низ/верх модели. Бокс расширен
            // симметрично на полный вылет, прежнее центрирование не тронуто.
            double wu = (maxPx - minPx + 1.0) + 2 * A;
            double hu = (maxPy - minPy + 1.0) + C + 2 * B;
            float s;
            if (boxW <= 0.0f || boxH <= 0.0f) {
                // NEOFORGE_PORT (26.3): fixed per-block scale (1.20.1 used a fixed scale, no box fit).
                s = maxS;
            } else {
                s = (float) Math.min(boxW / wu, boxH / hu);
                if (s > maxS) {
                    s = maxS;
                }
                if (s < 3.0f) {
                    s = 3.0f;
                }
            }
            // translate(-center) remaps the raw projection into the frame
            // translate(-xSize/2, -ySize/2, -zSize/2) produces here.
            double pxC = (xSize - zSize) / 2.0 * A;
            double pyC = (xSize + zSize) / 2.0 * B;
            float shiftX = (float) ((minPx + maxPx) / 2.0 - pxC) * -s;
            float shiftY = (float) ((minPy + maxPy) / 2.0 - pyC) * -s;
            int width = (int) Math.round(wu * s) + 8;
            int height = (int) Math.round(hu * s) + 8;
            // The projected cube-centre must land exactly on the GUI anchor
            // (cx, cy) — Forge's translate(x, y-10, 200). The cube-centre
            // (eye origin) sits at (shiftX, shiftY) relative to the texture
            // centre, so the box centre goes to anchor MINUS the shift.
            int bx = Math.round(cx - shiftX);
            int by = Math.round(cy - shiftY);
            int x0 = bx - width / 2;
            int y0 = by - height / 2;
            return new State(access, parts, xSize, ySize, zSize, cx, cy,
                    s, shiftX, shiftY, x0, y0, x0 + width, y0 + height);
        }

        @Override
        public int x0() {
            return this.x0;
        }

        @Override
        public int x1() {
            return this.x1;
        }

        @Override
        public int y0() {
            return this.y0;
        }

        @Override
        public int y1() {
            return this.y1;
        }

        @Override
        public float scale() {
            return 1.0f;
        }

        @Override
        public @Nullable ScreenRectangle scissorArea() {
            return null;
        }

        @Override
        public @Nullable ScreenRectangle bounds() {
            return PictureInPictureRenderState.getBounds(this.x0, this.y0, this.x1, this.y1, this.scissorArea());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof State other)) {
                return false;
            }
            return this.access == other.access
                    && this.parts == other.parts
                    && this.xSize == other.xSize
                    && this.ySize == other.ySize
                    && this.zSize == other.zSize
                    && Float.floatToIntBits(this.scale) == Float.floatToIntBits(other.scale)
                    && this.x0 == other.x0 && this.y0 == other.y0
                    && this.x1 == other.x1 && this.y1 == other.y1;
        }

        @Override
        public int hashCode() {
            int hash = this.access.hashCode();
            hash = 31 * hash + (this.parts != null ? System.identityHashCode(this.parts) : 0);
            hash = 31 * hash + this.xSize;
            hash = 31 * hash + this.ySize;
            hash = 31 * hash + this.zSize;
            hash = 31 * hash + Float.floatToIntBits(this.scale);
            hash = 31 * hash + this.x0;
            hash = 31 * hash + this.y0;
            hash = 31 * hash + this.x1;
            hash = 31 * hash + this.y1;
            return hash;
        }
    }

    public static final class Renderer extends PictureInPictureRenderer<State> {
        private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();
        private static final int LIGHT_FULLBRIGHT = 15728880;

        private final BlockModelResolver blockModelResolver = Minecraft.getInstance().getBlockModelResolver();
        private final BlockModelRenderState blockModel = new BlockModelRenderState();

        private Renderer() {
        }

        @Override
        public Class<State> getRenderStateClass() {
            return State.class;
        }

        @Override
        protected float getTranslateY(int height, int guiScale) {
            return height / 2.0f;
        }

        @Override
        protected void renderToTexture(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
            Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
            // Base frame (from prepare): T(width/2, height/2, 0) * S(k,k,-k),
            // k = guiScale (state.scale() == 1). Everything below works on
            // top of it in block units; shift centres the projected footprint.
            poseStack.translate(state.shiftX, state.shiftY, 0.0f);
            poseStack.scale(state.scale, state.scale, state.scale);
            // S(1,-1,-1): model y-up -> screen-down eye space, cancels the
            // base -scale on z; determinant positive keeps face winding.
            poseStack.scale(1.0f, -1.0f, -1.0f);
            poseStack.rotateDegrees(Axis.XP, 25.0f);
            poseStack.rotateDegrees(Axis.YP, -45.0f);
            poseStack.translate(-state.xSize / 2.0f, -state.ySize / 2.0f, -state.zSize / 2.0f);

            // Loop and gate identical to the 1.20.1 blueprint (slice build-up
            // via access.getBlockState) and to the 1:1 port it replaces.
            // NEOFORGE_PORT: нода рисуется и в пустой клетке (AIR вместо
            // стекла-заглушки), но скрытые срезом слои гасятся вместе с
            // блоками (access.isHiddenBySlice), как в 1.20.1 Forge.
            // NEOFORGE_PORT (26.3 order): светящийся вихрь ноды в 1.20.1
            // Forge падал в bufferSource() и флашнулся поверх всего; в
            // submit-системе всё лежит в order(0), и геометрия, рисуемая
            // в поздней фазе блоков, затирала вихрь непрозрачными лицами.
            // order(1) рисуется после order(0).
            OrderedSubmitNodeCollector nodeCollector = submitNodeCollector.order(1);
            // NEOFORGE_PORT (26.3 pip): биллборд под камеру PiP. В мире квад
            // фейчит мировую камеру; в изометрии мировой камеры нет, поэтому
            // кварн отматывает разворот кадра: (Rx·Ry)⁻¹ = Ry(+45)·Rx(-25), и
            // квад ложится в плоскость вьюера — как биллборд в 1.20.1 Forge.
            Quaternionf pipBillboard = new Quaternionf(Axis.YP.rotationDegrees(45.0f))
                    .mul(new Quaternionf(Axis.XP.rotationDegrees(-25.0f)));
            for (int h = 0; h < state.ySize; h++) {
                for (int l = 0; l < state.xSize; l++) {
                    for (int w = 0; w < state.zSize; w++) {
                        BlockState blockState = state.access.getBlockState(new BlockPos(l, h, w));
                        // structure cell (l,h,w) comes from
                        // parts[ySize - 1 - h][l][w] (BlueprintBlockAccess
                        // constructor mapping); true aura node swirl.
                        Part part = state.parts != null ? state.parts[state.ySize - 1 - h][l][w] : null;
                        if (!blockState.isAir()) {
                            poseStack.pushPose();
                            poseStack.translate(l, h, w);
                            if (BlueprintBlockAccess.isBerRendered(blockState)) {
                                // Блоки без статической модели рендерим как в
                                // мире — через их блок-ентити рендерер.
                                this.submitBlockEntity(blockState, poseStack, submitNodeCollector);
                            } else {
                                this.blockModelResolver.update(this.blockModel, blockState, DISPLAY_CONTEXT);
                                // submitMultiLayer: per-quad render types, so the
                                // translucent node surrogate glass renders like the
                                // 1.20.1 renderSingleBlock path did.
                                this.blockModel.submitMultiLayer(poseStack, submitNodeCollector,
                                        LIGHT_FULLBRIGHT, OverlayTexture.NO_OVERLAY, 0);
                            }
                            poseStack.popPose();
                        }
                        if (part != null && !state.access.isHiddenBySlice(h)) {
                            poseStack.pushPose();
                            poseStack.translate(l, h, w);
                            BlueprintBlockAccess.renderNodeOverlay(poseStack, nodeCollector, part, pipBillboard);
                            poseStack.popPose();
                        }
                    }
                }
            }
        }

        /**
         * NEOFORGE_PORT: аналог 1.20.1 Forge-пути, где такие блоки рисовались
         * тем же {@code renderSingleBlock}-пайплайном, но с ALTERNATE-моделью
         * блок-ентити; в 26.3 берётся сам рендерер блок-ентити (render-state
         * пайплайн). Блок-ентити создаётся без уровня, поэтому
         * {@code extractBase} даёт fullbright-свет, а публичный флаг
         * {@code hasLevel} (все BER-стейты мода гейтят submit по нему)
         * выставляется рефлексией, как настоящий.
         */
        private void submitBlockEntity(BlockState blockState, PoseStack poseStack,
                                       SubmitNodeCollector submitNodeCollector) {
            if (blockState.getBlock() instanceof EntityBlock entityBlock) {
                try {
                    BlockEntity blockEntity = entityBlock.newBlockEntity(BlockPos.ZERO, blockState);
                    if (blockEntity == null) {
                        return;
                    }
                    BlockEntityRenderer<BlockEntity, BlockEntityRenderState> renderer =
                            Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity);
                    if (renderer == null) {
                        return;
                    }
                    BlockEntityRenderState renderState = renderer.createRenderState();
                    renderer.extractRenderState(blockEntity, renderState, 0.0f, Vec3.ZERO, null);
                    try {
                        Field hasLevel = renderState.getClass().getField("hasLevel");
                        if (!Modifier.isStatic(hasLevel.getModifiers())
                                && hasLevel.getType() == boolean.class) {
                            hasLevel.setBoolean(renderState, true);
                        }
                    } catch (ReflectiveOperationException ignored) {
                    }
                    renderer.submit(renderState, poseStack, submitNodeCollector, new CameraRenderState());
                } catch (Exception ignored) {
                }
            }
        }

        @Override
        protected String getTextureLabel() {
            return "arcaneresearch_iso_structure";
        }
    }
}
