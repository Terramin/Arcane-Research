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
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.entity.EldritchCrabRenderer;

// NEOFORGE_PORT (26.3): EntityModel<State>; helm flag via State.
public class EldritchCrabModel extends EntityModel<EldritchCrabRenderer.State> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "eldritch_crab"), "main");

    private final ModelPart tailHelm;
    private final ModelPart tailBare;
    private final ModelPart rClaw1;
    private final ModelPart head1;
    private final ModelPart rClaw0;
    private final ModelPart rClaw2;
    private final ModelPart lClaw2;
    private final ModelPart lClaw1;
    private final ModelPart rArm;
    private final ModelPart torso;
    private final ModelPart head0;
    private final ModelPart rrLeg1;
    private final ModelPart rfLeg1;
    private final ModelPart lrLeg1;
    private final ModelPart lfLeg1;
    private final ModelPart rrLeg0;
    private final ModelPart rfLeg0;
    private final ModelPart lfLeg0;
    private final ModelPart lrLeg0;
    private final ModelPart lClaw0;
    private final ModelPart lArm;

    private boolean showHelm = true;

    public EldritchCrabModel(ModelPart root) {
        super(root);
        this.tailHelm = root.getChild("tail_helm");
        this.tailBare = root.getChild("tail_bare");
        this.rClaw1 = root.getChild("r_claw1");
        this.head1 = root.getChild("head1");
        this.rClaw0 = root.getChild("r_claw0");
        this.rClaw2 = root.getChild("r_claw2");
        this.lClaw2 = root.getChild("l_claw2");
        this.lClaw1 = root.getChild("l_claw1");
        this.rArm = root.getChild("r_arm");
        this.torso = root.getChild("torso");
        this.head0 = root.getChild("head0");
        this.rrLeg1 = root.getChild("rr_leg1");
        this.rfLeg1 = root.getChild("rf_leg1");
        this.lrLeg1 = root.getChild("lr_leg1");
        this.lfLeg1 = root.getChild("lf_leg1");
        this.rrLeg0 = root.getChild("rr_leg0");
        this.rfLeg0 = root.getChild("rf_leg0");
        this.lfLeg0 = root.getChild("lf_leg0");
        this.lrLeg0 = root.getChild("lr_leg0");
        this.lClaw0 = root.getChild("l_claw0");
        this.lArm = root.getChild("l_arm");
    }

    private static PartDefinition part(PartDefinition root, String name, int u, int v,
                                       float x, float y, float z, float w, float h, float d,
                                       float px, float py, float pz, float rx, float ry, float rz,
                                       boolean mirror) {
        CubeListBuilder builder = CubeListBuilder.create().texOffs(u, v)
                .addBox(x, y, z, w, h, d, CubeDeformation.NONE);
        if (mirror) {
            builder.mirror();
        }
        return root.addOrReplaceChild(name, builder, PartPose.offsetAndRotation(px, py, pz, rx, ry, rz));
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        part(root, "tail_helm", 0, 0, -4.5f, -4.5f, -0.4f, 9, 9, 9, 0.0f, 18.0f, 0.0f, 0.1047198f, 0.0f, 0.0f, false);
        part(root, "tail_bare", 64, 0, -4.0f, -4.0f, -0.4f, 8, 8, 8, 0.0f, 18.0f, 0.0f, 0.1047198f, 0.0f, 0.0f, false);
        part(root, "r_claw1", 0, 47, -2.0f, -1.0f, -5.066667f, 4, 3, 5, -6.0f, 15.5f, -10.0f, 0.0f, 0.0f, 0.0f, false);
        part(root, "head1", 0, 38, -2.0f, -1.5f, -9.066667f, 4, 4, 1, 0.0f, 18.0f, 0.0f, 0.0f, 0.0f, 0.0f, false);
        part(root, "r_claw0", 0, 55, -2.0f, -2.5f, -3.066667f, 4, 5, 3, -6.0f, 17.0f, -7.0f, 0.0f, 0.0f, 0.0f, false);
        part(root, "r_claw2", 14, 54, -1.5f, -1.0f, -4.066667f, 3, 2, 5, -6.0f, 18.5f, -10.0f, 0.3141593f, 0.0f, 0.0f, false);
        part(root, "r_arm", 44, 4, -1.0f, -1.0f, -5.066667f, 2, 2, 6, -3.0f, 17.0f, -4.0f, 0.0f, 0.7504916f, 0.0f, false);
        part(root, "l_claw2", 14, 54, -1.5f, -1.0f, -4.066667f, 3, 2, 5, 6.0f, 18.5f, -10.0f, 0.3141593f, 0.0f, 0.0f, false);
        part(root, "l_claw1", 0, 47, -2.0f, -1.0f, -5.066667f, 4, 3, 5, 6.0f, 15.5f, -10.0f, 0.0f, 0.0f, 0.0f, true);
        part(root, "l_claw0", 0, 55, -2.0f, -2.5f, -3.066667f, 4, 5, 3, 6.0f, 17.0f, -7.0f, 0.0f, 0.0f, 0.0f, true);
        part(root, "l_arm", 44, 4, -1.0f, -1.0f, -4.066667f, 2, 2, 6, 4.0f, 17.0f, -5.0f, 0.0f, -0.7504916f, 0.0f, false);
        part(root, "torso", 0, 18, -3.5f, -3.5f, -6.066667f, 7, 7, 6, 0.0f, 18.0f, 0.0f, 0.0523599f, 0.0f, 0.0f, false);
        part(root, "head0", 0, 31, -2.5f, -2.0f, -8.066667f, 5, 5, 2, 0.0f, 18.0f, 0.0f, 0.0f, 0.0f, 0.0f, false);
        part(root, "rr_leg1", 36, 4, -4.5f, 1.0f, -0.9f, 2, 5, 2, -4.0f, 20.0f, -1.5f, 0.0f, 0.0f, 0.0f, false);
        part(root, "rf_leg1", 36, 4, -5.0f, 1.0f, -1.066667f, 2, 5, 2, -4.0f, 20.0f, -3.5f, 0.0f, 0.0f, 0.0f, false);
        part(root, "lr_leg1", 36, 4, 2.5f, 1.0f, -0.9f, 2, 5, 2, 4.0f, 20.0f, -1.5f, 0.0f, 0.0f, 0.0f, false);
        part(root, "lf_leg1", 36, 4, 3.0f, 1.0f, -1.066667f, 2, 5, 2, 4.0f, 20.0f, -3.5f, 0.0f, 0.0f, 0.0f, false);
        part(root, "rr_leg0", 36, 0, -4.5f, -1.0f, -0.9f, 6, 2, 2, -4.0f, 20.0f, -1.5f, 0.0f, 0.0f, 0.0f, false);
        part(root, "rf_leg0", 36, 0, -5.0f, -1.0f, -1.066667f, 6, 2, 2, -4.0f, 20.0f, -3.5f, 0.0f, 0.0f, 0.0f, false);
        part(root, "lf_leg0", 36, 0, -1.0f, -1.0f, -1.066667f, 6, 2, 2, 4.0f, 20.0f, -3.5f, 0.0f, 0.0f, 0.0f, false);
        part(root, "lr_leg0", 36, 0, -1.5f, -1.0f, -0.9f, 6, 2, 2, 4.0f, 20.0f, -1.5f, 0.0f, 0.0f, 0.0f, false);
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static void setRot(ModelPart part, float x, float y, float z) {
        part.xRot = x;
        part.yRot = y;
        part.zRot = z;
    }

    @Override
    public void setupAnim(EldritchCrabRenderer.State state) {
        super.setupAnim(state);
        float limbSwing = state.walkAnimationPos;
        float limbSwingAmount = state.walkAnimationSpeed;
        float ageInTicks = state.ageInTicks;
        this.showHelm = state.helm;
        // NEOFORGE_PORT (26.3): the 8-float renderToBuffer (helm XOR bare tail) is dead;
        // the final root render draws every visible part, so toggle visibility here.
        this.tailHelm.visible = state.helm;
        this.tailBare.visible = !state.helm;
        setRot(this.rrLeg1, 0.0f, 0.2094395f, 0.4363323f);
        setRot(this.rfLeg1, 0.0f, -0.2094395f, 0.4363323f);
        setRot(this.lrLeg1, 0.0f, -0.2094395f, -0.4363323f);
        setRot(this.lfLeg1, 0.0f, 0.2094395f, -0.4363323f);
        setRot(this.rrLeg0, 0.0f, 0.2094395f, 0.4363323f);
        setRot(this.rfLeg0, 0.0f, -0.2094395f, 0.4363323f);
        setRot(this.lfLeg0, 0.0f, 0.2094395f, -0.4363323f);
        setRot(this.lrLeg0, 0.0f, -0.2094395f, -0.4363323f);
        float f9 = -(Mth.cos(limbSwing * 0.6662f * 2.0f) * 0.4f) * limbSwingAmount;
        float f10 = -(Mth.cos(limbSwing * 0.6662f * 2.0f + (float) Math.PI) * 0.4f) * limbSwingAmount;
        this.rrLeg1.yRot += f9;
        this.rrLeg0.yRot += f9;
        this.lrLeg1.yRot += -f9;
        this.lrLeg0.yRot += -f9;
        this.rfLeg1.yRot += f10;
        this.rfLeg0.yRot += f10;
        this.lfLeg1.yRot += -f10;
        this.lfLeg0.yRot += -f10;
        this.rrLeg1.zRot += f9;
        this.rrLeg0.zRot += f9;
        this.lrLeg1.zRot += -f9;
        this.lrLeg0.zRot += -f9;
        this.rfLeg1.zRot += f10;
        this.rfLeg0.zRot += f10;
        this.lfLeg1.zRot += -f10;
        this.lfLeg0.zRot += -f10;
        float n = Mth.cos(limbSwing * 0.6662f) * 2.0f * limbSwingAmount * 0.125f;
        this.tailHelm.yRot = n;
        this.tailBare.yRot = n;
        float n2 = Mth.cos(limbSwing * 0.6662f) * limbSwingAmount * 0.125f;
        this.tailHelm.zRot = n2;
        this.tailBare.zRot = n2;
        float ticks = ageInTicks;
        this.rClaw2.xRot = 0.3141593f - Mth.sin(ticks / 4.0f) * 0.25f;
        this.lClaw2.xRot = 0.3141593f + Mth.sin(ticks / 4.1f) * 0.25f;
        this.rClaw1.xRot = Mth.sin(ticks / 4.0f) * 0.125f;
        this.lClaw1.xRot = -Mth.sin(ticks / 4.1f) * 0.125f;
    }

    // NEOFORGE_PORT: @Override dropped (base renderToBuffer final, tint int).
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        if (this.showHelm) {
            this.tailHelm.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        } else {
            this.tailBare.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        }
        this.rfLeg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.rClaw1.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.head1.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.rClaw0.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.rClaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.lClaw2.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.lClaw1.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.rArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.torso.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.rrLeg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.head0.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.lrLeg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.lfLeg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.rrLeg0.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.rfLeg0.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.lfLeg0.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.lrLeg0.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.lClaw0.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        this.lArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
    }
}