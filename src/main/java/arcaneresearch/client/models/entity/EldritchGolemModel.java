package arcaneresearch.client.models.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.Identifier; 
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.entity.EldritchGolemRenderer;


public class EldritchGolemModel extends EntityModel<EldritchGolemRenderer.State> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "eldritch_golem"), "main");

    private final ModelPart head;
    private final ModelPart headStump;
    private final ModelPart torso;
    private final ModelPart collar;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart waistR;
    private final ModelPart waistL;
    private final ModelPart cloak1;
    private final ModelPart cloak2;
    private final ModelPart cloak3;
    private final ModelPart frontcloth0;
    private final ModelPart frontcloth1;
    private final ModelPart frontcloth2;

    public EldritchGolemModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.headStump = root.getChild("head_stump");
        this.torso = root.getChild("torso");
        this.collar = root.getChild("collar");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.waistR = root.getChild("waist_r");
        this.waistL = root.getChild("waist_l");
        this.cloak1 = root.getChild("cloak1");
        this.cloak2 = root.getChild("cloak2");
        this.cloak3 = root.getChild("cloak3");
        this.frontcloth0 = root.getChild("frontcloth0");
        this.frontcloth1 = root.getChild("frontcloth1");
        this.frontcloth2 = root.getChild("frontcloth2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(47, 12)
                        .addBox(-3.5F, -6.0F, -2.5F, 7.0F, 7.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 4.5F, -3.8F, -0.1047F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("head_stump",
                CubeListBuilder.create()
                        .texOffs(26, 16)
                        .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, -5.0F, -0.1047F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("torso",
                CubeListBuilder.create()
                        .texOffs(34, 45).mirror(true)
                        .addBox(-5.0F, 2.5F, -3.0F, 10.0F, 10.0F, 6.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.1745F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("collar",
                CubeListBuilder.create()
                        .texOffs(75, 50).mirror(false)
                        .addBox(3.5F, -0.5F, -7.0F, 1.0F, 4.0F, 10.0F, CubeDeformation.NONE)
                        .texOffs(67, 50)
                        .addBox(-4.5F, -0.5F, -7.0F, 1.0F, 4.0F, 10.0F, CubeDeformation.NONE)
                        .texOffs(77, 59)
                        .addBox(-3.5F, -0.5F, 2.0F, 7.0F, 4.0F, 1.0F, CubeDeformation.NONE)
                        .texOffs(77, 59)
                        .addBox(-3.5F, -0.5F, -7.0F, 7.0F, 4.0F, 1.0F, CubeDeformation.NONE)
                        .texOffs(22, 0)
                        .addBox(-3.5F, 0.0F, -6.0F, 7.0F, 1.0F, 8.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.8378F, 0.0F, 0.0F));

        PartDefinition rightArm = partdefinition.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(78, 32)
                        .addBox(-3.5F, 1.5F, -2.0F, 4.0F, 13.0F, 5.0F, CubeDeformation.NONE)
                        .texOffs(56, 31)
                        .addBox(-4.5F, -1.5F, -2.5F, 5.0F, 6.0F, 6.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-5.0F, 3.0F, -2.0F, 0.0F, 0.0F, 0.1047F));
        rightArm.addOrReplaceChild("right_pauldron",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.3F, -1.0F, -3.0F, 4.0F, 5.0F, 7.0F, CubeDeformation.NONE)
                        .texOffs(0, 23)
                        .addBox(-3.3F, 4.0F, -2.5F, 1.0F, 2.0F, 6.0F, CubeDeformation.NONE)
                        .texOffs(0, 12)
                        .addBox(-2.3F, 4.0F, -3.0F, 2.0F, 3.0F, 7.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.1868F));

        PartDefinition leftArm = partdefinition.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(78, 32).mirror()
                        .addBox(-0.5F, 1.5F, -2.0F, 4.0F, 13.0F, 5.0F, CubeDeformation.NONE)
                        .texOffs(56, 31)
                        .addBox(-0.5F, -1.5F, -2.5F, 5.0F, 6.0F, 6.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(5.0F, 3.0F, -2.0F, 0.0F, 0.0F, -0.1047F));
        leftArm.addOrReplaceChild("left_pauldron",
                CubeListBuilder.create()
                        .texOffs(0, 0).mirror()
                        .addBox(0.3F, -1.0F, -3.0F, 4.0F, 5.0F, 7.0F, CubeDeformation.NONE)
                        .texOffs(0, 23)
                        .addBox(2.3F, 4.0F, -2.5F, 1.0F, 2.0F, 6.0F, CubeDeformation.NONE)
                        .texOffs(0, 12).mirror()
                        .addBox(0.3F, 4.0F, -3.0F, 2.0F, 3.0F, 7.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.1868F));

        partdefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(79, 19)
                        .addBox(-2.5F, 2.5F, -2.0F, 4.0F, 9.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(-2.0F, 12.5F, 0.0F));
        partdefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(79, 19).mirror()
                        .addBox(-1.5F, 2.5F, -2.0F, 4.0F, 9.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(2.0F, 12.5F, 0.0F));

        partdefinition.addOrReplaceChild("waist_r",
                CubeListBuilder.create()
                        .texOffs(96, 14)
                        .addBox(-3.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F, CubeDeformation.NONE)
                        .texOffs(116, 13)
                        .addBox(-3.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F, CubeDeformation.NONE)
                        .texOffs(114, 5).mirror()
                        .addBox(-2.0F, 2.5F, -2.5F, 2.0F, 3.0F, 5.0F, CubeDeformation.NONE)
                        .texOffs(96, 7).mirror(false)
                        .addBox(0.0F, 2.5F, -2.5F, 2.0F, 2.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1396F));

        partdefinition.addOrReplaceChild("waist_l",
                CubeListBuilder.create()
                        .texOffs(96, 14).mirror()
                        .addBox(-2.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F, CubeDeformation.NONE)
                        .texOffs(116, 13)
                        .addBox(2.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F, CubeDeformation.NONE)
                        .texOffs(114, 5)
                        .addBox(0.0F, 2.5F, -2.5F, 2.0F, 3.0F, 5.0F, CubeDeformation.NONE)
                        .texOffs(96, 7)
                        .addBox(-2.0F, 2.5F, -2.5F, 2.0F, 2.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(2.0F, 12.0F, 0.0F, 0.0F, 0.0F, -0.1396F));

        partdefinition.addOrReplaceChild("cloak1",
                CubeListBuilder.create()
                        .texOffs(0, 47)
                        .addBox(-5.0F, 1.5F, 4.0F, 10.0F, 12.0F, 1.0F, CubeDeformation.NONE)
                        .texOffs(0, 43)
                        .addBox(3.0F, 0.5F, 2.0F, 2.0F, 1.0F, 3.0F, CubeDeformation.NONE)
                        .texOffs(0, 43)
                        .addBox(-5.0F, 0.5F, 2.0F, 2.0F, 1.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.1396F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("cloak2",
                CubeListBuilder.create()
                        .texOffs(0, 59)
                        .addBox(-5.0F, 13.5F, 1.7F, 10.0F, 4.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.3069F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("cloak3",
                CubeListBuilder.create()
                        .texOffs(0, 37)
                        .addBox(-5.0F, 17.5F, -0.8F, 10.0F, 4.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.4466F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("frontcloth0",
                CubeListBuilder.create()
                        .texOffs(114, 52)
                        .addBox(-3.0F, 3.2F, -3.5F, 6.0F, 10.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.1745F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("frontcloth1",
                CubeListBuilder.create()
                        .texOffs(114, 39)
                        .addBox(-1.0F, 1.5F, -3.5F, 6.0F, 6.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-2.0F, 12.0F, 0.0F, -0.1047F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("frontcloth2",
                CubeListBuilder.create()
                        .texOffs(114, 47)
                        .addBox(-1.0F, 8.5F, -1.5F, 6.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-2.0F, 11.0F, 0.0F, -0.3316F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public void setupAnim(EldritchGolemRenderer.State state) {
        super.setupAnim(state);
        float limbSwing = state.walkAnimationPos;
        float limbSwingAmount = state.walkAnimationSpeed;
        float ageInTicks = state.ageInTicks;
        float netHeadYaw = state.yRot;
        float headPitch = state.xRot;
        if (state.spawnTimer > 0) {
            this.head.xRot = state.spawnTimer / 2.0F / 57.3F;
            this.head.yRot = 0.0F;
        } else {
            this.head.yRot = netHeadYaw / 4.0F * ((float) Math.PI / 180F);

            this.head.xRot = headPitch / 2.0F * ((float) Math.PI / 180F);
            this.headStump.yRot = netHeadYaw * ((float) Math.PI / 180F);
            this.headStump.xRot = headPitch * ((float) Math.PI / 180F);
        }

        this.head.visible = !state.headless;
        this.headStump.visible = state.headless;

        this.rightLeg.xRot = Mth.cos(limbSwing * 0.4662F) * 1.4F * limbSwingAmount;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.4662F + (float) Math.PI) * 1.4F * limbSwingAmount;

        int attackTimer = state.attackTimer;
        if (attackTimer > 0) {
            float attackProgress = doAbs(attackTimer - (ageInTicks - (int) ageInTicks), 10.0F);
            this.rightArm.xRot = -2.0F + 1.5F * attackProgress;
            this.leftArm.xRot = -2.0F + 1.5F * attackProgress;
        } else {
            this.rightArm.xRot = Mth.cos(limbSwing * 0.4F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F;
            this.leftArm.xRot = Mth.cos(limbSwing * 0.4F) * 2.0F * limbSwingAmount * 0.5F;
        }

        float a = Mth.cos(limbSwing * 0.44F) * 1.4F * limbSwingAmount;
        float b = Mth.cos(limbSwing * 0.44F + (float) Math.PI) * 1.4F * limbSwingAmount;
        float c = Math.min(a, b);
        this.cloak1.xRot = -c / 3.0F + 0.1396F;
        this.cloak2.xRot = -c / 3.0F + 0.3069F;
        this.cloak3.xRot = -c / 3.0F + 0.4466F;
        this.frontcloth1.xRot = c - 0.1047F;
        this.frontcloth2.xRot = c - 0.3316F;
    }

    private float doAbs(float value, float range) {
        return (Math.abs(value % range - range * 0.5F) - range * 0.25F) / (range * 0.25F);
    }

    
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.headStump.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.torso.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.collar.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.waistR.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.waistL.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.cloak1.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.cloak2.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.cloak3.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.frontcloth0.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.frontcloth1.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.frontcloth2.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
    }
}