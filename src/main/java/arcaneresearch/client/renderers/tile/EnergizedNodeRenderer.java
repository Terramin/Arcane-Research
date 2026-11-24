package arcaneresearch.client.renderers.tile;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.tiles.aura.TileNodeEnergized;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class EnergizedNodeRenderer implements BlockEntityRenderer<TileNodeEnergized> {
    /** Thaumcraft's textures/items/lightningringv.png: 16 horizontal frames. */
    private static final ResourceLocation RING_TEX = ResourceLocation.fromNamespaceAndPath(
            Arcaneresearch.MODID, "textures/item/lightningringv.png");
    private static final int RING_FRAMES = 16;
    private static final int FULLBRIGHT = 0xF000F0;

    public EnergizedNodeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileNodeEnergized tile, float partialTicks, PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        net.minecraft.world.entity.Entity camEntity = Minecraft.getInstance().getCameraEntity();
        LivingEntity viewer = camEntity instanceof LivingEntity ? (LivingEntity) camEntity : null;
        Player player = viewer instanceof Player ? (Player) viewer : null;

        poseStack.pushPose();
        AuraNodeRenderHelper.renderNode(poseStack, buffer, tile.getAuraBase(), tile.getNodeType(),
                tile.getNodeModifier(), tile.getBlockPos(), viewer,
                new Vector3f(0.5F, 0.5F, 0.5F), 1.0F,
                AuraNodeRenderHelper.resolve(player, true), partialTicks);
        // The energized node is always visible and gets an extra additive,
        // rapidly flipping lightning ring on top (TileNodeEnergizedRenderer).
        renderRing(poseStack, buffer, tile);
        poseStack.popPose();
    }

    private static void renderRing(PoseStack poseStack, MultiBufferSource buffer,
                                   TileNodeEnergized tile) {
        long nano = System.nanoTime();
        int frame = (int) ((nano / 40000000L + tile.getBlockPos().getX()) % RING_FRAMES);
        if (frame < 0) frame += RING_FRAMES;

        Quaternionf cam = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        Vector3f right = new Vector3f(1.0F, 0.0F, 0.0F).rotate(cam);
        Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F).rotate(cam);
        Vector3f face = new Vector3f(0.0F, 0.0F, 1.0F).rotate(cam);

        float scale = 0.33F;
        float u0 = (float) frame / RING_FRAMES;
        float u1 = (float) (frame + 1) / RING_FRAMES;
        float[][] corners = {{-scale, -scale}, {-scale, scale}, {scale, scale}, {scale, -scale}};
        float[][] uvs = {{u1, 0.0F}, {u1, 1.0F}, {u0, 1.0F}, {u0, 0.0F}};
        Vector3f center = new Vector3f(0.5F, 0.5F, 0.5F);

        VertexConsumer consumer = buffer.getBuffer(TCRenderTypes.additiveTexturedNoCull(RING_TEX));
        for (int i = 0; i < 4; i++) {
            float px = center.x() + right.x() * corners[i][0] + up.x() * corners[i][1];
            float py = center.y() + right.y() * corners[i][0] + up.y() * corners[i][1];
            float pz = center.z() + right.z() * corners[i][0] + up.z() * corners[i][1];
            consumer.vertex(poseStack.last().pose(), px, py, pz)
                    .color(1.0F, 1.0F, 1.0F, 0.9F)
                    .uv(uvs[i][0], uvs[i][1])
                    .overlayCoords(0, 10)
                    .uv2(FULLBRIGHT)
                    .normal(poseStack.last().normal(), face.x(), face.y(), face.z())
                    .endVertex();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull TileNodeEnergized tile) {
        return true;
    }
}
