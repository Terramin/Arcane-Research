package arcaneresearch.client.renderers.entity.golem;

import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.parts.PartModel;
import arcaneresearch.common.golems.parts.GolemLegWheels;

public class PartModelWheel extends PartModel {

    public PartModelWheel(Identifier objModel, Identifier objTexture, EnumAttachPoint attachPoint) {
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