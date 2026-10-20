package arcaneresearch.client.renderers.entity.golem;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.client.renderers.entity.GolemRenderer;

// NEOFORGE_PORT (26.3): thread-local handoff to PartModel hooks becomes an
// explicit per-submit handoff (state + working pose + collector). Set/cleared
// synchronously around each emission batch; safe on the render thread.
public final class GolemRenderContext {

    public static GolemRenderer.State state;
    public static PoseStack poseStack;
    public static SubmitNodeCollector collector;
    public static int packedLight;
    public static int packedOverlay;

    private GolemRenderContext() {
    }

    public static void set(GolemRenderer.State current, PoseStack pose,
                           SubmitNodeCollector coll, int light, int overlay) {
        state = current;
        poseStack = pose;
        collector = coll;
        packedLight = light;
        packedOverlay = overlay;
    }

    public static void clear() {
        state = null;
        poseStack = null;
        collector = null;
        packedLight = 0;
        packedOverlay = 0;
    }
}