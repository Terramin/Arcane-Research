package arcaneresearch.client.renderers.tile;
import arcaneresearch.common.tiles.aura.NodeConverterTile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.client.renderers.entity.TurretObjModel;
@OnlyIn(Dist.CLIENT)
public class NodeConverterRenderer implements BlockEntityRenderer<NodeConverterTile> {
    private static final ResourceLocation MODEL = new ResourceLocation(Arcaneresearch.MODID,
            "models/block/node_converter.obj");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Arcaneresearch.MODID,
            "textures/models/node_converter.png");
    private static final ResourceLocation TEXTURE_OVER = new ResourceLocation(Arcaneresearch.MODID,
            "textures/models/node_converter_over.png");
    public NodeConverterRenderer(BlockEntityRendererProvider.Context context) {
    }
    @Override
    public void render(NodeConverterTile tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile.getLevel() == null) {
            return;
        }
        renderDevice(poseStack, buffer, tile.count, tile.status, packedLight, packedOverlay);
    }
    public static void renderDevice(PoseStack poseStack, MultiBufferSource buffer,
                                    int count, int status, int packedLight, int packedOverlay) {
        TurretObjModel model = TurretObjModel.load(MODEL);
        if (model == null) {
            return;
        }
        float v = Math.min(50, Math.max(0, count)) / 137.0F;
        float[] glow = statusColor(status);
        int glowLight = glowLightForPiston(v, 0);
        poseStack.pushPose();
        poseStack.translate(0.5, 1.0, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        // NEOFORGE_PORT (26.3 renderDevice 1:1): base/over рисуются одним
        // objSolid-слоём (ENTITY_CUTOUT c отсечением задних граней = наш
        // objSolidCulled); ранний over-слой был без cull и рендерил
        // внутренние грани насквозь.
        VertexConsumer base = buffer.getBuffer(TCRenderTypes.objSolidCulled(TEXTURE));
        model.renderGroup(poseStack, base, "lock", packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        VertexConsumer over = buffer.getBuffer(TCRenderTypes.objSolidCulled(TEXTURE_OVER));
        model.renderGroup(poseStack, over, "lock", glowLight, packedOverlay,
                glow[0], glow[1], glow[2], 1.0F);
        for (int a = 0; a < 4; a++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F * a));
            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            poseStack.translate(0.0, 0.0, v);
            model.renderGroup(poseStack, base, "piston", packedLight, packedOverlay,
                    1.0F, 1.0F, 1.0F, 1.0F);
            model.renderGroup(poseStack, over, "piston", glowLightForPiston(v, a),
                    packedOverlay, glow[0], glow[1], glow[2], 1.0F);
            poseStack.popPose();
        }
        poseStack.popPose();
    }
    private static float[] statusColor(int status) {
        if (status == 2) {
            return new float[]{1.0F, 0.0F, 0.3F};
        }
        if (status == 1) {
            return new float[]{1.0F, 0.6F, 0.1F};
        }
        return new float[]{0.5F, 1.0F, 0.5F};
    }
    private static int glowLightForPiston(float v, int offset) {
        int ticks = 0;
        try {
            if (Minecraft.getInstance().player != null) {
                ticks = Minecraft.getInstance().player.tickCount + offset * 5;
            }
        } catch (Exception ignored) {
        }
        float scale = Mth.sin(ticks / 3.0F) * 0.1F + 0.9F;
        int glow = 50 + (int) (170.0F * v * 2.5F * scale);
        return LightTexture.pack(Math.min(15, Math.max(0, glow >> 4)), 0);
    }
}
