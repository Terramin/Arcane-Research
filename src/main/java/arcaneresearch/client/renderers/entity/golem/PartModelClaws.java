package arcaneresearch.client.renderers.entity.golem;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.parts.PartModel;
@OnlyIn(Dist.CLIENT)
public class PartModelClaws extends PartModel {
    public PartModelClaws(ResourceLocation objModel, ResourceLocation objTexture, EnumAttachPoint attachPoint) {
        super(objModel, objTexture, attachPoint);
    }
    @Override
    public void preRenderObjectPart(String partName, IGolemAPI golem, float partialTicks, EnumLimbSide side) {
        if (!partName.startsWith("claw") || GolemRenderContext.poseStack == null) {
            return;
        }
        float f = golem.getGolemEntity().getAttackAnim(partialTicks) * 4.1f;
        f *= f;
        GolemRenderContext.poseStack.translate(0.0, -0.2, 0.0);
        GolemRenderHooks.rotateDeg(GolemRenderContext.poseStack, f,
                partName.endsWith("1") ? 1.0f : -1.0f, 0.0f, 0.0f);
    }
}
