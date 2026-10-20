package arcaneresearch.client.renderers.tile;
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
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.client.renderers.entity.TurretObjModel;
import arcaneresearch.common.tiles.aura.StabilizerTile;
@OnlyIn(Dist.CLIENT)
public class StabilizerRenderer implements BlockEntityRenderer<StabilizerTile> {
    private static final ResourceLocation MODEL = new ResourceLocation(Arcaneresearch.MODID,
            "models/block/node_converter.obj");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Arcaneresearch.MODID,
            "textures/models/node_stabilizer.png");
    private static final ResourceLocation TEXTURE_OVER = new ResourceLocation(Arcaneresearch.MODID,
            "textures/models/node_stabilizer_over.png");
    private static final ResourceLocation BUBBLE = new ResourceLocation(Arcaneresearch.MODID,
            "textures/misc/node_bubble.png");
    public StabilizerRenderer(BlockEntityRendererProvider.Context context) {
    }
    @Override
    public void render(StabilizerTile tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile.getLevel() == null) {
            return;
        }
        renderDevice(poseStack, buffer, tile.count, tile.getLock(), packedLight, packedOverlay);
    }
    public static void renderDevice(PoseStack poseStack, MultiBufferSource buffer,
                                    int count, int lock, int packedLight, int packedOverlay) {
        renderDevice(poseStack, buffer, count, lock, packedLight, packedOverlay, true);
    }
    public static void renderDevice(PoseStack poseStack, MultiBufferSource buffer,
                                    int count, int lock, int packedLight, int packedOverlay,
                                    boolean bubble) {
        TurretObjModel model = TurretObjModel.load(MODEL);
        if (model == null) {
            return;
        }
        float v = Math.max(0, count) / 100.0F;
        float[] glow = lock == 2
                ? new float[]{1.0F, 0.2F, 0.2F}
                : new float[]{1.0F, 1.0F, 1.0F};
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        // NEOFORGE_PORT (26.3 renderDevice 1:1): base/over рисуются одним
        // objSolid-слоём (ENTITY_CUTOUT c отсечением задних граней = наш
        // objSolidCulled); двойное внутреннее дублирование граней убрано.
        VertexConsumer base = buffer.getBuffer(TCRenderTypes.objSolidCulled(TEXTURE));
        model.renderGroup(poseStack, base, "lock", packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        VertexConsumer over = buffer.getBuffer(TCRenderTypes.objSolidCulled(TEXTURE_OVER));
        for (int a = 0; a < 4; a++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F * a));
            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            poseStack.translate(0.0, 0.0, v);
            model.renderGroup(poseStack, base, "piston", packedLight, packedOverlay,
                    1.0F, 1.0F, 1.0F, 1.0F);
            model.renderGroup(poseStack, over, "piston", glowLight(count, a),
                    packedOverlay, glow[0], glow[1], glow[2], 1.0F);
            poseStack.popPose();
        }
        poseStack.popPose();
        if (bubble && count > 0) {
            float alphaPulse = Mth.sin(playerTicks() / 8.0F) * 0.1F + 0.5F;
            float alpha = count / 37.0F * alphaPulse;
            int color = lock == 1 ? 0xFFFFFF : 16729156;
            VertexConsumer bubbleQuad = buffer.getBuffer(TCRenderTypes.additiveTextured(BUBBLE));
            poseStack.pushPose();
            UtilsFX.renderFacingQuad(poseStack, bubbleQuad, 0.5D, 1.5D, 0.5D,
                    1, 1, 0, 0.9F, color, alpha, LightTexture.FULL_BRIGHT);
            poseStack.popPose();
        }
    }
    private static int glowLight(int count, int piston) {
        float scale = Mth.sin((playerTicks() + piston * 5) / 3.0F) * 0.1F + 0.9F;
        int glow = 50 + (int) (170.0F * Math.max(0, count) / 37.0F * scale);
        return LightTexture.pack(Math.min(15, Math.max(0, glow >> 4)), 0);
    }
    private static int playerTicks() {
        try {
            if (Minecraft.getInstance().player != null) {
                return Minecraft.getInstance().player.tickCount;
            }
        } catch (Exception ignored) {
        }
        return 0;
    }
}
