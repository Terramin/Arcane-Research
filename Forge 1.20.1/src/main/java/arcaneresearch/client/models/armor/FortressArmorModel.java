package arcaneresearch.client.models.armor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
@OnlyIn(Dist.CLIENT)
public class FortressArmorModel extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(Arcaneresearch.MODID, "fortress_armor"), "main");
    private final ModelPart ornamentL;
    private final ModelPart ornamentL2;
    private final ModelPart ornamentR;
    private final ModelPart ornamentR2;
    private final ModelPart helmet;
    private final ModelPart helmetR;
    private final ModelPart helmetL;
    private final ModelPart helmetB;
    private final ModelPart capsthingy;
    private final ModelPart flapR;
    private final ModelPart flapL;
    private final ModelPart gemornament;
    private final ModelPart gem;
    private final ModelPart goggles;
    private final ModelPart[] mask;
    private final ModelPart beltR;
    private final ModelPart mbelt;
    private final ModelPart mbeltL;
    private final ModelPart mbeltR;
    private final ModelPart beltL;
    private final ModelPart chestplate;
    private final ModelPart scroll;
    private final ModelPart backplate;
    private final ModelPart book;
    private final ModelPart shoulderR;
    private final ModelPart gauntletR;
    private final ModelPart gauntletstrapR1;
    private final ModelPart gauntletstrapR2;
    private final ModelPart shoulderplateRtop;
    private final ModelPart shoulderplateR1;
    private final ModelPart shoulderplateR2;
    private final ModelPart shoulderplateR3;
    private final ModelPart shoulderL;
    private final ModelPart gauntletL;
    private final ModelPart gauntletstrapL1;
    private final ModelPart gauntletstrapL2;
    private final ModelPart shoulderplateLtop;
    private final ModelPart shoulderplateL1;
    private final ModelPart shoulderplateL2;
    private final ModelPart shoulderplateL3;
    private final ModelPart legpanelR1;
    private final ModelPart legpanelR2;
    private final ModelPart legpanelR3;
    private final ModelPart legpanelR4;
    private final ModelPart legpanelR5;
    private final ModelPart legpanelR6;
    private final ModelPart sidepanelR1;
    private final ModelPart sidepanelR2;
    private final ModelPart sidepanelR3;
    private final ModelPart backpanelR1;
    private final ModelPart backpanelR2;
    private final ModelPart backpanelR3;
    private final ModelPart backpanelL3;
    private final ModelPart legpanelL1;
    private final ModelPart legpanelL2;
    private final ModelPart legpanelL3;
    private final ModelPart legpanelL4;
    private final ModelPart legpanelL5;
    private final ModelPart legpanelL6;
    private final ModelPart sidepanelL1;
    private final ModelPart sidepanelL2;
    private final ModelPart sidepanelL3;
    private final ModelPart backpanelL1;
    private final ModelPart backpanelL2;
    public FortressArmorModel(ModelPart root) {
        super(root);
        ModelPart head = root.getChild("head");
        ModelPart body = root.getChild("body");
        this.ornamentL = head.getChild("ornament_l");
        this.ornamentL2 = head.getChild("ornament_l2");
        this.ornamentR = head.getChild("ornament_r");
        this.ornamentR2 = head.getChild("ornament_r2");
        this.helmet = head.getChild("helmet");
        this.helmetR = head.getChild("helmet_r");
        this.helmetL = head.getChild("helmet_l");
        this.helmetB = head.getChild("helmet_b");
        this.capsthingy = head.getChild("capsthingy");
        this.flapR = head.getChild("flap_r");
        this.flapL = head.getChild("flap_l");
        this.gemornament = head.getChild("gemornament");
        this.gem = head.getChild("gem");
        this.goggles = head.getChild("goggles");
        this.mask = new ModelPart[3];
        this.mask[0] = head.getChild("mask_0");
        this.mask[1] = head.getChild("mask_1");
        this.mask[2] = head.getChild("mask_2");
        this.beltR = body.getChild("belt_r");
        this.mbelt = body.getChild("mbelt");
        this.mbeltL = body.getChild("mbelt_l");
        this.mbeltR = body.getChild("mbelt_r");
        this.beltL = body.getChild("belt_l");
        this.chestplate = body.getChild("chestplate");
        this.scroll = body.getChild("scroll");
        this.backplate = body.getChild("backplate");
        this.book = body.getChild("book");
        ModelPart rightArm = root.getChild("right_arm");
        ModelPart leftArm = root.getChild("left_arm");
        this.shoulderR = rightArm.getChild("shoulder_r");
        this.gauntletR = rightArm.getChild("gauntlet_r");
        this.gauntletstrapR1 = rightArm.getChild("gauntletstrap_r1");
        this.gauntletstrapR2 = rightArm.getChild("gauntletstrap_r2");
        this.shoulderplateRtop = rightArm.getChild("shoulderplate_rtop");
        this.shoulderplateR1 = rightArm.getChild("shoulderplate_r1");
        this.shoulderplateR2 = rightArm.getChild("shoulderplate_r2");
        this.shoulderplateR3 = rightArm.getChild("shoulderplate_r3");
        this.shoulderL = leftArm.getChild("shoulder_l");
        this.gauntletL = leftArm.getChild("gauntlet_l");
        this.gauntletstrapL1 = leftArm.getChild("gauntletstrap_l1");
        this.gauntletstrapL2 = leftArm.getChild("gauntletstrap_l2");
        this.shoulderplateLtop = leftArm.getChild("shoulderplate_ltop");
        this.shoulderplateL1 = leftArm.getChild("shoulderplate_l1");
        this.shoulderplateL2 = leftArm.getChild("shoulderplate_l2");
        this.shoulderplateL3 = leftArm.getChild("shoulderplate_l3");
        ModelPart rightLeg = root.getChild("right_leg");
        ModelPart leftLeg = root.getChild("left_leg");
        this.legpanelR1 = rightLeg.getChild("legpanel_r1");
        this.legpanelR2 = rightLeg.getChild("legpanel_r2");
        this.legpanelR3 = rightLeg.getChild("legpanel_r3");
        this.legpanelR4 = rightLeg.getChild("legpanel_r4");
        this.legpanelR5 = rightLeg.getChild("legpanel_r5");
        this.legpanelR6 = rightLeg.getChild("legpanel_r6");
        this.sidepanelR1 = rightLeg.getChild("sidepanel_r1");
        this.sidepanelR2 = rightLeg.getChild("sidepanel_r2");
        this.sidepanelR3 = rightLeg.getChild("sidepanel_r3");
        this.backpanelR1 = rightLeg.getChild("backpanel_r1");
        this.backpanelR2 = rightLeg.getChild("backpanel_r2");
        this.backpanelR3 = rightLeg.getChild("backpanel_r3");
        this.backpanelL3 = leftLeg.getChild("backpanel_l3");
        this.legpanelL1 = leftLeg.getChild("legpanel_l1");
        this.legpanelL2 = leftLeg.getChild("legpanel_l2");
        this.legpanelL3 = leftLeg.getChild("legpanel_l3");
        this.legpanelL4 = leftLeg.getChild("legpanel_l4");
        this.legpanelL5 = leftLeg.getChild("legpanel_l5");
        this.legpanelL6 = leftLeg.getChild("legpanel_l6");
        this.sidepanelL1 = leftLeg.getChild("sidepanel_l1");
        this.sidepanelL2 = leftLeg.getChild("sidepanel_l2");
        this.sidepanelL3 = leftLeg.getChild("sidepanel_l3");
        this.backpanelL1 = leftLeg.getChild("backpanel_l1");
        this.backpanelL2 = leftLeg.getChild("backpanel_l2");
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
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
        head.addOrReplaceChild("ornament_l",
                CubeListBuilder.create().texOffs(78, 8).mirror().addBox(1.5F, -9.0F, -6.5F, 2.0F, 2.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396263F, 0.0F, 0.0F));
        head.addOrReplaceChild("ornament_l2",
                CubeListBuilder.create().texOffs(78, 8).mirror().addBox(3.5F, -10.0F, -6.5F, 1.0F, 2.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396263F, 0.0F, 0.0F));
        head.addOrReplaceChild("ornament_r",
                CubeListBuilder.create().texOffs(78, 8).addBox(-3.5F, -9.0F, -6.5F, 2.0F, 2.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396263F, 0.0F, 0.0F));
        head.addOrReplaceChild("ornament_r2",
                CubeListBuilder.create().texOffs(78, 8).addBox(-4.5F, -10.0F, -6.5F, 1.0F, 2.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396263F, 0.0F, 0.0F));
        head.addOrReplaceChild("helmet",
                CubeListBuilder.create().texOffs(41, 8).addBox(-4.5F, -9.0F, -4.5F, 9.0F, 4.0F, 9.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        head.addOrReplaceChild("helmet_r",
                CubeListBuilder.create().texOffs(21, 13).addBox(-6.5F, -3.0F, -4.5F, 1.0F, 5.0F, 9.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5235988F));
        head.addOrReplaceChild("helmet_l",
                CubeListBuilder.create().texOffs(21, 13).mirror().addBox(5.5F, -3.0F, -4.5F, 1.0F, 5.0F, 9.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5235988F));
        head.addOrReplaceChild("helmet_b",
                CubeListBuilder.create().texOffs(41, 21).addBox(-4.5F, -3.0F, 5.5F, 9.0F, 5.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5235988F, 0.0F, 0.0F));
        head.addOrReplaceChild("capsthingy",
                CubeListBuilder.create().texOffs(21, 0).addBox(-4.5F, -6.0F, -6.5F, 9.0F, 1.0F, 2.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        head.addOrReplaceChild("flap_r",
                CubeListBuilder.create().texOffs(59, 10).addBox(-10.0F, -2.0F, -1.0F, 3.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.5235988F, 0.5235988F));
        head.addOrReplaceChild("flap_l",
                CubeListBuilder.create().texOffs(59, 10).mirror().addBox(7.0F, -2.0F, -1.0F, 3.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.5235988F, -0.5235988F));
        head.addOrReplaceChild("gemornament",
                CubeListBuilder.create().texOffs(68, 11).addBox(-1.5F, -9.0F, -7.0F, 3.0F, 3.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396263F, 0.0F, 0.0F));
        head.addOrReplaceChild("gem",
                CubeListBuilder.create().texOffs(72, 8).addBox(-1.0F, -8.5F, -7.5F, 2.0F, 2.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396263F, 0.0F, 0.0F));
        head.addOrReplaceChild("goggles",
                CubeListBuilder.create().texOffs(100, 18).addBox(-4.5F, -5.0F, -4.25F, 9.0F, 5.0F, 1.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        for (int a = 0; a < 3; a++) {
            final int idx = a;
            head.addOrReplaceChild("mask_" + idx,
                    CubeListBuilder.create().texOffs(52 + a * 24, 2).addBox(-4.5F, -5.0F, -4.6F, 9.0F, 5.0F, 1.0F, CubeDeformation.NONE),
                    PartPose.ZERO);
        }
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
        body.addOrReplaceChild("chestplate",
                CubeListBuilder.create().texOffs(56, 45).addBox(-4.0F, 1.0F, -4.0F, 8.0F, 7.0F, 2.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        body.addOrReplaceChild("scroll",
                CubeListBuilder.create().texOffs(34, 27).addBox(-2.0F, 9.5F, 4.0F, 8.0F, 3.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1919862F));
        body.addOrReplaceChild("backplate",
                CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 2.0F, 8.0F, 11.0F, 2.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        body.addOrReplaceChild("book",
                CubeListBuilder.create().texOffs(100, 8).addBox(1.0F, -0.3F, 4.0F, 5.0F, 7.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7679449F));
        rightArm.addOrReplaceChild("shoulder_r",
                CubeListBuilder.create().texOffs(56, 35).addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        rightArm.addOrReplaceChild("gauntlet_r",
                CubeListBuilder.create().texOffs(100, 26).addBox(-3.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        rightArm.addOrReplaceChild("gauntletstrap_r1",
                CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        rightArm.addOrReplaceChild("gauntletstrap_r2",
                CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        rightArm.addOrReplaceChild("shoulderplate_rtop",
                CubeListBuilder.create().texOffs(110, 37).addBox(-5.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightArm.addOrReplaceChild("shoulderplate_r1",
                CubeListBuilder.create().texOffs(110, 45).addBox(-4.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightArm.addOrReplaceChild("shoulderplate_r2",
                CubeListBuilder.create().texOffs(94, 45).addBox(-3.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightArm.addOrReplaceChild("shoulderplate_r3",
                CubeListBuilder.create().texOffs(94, 45).addBox(-2.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        leftArm.addOrReplaceChild("shoulder_l",
                CubeListBuilder.create().texOffs(56, 35).mirror().addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        leftArm.addOrReplaceChild("gauntlet_l",
                CubeListBuilder.create().texOffs(114, 26).addBox(1.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        leftArm.addOrReplaceChild("gauntletstrap_l1",
                CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        leftArm.addOrReplaceChild("gauntletstrap_l2",
                CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        leftArm.addOrReplaceChild("shoulderplate_ltop",
                CubeListBuilder.create().texOffs(110, 37).mirror().addBox(3.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftArm.addOrReplaceChild("shoulderplate_l1",
                CubeListBuilder.create().texOffs(110, 45).mirror().addBox(3.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftArm.addOrReplaceChild("shoulderplate_l2",
                CubeListBuilder.create().texOffs(94, 45).mirror().addBox(2.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftArm.addOrReplaceChild("shoulderplate_l3",
                CubeListBuilder.create().texOffs(94, 45).mirror().addBox(1.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        rightLeg.addOrReplaceChild("legpanel_r1",
                CubeListBuilder.create().texOffs(0, 51).addBox(-1.0F, 0.5F, -3.5F, 3.0F, 4.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanel_r2",
                CubeListBuilder.create().texOffs(8, 51).addBox(-1.0F, 3.5F, -2.5F, 3.0F, 4.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanel_r3",
                CubeListBuilder.create().texOffs(0, 56).addBox(-1.0F, 6.5F, -1.5F, 3.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanel_r4",
                CubeListBuilder.create().texOffs(0, 43).addBox(-3.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanel_r5",
                CubeListBuilder.create().texOffs(0, 47).addBox(-3.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("legpanel_r6",
                CubeListBuilder.create().texOffs(6, 43).addBox(-3.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("sidepanel_r1",
                CubeListBuilder.create().texOffs(0, 22).addBox(-2.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightLeg.addOrReplaceChild("sidepanel_r2",
                CubeListBuilder.create().texOffs(0, 31).addBox(-1.5F, 3.5F, -2.5F, 1.0F, 3.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightLeg.addOrReplaceChild("sidepanel_r3",
                CubeListBuilder.create().texOffs(12, 31).addBox(-0.5F, 5.5F, -2.5F, 1.0F, 3.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363323F));
        rightLeg.addOrReplaceChild("backpanel_r1",
                CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, 0.5F, 2.5F, 5.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("backpanel_r2",
                CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, 2.5F, 1.5F, 5.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("backpanel_r3",
                CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, 4.5F, 0.5F, 5.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("backpanel_l3",
                CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0F, 4.5F, 0.5F, 5.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l1",
                CubeListBuilder.create().texOffs(0, 51).mirror().addBox(-2.0F, 0.5F, -3.5F, 3.0F, 4.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l2",
                CubeListBuilder.create().texOffs(8, 51).mirror().addBox(-2.0F, 3.5F, -2.5F, 3.0F, 4.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l3",
                CubeListBuilder.create().texOffs(0, 56).mirror().addBox(-2.0F, 6.5F, -1.5F, 3.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l4",
                CubeListBuilder.create().texOffs(0, 43).mirror().addBox(1.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l5",
                CubeListBuilder.create().texOffs(0, 47).mirror().addBox(1.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("legpanel_l6",
                CubeListBuilder.create().texOffs(6, 43).mirror().addBox(1.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("sidepanel_l1",
                CubeListBuilder.create().texOffs(0, 22).mirror().addBox(1.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftLeg.addOrReplaceChild("sidepanel_l2",
                CubeListBuilder.create().texOffs(0, 31).mirror().addBox(0.5F, 3.5F, -2.5F, 1.0F, 3.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftLeg.addOrReplaceChild("sidepanel_l3",
                CubeListBuilder.create().texOffs(12, 31).mirror().addBox(-0.5F, 5.5F, -2.5F, 1.0F, 3.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363323F));
        leftLeg.addOrReplaceChild("backpanel_l1",
                CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0F, 0.5F, 2.5F, 5.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("backpanel_l2",
                CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-2.0F, 2.5F, 1.5F, 5.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363323F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }
    public void applyVariant(int setCount, boolean hasGoggles, int maskIndex) {
        goggles.visible = hasGoggles;
        for (int i = 0; i < mask.length; i++) {
            mask[i].visible = (i == maskIndex);
        }
        boolean set2 = setCount >= 2;
        boolean set3 = setCount >= 3;
        scroll.visible = set3;
        book.visible = set2;
        ornamentL.visible = set3;
        ornamentL2.visible = set3;
        ornamentR.visible = set3;
        ornamentR2.visible = set3;
        gemornament.visible = set3;
        gem.visible = set3;
        flapL.visible = set2;
        flapR.visible = set2;
        shoulderplateLtop.visible = set2;
        shoulderplateL1.visible = set2;
        shoulderplateL2.visible = set3;
        shoulderplateL3.visible = set3;
        shoulderplateRtop.visible = set2;
        shoulderplateR1.visible = set2;
        shoulderplateR2.visible = set3;
        shoulderplateR3.visible = set3;
        sidepanelR2.visible = set2;
        sidepanelL2.visible = set2;
        sidepanelR3.visible = set3;
        sidepanelL3.visible = set3;
    }
    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }
}
