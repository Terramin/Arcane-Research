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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import arcaneresearch.common.tiles.crafting.TileVoidSiphon;

@OnlyIn(Dist.CLIENT)
public class VoidSiphonRenderer implements BlockEntityRenderer<TileVoidSiphon> {
    private static final ResourceLocation END_PORTAL_TEXTURE =
            ResourceLocation.tryParse("textures/entity/end_portal.png");
    private static final RenderType VOID_TOP = makeVoidType("tc_void_top", true);
    private static final RenderType VOID_BOTTOM = makeVoidType("tc_void_bottom", false);

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Quaternionf[] FACE_ROTATIONS = new Quaternionf[DIRECTIONS.length];
    private static final Quaternionf ROT_YP_180 = Axis.YP.rotationDegrees(180.0f);
    private static final Quaternionf ROT_ZN_90 = Axis.ZN.rotationDegrees(90.0f);

    static {
        for (int i = 0; i < DIRECTIONS.length; i++) {
            Direction face = DIRECTIONS[i];
            Vector3f axis = new Vector3f(-face.getStepY(), face.getStepX(), -face.getStepZ());
            FACE_ROTATIONS[i] = Axis.of(axis).rotationDegrees(90.0f);
        }
    }

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
    public void render(@NotNull TileVoidSiphon tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = tile.getLevel();
        if (level == null) return;
        BlockState state = level.getBlockState(tile.getBlockPos());
        if (state.hasProperty(BlockStateProperties.ENABLED)
                && !state.getValue(BlockStateProperties.ENABLED)) {
            return;
        }
        renderVoidBox(poseStack, buffer, 0.875f, 0.126f, 0.2f, 0.2f, 0.2f, true, true);
        renderVoidBox(poseStack, buffer, 0.3125f, 0.26f, 0.25f, 0.5f, 0.25f, false, false);
    }

    private void renderVoidBox(PoseStack poseStack, MultiBufferSource buffer,
                               float height, float offset,
                               float sx, float sy, float sz, boolean alwaysFlat, boolean additive) {
        poseStack.pushPose();
        try {
            poseStack.translate(0.5, height, 0.5);
            VertexConsumer vertexConsumer = buffer.getBuffer(additive ? VOID_TOP : VOID_BOTTOM);
            for (int i = 0; i < DIRECTIONS.length; i++) {
                Direction face = DIRECTIONS[i];
                poseStack.pushPose();
                try {
                    poseStack.mulPose(FACE_ROTATIONS[i]);
                    if (face.getStepZ() < 0) {
                        poseStack.translate(0, 0, offset);
                        poseStack.mulPose(ROT_YP_180);
                    } else {
                        poseStack.translate(0, 0, -offset);
                    }
                    if (alwaysFlat || face.getStepZ() != 0) {
                        poseStack.mulPose(ROT_ZN_90);
                    }
                    poseStack.scale(sx, sy, sz);
                    Matrix4f matrix = poseStack.last().pose();
                    float half = 0.5f;
                    vertexConsumer.vertex(matrix, -half, -half, 0).endVertex();
                    vertexConsumer.vertex(matrix, half, -half, 0).endVertex();
                    vertexConsumer.vertex(matrix, half, half, 0).endVertex();
                    vertexConsumer.vertex(matrix, -half, half, 0).endVertex();
                } finally {
                    poseStack.popPose();
                }
            }
        } finally {
            poseStack.popPose();
        }
    }
}