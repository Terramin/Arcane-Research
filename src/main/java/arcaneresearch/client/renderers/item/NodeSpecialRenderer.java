package arcaneresearch.client.renderers.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.client.models.block.JarNodeModel;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.common.lib.aura.NodeModifier;
import arcaneresearch.common.lib.aura.NodeStackData;
import arcaneresearch.common.lib.aura.NodeType;

import java.util.function.Consumer;



public class NodeSpecialRenderer implements SpecialModelRenderer<NodeSpecialRenderer.NodeData> {

    private static final Identifier JAR_TEX =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/jar.png");
    private static final Identifier NODETEX =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/nodes.png");
    private static final int FRAMES = 32;
    private static final int FULLBRIGHT = 0xF000F0;

    public record NodeData(AspectList aspects, NodeType type, NodeModifier mod) {
    }

    private final boolean jar;
    private final JarNodeModel model;

    public NodeSpecialRenderer(boolean jar, JarNodeModel model) {
        this.jar = jar;
        this.model = model;
    }

    @Override
    public @Nullable NodeData extractArgument(ItemStack stack) {
        AspectList aspects = NodeStackData.readAspects(stack);
        NodeType type = NodeStackData.readType(stack);
        NodeModifier mod = null;
        try {
            net.minecraft.nbt.CompoundTag tag =
                    arcaneresearch.api.ArcaneresearchInvHelper.getCustomTag(stack);
            if (tag != null) {
                mod = NodeStackData.readModifier(tag);
            }
        } catch (Exception ignored) {
        }
        if (!jar && (aspects == null || aspects.size() == 0)) {
            aspects = new AspectList().add(Aspect.AURA, 30);
            if (type == null) {
                type = NodeType.NORMAL;
            }
        }
        return new NodeData(aspects, type, mod);
    }

    @Override
    public void getExtents(Consumer<org.joml.Vector3fc> output) {
        output.accept(new Vector3f(0, 0, 0));
        output.accept(new Vector3f(1, 1, 1));
    }

    @Override
    public void submit(@Nullable NodeData arg, PoseStack poseStack, SubmitNodeCollector collector,
                       int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (arg == null) {
            return;
        }
        if (this.jar) {
            submitNode(poseStack, collector, arg.aspects(), arg.type(), arg.mod(), 0.7F,
                    new Vector3f(0.5F, 0.48F, 0.5F));

            poseStack.pushPose();
            poseStack.translate(0.5D, 0.03D, 0.5D);
            poseStack.rotate(Axis.XP.rotationDegrees(180.0F));
            net.minecraft.client.renderer.rendertype.RenderType lidType = TCRenderTypes.objSolid(JAR_TEX);
            collector.submitCustomGeometry(poseStack, lidType, (pose, consumer) -> {
                PoseStack work = bridge(pose);
                model.renderLid(work, consumer, lightCoords, OverlayTexture.NO_OVERLAY);
            });
            net.minecraft.client.renderer.rendertype.RenderType glassType =
                    TCRenderTypes.translucentPortal(JAR_TEX);
            collector.submitCustomGeometry(poseStack, glassType, (pose, consumer) -> {
                PoseStack work = bridge(pose);
                model.renderCore(work, consumer, lightCoords, OverlayTexture.NO_OVERLAY, 0.55F);
            });
            poseStack.popPose();
        } else {
            poseStack.pushPose();
            poseStack.translate(0.5D, 0.5D, 0.5D);
            submitNode(poseStack, collector, arg.aspects(), arg.type(), arg.mod(), 0.45F,
                    new Vector3f(0.0F, 0.0F, 0.0F));
            poseStack.popPose();
        }
    }

    private static PoseStack bridge(PoseStack.Pose pose) {
        PoseStack work = new PoseStack();
        work.last().pose().set(pose.pose());
        work.last().normal().set(pose.normal());
        return work;
    }

    private static net.minecraft.client.renderer.rendertype.RenderType nodeLayer(boolean additive) {
        return additive ? TCRenderTypes.additiveTexturedNoCull(NODETEX)
                : TCRenderTypes.translucentPortal(NODETEX);
    }

    private static void submitNode(PoseStack poseStack, SubmitNodeCollector collector,
                                   AspectList aspects, NodeType type, NodeModifier mod, float size,
                                   Vector3f center) {
        if (aspects == null || aspects.size() == 0) {
            return;
        }
        long nano = System.nanoTime();
        int frame = (int) ((nano / 40000000L) % FRAMES);

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
            emitQuad(poseStack, collector, nodeLayer(aspect.getBlend() != 771),
                    center, scale,
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
        emitQuad(poseStack, collector, nodeLayer(additive),
                center, scale, 0.9F, strip, frame, 0xFFFFFF);
    }

    private static void emitQuad(PoseStack poseStack, SubmitNodeCollector collector,
                                 net.minecraft.client.renderer.rendertype.RenderType layer,
                                 Vector3f center,
                                 float scale, float alpha,
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
            
            
            
            
            
            
            
            
            
            Vector3f right = new Vector3f(1.0F, 0.0F, 0.0F);
            Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
            Vector3f face = new Vector3f(0.0F, 0.0F, 1.0F);
            try {
                org.joml.Matrix4f eff = new org.joml.Matrix4f(
                        com.mojang.blaze3d.systems.RenderSystem.getModelViewStack());
                eff.mul(poseStack.last().pose());
                eff.invert();
                eff.transformDirection(right);
                eff.transformDirection(up);
                eff.transformDirection(face);
                right.normalize();
                up.normalize();
                face.normalize();
            } catch (Exception ignored) {
                right.set(1.0F, 0.0F, 0.0F);
                up.set(0.0F, 1.0F, 0.0F);
                face.set(0.0F, 0.0F, 1.0F);
            }
            final Vector3f fRight = right;
            final Vector3f fUp = up;
            final Vector3f fFace = face;
            collector.submitCustomGeometry(poseStack, layer, (pose, consumer) -> {
                for (int i = 0; i < 4; i++) {
                    float px = center.x() + fRight.x() * corners[i][0] + fUp.x() * corners[i][1];
                    float py = center.y() + fRight.y() * corners[i][0] + fUp.y() * corners[i][1];
                    float pz = center.z() + fRight.z() * corners[i][0] + fUp.z() * corners[i][1];
                    consumer.addVertex(pose, px, py, pz)
                            .setColor(r, g, b, alpha)
                            .setUv(uvs[i][0], uvs[i][1])
                            .setOverlay(OverlayTexture.NO_OVERLAY)
                            .setLight(FULLBRIGHT)
                            .setNormal(pose, fFace.x(), fFace.y(), fFace.z());
                }
            });
    }

    public record Unbaked(boolean jar) implements SpecialModelRenderer.Unbaked<NodeData> {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        com.mojang.serialization.Codec.BOOL.optionalFieldOf("jar", false).forGetter(Unbaked::jar)
                ).apply(instance, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<NodeData> bake(SpecialModelRenderer.BakingContext context) {
            JarNodeModel jarModel = new JarNodeModel(
                    context.entityModelSet().bakeLayer(JarNodeModel.LAYER_LOCATION));
            return new NodeSpecialRenderer(jar, jarModel);
        }
    }
}