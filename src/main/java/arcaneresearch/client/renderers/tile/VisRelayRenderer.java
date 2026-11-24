package arcaneresearch.client.renderers.tile;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.client.renderers.entity.TurretObjModel;
import arcaneresearch.common.blocks.aura.BlockVisRelay;
import arcaneresearch.common.lib.aura.VisNetHandler;
import arcaneresearch.common.tiles.aura.VisRelayTile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Renders the OBJ relay crystal and the CV beam to its parent. */
@OnlyIn(Dist.CLIENT)
public class VisRelayRenderer implements BlockEntityRenderer<VisRelayTile> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
            Arcaneresearch.MODID, "models/block/vis_relay.obj");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Arcaneresearch.MODID, "textures/models/vis_relay.png");
    private static final Vector3f UP = new Vector3f(0.0F, 1.0F, 0.0F);

    public VisRelayRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull VisRelayTile tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = tile.getLevel();
        if (level == null) return;

        Direction facing = tile.getBlockState().getValue(BlockVisRelay.FACING);
        Vec3i normal = facing.getNormal();
        int color = VisRelayTile.colorFor(tile.getAttunement());
        boolean linked = VisNetHandler.isNodeValid(tile.getParent());
        float ticks = (Minecraft.getInstance().player == null
                ? level.getGameTime() : Minecraft.getInstance().player.tickCount) + partialTicks;

        poseStack.pushPose();
        try {
            // Start at the centre of the face the relay is attached to, not at
            // the block centre. The OBJ is built with its rings at local y=0
            // and the crystal above, and the collision shape occupies only
            // 0..0.375 of the block from that face.
            // Lift half a pixel (0.5/16) off the face so the rings do not
            // z-fight with the block they are attached to.
            poseStack.translate(
                    0.5F - normal.getX() * 0.46875F,
                    0.5F - normal.getY() * 0.46875F,
                    0.5F - normal.getZ() * 0.46875F);
            poseStack.mulPose(new Quaternionf().rotateTo(UP,
                    new Vector3f(normal.getX(), normal.getY(), normal.getZ())));
            poseStack.mulPose(Axis.YP.rotation(ticks / 24.0F));
            poseStack.scale(0.56F, 0.56F, 0.56F);
            renderDevice(poseStack, buffer, color, linked, tile.getPulse() > 0,
                    packedLight, packedOverlay);
        } finally {
            poseStack.popPose();
        }

        // The vis-channel beam is drawn by FXBeamPower from VisRelayTile's
        // client tick, matching Thaumcraft's TileVisRelay.drawEffect().
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull VisRelayTile tile) {
        return true;
    }

    /** Used by both the block renderer and the inventory item renderer. */
    public static void renderDevice(PoseStack poseStack, MultiBufferSource buffer,
                                    int color, int packedLight, int packedOverlay) {
        renderDevice(poseStack, buffer, color, true, false, packedLight, packedOverlay);
    }

    private static void renderDevice(PoseStack poseStack, MultiBufferSource buffer, int color,
                                     boolean linked, boolean pulsing,
                                     int packedLight, int packedOverlay) {
        TurretObjModel model = TurretObjModel.load(MODEL);
        if (model == null) return;

        VertexConsumer consumer = buffer.getBuffer(TCRenderTypes.objSolidCulled(TEXTURE));
        model.renderGroup(poseStack, consumer, "RingBase", packedLight, packedOverlay,
                1.0F, 1.0F, 1.0F, 1.0F);
        model.renderGroup(poseStack, consumer, "RingFloat", packedLight, packedOverlay,
                1.0F, 1.0F, 1.0F, 1.0F);

        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float pulse = pulsing ? 1.25F : 1.0F;
        if (!linked) pulse *= 0.75F;
        int crystalLight = linked || pulsing ? LightTexture.pack(15, 15) : packedLight;
        model.renderGroup(poseStack, consumer, "Crystal", crystalLight, packedOverlay,
                Math.min(1.0F, r * pulse), Math.min(1.0F, g * pulse),
                Math.min(1.0F, b * pulse), 1.0F);
    }
}
