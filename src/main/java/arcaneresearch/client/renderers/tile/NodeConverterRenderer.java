package arcaneresearch.client.renderers.tile;

import arcaneresearch.common.tiles.aura.NodeConverterTile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.client.renderers.entity.TurretObjModel;

@OnlyIn(Dist.CLIENT)
public class NodeConverterRenderer implements BlockEntityRenderer<NodeConverterTile> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID,
            "models/block/node_converter.obj");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID,
            "textures/models/node_converter.png");
    private static final ResourceLocation TEXTURE_OVER = ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID,
            "textures/models/node_converter_over.png");

    private static final float[] GLOW_STATUS_2 = {1.0F, 0.0F, 0.3F};
    private static final float[] GLOW_STATUS_1 = {1.0F, 0.6F, 0.1F};
    private static final float[] GLOW_STATUS_DEFAULT = {0.5F, 1.0F, 0.5F};

    private static final Quaternionf ROT_XP_90 = Axis.XP.rotationDegrees(90.0F);
    private static final Quaternionf ROT_YP_45 = Axis.YP.rotationDegrees(45.0F);
    private static final Quaternionf[] PISTON_ROTATIONS = new Quaternionf[4];

    static {
        for (int a = 0; a < 4; a++) {
            PISTON_ROTATIONS[a] = Axis.ZP.rotationDegrees(90.0F * a);
        }
    }

    public NodeConverterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull NodeConverterTile tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = tile.getLevel();
        if (level == null) return;
        renderDevice(poseStack, buffer, tile.count, tile.status, packedLight, packedOverlay);
    }

    public static void renderDevice(PoseStack poseStack, MultiBufferSource buffer,
                                    int count, int status, int packedLight, int packedOverlay) {
        if (poseStack == null || buffer == null) return;
        TurretObjModel model = TurretObjModel.load(MODEL);
        if (model == null) return;

        int ticks = playerTicks();
        float v = Math.min(50, Math.max(0, count)) / 137.0F;
        float[] glow = statusColor(status);
        int glowLightBase = glowLightForPiston(ticks, v, 0);

        RenderType baseType = TCRenderTypes.objSolidCulled(TEXTURE);
        RenderType overType = TCRenderTypes.objSolidCulled(TEXTURE_OVER);

        VertexConsumer base = buffer.getBuffer(baseType);
        poseStack.pushPose();
        try {
            poseStack.translate(0.5, 1.0, 0.5);
            poseStack.mulPose(ROT_XP_90);

            model.renderGroup(poseStack, base, "lock", packedLight, packedOverlay,
                    1.0F, 1.0F, 1.0F, 1.0F);

            for (int a = 0; a < 4; a++) {
                poseStack.pushPose();
                try {
                    poseStack.mulPose(PISTON_ROTATIONS[a]);
                    poseStack.mulPose(ROT_YP_45);
                    poseStack.translate(0.0, 0.0, v);
                    model.renderGroup(poseStack, base, "piston", packedLight, packedOverlay,
                            1.0F, 1.0F, 1.0F, 1.0F);
                } finally {
                    poseStack.popPose();
                }
            }
        } finally {
            poseStack.popPose();
        }

        VertexConsumer over = buffer.getBuffer(overType);
        poseStack.pushPose();
        try {
            poseStack.translate(0.5, 1.0, 0.5);
            poseStack.mulPose(ROT_XP_90);

            model.renderGroup(poseStack, over, "lock", glowLightBase, packedOverlay,
                    glow[0], glow[1], glow[2], 1.0F);

            for (int a = 0; a < 4; a++) {
                poseStack.pushPose();
                try {
                    poseStack.mulPose(PISTON_ROTATIONS[a]);
                    poseStack.mulPose(ROT_YP_45);
                    poseStack.translate(0.0, 0.0, v);
                    model.renderGroup(poseStack, over, "piston", glowLightForPiston(ticks, v, a),
                            packedOverlay, glow[0], glow[1], glow[2], 1.0F);
                } finally {
                    poseStack.popPose();
                }
            }
        } finally {
            poseStack.popPose();
        }
    }

    private static float[] statusColor(int status) {
        if (status == 2) return GLOW_STATUS_2;
        if (status == 1) return GLOW_STATUS_1;
        return GLOW_STATUS_DEFAULT;
    }

    private static int glowLightForPiston(int ticks, float v, int offset) {
        float scale = Mth.sin((ticks + offset * 5) / 3.0F) * 0.1F + 0.9F;
        int glow = 50 + (int) (170.0F * v * 2.5F * scale);
        return LightTexture.pack(Math.min(15, Math.max(0, glow >> 4)), 0);
    }

    private static int playerTicks() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        return player != null ? player.tickCount : 0;
    }
}