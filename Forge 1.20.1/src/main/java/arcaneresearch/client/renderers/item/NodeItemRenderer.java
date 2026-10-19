package arcaneresearch.client.renderers.item;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.client.models.block.JarNodeModel;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.lib.aura.NodeModifier;
import arcaneresearch.common.lib.aura.NodeStackData;
import arcaneresearch.common.lib.aura.NodeType;
@OnlyIn(Dist.CLIENT)
public class NodeItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation JAR_TEX =
            new ResourceLocation(Arcaneresearch.MODID, "textures/models/jar.png");
    private static final ResourceLocation NODETEX =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/nodes.png");
    private static final int FRAMES = 32;
    private static final int FULLBRIGHT = 0xF000F0;
    private final boolean jar;
    private volatile JarNodeModel model;
    public NodeItemRenderer(boolean jar) {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
        this.jar = jar;
    }
    private static RenderType nodeLayer(boolean additive) {
        return additive ? TCRenderTypes.additiveTexturedNoCull(NODETEX)
                : TCRenderTypes.translucentPortal(NODETEX);
    }
    private static void renderItemNode(PoseStack poseStack, MultiBufferSource buffer,
                                       AspectList aspects, NodeType type, NodeModifier mod, float size,
                                       Vector3f center) {
        if (aspects == null || aspects.size() == 0) {
            return;
        }
        long nano = System.nanoTime();
        int frame = (int) ((nano / 40000000L) % FRAMES);
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
        float count = 0.0F;
        float average = 0.0F;
        for (Aspect aspect : aspects.getAspects()) {
            if (aspect == null) {
                continue;
            }
            float a = 0.9F;
            if (aspect.getBlend() == 771) {
                a *= 1.5F;
            }
            average += aspects.getAmount(aspect);
            float scale = 0.2F + 0.5F * aspects.getAmount(aspect) / 50.0F;
            scale *= size;
            emitQuad(poseStack, buffer.getBuffer(nodeLayer(aspect.getBlend() != 771)),
                    center, right, up, face, scale,
                    a / Math.max(1.0F, aspects.size() / 2.0F),
                    0, frame, aspect.getColor());
            count++;
        }
        average /= Math.max(1, aspects.size());
        float scale = 0.1F + average / 150.0F;
        scale *= size;
        boolean additive = true;
        int strip = 1;
        if (type != null) {
            switch (type) {
                case NORMAL:
                    strip = 1;
                    break;
                case UNSTABLE:
                    strip = 6;
                    break;
                case DARK:
                    additive = false;
                    strip = 2;
                    break;
                case TAINTED:
                    additive = false;
                    strip = 5;
                    break;
                case PURE:
                    strip = 4;
                    break;
                case HUNGRY:
                    scale *= 0.75F;
                    strip = 3;
                    break;
                default:
                    break;
            }
        }
        emitQuad(poseStack, buffer.getBuffer(nodeLayer(additive)),
                center, right, up, face, scale, 0.9F, strip, frame, 0xFFFFFF);
    }
    private static void emitQuad(PoseStack poseStack, VertexConsumer builder, Vector3f center,
                                 Vector3f right, Vector3f up, Vector3f face, float scale, float alpha,
                                 int strip, int frame, int color) {
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float u0 = frame / (float) FRAMES;
        float u1 = (frame + 1) / (float) FRAMES;
        float v0 = strip / (float) FRAMES;
        float v1 = (strip + 1.0F) / (float) FRAMES;
        float[][] corners = {{-scale, -scale}, {-scale, scale}, {scale, scale}, {scale, -scale}};
        float[][] uvs = {{u1, v1}, {u1, v0}, {u0, v0}, {u0, v1}};
        for (int i = 0; i < 4; i++) {
            float px = center.x() + right.x() * corners[i][0] + up.x() * corners[i][1];
            float py = center.y() + right.y() * corners[i][0] + up.y() * corners[i][1];
            float pz = center.z() + right.z() * corners[i][0] + up.z() * corners[i][1];
            builder.vertex(poseStack.last().pose(), px, py, pz)
                    .color(r, g, b, alpha)
                    .uv(uvs[i][0], uvs[i][1])
                    .overlayCoords(0, 10)
                    .uv2(FULLBRIGHT)
                    .normal(poseStack.last().normal(), face.x(), face.y(), face.z())
                    .endVertex();
        }
    }
    private JarNodeModel model() {
        if (this.model == null) {
            this.model = new JarNodeModel(
                    Minecraft.getInstance().getEntityModels().bakeLayer(JarNodeModel.LAYER_LOCATION));
        }
        return this.model;
    }
    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transform, PoseStack poseStack,
                             MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AspectList aspects = NodeStackData.readAspects(stack);
        NodeType type = NodeStackData.readType(stack);
        NodeModifier mod = null;
        try {
            if (stack.hasTag()) {
                mod = NodeStackData.readModifier(stack.getTag());
            }
        } catch (Exception ignored) {
        }
        if (this.jar) {
            renderItemNode(poseStack, buffer, aspects, type, mod, 0.7F,
                    new Vector3f(0.5F, 0.48F, 0.5F));
            if (buffer instanceof net.minecraft.client.renderer.MultiBufferSource.BufferSource bs) {
                bs.endBatch();
            }
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.03D, 0.5D);
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            VertexConsumer lid = buffer.getBuffer(RenderType.entityCutout(JAR_TEX));
            model().renderLid(poseStack, lid, packedLight, packedOverlay);
            VertexConsumer glass = buffer.getBuffer(RenderType.entityTranslucent(JAR_TEX));
            model().renderCore(poseStack, glass, packedLight, packedOverlay, 0.55F);
            poseStack.popPose();
        } else {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.5D, 0.5D);
            if (aspects == null || aspects.size() == 0) {
                aspects = new AspectList().add(Aspect.AURA, 30);
                if (type == null) {
                    type = NodeType.NORMAL;
                }
            }
            renderItemNode(poseStack, buffer, aspects, type, mod, 0.45F,
                    new Vector3f(0.0F, 0.0F, 0.0F));
            poseStack.popPose();
        }
    }
}
