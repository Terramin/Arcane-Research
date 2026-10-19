package arcaneresearch.discovered.client;
import arcaneresearch.helper.IDsHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.client.models.block.ResearchTableModel;
import arcaneresearch.discovered.table.HexNoteTableBlock;
import arcaneresearch.discovered.table.HexNoteTableTile;
import arcaneresearch.init.ModItems;
@OnlyIn(Dist.CLIENT)
public class HexNoteTableRenderer implements BlockEntityRenderer<HexNoteTableTile> {
    private static final ResourceLocation QUILL_TEXTURE =
            IDsHelper.mod("textures/research/quill.png");
    private static final ResourceLocation SCROLL_TEXTURE =
            IDsHelper.mod("textures/block/research_table_model.png");
    private static ItemStack thaumoStack;
    private final ResearchTableModel scrollModel;
    public HexNoteTableRenderer(BlockEntityRendererProvider.Context context) {
        this.scrollModel = new ResearchTableModel(context.bakeLayer(ResearchTableModel.LAYER_LOCATION));
    }
    @Override
    public void render(HexNoteTableTile tile, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack tools;
        try {
            tools = tile.getItem(HexNoteTableTile.INK_SLOT);
        } catch (Exception e) {
            tools = ItemStack.EMPTY;
        }
        poseStack.pushPose();
        poseStack.translate(0.5, 1.0, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        applyRotation(poseStack, tile.getBlockState().getValue(HexNoteTableBlock.FACING));
        try {
            if (!tile.getItem(HexNoteTableTile.NOTE_SLOT).isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(0.125, 0.0, -0.125);
                VertexConsumer scrollBuffer =
                        buffer.getBuffer(RenderType.entityCutout(SCROLL_TEXTURE));
                int alchemy = Aspect.ALCHEMY.getColor();
                float r = ((alchemy >> 16) & 0xFF) / 255.0F;
                float g = ((alchemy >> 8) & 0xFF) / 255.0F;
                float b = (alchemy & 0xFF) / 255.0F;
                scrollModel.renderScroll(poseStack, scrollBuffer, packedLight,
                        packedOverlay, r, g, b, 1.0f);
                poseStack.popPose();
            }
        } catch (Exception ignored) {
        }
        try {
            if (thaumoStack == null || thaumoStack.isEmpty()) {
                thaumoStack = new ItemStack(ModItems.THAUMOMETER.get());
            }
            ItemStack thaumo = thaumoStack;
            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(180));
            poseStack.translate(1.0, 0.15, 0.0);
            poseStack.mulPose(Axis.YP.rotationDegrees(-22.5F));
            poseStack.scale(0.6F, 0.6F, 0.6F);
            Minecraft.getInstance().getItemRenderer().renderStatic(thaumo,
                    ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY,
                    poseStack, buffer, tile.getLevel(), 0);
            poseStack.popPose();
        } catch (Exception ignored) {
        }
        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        if (!tools.isEmpty()) {
            drawInkwell(poseStack, buffer, packedLight);
        }
        poseStack.translate(-0.28, 0.05, -0.28);
        poseStack.mulPose(Axis.YP.rotationDegrees(60));
        poseStack.translate(0.25f, 0.25f, 0.0f);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.translate(-0.25f, -0.25f, 0.0f);
        poseStack.scale(0.5f, 0.5f, 0.5f);
        if (!tools.isEmpty()) {
            VertexConsumer quillBuffer = buffer.getBuffer(RenderType.entityTranslucent(QUILL_TEXTURE));
            UtilsFX.renderItemIn2D(QUILL_TEXTURE, 0.0625f, quillBuffer, poseStack, packedLight);
        }
        poseStack.popPose();
        poseStack.popPose();
    }
    private static final ResourceLocation INKWELL_TEXTURE =
            IDsHelper.mod("textures/block/restable_model.png");
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
