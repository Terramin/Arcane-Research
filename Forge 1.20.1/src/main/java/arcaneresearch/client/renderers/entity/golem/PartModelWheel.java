package arcaneresearch.client.renderers.entity.golem;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.parts.PartModel;
import arcaneresearch.common.golems.parts.GolemLegWheels;
@OnlyIn(Dist.CLIENT)
public class PartModelWheel extends PartModel {
    public PartModelWheel(ResourceLocation objModel, ResourceLocation objTexture, EnumAttachPoint attachPoint) {
        super(objModel, objTexture, attachPoint);
    }
    @Override
    public void preRenderObjectPart(String partName, IGolemAPI golem, float partialTicks, EnumLimbSide side) {
        if (!partName.equals("wheel") || GolemRenderContext.poseStack == null) {
            return;
        }
        float lastRot = 0.0f;
        Integer id = golem.getGolemEntity().getId();
        if (GolemLegWheels.ani.containsKey(id)) {
            lastRot = GolemLegWheels.ani.get(id);
        }
        GolemRenderContext.poseStack.translate(0.0, -0.375, 0.0);
        GolemRenderHooks.rotateDeg(GolemRenderContext.poseStack, lastRot, -1.0f, 0.0f, 0.0f);
    }
}
