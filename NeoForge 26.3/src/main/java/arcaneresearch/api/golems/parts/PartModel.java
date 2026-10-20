package arcaneresearch.api.golems.parts;

import net.minecraft.resources.Identifier;
import arcaneresearch.api.golems.IGolemAPI;

public class PartModel {

    private Identifier objModel;
    private Identifier texture;
    private EnumAttachPoint attachPoint;

    public PartModel(Identifier objModel, Identifier objTexture, EnumAttachPoint attachPoint) {
        this.objModel = objModel;
        this.texture = objTexture;
        this.attachPoint = attachPoint;
    }

    public Identifier getObjModel() {
        return objModel;
    }

    public Identifier getTexture() {
        return texture;
    }

    public EnumAttachPoint getAttachPoint() {
        return attachPoint;
    }

    public boolean useMaterialTextureForObjectPart(String partName) {
        return partName.startsWith("bm");
    }

    public void preRenderObjectPart(String partName, IGolemAPI golem, float partialTicks, EnumLimbSide side) {
    }

    public void postRenderObjectPart(String partName, IGolemAPI golem, float partialTicks, EnumLimbSide side) {
    }

    public float preRenderArmRotationX(IGolemAPI golem, float partialTicks, EnumLimbSide side, float inputRot) {
        return inputRot;
    }

    public float preRenderArmRotationY(IGolemAPI golem, float partialTicks, EnumLimbSide side, float inputRot) {
        return inputRot;
    }

    public float preRenderArmRotationZ(IGolemAPI golem, float partialTicks, EnumLimbSide side, float inputRot) {
        return inputRot;
    }

    public enum EnumAttachPoint {
        ARMS,
        LEGS,
        BODY,
        HEAD
    }

    public enum EnumLimbSide {
        LEFT,
        RIGHT,
        MIDDLE
    }
}
