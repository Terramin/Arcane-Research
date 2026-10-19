package arcaneresearch.client.renderers.item;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import arcaneresearch.Arcaneresearch;
@OnlyIn(Dist.CLIENT)
public class EldritchPortalItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation PORTAL_TEX =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/eldritch_portal.png");
    private static final int FRAMES = 16;
    private static final int FULLBRIGHT = 0xF000F0;
    public EldritchPortalItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }
    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transform, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        float ctxScale = 1.0F;
        if (transform == ItemDisplayContext.GUI || transform == ItemDisplayContext.FIXED) {
            ctxScale = 0.9F;
        } else if (transform == ItemDisplayContext.GROUND) {
            ctxScale = 0.7F;
        }
        long time = System.nanoTime() / 50_000_000L;
        int frame = (int) (time % FRAMES);
        float u0 = frame / (float) FRAMES;
        float u1 = u0 + 1.0F / FRAMES;
        Vector3f right = new Vector3f(1.0F, 0.0F, 0.0F);
        Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
        Vector3f face = new Vector3f(0.0F, 0.0F, 1.0F);
        try {
            org.joml.Matrix4f inv = new org.joml.Matrix4f(poseStack.last().pose());
            inv.invert();
            inv.transformDirection(right);
            inv.transformDirection(up);
            inv.transformDirection(face);
            right.normalize();
            up.normalize();
            face.normalize();
        } catch (Exception ignored) {
            right.set(1.0F, 0.0F, 0.0F);
            up.set(0.0F, 1.0F, 0.0F);
            face.set(0.0F, 0.0F, 1.0F);
        }
        Vector3f center = new Vector3f(0.0F, 0.0F, 0.0F);
        float scale = 0.5F * ctxScale;
        emitQuad(poseStack, buffer.getBuffer(RenderType.entityTranslucent(PORTAL_TEX)),
                center, right, up, face, scale, scale,
                1.0F, 0.0F, 1.0F, 1.0F, u0, u1, packedOverlay);
        float core = 0.18F * ctxScale;
        emitQuad(poseStack,
                buffer.getBuffer(
                        arcaneresearch.client.renderers.TCRenderTypes.additiveTexturedNoCull(PORTAL_TEX)),
                center, right, up, face, core, core,
                1.0F, 0.6F, 1.0F, 0.65F, u0, u1, packedOverlay);
        poseStack.popPose();
    }
    private static void emitQuad(PoseStack poseStack, VertexConsumer builder, Vector3f center,
                                 Vector3f right, Vector3f up, Vector3f face,
                                 float halfX, float halfY,
                                 float r, float g, float b, float a,
                                 float u0, float u1, int packedOverlay) {
        float[][] corners = {{-halfX, -halfY}, {-halfX, halfY}, {halfX, halfY}, {halfX, -halfY}};
        float[][] uvs = {{u1, 1.0F}, {u1, 0.0F}, {u0, 0.0F}, {u0, 1.0F}};
        for (int i = 0; i < 4; i++) {
            float px = center.x() + right.x() * corners[i][0] + up.x() * corners[i][1];
            float py = center.y() + right.y() * corners[i][0] + up.y() * corners[i][1];
            float pz = center.z() + right.z() * corners[i][0] + up.z() * corners[i][1];
            builder.vertex(poseStack.last().pose(), px, py, pz)
                    .color(r, g, b, a)
                    .uv(uvs[i][0], uvs[i][1])
                    .overlayCoords(packedOverlay)
                    .uv2(FULLBRIGHT)
                    .normal(poseStack.last().normal(), face.x(), face.y(), face.z())
                    .endVertex();
        }
    }
}
