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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.client.models.block.ResearchTableModel;
import arcaneresearch.discovered.table.HexNoteTableBlock;
import arcaneresearch.discovered.table.HexNoteTableTile;
import arcaneresearch.init.ModItems;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

@OnlyIn(Dist.CLIENT)
public class HexNoteTableRenderer implements BlockEntityRenderer<HexNoteTableTile> {
    private static final ResourceLocation QUILL_TEXTURE =
            IDsHelper.mod("textures/research/quill.png");
    private static final ResourceLocation SCROLL_TEXTURE =
            IDsHelper.mod("textures/block/research_table_model.png");
    private static final ResourceLocation INKWELL_TEXTURE =
            IDsHelper.mod("textures/block/restable_model.png");

    private static ItemStack thaumoStack;
    private final ResearchTableModel scrollModel;

    public HexNoteTableRenderer(BlockEntityRendererProvider.Context context) {
        ResearchTableModel model = null;
        if (context != null) {
            try {
                var baked = context.bakeLayer(ResearchTableModel.LAYER_LOCATION);
                model = new ResearchTableModel(baked);
            } catch (Exception ignored) {
            }
        }
        this.scrollModel = model;
    }

    @Override
    public void render(@NotNull HexNoteTableTile tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Direction facing = Direction.NORTH;
        try {
            BlockState state = tile.getBlockState();
            if (state.getBlock() instanceof HexNoteTableBlock) {
                AtomicReference<Direction> f = new AtomicReference<>(state.getValue(HexNoteTableBlock.FACING));
                facing = f.get();
            }
        } catch (Exception ignored) {
        }
        ItemStack tools = ItemStack.EMPTY;
        try {
            tools = tile.getItem(HexNoteTableTile.INK_SLOT);
        } catch (Exception ignored) {
        }
        poseStack.pushPose();
        try {
            poseStack.translate(0.5, 1.0, 0.5);
            poseStack.mulPose(Axis.XP.rotationDegrees(180));
            applyRotation(poseStack, facing);

            ItemStack noteStack = null;
            try {
                noteStack = tile.getItem(HexNoteTableTile.NOTE_SLOT);
            } catch (Exception ignored) {
            }
            if (noteStack != null && !noteStack.isEmpty() && scrollModel != null) {
                poseStack.pushPose();
                try {
                    poseStack.translate(0.125, 0.0, -0.125);
                    VertexConsumer scrollBuffer =
                            buffer.getBuffer(RenderType.entityCutout(SCROLL_TEXTURE));
                    int alchemy = Aspect.ALCHEMY.getColor();
                    float r = ((alchemy >> 16) & 0xFF) / 255.0F;
                    float g = ((alchemy >> 8) & 0xFF) / 255.0F;
                    float b = (alchemy & 0xFF) / 255.0F;
                    scrollModel.renderScroll(poseStack, scrollBuffer, packedLight,
                            packedOverlay, r, g, b, 1.0F);
                } catch (Exception ignored) {
                } finally {
                    poseStack.popPose();
                }
            }

            Minecraft mc = Minecraft.getInstance();
            ItemStack thaumo = null;
            try {
                if (thaumoStack == null || thaumoStack.isEmpty()) {
                    var item = ModItems.THAUMOMETER.get();
                    thaumoStack = new ItemStack(item);
                }
                if (thaumoStack != null && !thaumoStack.isEmpty()) {
                    thaumo = thaumoStack;
                }
            } catch (Exception ignored) {
            }
            if (thaumo != null) {
                poseStack.pushPose();
                try {
                    poseStack.mulPose(Axis.XP.rotationDegrees(180));
                    poseStack.translate(1.0, 0.15, 0.0);
                    poseStack.mulPose(Axis.YP.rotationDegrees(-22.5F));
                    poseStack.scale(0.6F, 0.6F, 0.6F);
                    mc.getItemRenderer().renderStatic(thaumo,
                            ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY,
                            poseStack, buffer, tile.getLevel(), 0);
                } catch (Exception ignored) {
                } finally {
                    poseStack.popPose();
                }
            }

            poseStack.pushPose();
            try {
                poseStack.mulPose(Axis.XP.rotationDegrees(180));
                if (!tools.isEmpty()) {
                    try {
                        drawInkwell(poseStack, buffer, packedLight);
                    } catch (Exception ignored) {
                    }
                }
                poseStack.translate(-0.28, 0.05, -0.28);
                poseStack.mulPose(Axis.YP.rotationDegrees(60));
                poseStack.translate(0.25f, 0.25f, 0.0f);
                poseStack.mulPose(Axis.ZP.rotationDegrees(180));
                poseStack.translate(-0.25f, -0.25f, 0.0f);
                poseStack.scale(0.5f, 0.5f, 0.5f);
                if (!tools.isEmpty()) {
                    try {
                        VertexConsumer quillBuffer =
                                buffer.getBuffer(RenderType.entityTranslucent(QUILL_TEXTURE));
                        UtilsFX.renderItemIn2D(QUILL_TEXTURE, 0.0625f, quillBuffer,
                                poseStack, packedLight);
                    } catch (Exception ignored) {
                    }
                }
            } finally {
                poseStack.popPose();
            }
        } finally {
            poseStack.popPose();
        }
    }

    private static void drawInkwell(PoseStack pose, MultiBufferSource buffer, int light) {
        double cx = -0.28125, cy = 0.0625, cz = -0.28125;
        double x0 = cx - 0.09375, x1 = cx + 0.09375;
        double y0 = cy - 0.0625, y1 = cy + 0.0625;
        double z0 = cz - 0.09375, z1 = cz + 0.09375;
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(INKWELL_TEXTURE));
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
        org.joml.Matrix4f matrix = pose.last().pose();
        float uu1 = (float) (u1 / 16.0);
        float vv1 = (float) (v1 / 16.0);
        float uu2 = (float) (u2 / 16.0);
        float vv2 = (float) (11.75 / 16.0);
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
        if (facing == null) {
            return;
        }
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            default -> {
            }
        }
    }
}