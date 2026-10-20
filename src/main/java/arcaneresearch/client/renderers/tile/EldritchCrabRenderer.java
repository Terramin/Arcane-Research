package arcaneresearch.client.renderers.tile;
import arcaneresearch.common.tiles.aura.EldritchCrabTile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.client.renderers.entity.TurretObjModel;
@OnlyIn(Dist.CLIENT)
public class EldritchCrabRenderer implements BlockEntityRenderer<EldritchCrabTile> {
    private static final ResourceLocation VENT_MODEL =
            new ResourceLocation(Arcaneresearch.MODID, "models/block/crabvent.obj");
    private static final ResourceLocation VENT_TEX =
            new ResourceLocation(Arcaneresearch.MODID, "textures/block/crabvent.png");
    private static volatile TurretObjModel VENT_OBJ = null;
    private static volatile boolean VENT_TRIED = false;
    private static TurretObjModel ventModel() {
        if (!VENT_TRIED) {
            synchronized (EldritchCrabRenderer.class) {
                if (!VENT_TRIED) {
                    VENT_TRIED = true;
                    try {
                        VENT_OBJ = TurretObjModel.load(VENT_MODEL);
                    } catch (Throwable ignored) {
                        VENT_OBJ = null;
                    }
                }
            }
        }
        return VENT_OBJ;
    }
    public EldritchCrabRenderer(BlockEntityRendererProvider.Context context) {
    }
    @Override
    public void render(EldritchCrabTile tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile.getLevel() == null) {
            return;
        }
        byte facing = 0;
        try {
            facing = tile.getFacing();
        } catch (Exception e) {
            return;
        }
        TurretObjModel model;
        try {
            model = ventModel();
        } catch (Throwable ignored) {
            return;
        }
        if (model == null || !model.hasGroup("Vent")) {
            return;
        }
        try {
            VertexConsumer consumer = buffer.getBuffer(TCRenderTypes.objSolid(VENT_TEX));
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.5D, 0.5D);
            switch (facing) {
                case 0 -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                case 2 -> poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
                case 3 -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                default -> {
                }
            }
            model.renderGroup(poseStack, consumer, "Vent", packedLight, packedOverlay,
                    1.0F, 1.0F, 1.0F, 1.0F);
            poseStack.popPose();
        } catch (Throwable ignored) {
        }
    }
    @Override
    public boolean shouldRenderOffScreen(EldritchCrabTile tile) {
        return true;
    }
}
