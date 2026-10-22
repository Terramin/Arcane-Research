package arcaneresearch.client.renderers.entity.golem;

import net.minecraft.resources.Identifier; 
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.parts.PartModel;

public class PartModelClaws extends PartModel {

    public PartModelClaws(Identifier objModel, Identifier objTexture, EnumAttachPoint attachPoint) {
        super(objModel, objTexture, attachPoint);
    }

    @Override
    public void preRenderObjectPart(String partName, IGolemAPI golem, float partialTicks, EnumLimbSide side) {
        if (!partName.startsWith("claw") || GolemRenderContext.poseStack == null) {
            return;
        }
        
        float swing = golem.getGolemEntity() instanceof arcaneresearch.common.golems.EntityArcaneresearchGolem golemEntity
                ? golemEntity.getArmSwingPhase(partialTicks) : 0.0F;
        float f = swing * 4.1f;
        f *= f;
        GolemRenderContext.poseStack.translate(0.0, -0.2, 0.0);
        GolemRenderHooks.rotateDeg(GolemRenderContext.poseStack, f,
                partName.endsWith("1") ? 1.0f : -1.0f, 0.0f, 0.0f);
    }
}