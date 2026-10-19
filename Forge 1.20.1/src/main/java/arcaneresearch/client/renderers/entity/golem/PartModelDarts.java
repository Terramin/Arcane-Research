package arcaneresearch.client.renderers.entity.golem;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.golems.IGolemAPI;
import arcaneresearch.api.golems.parts.PartModel;
@OnlyIn(Dist.CLIENT)
public class PartModelDarts extends PartModel {
    public PartModelDarts(ResourceLocation objModel, ResourceLocation objTexture, EnumAttachPoint attachPoint) {
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
