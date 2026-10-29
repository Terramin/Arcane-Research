package arcaneresearch.discovered.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.discovered.table.CognitionNoteTableTile;
import arcaneresearch.helper.IDsHelper;
import org.jspecify.annotations.NonNull;

@OnlyIn(Dist.CLIENT)
public class CognitionNoteTableRenderer implements BlockEntityRenderer<CognitionNoteTableTile, CognitionNoteTableRenderer.State> {

    private static final Identifier QUILL_TEXTURE =
            IDsHelper.mod("textures/research/quill.png");
    private static final Identifier INKWELL_TEXTURE =
            IDsHelper.mod("textures/block/restable_model.png");

    private static final Identifier PARCHMENT_TEXTURE =
            IDsHelper.mod("textures/misc/parchment.png");

    private static final Identifier SCROLL_TEXTURE =
            IDsHelper.mod("textures/block/research_table_model.png");

    public static class State extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public boolean hasPaper;
        public boolean hasNote;
        public int noteColor = 0xFFFFFF;
    }

    private final arcaneresearch.client.models.block.ResearchTableModel scrollModel;

    public CognitionNoteTableRenderer(BlockEntityRendererProvider.Context context) {
        arcaneresearch.client.models.block.ResearchTableModel model = null;
        if (context != null) {
            try {
                model = new arcaneresearch.client.models.block.ResearchTableModel(
                        context.bakeLayer(arcaneresearch.client.models.block.ResearchTableModel.LAYER_LOCATION));
            } catch (Exception ignored) {
            }
        }
        this.scrollModel = model;
    }

    @Override
    public @NonNull State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(@NonNull CognitionNoteTableTile tile, @NonNull State state, float partialTicks,
                                   @NonNull Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        try {
            state.facing = tile.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        } catch (Exception e) {
            state.facing = Direction.NORTH;
        }
        ItemStack paper = ItemStack.EMPTY;
        ItemStack note = ItemStack.EMPTY;
        try {
            paper = tile.getItem(CognitionNoteTableTile.PAPER_SLOT);
        } catch (Exception ignored) {
        }
        try {
            note = tile.getItem(CognitionNoteTableTile.NOTE_SLOT);
        } catch (Exception ignored) {
        }
        state.hasPaper = !paper.isEmpty();
        state.hasNote = !note.isEmpty() && note.getItem() instanceof arcaneresearch.discovered.note.ItemDiscoveredResearchNote;
        state.noteColor = 0xFFFFFF;
        if (state.hasNote) {
            try {
                arcaneresearch.discovered.note.DiscoveredNoteData data =
                        arcaneresearch.discovered.note.DiscoveredNoteData.read(note);
                state.noteColor = arcaneresearch.helper.TintHelper.researchNoteColor(note, 1, data);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, @NonNull CameraRenderState cameraState) {
        poseStack.pushPose();
        poseStack.translate(0.5, 1.0, 0.5);
        poseStack.rotate(Axis.XP.rotationDegrees(180));
        applyRotation(poseStack, state.facing);

        poseStack.pushPose();
        poseStack.rotate(Axis.XP.rotationDegrees(180));


        RenderType inkwellType = TCRenderTypes.translucentPortal(INKWELL_TEXTURE);
        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, inkwellType, (pose, consumer) -> {
            PoseStack work = bridge(pose);
            drawInkwell(work, consumer, light);
        });


        poseStack.translate(-0.28, 0.05, -0.28);
        poseStack.rotate(Axis.YP.rotationDegrees(60));
        poseStack.translate(0.25f, 0.25f, 0.0f);
        poseStack.rotate(Axis.ZP.rotationDegrees(180));
        poseStack.translate(-0.25f, -0.25f, 0.0f);
        poseStack.scale(0.5f, 0.5f, 0.5f);

        RenderType quillType = TCRenderTypes.translucentPortal(QUILL_TEXTURE);
        collector.submitCustomGeometry(poseStack, quillType, (pose, consumer) -> {
            PoseStack work = bridge(pose);
            UtilsFX.renderItemIn2D(QUILL_TEXTURE, 0.0625f, consumer, work, light);
        });
        poseStack.popPose();


        if (state.hasPaper) {


            for (int a = 0; a < 6; a++) {
                poseStack.pushPose();
                poseStack.translate(1.0F, -0.01F - a * 0.015F, 0.0F);
                poseStack.rotate(Axis.XP.rotationDegrees(-90.0F));
                poseStack.rotate(Axis.ZP.rotationDegrees(15 + a % 3 * 2));
                poseStack.scale(0.5F, 0.6F, 0.6F);
                UtilsFX.renderQuadCentered(poseStack, collector, PARCHMENT_TEXTURE,
                        1.0F, 1.0F, 1.0F, 1.0F, light, 0, 1.0F);
                poseStack.popPose();
            }
        }
        if (state.hasNote && scrollModel != null) {
            poseStack.pushPose();
            poseStack.translate(0.125, 0.0, -0.125);
            RenderType scrollType = TCRenderTypes.objSolid(SCROLL_TEXTURE);
            int color = state.noteColor;
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;
            collector.submitCustomGeometry(poseStack, scrollType, (pose, consumer) -> {
                PoseStack work = bridge(pose);
                scrollModel.renderScroll(work, consumer, light,
                        OverlayTexture.NO_OVERLAY, r, g, b, 1.0f);
            });
            poseStack.popPose();
        }

        poseStack.popPose();
    }


    private static void paperQuad(VertexConsumer consumer, PoseStack.Pose pose, int light) {
        consumer.addVertex(pose, -0.5F, -0.5F, 0.0F)
                .setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, 0, 0, 1);
        consumer.addVertex(pose, 0.5F, -0.5F, 0.0F)
                .setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, 0, 0, 1);
        consumer.addVertex(pose, 0.5F, 0.5F, 0.0F)
                .setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, 0, 0, 1);
        consumer.addVertex(pose, -0.5F, 0.5F, 0.0F)
                .setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose, 0, 0, 1);
    }

    private static PoseStack bridge(PoseStack.Pose pose) {
        PoseStack work = new PoseStack();
        work.last().pose().set(pose.pose());
        work.last().normal().set(pose.normal());
        return work;
    }


    private static void drawInkwell(PoseStack pose, VertexConsumer vc, int light) {
        double cx = -0.28125, cy = 0.0625, cz = -0.28125;
        double x0 = cx - 0.09375, x1 = cx + 0.09375;
        double y0 = cy - 0.0625, y1 = cy + 0.0625;
        double z0 = cz - 0.09375, z1 = cz + 0.09375;


        face(vc, pose, light, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0,
                0.375, 11.0, 0.75, 0, 1, 0);
        face(vc, pose, light, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1,
                0.75, 11.0, 1.125, 0, -1, 0);
        face(vc, pose, light, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1,
                0.375, 12.25, 0.75, 0, 0, 1);
        face(vc, pose, light, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0,
                1.125, 12.25, 1.5, 0, 0, -1);
        face(vc, pose, light, x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1,
                0.0, 12.25, 0.375, 1, 0, 0);
        face(vc, pose, light, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0,
                0.75, 12.25, 1.125, -1, 0, 0);
    }

    private static void face(VertexConsumer vc, PoseStack pose, int light,
                             double x1, double y1, double z1, double x2, double y2, double z2,
                             double x3, double y3, double z3, double x4, double y4, double z4,
                             double u1, double v1, double u2,
                             float nx, float ny, float nz) {

        PoseStack.Pose p = pose.last();
        float uu1 = (float) (u1 / 16.0);
        float vv1 = (float) (v1 / 16.0);
        float uu2 = (float) (u2 / 16.0);
        float vv2 = (float) (11.75 / 16.0);
        vc.addVertex(p, (float) x1, (float) y1, (float) z1)
                .setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(uu1, vv1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(p, nx, ny, nz);
        vc.addVertex(p, (float) x2, (float) y2, (float) z2)
                .setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(uu2, vv1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(p, nx, ny, nz);
        vc.addVertex(p, (float) x3, (float) y3, (float) z3)
                .setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(uu2, vv2)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(p, nx, ny, nz);
        vc.addVertex(p, (float) x4, (float) y4, (float) z4)
                .setColor(1.0F, 1.0F, 1.0F, 1.0F).setUv(uu1, vv2)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(p, nx, ny, nz);
    }


    private static void applyRotation(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case SOUTH -> poseStack.rotate(Axis.YP.rotationDegrees(90));
            case WEST -> poseStack.rotate(Axis.YP.rotationDegrees(180));
            case NORTH -> poseStack.rotate(Axis.YP.rotationDegrees(270));
            default -> {
            }
        }
    }
}
