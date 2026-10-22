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

 




















 
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public final class IsoStructurePip {

    private IsoStructurePip() {
    }

      
    private static final float A = 0.70710678f;
      
    private static final float B = 0.29889f;
      
    private static final float C = 0.90631f;

     




 
    public static void submit(GuiGraphicsExtractor graphics, BlueprintBlockAccess access,
                              @Nullable Part[][][] parts, int xSize, int ySize, int zSize,
                              int cx, int cy, float boxW, float boxH, float maxS) {
        State state = State.create(access, parts, xSize, ySize, zSize, cx, cy, boxW, boxH, maxS);
        if (state != null) {
            graphics.submitPictureInPictureRenderState(state);
        }
    }

      
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
          
        final float scale;
          
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
            
            
            
            
            
            
            
            double wu = (maxPx - minPx + 1.0) + 2 * A;
            double hu = (maxPy - minPy + 1.0) + C + 2 * B;
            float s;
            if (boxW <= 0.0f || boxH <= 0.0f) {
                
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
            
            
            double pxC = (xSize - zSize) / 2.0 * A;
            double pyC = (xSize + zSize) / 2.0 * B;
            float shiftX = (float) ((minPx + maxPx) / 2.0 - pxC) * -s;
            float shiftY = (float) ((minPy + maxPy) / 2.0 - pyC) * -s;
            int width = (int) Math.round(wu * s) + 8;
            int height = (int) Math.round(hu * s) + 8;
            
            
            
            
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
            
            
            
            poseStack.translate(state.shiftX, state.shiftY, 0.0f);
            poseStack.scale(state.scale, state.scale, state.scale);
            
            
            poseStack.scale(1.0f, -1.0f, -1.0f);
            poseStack.rotateDegrees(Axis.XP, 25.0f);
            poseStack.rotateDegrees(Axis.YP, -45.0f);
            poseStack.translate(-state.xSize / 2.0f, -state.ySize / 2.0f, -state.zSize / 2.0f);

            
            
            
            
            
            
            
            
            
            
            OrderedSubmitNodeCollector nodeCollector = submitNodeCollector.order(1);
            
            
            
            
            Quaternionf pipBillboard = new Quaternionf(Axis.YP.rotationDegrees(45.0f))
                    .mul(new Quaternionf(Axis.XP.rotationDegrees(-25.0f)));
            for (int h = 0; h < state.ySize; h++) {
                for (int l = 0; l < state.xSize; l++) {
                    for (int w = 0; w < state.zSize; w++) {
                        BlockState blockState = state.access.getBlockState(new BlockPos(l, h, w));
                        
                        
                        
                        Part part = state.parts != null ? state.parts[state.ySize - 1 - h][l][w] : null;
                        if (!blockState.isAir()) {
                            poseStack.pushPose();
                            poseStack.translate(l, h, w);
                            if (BlueprintBlockAccess.isBerRendered(blockState)) {
                                
                                
                                this.submitBlockEntity(blockState, poseStack, submitNodeCollector);
                            } else {
                                this.blockModelResolver.update(this.blockModel, blockState, DISPLAY_CONTEXT);
                                
                                
                                
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
