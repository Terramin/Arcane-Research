package arcaneresearch.client.renderers.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import arcaneresearch.client.renderers.TCRenderTypes;

import java.util.function.Consumer;





public class EldritchPortalSpecialRenderer implements SpecialModelRenderer<Void> {
    private static final Identifier PORTAL_TEX =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/eldritch_portal.png");
    private static final int FRAMES = 16;
    private static final int FULLBRIGHT = 0xF000F0;

    private final float ctxScale;

    public EldritchPortalSpecialRenderer(float ctxScale) {
        this.ctxScale = ctxScale;
    }

    @Override
    public @Nullable Void extractArgument(ItemStack stack) {
        return null;
    }

    @Override
    public void getExtents(Consumer<org.joml.Vector3fc> output) {
        output.accept(new Vector3f(0, 0, 0));
        output.accept(new Vector3f(1, 1, 1));
    }

    @Override
    public void submit(@Nullable Void arg, PoseStack poseStack, SubmitNodeCollector collector,
                       int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);

        float scale = ctxScale;

        long time = System.nanoTime() / 50_000_000L;
        int frame = (int) (time % FRAMES);
        float u0 = frame / (float) FRAMES;
        float u1 = u0 + 1.0F / FRAMES;
        Vector3f center = new Vector3f(0.0F, 0.0F, 0.0F);

        
        float half = 0.5F * scale;
        emitQuad(poseStack, collector, TCRenderTypes.translucentPortal(PORTAL_TEX),
                center, half, half,
                1.0F, 0.0F, 1.0F, 1.0F, u0, u1, overlayCoords);

        
        float core = 0.18F * scale;
        emitQuad(poseStack, collector, TCRenderTypes.additiveTexturedNoCull(PORTAL_TEX),
                center, core, core,
                1.0F, 0.6F, 1.0F, 0.65F, u0, u1, overlayCoords);

        poseStack.popPose();
    }

    private static void emitQuad(PoseStack poseStack, SubmitNodeCollector collector,
                                 net.minecraft.client.renderer.rendertype.RenderType layer,
                                 Vector3f center,
                                 float halfX, float halfY,
                                 float r, float g, float b, float a,
                                 float u0, float u1, int packedOverlay) {
        float[][] corners = {{-halfX, -halfY}, {-halfX, halfY}, {halfX, halfY}, {halfX, -halfY}};
        float[][] uvs = {{u1, 1.0F}, {u1, 0.0F}, {u0, 0.0F}, {u0, 1.0F}};
        collector.submitCustomGeometry(poseStack, layer, (pose, consumer) -> {
            
            
            
            Vector3f right = new Vector3f(1.0F, 0.0F, 0.0F);
            Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
            Vector3f face = new Vector3f(0.0F, 0.0F, 1.0F);
            try {
                org.joml.Matrix4f inv = new org.joml.Matrix4f(pose.pose());
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
            for (int i = 0; i < 4; i++) {
                float px = center.x() + right.x() * corners[i][0] + up.x() * corners[i][1];
                float py = center.y() + right.y() * corners[i][0] + up.y() * corners[i][1];
                float pz = center.z() + right.z() * corners[i][0] + up.z() * corners[i][1];
                consumer.addVertex(pose, px, py, pz)
                        .setColor(r, g, b, a)
                        .setUv(uvs[i][0], uvs[i][1])
                        .setOverlay(packedOverlay)
                        .setLight(FULLBRIGHT)
                        .setNormal(pose, face.x(), face.y(), face.z());
            }
        });
    }

    public record Unbaked(String preset) implements SpecialModelRenderer.Unbaked<Void> {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        com.mojang.serialization.Codec.STRING.optionalFieldOf("preset", "gui").forGetter(Unbaked::preset)
                ).apply(instance, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<Void> bake(SpecialModelRenderer.BakingContext context) {
            
            float scale = switch (preset) {
                case "ground" -> 0.7F;
                case "gui", "fixed" -> 0.9F;
                default -> 1.0F;
            };
            return new EldritchPortalSpecialRenderer(scale);
        }
    }
}