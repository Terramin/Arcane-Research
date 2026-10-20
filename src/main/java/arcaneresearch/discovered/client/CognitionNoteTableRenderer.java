package arcaneresearch.discovered.client;
import arcaneresearch.helper.IDsHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.discovered.table.CognitionNoteTableTile;
@OnlyIn(Dist.CLIENT)
public class CognitionNoteTableRenderer implements BlockEntityRenderer<CognitionNoteTableTile> {
    private static final ResourceLocation QUILL_TEXTURE =
            IDsHelper.mod("textures/research/quill.png");
    private static final ResourceLocation INKWELL_TEXTURE =
            IDsHelper.mod("textures/block/restable_model.png");
    private static final ResourceLocation PARCHMENT_TEXTURE =
            IDsHelper.mod("textures/misc/parchment.png");
    private static final ResourceLocation SCROLL_TEXTURE =
            IDsHelper.mod("textures/block/research_table_model.png");
    private final arcaneresearch.client.models.block.ResearchTableModel scrollModel;
    public CognitionNoteTableRenderer(BlockEntityRendererProvider.Context context) {
        this.scrollModel = new arcaneresearch.client.models.block.ResearchTableModel(
                context.bakeLayer(arcaneresearch.client.models.block.ResearchTableModel.LAYER_LOCATION));
    }
    @Override
    public void render(CognitionNoteTableTile tile, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 1.0, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        applyRotation(poseStack, tile.getBlockState().getValue(HorizontalDirectionalBlock.FACING));
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        drawInkwell(poseStack, buffer, packedLight);
        poseStack.translate(-0.28, 0.05, -0.28);
        poseStack.mulPose(Axis.YP.rotationDegrees(60));
        poseStack.translate(0.25f, 0.25f, 0.0f);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.translate(-0.25f, -0.25f, 0.0f);
        poseStack.scale(0.5f, 0.5f, 0.5f);
        VertexConsumer quillBuffer = buffer.getBuffer(RenderType.entityTranslucent(QUILL_TEXTURE));
        UtilsFX.renderItemIn2D(QUILL_TEXTURE, 0.0625f, quillBuffer, poseStack, packedLight);
        poseStack.popPose();
        try {
            ItemStack paper = tile.getItem(CognitionNoteTableTile.PAPER_SLOT);
            if (paper != null && !paper.isEmpty()) {
                for (int a = 0; a < 6; a++) {
                    poseStack.pushPose();
                    poseStack.translate(1.0F, -0.01F - a * 0.015F, 0.0F);
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(15 + a % 3 * 2));
                    poseStack.scale(0.5F, 0.6F, 0.6F);
                    UtilsFX.renderQuadCentered(poseStack, buffer, PARCHMENT_TEXTURE,
                            1.0F, 1.0F, 1.0F, 1.0F, packedLight, 0, 1.0F);
                    poseStack.popPose();
                }
            }
        } catch (Exception ignored) {
        }
        try {
            ItemStack note = tile.getItem(CognitionNoteTableTile.NOTE_SLOT);
            if (note != null && !note.isEmpty()
                    && note.getItem() instanceof arcaneresearch.discovered.note.ItemDiscoveredResearchNote) {
                int color = 0xFFFFFF;
                try {
                    arcaneresearch.discovered.note.DiscoveredNoteData data =
                            arcaneresearch.discovered.note.DiscoveredNoteData.read(note);
                    color = arcaneresearch.helper.TintHelper.researchNoteColor(note, 1, data);
                } catch (Exception ignored) {
                }
                float r = ((color >> 16) & 0xFF) / 255.0F;
                float g = ((color >> 8) & 0xFF) / 255.0F;
                float b = (color & 0xFF) / 255.0F;
                poseStack.pushPose();
                poseStack.translate(0.125, 0.0, -0.125);
                VertexConsumer scrollBuffer =
                        buffer.getBuffer(RenderType.entityCutout(SCROLL_TEXTURE));
                scrollModel.renderScroll(poseStack, scrollBuffer, packedLight,
                        packedOverlay, r, g, b, 1.0f);
                poseStack.popPose();
            }
        } catch (Exception ignored) {
        }
        poseStack.popPose();
    }
    private static void drawInkwell(PoseStack pose, MultiBufferSource buffer, int light) {
        double cx = -0.28125, cy = 0.0625, cz = -0.28125;
        double x0 = cx - 0.09375, x1 = cx + 0.09375;
        double y0 = cy - 0.0625, y1 = cy + 0.0625;
        double z0 = cz - 0.09375, z1 = cz + 0.09375;
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(INKWELL_TEXTURE));
        face(vc, pose, light, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0,
                0.375, 11.0, 0.75, 11.75, 0, 1, 0);
        face(vc, pose, light, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1,
                0.75, 11.0, 1.125, 11.75, 0, -1, 0);
        face(vc, pose, light, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1,
                0.375, 12.25, 0.75, 11.75, 0, 0, 1);
        face(vc, pose, light, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0,
                1.125, 12.25, 1.5, 11.75, 0, 0, -1);
        face(vc, pose, light, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1,
                0.0, 12.25, 0.375, 11.75, 1, 0, 0);
        face(vc, pose, light, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0,
                0.75, 12.25, 1.125, 11.75, -1, 0, 0);
    }
    private static void face(VertexConsumer vc, PoseStack pose, int light,
            double x1, double y1, double z1, double x2, double y2, double z2,
            double x3, double y3, double z3, double x4, double y4, double z4,
            double u1, double v1, double u2, double v2,
            float nx, float ny, float nz) {
        org.joml.Matrix4f matrix = pose.last().pose();
        float uu1 = (float) (u1 / 16.0);
        float vv1 = (float) (v1 / 16.0);
        float uu2 = (float) (u2 / 16.0);
        float vv2 = (float) (v2 / 16.0);
        vc.vertex(matrix, (float) x1, (float) y1, (float) z1)
                .color(1.0F, 1.0F, 1.0F, 1.0F).uv(uu1, vv1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(nx, ny, nz).endVertex();
        vc.vertex(matrix, (float) x2, (float) y2, (float) z2)
                .color(1.0F, 1.0F, 1.0F, 1.0F).uv(uu2, vv1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(nx, ny, nz).endVertex();
        vc.vertex(matrix, (float) x3, (float) y3, (float) z3)
                .color(1.0F, 1.0F, 1.0F, 1.0F).uv(uu2, vv2)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(nx, ny, nz).endVertex();
        vc.vertex(matrix, (float) x4, (float) y4, (float) z4)
                .color(1.0F, 1.0F, 1.0F, 1.0F).uv(uu1, vv2)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(nx, ny, nz).endVertex();
    }
    private static void applyRotation(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            default -> {
            }
        }
    }
}
