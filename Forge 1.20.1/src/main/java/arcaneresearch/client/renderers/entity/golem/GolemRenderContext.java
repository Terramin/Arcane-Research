package arcaneresearch.client.renderers.entity.golem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
@OnlyIn(Dist.CLIENT)
public final class GolemRenderContext {
    public static PoseStack poseStack;
    public static MultiBufferSource buffer;
    public static int packedLight;
    public static int packedOverlay;
    private GolemRenderContext() {
    }
    public static void set(PoseStack pose, MultiBufferSource buf, int light, int overlay) {
        poseStack = pose;
        buffer = buf;
        packedLight = light;
        packedOverlay = overlay;
    }
    public static void clear() {
        poseStack = null;
        buffer = null;
        packedLight = 0;
        packedOverlay = 0;
    }
}
