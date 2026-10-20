package arcaneresearch.client.models.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.entities.monster.EntityPech;
@OnlyIn(Dist.CLIENT)
public class PechModel extends EntityModel<EntityPech> implements ArmedModel {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(Arcaneresearch.MODID, "pech"), "main");
    private final ModelPart head;
    private final ModelPart jowls;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart lowerPack;
    private final ModelPart upperPack;
    public PechModel(ModelPart root) {
        this.head = root.getChild("head");
        this.jowls = root.getChild("jowls");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.lowerPack = root.getChild("lower_pack");
        this.upperPack = root.getChild("upper_pack");
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(2, 11)
                        .addBox(-3.5F, -5.0F, -5.0F, 7.0F, 5.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        partdefinition.addOrReplaceChild("jowls",
                CubeListBuilder.create()
                        .texOffs(1, 21)
                        .addBox(-4.0F, -1.0F, -6.0F, 8.0F, 3.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(34, 12)
                        .addBox(-3.0F, 0.0F, 0.0F, 6.0F, 10.0F, 6.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 9.0F, -3.0F, 0.313F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(52, 2)
                        .addBox(-2.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offset(-3.0F, 10.0F, -1.0F));
        partdefinition.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(52, 2).mirror()
                        .addBox(0.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offset(3.0F, 10.0F, -1.0F));
        partdefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(35, 1)
                        .addBox(-2.9F, 0.0F, 0.0F, 3.0F, 6.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 18.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(35, 1).mirror()
                        .addBox(-0.1F, 0.0F, 0.0F, 3.0F, 6.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 18.0F, 0.0F));
        partdefinition.addOrReplaceChild("lower_pack",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-5.0F, 0.0F, 0.0F, 10.0F, 5.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 10.0F, 3.5F, 0.301F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("upper_pack",
                CubeListBuilder.create()
                        .texOffs(64, 1)
                        .addBox(-7.5F, -14.0F, 0.0F, 15.0F, 14.0F, 11.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 10.0F, 3.0F, 0.454F, 0.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 128, 64);
    }
    @Override
    public void setupAnim(EntityPech entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        float mumble = entity.getMumble();
        this.jowls.yRot = this.head.yRot;
        this.jowls.xRot = this.head.xRot + 0.2618F + Mth.cos(limbSwing * 0.6662F) * limbSwingAmount * 0.25F
                + 0.349F * Math.abs(Mth.sin(mumble / 8.0F));
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = 0.0F;
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.rightLeg.yRot = 0.0F;
        this.leftLeg.yRot = 0.0F;
        this.lowerPack.yRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.125F;
        this.lowerPack.zRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.125F;
        if (riding) {
            this.rightArm.xRot -= 0.6283F;
            this.leftArm.xRot -= 0.6283F;
            this.rightLeg.xRot = -1.2566F;
            this.leftLeg.xRot = -1.2566F;
            this.rightLeg.yRot = 0.3142F;
            this.leftLeg.yRot = -0.3142F;
        }
        this.rightArm.yRot = 0.0F;
        this.leftArm.yRot = 0.0F;
        if (attackTime > 0) {
            float swingProgress = attackTime;
            this.rightArm.yRot += body.yRot;
            this.leftArm.yRot += body.yRot;
            this.leftArm.xRot += body.yRot;
            float f6 = 1.0F - attackTime;
            f6 = f6 * f6;
            f6 = f6 * f6;
            f6 = 1.0F - f6;
            float f7 = Mth.sin(f6 * (float) Math.PI);
            float f8 = Mth.sin(attackTime * (float) Math.PI) * -(head.xRot - 0.7F) * 0.75F;
            this.rightArm.xRot -= f7 * 1.2F + f8;
            this.rightArm.yRot += body.yRot * 2.0F;
            this.rightArm.zRot = Mth.sin(attackTime * (float) Math.PI) * -0.4F;
        }
        if (entity.isShiftKeyDown()) {
            this.rightArm.xRot += 0.4F;
            this.leftArm.xRot += 0.4F;
        }
        this.rightArm.zRot += Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.leftArm.zRot -= Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.rightArm.xRot += Mth.sin(ageInTicks * 0.067F) * 0.05F;
        this.leftArm.xRot -= Mth.sin(ageInTicks * 0.067F) * 0.05F;
    }
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        jowls.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        lowerPack.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        upperPack.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        ModelPart modelpart = arm == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
        modelpart.translateAndRotate(poseStack);
    }
}
