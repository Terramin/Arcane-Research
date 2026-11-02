package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.ArcaneresearchApiHelper;
import arcaneresearch.client.models.block.TubeValveModel;
import arcaneresearch.common.tiles.essentia.TileTubeBuffer;

@OnlyIn(Dist.CLIENT)
public class TubeBufferRenderer implements BlockEntityRenderer<TileTubeBuffer> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/valve.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(TEXTURE);
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Quaternionf[] DIRECTION_ROTATIONS = new Quaternionf[DIRECTIONS.length];

    static {
        for (Direction dir : DIRECTIONS) {
            Quaternionf q = new Quaternionf();
            Direction opp = dir.getOpposite();
            if (opp.getAxis() != Direction.Axis.Y) {
                q.mul(Axis.YP.rotationDegrees(90.0f));
            } else {
                q.mul(Axis.XN.rotationDegrees(90.0f));
                q.mul(Axis.YP.rotationDegrees(90.0f * opp.getStepY()));
            }
            q.mul(Axis.XP.rotationDegrees(90.0f * opp.getStepX()));
            q.mul(Axis.YP.rotationDegrees(90.0f * opp.getStepY()));
            q.mul(Axis.ZP.rotationDegrees(90.0f * opp.getStepZ()));
            DIRECTION_ROTATIONS[dir.ordinal()] = q;
        }
    }

    private final TubeValveModel model;

    public TubeBufferRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new TubeValveModel(context.bakeLayer(TubeValveModel.LAYER_LOCATION));
    }

    @Override
    public void render(@NotNull TileTubeBuffer tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = tile.getLevel();
        if (level == null) return;
        byte[] choked = tile.chokedSides;
        boolean[] open = tile.openSides;
        if (choked == null || open == null) return;
        if (choked.length < DIRECTIONS.length || open.length < DIRECTIONS.length) return;

        VertexConsumer vertexConsumer = buffer.getBuffer(RENDER_TYPE);
        for (int i = 0; i < DIRECTIONS.length; i++) {
            Direction dir = DIRECTIONS[i];
            byte choke = choked[i];
            if (choke == 0 || !open[i]) continue;

            BlockEntity connectedTile = ArcaneresearchApiHelper.getConnectableTile(
                    level, tile.getBlockPos(), dir);
            if (connectedTile == null) continue;

            poseStack.pushPose();
            try {
                poseStack.translate(0.5, 0.5, 0.5);
                poseStack.mulPose(DIRECTION_ROTATIONS[i]);
                float r, g, b;
                if (choke == 2) {
                    r = 1.0f;
                    g = 0.3f;
                    b = 0.3f;
                } else {
                    r = 0.3f;
                    g = 0.3f;
                    b = 1.0f;
                }
                poseStack.scale(0.0625f, 0.0625f, 0.0625f);
                poseStack.scale(2.0f, 1.0f, 2.0f);
                poseStack.translate(0, -0.5 / 0.0625, 0);
                model.renderRod(poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, 1.0f);
            } finally {
                poseStack.popPose();
            }
        }
    }
}