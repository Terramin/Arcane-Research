package arcaneresearch.client.renderers.tile;

import arcaneresearch.common.tiles.aura.EldritchCrabTile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.client.renderers.entity.TurretObjModel;

@OnlyIn(Dist.CLIENT)
public class EldritchCrabRenderer implements BlockEntityRenderer<EldritchCrabTile> {
    private static final ResourceLocation VENT_MODEL =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "models/block/crabvent.obj");
    private static final ResourceLocation VENT_TEX =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/block/crabvent.png");

    private static final Quaternionf[] FACING_ROTATIONS = new Quaternionf[4];

    static {
        FACING_ROTATIONS[0] = Axis.YP.rotationDegrees(180.0F);
        FACING_ROTATIONS[1] = new Quaternionf();
        FACING_ROTATIONS[2] = Axis.YP.rotationDegrees(270.0F);
        FACING_ROTATIONS[3] = Axis.YP.rotationDegrees(90.0F);
    }

    private static final class Holder {
        static final TurretObjModel MODEL = load();
        static final RenderType TYPE = TCRenderTypes.objSolid(VENT_TEX);

        private static TurretObjModel load() {
            try {
                return TurretObjModel.load(VENT_MODEL);
            } catch (Throwable ignored) {
                return null;
            }
        }
    }

    public EldritchCrabRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull EldritchCrabTile tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = tile.getLevel();
        if (level == null) return;

        byte facing;
        try {
            facing = tile.getFacing();
        } catch (Exception e) {
            return;
        }

        TurretObjModel model = Holder.MODEL;
        if (model == null || !model.hasGroup("Vent")) return;

        Quaternionf facingRot;
        if (facing >= 0 && facing < FACING_ROTATIONS.length) {
            facingRot = FACING_ROTATIONS[facing];
        } else {
            facingRot = FACING_ROTATIONS[1];
        }

        try {
            VertexConsumer consumer = buffer.getBuffer(Holder.TYPE);
            poseStack.pushPose();
            try {
                poseStack.translate(0.5D, 0.5D, 0.5D);
                if (facingRot != null && facing != 1) {
                    poseStack.mulPose(facingRot);
                }
                model.renderGroup(poseStack, consumer, "Vent", packedLight, packedOverlay,
                        1.0F, 1.0F, 1.0F, 1.0F);
            } finally {
                poseStack.popPose();
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull EldritchCrabTile tile) {
        return true;
    }
}