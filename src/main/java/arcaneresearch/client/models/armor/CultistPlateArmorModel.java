package arcaneresearch.client.models.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;

public class CultistPlateArmorModel extends HumanoidModel<net.minecraft.client.renderer.entity.state.HumanoidRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "cultist_plate_armor"), "main");

    private final ModelPart frontcloth1;
    private final ModelPart frontcloth2;
    private final ModelPart cloak1;
    private final ModelPart cloak2;
    private final ModelPart cloak3;

    public CultistPlateArmorModel(ModelPart root) {
        super(root);
        ModelPart body = root.getChild("body");
        this.frontcloth1 = body.getChild("frontcloth1");
        this.frontcloth2 = body.getChild("frontcloth2");
        this.cloak1 = body.getChild("cloak1");
        this.cloak2 = body.getChild("cloak2");
        this.cloak3 = body.getChild("cloak3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create(), PartPose.ZERO);
        // NEOFORGE_PORT (26.3): hat is head.getChild (was root child in 1.20.1).
        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("helmet",
                CubeListBuilder.create()
                        .texOffs(41, 8)
                        .addBox(-4.5F, -9.0F, -4.5F, 9.0F, 9.0F, 9.0F, CubeDeformation.NONE),
                PartPose.ZERO);

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create(), PartPose.ZERO);
        body.addOrReplaceChild("belt_r",
                CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        body.addOrReplaceChild("mbelt",
                CubeListBuilder.create().texOffs(56, 55).addBox(-4.0F, 8.0F, -3.0F, 8.0F, 4.0F, 1.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        body.addOrReplaceChild("mbelt_l",
                CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        body.addOrReplaceChild("mbelt_r",
                CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        body.addOrReplaceChild("belt_l",
                CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        body.addOrReplaceChild("tabbard",
                CubeListBuilder.create().texOffs(114, 52).addBox(-3.0F, 1.2F, -3.5F, 6.0F, 10.0F, 1.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        body.addOrReplaceChild("cloak_at_l",
                CubeListBuilder.create().texOffs(0, 43).addBox(2.5F, 1.0F, 2.0F, 2.0F, 1.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1396263F, 0.0F, 0.0F));
        body.addOrReplaceChild("backplate",
                CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 2.0F, 8.0F, 11.0F, 2.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        body.addOrReplaceChild("cloak1",
                CubeListBuilder.create().texOffs(0, 47).addBox(0.0F, 0.0F, 0.0F, 9.0F, 12.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-4.5F, 1.3F, 4.2F, 0.1396263F, 0.0F, 0.0F));
        body.addOrReplaceChild("cloak2",
                CubeListBuilder.create().texOffs(0, 59).addBox(0.0F, 11.7F, -2.0F, 9.0F, 4.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-4.5F, 1.3F, 4.2F, 0.3069452F, 0.0F, 0.0F));
        body.addOrReplaceChild("cloak3",
                CubeListBuilder.create().texOffs(0, 59).addBox(0.0F, 15.2F, -4.2F, 9.0F, 4.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-4.5F, 1.3F, 4.2F, 0.4465716F, 0.0F, 0.0F));
        body.addOrReplaceChild("cloak_at_r",
                CubeListBuilder.create().texOffs(0, 43).addBox(-4.5F, 1.0F, 2.0F, 2.0F, 1.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1396263F, 0.0F, 0.0F));
        body.addOrReplaceChild("chestplate",
                CubeListBuilder.create().texOffs(56, 45).addBox(-4.0F, 1.0F, -3.0F, 8.0F, 7.0F, 1.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        body.addOrReplaceChild("frontcloth1",
                CubeListBuilder.create().texOffs(120, 39).addBox(0.0F, 0.0F, 0.0F, 6.0F, 8.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-3.0F, 11.0F, -3.5F, -0.1047198F, 0.0F, 0.0F));
        body.addOrReplaceChild("frontcloth2",
                CubeListBuilder.create().texOffs(100, 37).addBox(0.0F, 7.5F, 1.8F, 6.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-3.0F, 11.0F, -3.5F, -0.3316126F, 0.0F, 0.0F));

        PartDefinition rightArm = root.addOrReplaceChild("right_arm",
                CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        rightArm.addOrReplaceChild("shoulder_r",
                CubeListBuilder.create().texOffs(56, 35).mirror().addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        rightArm.addOrReplaceChild("shoulder_r0",
                CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-4.3F, -1.5F, -3.0F, 3.0F, 5.0F, 6.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7853982F));
        rightArm.addOrReplaceChild("shoulder_r1",
                CubeListBuilder.create().texOffs(0, 19).addBox(-3.3F, 3.5F, -2.5F, 1.0F, 1.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7853982F));
        rightArm.addOrReplaceChild("shoulder_r2",
                CubeListBuilder.create().texOffs(0, 11).mirror().addBox(-2.3F, 3.5F, -3.0F, 1.0F, 2.0F, 6.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7853982F));
        rightArm.addOrReplaceChild("gauntlet_r",
                CubeListBuilder.create().texOffs(100, 26).mirror().addBox(-3.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        rightArm.addOrReplaceChild("gauntletstrap_r1",
                CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        rightArm.addOrReplaceChild("gauntletstrap_r2",
                CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);

        PartDefinition leftArm = root.addOrReplaceChild("left_arm",
                CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        leftArm.addOrReplaceChild("shoulder_l",
                CubeListBuilder.create().texOffs(56, 35).addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        leftArm.addOrReplaceChild("shoulder_l0",
                CubeListBuilder.create().texOffs(0, 0).addBox(1.3F, -1.5F, -3.0F, 3.0F, 5.0F, 6.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7853982F));
        leftArm.addOrReplaceChild("shoulder_l1",
                CubeListBuilder.create().texOffs(0, 19).mirror().addBox(2.3F, 3.5F, -2.5F, 1.0F, 1.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7853982F));
        leftArm.addOrReplaceChild("shoulder_l2",
                CubeListBuilder.create().texOffs(0, 11).addBox(1.3F, 3.5F, -3.0F, 1.0F, 2.0F, 6.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7853982F));
        leftArm.addOrReplaceChild("gauntlet_l",
                CubeListBuilder.create().texOffs(114, 26).addBox(1.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        leftArm.addOrReplaceChild("gauntletstrap_l1",
                CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        leftArm.addOrReplaceChild("gauntletstrap_l2",
                CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);

        PartDefinition rightLeg = root.addOrReplaceChild("right_leg",
                CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        rightLeg.addOrReplaceChild("sidepanel_r0",
                CubeListBuilder.create().texOffs(96, 14).addBox(-3.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1396263F));
        rightLeg.addOrReplaceChild("sidepanel_r1",
                CubeListBuilder.create().texOffs(96, 7).mirror().addBox(0.0F, 2.5F, -2.5F, 2.0F, 2.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1396263F));
        rightLeg.addOrReplaceChild("sidepanel_r2",
                CubeListBuilder.create().texOffs(114, 5).mirror().addBox(-2.0F, 2.5F, -2.5F, 2.0F, 3.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1396263F));
        rightLeg.addOrReplaceChild("sidepanel_r3",
                CubeListBuilder.create().texOffs(116, 13).addBox(-3.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1396263F));

        PartDefinition leftLeg = root.addOrReplaceChild("left_leg",
                CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
        leftLeg.addOrReplaceChild("sidepanel_l0",
                CubeListBuilder.create().texOffs(96, 14).addBox(-2.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1396263F));
        leftLeg.addOrReplaceChild("sidepanel_l1",
                CubeListBuilder.create().texOffs(96, 7).addBox(-2.0F, 2.5F, -2.5F, 2.0F, 2.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1396263F));
        leftLeg.addOrReplaceChild("sidepanel_l2",
                CubeListBuilder.create().texOffs(114, 5).addBox(0.0F, 2.5F, -2.5F, 2.0F, 3.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1396263F));
        leftLeg.addOrReplaceChild("sidepanel_l3",
                CubeListBuilder.create().texOffs(116, 13).addBox(2.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1396263F));

        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    // NEOFORGE_PORT (26.3): HumanoidModel<State>; walk/yaw/pitch via state.
    public void setupAnim(net.minecraft.client.renderer.entity.state.HumanoidRenderState state) {
        super.setupAnim(state);
        float limbSwing = state.walkAnimationPos;
        float limbSwingAmount = state.walkAnimationSpeed;
        float ageInTicks = state.ageInTicks;
        float netHeadYaw = state.yRot;
        float headPitch = state.xRot;
        

        float a = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        float b = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.4F * limbSwingAmount;
        float c = Math.min(a, b);
        frontcloth1.xRot = c - 0.1047198F;
        frontcloth2.xRot = c - 0.3316126F;
        cloak1.xRot = -c / 2.0F + 0.1396263F;
        cloak2.xRot = -c / 2.0F + 0.3069452F;
        cloak3.xRot = -c / 2.0F + 0.4465716F;
    }
}