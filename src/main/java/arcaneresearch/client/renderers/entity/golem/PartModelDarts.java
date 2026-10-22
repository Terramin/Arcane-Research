package arcaneresearch.client.renderers.entity.golem;

import net.minecraft.resources.Identifier; 
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.parts.PartModel;

public class PartModelDarts extends PartModel {

    public PartModelDarts(Identifier objModel, Identifier objTexture, EnumAttachPoint attachPoint) {
        super(objModel, objTexture, attachPoint);
    }

    @Override
    public float preRenderArmRotationX(IGolemAPI golem, float partialTicks, EnumLimbSide side, float inputRot) {
        if (golem.isInCombat()) {
            inputRot = 90.0f - golem.getGolemEntity().xRotO + inputRot / 10.0f;
        }
        return inputRot;
    }

    @Override
    public float preRenderArmRotationY(IGolemAPI golem, float partialTicks, EnumLimbSide side, float inputRot) {
        if (golem.isInCombat()) {
            inputRot /= 10.0f;
        }
        return inputRot;
    }

    @Override
    public float preRenderArmRotationZ(IGolemAPI golem, float partialTicks, EnumLimbSide side, float inputRot) {
        if (golem.isInCombat()) {
            inputRot /= 10.0f;
        }
        return inputRot;
    }
}