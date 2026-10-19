package arcaneresearch.client.renderers.tile;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import arcaneresearch.common.tiles.crafting.TileVoidSiphon;
@OnlyIn(Dist.CLIENT)
public class VoidSiphonRenderer implements BlockEntityRenderer<TileVoidSiphon> {
    private static final ResourceLocation END_PORTAL_TEXTURE =
            new ResourceLocation("textures/entity/end_portal.png");
    private static final RenderType VOID_TOP = makeVoidType("tc_void_top", true);
    private static final RenderType VOID_BOTTOM = makeVoidType("tc_void_bottom", false);
    public VoidSiphonRenderer(BlockEntityRendererProvider.Context context) {
    }
    private static RenderType makeVoidType(String name, boolean additive) {
        RenderStateShard.TransparencyStateShard transparency = additive
                ? new RenderStateShard.TransparencyStateShard("void_additive",
                () -> {
                    RenderSystem.enableBlend();
                    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                            GlStateManager.DestFactor.ONE);
                },
                () -> {
                    RenderSystem.disableBlend();
                    RenderSystem.defaultBlendFunc();
                })
                : new RenderStateShard.TransparencyStateShard("void_translucent",
                () -> {
                    RenderSystem.enableBlend();
                    RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                },
                () -> {
                    RenderSystem.disableBlend();
                    RenderSystem.defaultBlendFunc();
                });
        return RenderType.create(name,
                DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS,
                256, false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(new RenderStateShard.ShaderStateShard(
                                GameRenderer::getRendertypeEndPortalShader))
                        .setTextureState(RenderStateShard.MultiTextureStateShard.builder()
                                .add(TheEndPortalRenderer.END_SKY_LOCATION, false, false)
                                .add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false)
                                .build())
                        .setTransparencyState(transparency)
                        .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
                        .setCullState(new RenderStateShard.CullStateShard(true))
                        .setDepthTestState(new RenderStateShard.DepthTestStateShard(
                                "void_depth", org.lwjgl.opengl.GL11.GL_LEQUAL))
                        .createCompositeState(false));
    }
    @Override
    public void render(TileVoidSiphon tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile.getLevel() == null) return;
        net.minecraft.world.level.block.state.BlockState state =
                tile.getLevel().getBlockState(tile.getBlockPos());
        if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.ENABLED)
                && !state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.ENABLED)) {
            return;
        }
        renderVoidBox(poseStack, buffer, 0.875f, 0.126f, 0.2f, 0.2f, 0.2f, true, true);
        renderVoidBox(poseStack, buffer, 0.3125f, 0.26f, 0.25f, 0.5f, 0.25f, false, false);
    }
    private void renderVoidBox(PoseStack poseStack, MultiBufferSource buffer,
                               float height, float offset,
                               float sx, float sy, float sz, boolean alwaysFlat, boolean additive) {
        poseStack.pushPose();
        poseStack.translate(0.5, height, 0.5);
        VertexConsumer vertexConsumer = buffer.getBuffer(additive ? VOID_TOP : VOID_BOTTOM);
        for (Direction face : Direction.values()) {
            poseStack.pushPose();
            org.joml.Vector3f axis = new org.joml.Vector3f(
                    -face.getStepY(), face.getStepX(), -face.getStepZ());
            poseStack.mulPose(Axis.of(axis).rotationDegrees(90.0f));
            if (face.getStepZ() < 0) {
                poseStack.translate(0, 0, offset);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
            } else {
                poseStack.translate(0, 0, -offset);
            }
            if (alwaysFlat || face.getStepZ() != 0) {
                poseStack.mulPose(Axis.ZN.rotationDegrees(90.0f));
            }
            poseStack.scale(sx, sy, sz);
            Matrix4f matrix = poseStack.last().pose();
            float half = 0.5f;
            vertexConsumer.vertex(matrix, -half, -half, 0).endVertex();
            vertexConsumer.vertex(matrix, half, -half, 0).endVertex();
            vertexConsumer.vertex(matrix, half, half, 0).endVertex();
            vertexConsumer.vertex(matrix, -half, half, 0).endVertex();
            poseStack.popPose();
        }
        poseStack.popPose();
    }
}
