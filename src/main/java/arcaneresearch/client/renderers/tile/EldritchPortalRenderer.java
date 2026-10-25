package arcaneresearch.client.renderers.tile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import arcaneresearch.common.tiles.aura.EldritchPortalTile;
@OnlyIn(Dist.CLIENT)
public class EldritchPortalRenderer implements BlockEntityRenderer<EldritchPortalTile> {
    private static final ResourceLocation PORTAL_TEX =
            new ResourceLocation("arcaneresearch", "textures/misc/eldritch_portal.png");
    public EldritchPortalRenderer(BlockEntityRendererProvider.Context context) {
    }
    @Override
    public void render(EldritchPortalTile tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        int c = Math.min(30, tile.opencount + (int) partialTicks);
        int e = Math.min(5, tile.opencount + (int) partialTicks);
        float scale = e / 5.0F;
        float scaley = c / 30.0F;
        long time = System.nanoTime() / 50_000_000L;
        int frame = (int) (time % 16);
        float u0 = frame / 16.0F;
        float u1 = u0 + 0.0625F;
        Quaternionf rotation = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        Vector3f[] corners = {
                new Vector3f(-0.5F * scale, -0.5F * scaley, 0.0F),
                new Vector3f(0.5F * scale, -0.5F * scaley, 0.0F),
                new Vector3f(0.5F * scale, 0.5F * scaley, 0.0F),
                new Vector3f(-0.5F * scale, 0.5F * scaley, 0.0F)};
        float[] us = {u0, u1, u1, u0};
        com.mojang.blaze3d.vertex.VertexConsumer builder =
                buffer.getBuffer(RenderType.entityTranslucent(PORTAL_TEX));
        for (int i = 0; i < 4; i++) {
            Vector3f corner = corners[i];
            corner.rotate(rotation);
            builder.vertex(poseStack.last().pose(), corner.x(), corner.y(), corner.z())
                    .color(1.0F, 0.0F, 1.0F, 1.0F)
                    .uv(us[i], i < 2 ? 1.0F : 0.0F)
                    .overlayCoords(packedOverlay)
                    .uv2(0xF000F0)
                    .normal(poseStack.last().normal(), 0.0F, 1.0F, 0.0F)
                    .endVertex();
        }
        poseStack.popPose();
    }
    @Override
    public boolean shouldRenderOffScreen(EldritchPortalTile tile) {
        return true;
    }
}
