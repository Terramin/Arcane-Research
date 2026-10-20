package arcaneresearch.client.models.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import arcaneresearch.Arcaneresearch;

// NEOFORGE_PORT (26.3): model shared by guardian + warden renderers via the
// common GuardianState (arm lifts); concrete EntityModel<GuardianState>.
public class EldritchGuardianModel extends EntityModel<EldritchGuardianModel.GuardianState> {

    public static class GuardianState extends LivingEntityRenderState {
        public float armLiftR;
        public float armLiftL;
        // NEOFORGE_PORT (26.3): Forge model.renderAlpha (ghost fade), applied as model tint.
        public float ghostAlpha = 1.0F;
    }

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "eldritch_guardian"), "main");

    private final ModelPart beltR;
    private final ModelPart mbelt;
    private final ModelPart mbeltL;
    private final ModelPart mbeltR;
    private final ModelPart beltL;
    private final ModelPart chestplate;
    private final ModelPart hoodEye;
    private final ModelPart hood1;
    private final ModelPart hood2;
    private final ModelPart hood3;
    private final ModelPart hood4;
    private final ModelPart backplate;
    private final ModelPart shoulderplateTopR;
    private final ModelPart shoulderplateR1;
    private final ModelPart shoulderplateR2;
    private final ModelPart shoulderplateR3;
    private final ModelPart shoulderR;
    private final ModelPart armL1;
    private final ModelPart armL2;
    private final ModelPart armL3;
    private final ModelPart armR1;
    private final ModelPart armR2;
    private final ModelPart armR3;
    private final ModelPart shoulderL;
    private final ModelPart shoulderplateLtop;
    private final ModelPart shoulderplateL1;
    private final ModelPart shoulderplateL2;
    private final ModelPart shoulderplateL3;
    private final ModelPart legpanelR4;
    private final ModelPart legpanelR5;
    private final ModelPart legpanelR6;
    private final ModelPart backpanelR1;
    private final ModelPart backpanelR2;
    private final ModelPart backpanelR3;
    private final ModelPart backpanelL3;
    private final ModelPart legpanelL4;
    private final ModelPart legpanelL5;
    private final ModelPart legpanelL6;
    private final ModelPart backpanelL1;
    private final ModelPart backpanelL2;
    private final ModelPart sidepanelL1;
    private final ModelPart sidepanelR1;
    private final ModelPart sidepanelR2;
    private final ModelPart sidepanelR3;
    private final ModelPart sidepanelR4;
    private final ModelPart sidepanelL2;
    private final ModelPart sidepanelL3;
    private final ModelPart sidepanelL4;
    private final ModelPart legpanelC1;
    private final ModelPart legpanelC2;
    private final ModelPart legpanelC3;
    private final ModelPart cloak1;
    private final ModelPart cloak2;
    private final ModelPart cloak3;

    public EldritchGuardianModel(ModelPart root) {
        super(root);
        this.beltR = root.getChild("belt_r");
        this.mbelt = root.getChild("mbelt");
        this.mbeltL = root.getChild("mbelt_l");
        this.mbeltR = root.getChild("mbelt_r");
        this.beltL = root.getChild("belt_l");
        this.chestplate = root.getChild("chestplate");
        this.hoodEye = root.getChild("hood_eye");
        // NEOFORGE_PORT (26.3): Forge renderToBuffer skipped hood_eye (renderEye unused);
        // the final root render would draw every root child, so hide it.
        this.hoodEye.visible = false;
        this.hood1 = root.getChild("hood1");
        this.hood2 = this.hood1.getChild("hood2");
        this.hood3 = this.hood1.getChild("hood3");
        this.hood4 = this.hood1.getChild("hood4");
        this.backplate = root.getChild("backplate");
        this.shoulderplateTopR = root.getChild("shoulderplate_top_r");
        this.shoulderplateR1 = root.getChild("shoulderplate_r1");
        this.shoulderplateR2 = root.getChild("shoulderplate_r2");
        this.shoulderplateR3 = root.getChild("shoulderplate_r3");
        this.shoulderR = root.getChild("shoulder_r");
        this.armL1 = root.getChild("arm_l1");
        this.armL2 = this.armL1.getChild("arm_l2");
        this.armL3 = this.armL1.getChild("arm_l3");
        this.armR1 = root.getChild("arm_r1");
        this.armR2 = this.armR1.getChild("arm_r2");
        this.armR3 = this.armR1.getChild("arm_r3");
        this.shoulderL = root.getChild("shoulder_l");
        this.shoulderplateLtop = root.getChild("shoulderplate_ltop");
        this.shoulderplateL1 = root.getChild("shoulderplate_l1");
        this.shoulderplateL2 = root.getChild("shoulderplate_l2");
        this.shoulderplateL3 = root.getChild("shoulderplate_l3");
        this.legpanelR4 = root.getChild("legpanel_r4");
        this.legpanelR5 = root.getChild("legpanel_r5");
        this.legpanelR6 = root.getChild("legpanel_r6");
        this.backpanelR1 = root.getChild("backpanel_r1");
        this.backpanelR2 = root.getChild("backpanel_r2");
        this.backpanelR3 = root.getChild("backpanel_r3");
        this.backpanelL3 = root.getChild("backpanel_l3");
        this.legpanelL4 = root.getChild("legpanel_l4");
        this.legpanelL5 = root.getChild("legpanel_l5");
        this.legpanelL6 = root.getChild("legpanel_l6");
        this.backpanelL1 = root.getChild("backpanel_l1");
        this.backpanelL2 = root.getChild("backpanel_l2");
        this.sidepanelL1 = root.getChild("sidepanel_l1");
        this.sidepanelR1 = root.getChild("sidepanel_r1");
        this.sidepanelR2 = root.getChild("sidepanel_r2");
        this.sidepanelR3 = this.sidepanelR2.getChild("sidepanel_r3");
        this.sidepanelR4 = this.sidepanelR3.getChild("sidepanel_r4");
        this.sidepanelL2 = root.getChild("sidepanel_l2");
        this.sidepanelL3 = this.sidepanelL2.getChild("sidepanel_l3");
        this.sidepanelL4 = this.sidepanelL3.getChild("sidepanel_l4");
        this.legpanelC1 = root.getChild("legpanel_c1");
        this.legpanelC2 = this.legpanelC1.getChild("legpanel_c2");
        this.legpanelC3 = this.legpanelC2.getChild("legpanel_c3");
        this.cloak1 = root.getChild("cloak1");
        this.cloak2 = this.cloak1.getChild("cloak2");
        this.cloak3 = this.cloak2.getChild("cloak3");
    }

    private static PartDefinition part(PartDefinition parent, String name, int u, int v,
                                       float x, float y, float z, float w, float h, float d,
                                       float px, float py, float pz, float rx, float ry, float rz,
                                       boolean mirror) {
        CubeListBuilder builder = CubeListBuilder.create().texOffs(u, v)
                .addBox(x, y, z, w, h, d, CubeDeformation.NONE);
        if (mirror) {
            builder.mirror();
        }
        return parent.addOrReplaceChild(name, builder, PartPose.offsetAndRotation(px, py, pz, rx, ry, rz));
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        part(root, "belt_r", 76, 44, -5.0f, 4.0f, -3.0f, 1, 3, 6, 0.0f, -6.0f, 0.0f, 0, 0, 0, true);
        part(root, "mbelt", 56, 55, -4.0f, 8.0f, -3.0f, 8, 4, 1, 0.0f, -6.0f, 0.0f, 0, 0, 0, true);
        part(root, "mbelt_l", 76, 44, 4.0f, 8.0f, -3.0f, 1, 3, 6, 0.0f, -6.0f, 0.0f, 0, 0, 0, true);
        part(root, "mbelt_r", 76, 44, -5.0f, 8.0f, -3.0f, 1, 3, 6, 0.0f, -6.0f, 0.0f, 0, 0, 0, true);
        part(root, "belt_l", 76, 44, 4.0f, 4.0f, -3.0f, 1, 3, 6, 0.0f, -6.0f, 0.0f, 0, 0, 0, true);
        part(root, "chestplate", 56, 45, -4.0f, 1.0f, -4.0f, 8, 7, 2, 0.0f, -6.0f, 0.0f, 0, 0, 0, true);
        part(root, "hood_eye", 0, 0, -4.0f, -8.0f, -4.0f, 8, 8, 8, 0.0f, -6.0f, 0.0f, 0, 0, 0, true);
        PartDefinition hood1 = part(root, "hood1", 40, 12, -4.0f, -8.0f, -4.0f, 8, 8, 8, 0.0f, -6.0f, 0.0f, 0, 0, 0, true);
        part(hood1, "hood2", 36, 28, -3.5f, -8.7f, 2.0f, 7, 7, 3, 0, 0, 0, -0.2268928f, 0, 0, true);
        part(hood1, "hood3", 22, 19, -3.0f, -9.0f, 2.5f, 6, 6, 3, 0, 0, 0, -0.3490659f, 0, 0, true);
        part(hood1, "hood4", 40, 4, -2.5f, -9.7f, 3.5f, 5, 5, 3, 0, 0, 0, -0.5759587f, 0, 0, true);
        part(root, "backplate", 36, 45, -4.0f, 1.0f, 2.0f, 8, 11, 2, 0.0f, -6.0f, 0.0f, 0, 0, 0, true);
        part(root, "shoulderplate_top_r", 110, 37, -5.5f, -2.5f, -3.5f, 2, 1, 7, -5.0f, -4.0f, 0.0f, -0.3665191f, 0.3141593f, 0.4363323f, true);
        part(root, "shoulderplate_r1", 110, 45, 3.5f, -1.5f, -3.5f, 1, 4, 7, 5.0f, -4.0f, 0.0f, -0.3665191f, -0.3141593f, -0.4363323f, true);
        part(root, "shoulderplate_r2", 94, 45, -3.5f, 1.5f, -3.5f, 1, 3, 7, -5.0f, -4.0f, 0.0f, -0.3665191f, 0.3141593f, 0.4363323f, true);
        part(root, "shoulderplate_r3", 94, 45, -2.5f, 3.5f, -3.5f, 1, 3, 7, -5.0f, -4.0f, 0.0f, -0.3665191f, 0.3141593f, 0.4363323f, true);
        part(root, "shoulder_r", 56, 35, -3.5f, -2.5f, -2.5f, 5, 5, 5, -5.0f, -4.0f, 0.0f, -0.3665191f, 0.122173f, 0.0349066f, true);
        PartDefinition armL1 = part(root, "arm_l1", 72, 8, -1.0f, 2.5f, -1.5f, 4, 10, 5, 5.0f, -4.0f, 0.0f, -0.9599311f, -0.1047198f, -0.1919862f, true);
        part(armL1, "arm_l2", 76, 28, -1.0f, 9.5f, 3.5f, 4, 3, 3, 0, 0, 0, 0, 0, 0, true);
        part(armL1, "arm_l3", 76, 23, -1.0f, 6.5f, 3.5f, 4, 3, 2, 0, 0, 0, 0, 0, 0, true);
        PartDefinition armR1 = part(root, "arm_r1", 72, 8, -3.0f, 2.5f, -1.5f, 4, 10, 5, -5.0f, -4.0f, 0.0f, -0.9599311f, 0.1047198f, 0.1919862f, true);
        part(armR1, "arm_r2", 76, 28, -3.0f, 9.5f, 3.5f, 4, 3, 3, 0, 0, 0, 0, 0, 0, true);
        part(armR1, "arm_r3", 76, 23, -3.0f, 6.5f, 3.5f, 4, 3, 2, 0, 0, 0, 0, 0, 0, true);
        part(root, "shoulder_l", 56, 35, -1.5f, -2.5f, -2.5f, 5, 5, 5, 5.0f, -4.0f, 0.0f, -0.3665191f, -0.122173f, -0.0349066f, true);
        part(root, "shoulderplate_ltop", 110, 37, 3.5f, -2.5f, -3.5f, 2, 1, 7, 5.0f, -4.0f, 0.0f, -0.3665191f, -0.3141593f, -0.4363323f, false);
        part(root, "shoulderplate_l1", 110, 45, -4.5f, -1.5f, -3.5f, 1, 4, 7, -5.0f, -4.0f, 0.0f, -0.3665191f, 0.3141593f, 0.4363323f, true);
        part(root, "shoulderplate_l2", 94, 45, 2.5f, 1.5f, -3.5f, 1, 3, 7, 5.0f, -4.0f, 0.0f, -0.3665191f, -0.3141593f, -0.4363323f, false);
        part(root, "shoulderplate_l3", 94, 45, 1.5f, 3.5f, -3.5f, 1, 3, 7, 5.0f, -4.0f, 0.0f, -0.3665191f, -0.3141593f, -0.4363323f, false);
        part(root, "legpanel_r4", 0, 43, -3.0f, 0.5f, -3.5f, 2, 3, 1, -2.0f, 6.0f, 0.0f, -0.4363323f, 0, 0, true);
        part(root, "legpanel_r5", 0, 47, -3.0f, 2.5f, -2.5f, 2, 3, 1, -2.0f, 6.0f, 0.0f, -0.4363323f, 0, 0, true);
        part(root, "legpanel_r6", 6, 43, -3.0f, 4.5f, -1.5f, 2, 3, 1, -2.0f, 6.0f, 0.0f, -0.4363323f, 0, 0, true);
        part(root, "backpanel_r1", 0, 18, -3.0f, 0.5f, 2.5f, 5, 3, 1, -2.0f, 6.0f, 0.0f, 0.4363323f, 0, 0, true);
        part(root, "backpanel_r2", 0, 18, -3.0f, 2.5f, 1.5f, 5, 3, 1, -2.0f, 6.0f, 0.0f, 0.4363323f, 0, 0, true);
        part(root, "backpanel_r3", 0, 18, -3.0f, 4.5f, 0.5f, 5, 3, 1, -2.0f, 6.0f, 0.0f, 0.4363323f, 0, 0, true);
        part(root, "backpanel_l3", 0, 18, -2.0f, 4.5f, 0.5f, 5, 3, 1, 2.0f, 6.0f, 0.0f, 0.4363323f, 0, 0, true);
        part(root, "legpanel_l4", 0, 43, 1.0f, 0.5f, -3.5f, 2, 3, 1, 2.0f, 6.0f, 0.0f, -0.4363323f, 0, 0, false);
        part(root, "legpanel_l5", 0, 47, 1.0f, 2.5f, -2.5f, 2, 3, 1, 2.0f, 6.0f, 0.0f, -0.4363323f, 0, 0, false);
        part(root, "legpanel_l6", 6, 43, 1.0f, 4.5f, -1.5f, 2, 3, 1, 2.0f, 6.0f, 0.0f, -0.4363323f, 0, 0, false);
        part(root, "backpanel_l1", 0, 18, -2.0f, 0.5f, 2.5f, 5, 3, 1, 2.0f, 6.0f, 0.0f, 0.4363323f, 0, 0, true);
        part(root, "backpanel_l2", 0, 18, -2.0f, 2.5f, 1.5f, 5, 3, 1, 2.0f, 6.0f, 0.0f, 0.4363323f, 0, 0, true);
        part(root, "sidepanel_l1", 0, 22, 1.5f, 0.5f, -2.5f, 1, 4, 5, 2.0f, 6.0f, 0.0f, 0, 0, -0.4363323f, true);
        part(root, "sidepanel_r1", 0, 22, -2.5f, 0.5f, -2.5f, 1, 4, 5, -2.0f, 6.0f, 0.0f, 0, 0, 0.4363323f, true);
        PartDefinition sidepanelR2 = part(root, "sidepanel_r2", 0, 54, 0.0f, 0.0f, -0.5f, 1, 5, 5, -4.5f, 9.5f, -2.0f, 0, 0, 0.122173f, true);
        PartDefinition sidepanelR3 = part(sidepanelR2, "sidepanel_r3", 0, 35, 0.0f, 0.0f, -0.5f, 1, 3, 5, 0.0f, 5.0f, 0.0f, 0, 0, 0, true);
        part(sidepanelR3, "sidepanel_r4", 24, 35, 0.0f, 0.0f, -0.5f, 1, 3, 5, 0.0f, 3.0f, 0.0f, 0, 0, 0, true);
        PartDefinition sidepanelL2 = part(root, "sidepanel_l2", 0, 54, 0.0f, 0.0f, -0.5f, 1, 5, 5, 4.5f, 9.5f, -2.0f, 0, 0, -0.122173f, true);
        PartDefinition sidepanelL3 = part(sidepanelL2, "sidepanel_l3", 0, 35, 0.0f, 0.0f, -0.5f, 1, 3, 5, 0.0f, 5.0f, 0.0f, 0, 0, 0, true);
        part(sidepanelL3, "sidepanel_l4", 24, 35, 0.0f, 0.0f, -0.5f, 1, 3, 5, 0.0f, 3.0f, 0.0f, 0, 0, 0, true);
        PartDefinition legpanelC1 = part(root, "legpanel_c1", 16, 45, -3.0f, 0.0f, -0.5f, 6, 8, 1, 0.0f, 5.5f, -3.0f, 0, 0, 0, true);
        PartDefinition legpanelC2 = part(legpanelC1, "legpanel_c2", 16, 54, -3.0f, 0.0f, -0.5f, 6, 4, 1, 0.0f, 8.0f, 0.0f, 0, 0, 0, true);
        part(legpanelC2, "legpanel_c3", 32, 59, -3.0f, 0.0f, -0.5f, 6, 4, 1, 0.0f, 4.0f, 0.0f, 0, 0, 0, true);
        PartDefinition cloak1 = part(root, "cloak1", 106, 0, 0.0f, 0.0f, -0.5f, 10, 18, 1, -5.0f, -6.0f, 4.0f, 0, 0, 0, true);
        PartDefinition cloak2 = part(cloak1, "cloak2", 106, 19, 0.0f, 0.0f, -0.5f, 10, 4, 1, 0.0f, 18.0f, 0.0f, 0, 0, 0, true);
        part(cloak2, "cloak3", 106, 24, 0.0f, 0.0f, -0.5f, 10, 4, 1, 0.0f, 4.0f, 0.0f, 0, 0, 0, true);
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(GuardianState state) {
        super.setupAnim(state);
        float ageInTicks = state.ageInTicks;
        float netHeadYaw = state.yRot;
        float headPitch = state.xRot;
        this.hood1.yRot = netHeadYaw / 57.295776f;
        this.hood1.xRot = headPitch / 57.295776f;
        this.hoodEye.xRot = this.hood1.xRot;
        this.hoodEye.yRot = this.hood1.yRot;
        float alr = state.armLiftR;
        float all = state.armLiftL;
        this.armL1.xRot = -1.0f - all + Mth.sin((ageInTicks + 20.0f) / 10.0f) * 0.08f;
        this.armR1.xRot = -1.0f - alr + Mth.sin(ageInTicks / 10.0f) * 0.08f;
        this.legpanelC1.xRot = -0.15f + Mth.sin(ageInTicks / 8.0f) * 0.12f;
        this.legpanelC2.xRot = Mth.sin((ageInTicks - 5.0f) / 8.0f) * 0.13f;
        this.legpanelC3.xRot = Mth.sin((ageInTicks - 10.0f) / 8.0f) * 0.14f;
        this.cloak1.xRot = 0.2f + Mth.sin(ageInTicks / 7.0f) * 0.08f;
        this.cloak2.xRot = Mth.sin((ageInTicks - 5.0f) / 7.0f) * 0.1f;
        this.cloak3.xRot = Mth.sin((ageInTicks - 10.0f) / 7.0f) * 0.12f;
        this.sidepanelL2.zRot = -0.2f + Mth.sin((ageInTicks + 10.0f) / 8.0f) * 0.12f;
        this.sidepanelL3.zRot = Mth.sin((ageInTicks + 5.0f) / 8.0f) * 0.13f;
        this.sidepanelL4.zRot = Mth.sin(ageInTicks / 8.0f) * 0.14f;
        this.sidepanelR2.zRot = 0.2f + Mth.sin((ageInTicks - 5.0f) / 8.0f) * 0.12f;
        this.sidepanelR3.zRot = Mth.sin((ageInTicks - 10.0f) / 8.0f) * 0.13f;
        this.sidepanelR4.zRot = Mth.sin((ageInTicks - 15.0f) / 8.0f) * 0.14f;
    }

    private void renderPart(ModelPart part, PoseStack poseStack, VertexConsumer consumer,
                            int packedLight, int packedOverlay, float r, float g, float b, float a) {
        part.render(poseStack, consumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(a, r, g, b));
    }

    /**
     * Общий множитель альфы вершин, выставляется рендерером перед рендером.
     * Дистанционный фейд гардиана (0.6 → 0) идёт через него — рендер-тайп
     * entityTranslucent уважает вершинную альфу (как призрачная слизь).
     */
    public float renderAlpha = 1.0f;

    // NEOFORGE_PORT: @Override dropped (base renderToBuffer final, tint int).
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        float r = red, g = green, b = blue, a = alpha * renderAlpha;
        renderPart(this.beltR, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.mbelt, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.mbeltL, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.mbeltR, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.beltL, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.chestplate, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.hood1, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.backplate, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.shoulderplateTopR, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.shoulderplateR1, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.shoulderplateR2, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.shoulderplateR3, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.shoulderR, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.armL1, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.armR1, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.shoulderL, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.shoulderplateLtop, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.shoulderplateL1, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.shoulderplateL2, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.shoulderplateL3, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.legpanelR4, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.legpanelR5, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.legpanelR6, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.backpanelR1, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.backpanelR2, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.backpanelR3, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.backpanelL3, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.legpanelL4, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.legpanelL5, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.legpanelL6, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.backpanelL1, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.backpanelL2, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.cloak1, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.sidepanelR1, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.sidepanelL1, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.sidepanelL2, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.sidepanelR2, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
        renderPart(this.legpanelC1, poseStack, vertexConsumer, packedLight, packedOverlay, r, g, b, a);
    }

    public void renderEye(PoseStack poseStack, VertexConsumer consumer, int packedOverlay) {
        this.hoodEye.render(poseStack, consumer, 0xF000F0, packedOverlay, -1);
    }
}