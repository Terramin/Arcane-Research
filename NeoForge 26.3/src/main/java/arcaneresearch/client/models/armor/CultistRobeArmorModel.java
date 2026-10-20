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

public class CultistRobeArmorModel extends HumanoidModel<net.minecraft.client.renderer.entity.state.HumanoidRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "cultist_robe_armor"), "main");

    private final ModelPart hood1;
    private final ModelPart hood2;
    private final ModelPart hood3;
    private final ModelPart hood4;
    private final ModelPart chestthing;
    private final ModelPart mbelt;
    private final ModelPart mbeltB;
    private final ModelPart mbeltL;
    private final ModelPart mbeltR;
    private final ModelPart beltR;
    private final ModelPart beltL;
    private final ModelPart chestplate;
    private final ModelPart shoulderplateR1;
    private final ModelPart shoulderplateR2;
    private final ModelPart shoulderplateR3;
    private final ModelPart shoulderplateTopR;
    private final ModelPart rArm1;
    private final ModelPart rArm2;
    private final ModelPart rArm3;
    private final ModelPart lArm1;
    private final ModelPart lArm2;
    private final ModelPart lArm3;
    private final ModelPart shoulderplateL1;
    private final ModelPart shoulderplateL2;
    private final ModelPart shoulderplateL3;
    private final ModelPart shoulderplateTopL;
    private final ModelPart shoulderL;
    private final ModelPart shoulderR;
    private final ModelPart frontclothR1;
    private final ModelPart frontclothR2;
    private final ModelPart frontclothL1;
    private final ModelPart frontclothL2;
    private final ModelPart clothBackR1;
    private final ModelPart clothBackR2;
    private final ModelPart clothBackR3;
    private final ModelPart clothBackL1;
    private final ModelPart clothBackL2;
    private final ModelPart clothBackL3;
    private final ModelPart sideclothR1;
    private final ModelPart sideclothR2;
    private final ModelPart sideclothR3;
    private final ModelPart sideclothL1;
    private final ModelPart sideclothL2;
    private final ModelPart sideclothL3;
    private final ModelPart sidepanelR1;
    private final ModelPart legpanelR4;
    private final ModelPart legpanelR5;
    private final ModelPart legpanelR6;
    private final ModelPart legpanelL4;
    private final ModelPart legpanelL5;
    private final ModelPart legpanelL6;
    private final ModelPart sidepanelL1;
    private final ModelPart focipouch;
    private final ModelPart book;
    private final ModelPart scroll;
    private final ModelPart backplate;
    private final ModelPart clothchestL;
    private final ModelPart clothchestR;

    public CultistRobeArmorModel(ModelPart root) {
        super(root);
        ModelPart body = root.getChild("body");
        ModelPart head = root.getChild("head");
        ModelPart rightArm = root.getChild("right_arm");
        ModelPart leftArm = root.getChild("left_arm");
        ModelPart rightLeg = root.getChild("right_leg");
        ModelPart leftLeg = root.getChild("left_leg");
        this.hood1 = head.getChild("hood_1");
        this.hood2 = head.getChild("hood_2");
        this.hood3 = head.getChild("hood_3");
        this.hood4 = head.getChild("hood_4");
        this.chestthing = body.getChild("chestthing");
        this.mbelt = body.getChild("mbelt");
        this.mbeltB = body.getChild("mbelt_b");
        this.mbeltL = body.getChild("mbelt_l");
        this.mbeltR = body.getChild("mbelt_r");
        this.beltR = body.getChild("belt_r");
        this.beltL = body.getChild("belt_l");
        this.chestplate = body.getChild("chestplate");
        this.shoulderplateR1 = rightArm.getChild("shoulderplate_r1");
        this.shoulderplateR2 = rightArm.getChild("shoulderplate_r2");
        this.shoulderplateR3 = rightArm.getChild("shoulderplate_r3");
        this.shoulderplateTopR = rightArm.getChild("shoulderplate_top_r");
        this.rArm1 = rightArm.getChild("rarm_1");
        this.rArm2 = rightArm.getChild("rarm_2");
        this.rArm3 = rightArm.getChild("rarm_3");
        this.lArm1 = leftArm.getChild("larm_1");
        this.lArm2 = leftArm.getChild("larm_2");
        this.lArm3 = leftArm.getChild("larm_3");
        this.shoulderplateL1 = leftArm.getChild("shoulderplate_l1");
        this.shoulderplateL2 = leftArm.getChild("shoulderplate_l2");
        this.shoulderplateL3 = leftArm.getChild("shoulderplate_l3");
        this.shoulderplateTopL = leftArm.getChild("shoulderplate_top_l");
        this.shoulderL = leftArm.getChild("shoulder_l");
        this.shoulderR = rightArm.getChild("shoulder_r");
        this.frontclothR1 = body.getChild("frontcloth_r1");
        this.frontclothR2 = body.getChild("frontcloth_r2");
        this.frontclothL1 = body.getChild("frontcloth_l1");
        this.frontclothL2 = body.getChild("frontcloth_l2");
        this.clothBackR1 = body.getChild("clothback_r1");
        this.clothBackR2 = body.getChild("clothback_r2");
        this.clothBackR3 = body.getChild("clothback_r3");
        this.clothBackL1 = body.getChild("clothback_l1");
        this.clothBackL2 = body.getChild("clothback_l2");
        this.clothBackL3 = body.getChild("clothback_l3");
        this.sideclothR1 = rightLeg.getChild("sidecloth_r1");
        this.sideclothR2 = rightLeg.getChild("sidecloth_r2");
        this.sideclothR3 = rightLeg.getChild("sidecloth_r3");
        this.sideclothL1 = leftLeg.getChild("sidecloth_l1");
        this.sideclothL2 = leftLeg.getChild("sidecloth_l2");
        this.sideclothL3 = leftLeg.getChild("sidecloth_l3");
        this.sidepanelR1 = rightLeg.getChild("sidepanel_r1");
        this.legpanelR4 = rightLeg.getChild("legpanel_r4");
        this.legpanelR5 = rightLeg.getChild("legpanel_r5");
        this.legpanelR6 = rightLeg.getChild("legpanel_r6");
        this.legpanelL4 = leftLeg.getChild("legpanel_l4");
        this.legpanelL5 = leftLeg.getChild("legpanel_l5");
        this.legpanelL6 = leftLeg.getChild("legpanel_l6");
        this.sidepanelL1 = leftLeg.getChild("sidepanel_l1");
        this.focipouch = leftLeg.getChild("focipouch");
        this.book = body.getChild("book");
        this.scroll = body.getChild("scroll");
        this.backplate = body.getChild("backplate");
        this.clothchestL = body.getChild("clothchest_l");
        this.clothchestR = body.getChild("clothchest_r");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create(), PartPose.ZERO);
        // NEOFORGE_PORT (26.3): hat is head.getChild (was root child in 1.20.1).
        head.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        head.addOrReplaceChild("hood_1",
                CubeListBuilder.create().texOffs(16, 7).addBox(-4.5F, -9.0F, -4.6F, 9.0F, 9.0F, 9.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        head.addOrReplaceChild("hood_2",
                CubeListBuilder.create().texOffs(52, 13).addBox(-4.0F, -9.7F, 2.0F, 8.0F, 9.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2268928F, 0.0F, 0.0F));
        head.addOrReplaceChild("hood_3",
                CubeListBuilder.create().texOffs(52, 14).addBox(-3.5F, -10.0F, 3.5F, 7.0F, 8.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3490659F, 0.0F, 0.0F));
        head.addOrReplaceChild("hood_4",
                CubeListBuilder.create().texOffs(53, 15).addBox(-3.0F, -10.7F, 3.5F, 6.0F, 7.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5759587F, 0.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition rightArm = root.addOrReplaceChild("right_arm",
                CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm",
                CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition rightLeg = root.addOrReplaceChild("right_leg",
                CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg",
                CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

        body.addOrReplaceChild("chestthing",
                CubeListBuilder.create().texOffs(56, 50).addBox(-2.5F, 1.0F, -4.0F, 5.0F, 7.0F, 1.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        body.addOrReplaceChild("mbelt",
                CubeListBuilder.create().texOffs(16, 55).addBox(-4.0F, 7.0F, -3.0F, 8.0F, 5.0F, 1.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        body.addOrReplaceChild("mbelt_b",
                CubeListBuilder.create().texOffs(16, 55).addBox(-4.0F, 7.0F, -4.0F, 8.0F, 5.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.141593F, 0.0F));
        body.addOrReplaceChild("mbelt_l",
                CubeListBuilder.create().texOffs(16, 36).addBox(4.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        body.addOrReplaceChild("mbelt_r",
                CubeListBuilder.create().texOffs(16, 36).addBox(-5.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        body.addOrReplaceChild("belt_r",
                CubeListBuilder.create().texOffs(16, 36).addBox(-5.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        body.addOrReplaceChild("belt_l",
                CubeListBuilder.create().texOffs(16, 36).addBox(4.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        body.addOrReplaceChild("chestplate",
                CubeListBuilder.create().texOffs(16, 25).addBox(-4.0F, 1.0F, -3.0F, 8.0F, 6.0F, 1.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        body.addOrReplaceChild("backplate",
                CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 1.9F, 8.0F, 11.0F, 2.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        body.addOrReplaceChild("book",
                CubeListBuilder.create().texOffs(81, 16).addBox(1.0F, 0.0F, 4.0F, 5.0F, 7.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7679449F));
        body.addOrReplaceChild("scroll",
                CubeListBuilder.create().texOffs(78, 25).addBox(-2.0F, 9.5F, 4.0F, 8.0F, 3.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1919862F));

        rightArm.addOrReplaceChild("shoulder_r",
                CubeListBuilder.create().texOffs(16, 45).mirror().addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        rightArm.addOrReplaceChild("rarm_1",
                CubeListBuilder.create().texOffs(88, 39).addBox(-3.5F, 2.5F, -2.5F, 5.0F, 7.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        rightArm.addOrReplaceChild("rarm_2",
                CubeListBuilder.create().texOffs(76, 32).addBox(-3.0F, 5.5F, 2.5F, 4.0F, 4.0F, 2.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        rightArm.addOrReplaceChild("rarm_3",
                CubeListBuilder.create().texOffs(88, 32).addBox(-2.5F, 3.5F, 2.5F, 3.0F, 2.0F, 1.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        rightArm.addOrReplaceChild("shoulderplate_top_r",
                CubeListBuilder.create().texOffs(56, 25).addBox(-5.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightArm.addOrReplaceChild("shoulderplate_r1",
                CubeListBuilder.create().texOffs(56, 33).addBox(-4.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightArm.addOrReplaceChild("shoulderplate_r2",
                CubeListBuilder.create().texOffs(40, 33).addBox(-3.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightArm.addOrReplaceChild("shoulderplate_r3",
                CubeListBuilder.create().texOffs(40, 33).addBox(-2.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));

        leftArm.addOrReplaceChild("shoulder_l",
                CubeListBuilder.create().texOffs(16, 45).mirror().addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        leftArm.addOrReplaceChild("larm_1",
                CubeListBuilder.create().texOffs(88, 39).mirror().addBox(-1.5F, 2.5F, -2.5F, 5.0F, 7.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        leftArm.addOrReplaceChild("larm_2",
                CubeListBuilder.create().texOffs(76, 32).addBox(-1.0F, 5.5F, 2.5F, 4.0F, 4.0F, 2.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        leftArm.addOrReplaceChild("larm_3",
                CubeListBuilder.create().texOffs(88, 32).addBox(-0.5F, 3.5F, 2.5F, 3.0F, 2.0F, 1.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        leftArm.addOrReplaceChild("shoulderplate_top_l",
                CubeListBuilder.create().texOffs(56, 25).addBox(3.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftArm.addOrReplaceChild("shoulderplate_l1",
                CubeListBuilder.create().texOffs(56, 33).addBox(3.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftArm.addOrReplaceChild("shoulderplate_l2",
                CubeListBuilder.create().texOffs(40, 33).addBox(2.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftArm.addOrReplaceChild("shoulderplate_l3",
                CubeListBuilder.create().texOffs(40, 33).addBox(1.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));

        rightLeg.addOrReplaceChild("legpanel_r4",
                CubeListBuilder.create().texOffs(76, 38).addBox(-3.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanel_r5",
                CubeListBuilder.create().texOffs(76, 42).addBox(-3.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanel_r6",
                CubeListBuilder.create().texOffs(82, 38).addBox(-3.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("sidepanel_r1",
                CubeListBuilder.create().texOffs(116, 25).mirror().addBox(-2.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));

        leftLeg.addOrReplaceChild("legpanel_l4",
                CubeListBuilder.create().texOffs(76, 38).mirror().addBox(1.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l5",
                CubeListBuilder.create().texOffs(76, 42).mirror().addBox(1.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l6",
                CubeListBuilder.create().texOffs(82, 38).mirror().addBox(1.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("sidepanel_l1",
                CubeListBuilder.create().texOffs(116, 25).addBox(1.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));

        body.addOrReplaceChild("frontcloth_r1",
                CubeListBuilder.create().texOffs(108, 38).addBox(0.0F, 0.0F, 0.0F, 3.0F, 8.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-3.0F, 11.0F, -2.9F, -0.1047198F, 0.0F, 0.0F));
        body.addOrReplaceChild("frontcloth_r2",
                CubeListBuilder.create().texOffs(108, 47).addBox(0.0F, 7.5F, 1.7F, 3.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-3.0F, 11.0F, -2.9F, -0.3316126F, 0.0F, 0.0F));
        body.addOrReplaceChild("frontcloth_l1",
                CubeListBuilder.create().texOffs(108, 38).mirror().addBox(0.0F, 0.0F, 0.0F, 3.0F, 8.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 11.0F, -2.9F, -0.1047198F, 0.0F, 0.0F));
        body.addOrReplaceChild("frontcloth_l2",
                CubeListBuilder.create().texOffs(108, 47).mirror().addBox(0.0F, 7.5F, 1.7F, 3.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 11.0F, -2.9F, -0.3316126F, 0.0F, 0.0F));

        body.addOrReplaceChild("clothback_r1",
                CubeListBuilder.create().texOffs(118, 16).mirror().addBox(0.0F, 0.0F, 0.0F, 4.0F, 8.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-4.0F, 11.5F, 2.9F, 0.1047198F, 0.0F, 0.0F));
        body.addOrReplaceChild("clothback_r2",
                CubeListBuilder.create().texOffs(123, 9).addBox(0.0F, 7.8F, -0.9F, 1.0F, 2.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-4.0F, 11.5F, 2.9F, 0.2268928F, 0.0F, 0.0F));
        body.addOrReplaceChild("clothback_r3",
                CubeListBuilder.create().texOffs(120, 12).mirror().addBox(1.0F, 7.8F, -0.9F, 3.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-4.0F, 11.5F, 2.9F, 0.2268928F, 0.0F, 0.0F));
        body.addOrReplaceChild("clothback_l1",
                CubeListBuilder.create().texOffs(118, 16).addBox(0.0F, 0.0F, 0.0F, 4.0F, 8.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 11.5F, 2.9F, 0.1047198F, 0.0F, 0.0F));
        body.addOrReplaceChild("clothback_l2",
                CubeListBuilder.create().texOffs(123, 9).mirror().addBox(3.0F, 7.8F, -0.9F, 1.0F, 2.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 11.5F, 2.9F, 0.2268928F, 0.0F, 0.0F));
        body.addOrReplaceChild("clothback_l3",
                CubeListBuilder.create().texOffs(120, 12).addBox(0.0F, 7.8F, -0.9F, 3.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 11.5F, 2.9F, 0.2268928F, 0.0F, 0.0F));

        rightLeg.addOrReplaceChild("sidecloth_r1",
                CubeListBuilder.create().texOffs(116, 42).addBox(-2.5F, 0.5F, -2.5F, 1.0F, 5.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.122173F));
        rightLeg.addOrReplaceChild("sidecloth_r2",
                CubeListBuilder.create().texOffs(116, 34).addBox(-1.5F, 5.5F, -2.5F, 1.0F, 3.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.296706F));
        rightLeg.addOrReplaceChild("sidecloth_r3",
                CubeListBuilder.create().texOffs(116, 1).addBox(0.4F, 8.4F, -2.5F, 1.0F, 3.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5235988F));
        leftLeg.addOrReplaceChild("sidecloth_l1",
                CubeListBuilder.create().texOffs(116, 42).addBox(1.5F, 0.5F, -2.5F, 1.0F, 5.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.122173F));
        leftLeg.addOrReplaceChild("sidecloth_l2",
                CubeListBuilder.create().texOffs(116, 34).addBox(0.5F, 5.5F, -2.5F, 1.0F, 3.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.296706F));
        leftLeg.addOrReplaceChild("sidecloth_l3",
                CubeListBuilder.create().texOffs(116, 1).addBox(-1.4F, 8.4F, -2.5F, 1.0F, 3.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5235988F));

        leftLeg.addOrReplaceChild("focipouch",
                CubeListBuilder.create().texOffs(100, 20).addBox(3.5F, 0.5F, -2.5F, 3.0F, 6.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.122173F));
        body.addOrReplaceChild("clothchest_l",
                CubeListBuilder.create().texOffs(108, 38).mirror().addBox(2.1F, 0.5F, -3.5F, 2.0F, 8.0F, 1.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        body.addOrReplaceChild("clothchest_r",
                CubeListBuilder.create().texOffs(108, 38).addBox(-4.1F, 0.5F, -3.5F, 2.0F, 8.0F, 1.0F, CubeDeformation.NONE),
                PartPose.ZERO);

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
        frontclothR1.xRot = c - 0.1047198F;
        frontclothL1.xRot = c - 0.1047198F;
        frontclothR2.xRot = c - 0.3316126F;
        frontclothL2.xRot = c - 0.3316126F;
        clothBackR1.xRot = -c + 0.1047198F;
        clothBackL1.xRot = -c + 0.1047198F;
        clothBackR2.xRot = -c + 0.2268928F;
        clothBackL2.xRot = -c + 0.2268928F;
        clothBackR3.xRot = -c + 0.2268928F;
        clothBackL3.xRot = -c + 0.2268928F;
    }
}