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
import arcaneresearch.client.renderers.entity.TaintSeedRenderer;

// NEOFORGE_PORT (26.3): EntityModel<State>; hurt/attack/age via renderer State.
public class TaintSeedModel extends EntityModel<TaintSeedRenderer.State> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "taint_seed"), "main");

    private static final int SEGMENTS = 7;

    private final ModelPart tentacle;
    private final ModelPart[] tents = new ModelPart[SEGMENTS];
    private float age;

    public TaintSeedModel(ModelPart root) {
        super(root);
        this.tentacle = root.getChild("tentacle");
        ModelPart current = this.tentacle;
        for (int k = 0; k < SEGMENTS; k++) {
            current = current.getChild("tent" + k);
            this.tents[k] = current;
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition tentacle = partdefinition.addOrReplaceChild("tentacle",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition currentParent = tentacle;
        for (int k = 0; k < SEGMENTS; k++) {
            int texV;
            float size;
            float yOff;
            if (k < SEGMENTS - 3) {
                texV = 16;
                size = 8.0F;
                yOff = -8.0F;
            } else if (k == SEGMENTS - 3) {
                texV = 48;
                size = 4.0F;
                yOff = -8.0F;
            } else {
                texV = 56;
                size = 4.0F;
                yOff = -4.0F;
            }
            float half = size / 2.0F;
            currentParent = currentParent.addOrReplaceChild("tent" + k,
                    CubeListBuilder.create()
                            .texOffs(0, texV)
                            .addBox(-half, -half, -half, size, size, size, CubeDeformation.NONE),
                    PartPose.offset(0.0F, yOff, 0.0F));
        }

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    private static void renderOwn(ModelPart part, ModelPart childToHide, PoseStack poseStack,
                                  VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                                  float red, float green, float blue, float alpha) {
        if (childToHide != null) {
            boolean visible = childToHide.visible;
            childToHide.visible = false;
            part.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
            childToHide.visible = visible;
        } else {
            part.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        }
    }

    @Override
    public void setupAnim(TaintSeedRenderer.State state) {
        super.setupAnim(state);
        float ageInTicks = state.ageInTicks;

        this.age = ageInTicks;
        float ht = state.hurtAmount;
        float attackAnim = state.attackPhase;
        float mod = 0.0625f * 0.2f;
        float fi = (0.1f - mod) * 3.0f;
        // NEOFORGE_PORT: base submit renders root() directly (the old recursive
        // renderToBuffer has no submit path); the per-level applySeedScale pulse
        // moves into part scales (same compounding as the pose-stack version).
        this.tentacle.xScale = 1.0F;
        this.tentacle.yScale = 1.0F;
        this.tentacle.zScale = 1.0F;
        for (int k = 0; k < SEGMENTS; k++) {
            this.tents[k].xRot = 0.1f / fi * Mth.sin(ageInTicks * 0.06f - k / 2.0f) / 5.0f
                    + ht + attackAnim;
            this.tents[k].zRot = 0.1f / fi * Mth.sin(ageInTicks * 0.05f - k / 2.0f) / 5.0f;
            float qq = (float) (Math.PI * (k / 7.0f));
            float s = 1.6f - Mth.sin(qq);
            float pulse = Mth.sin(ageInTicks / 12.0f - qq) * 0.33f;
            pulse *= pulse;
            this.tents[k].xScale = s + pulse;
            this.tents[k].yScale = s * 0.9f;
            this.tents[k].zScale = s + pulse;
            // NEOFORGE_PORT (26.3): Forge scaled before the child's translate, so the
            // segment offset is scaled too (part scale applies only after it).
            this.tents[k].y *= s * 0.9f;
        }
    }

    // NEOFORGE_PORT: custom recursive tentacle rendering kept (base root render
    // would double-draw); old @Override dropped (base is final with tint int).
    // Called by renderWithAlpha below. The applySeedScale pulse now also lives
    // in setupAnim part scales, so base submit draws the same shape.
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {

        renderOwn(this.tentacle, this.tents[0], poseStack, vertexConsumer,
                packedLight, packedOverlay, red, green, blue, alpha);
        poseStack.pushPose();
        this.tentacle.translateAndRotate(poseStack);
        applySeedScale(poseStack, 0);
        for (int k = 0; k < SEGMENTS; k++) {
            renderOwn(this.tents[k], k + 1 < SEGMENTS ? this.tents[k + 1] : null,
                    poseStack, vertexConsumer, packedLight, packedOverlay,
                    red, green, blue, alpha);
            if (k + 1 < SEGMENTS) {
                this.tents[k].translateAndRotate(poseStack);
                applySeedScale(poseStack, k + 1);
            }
        }
        poseStack.popPose();
    }

    private void applySeedScale(PoseStack poseStack, int depth) {
        float qq = (float) (Math.PI * (depth / 7.0f));
        float s = 1.6f - Mth.sin(qq);
        float pulse = Mth.sin(this.age / 12.0f - qq) * 0.33f;
        pulse *= pulse;
        poseStack.scale(s + pulse, s * 0.9f, s + pulse);
    }

    public void renderWithAlpha(PoseStack poseStack, VertexConsumer vertexConsumer,
                                int packedLight, int packedOverlay, float alpha) {
        renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay,
                0.82F, 0.82F, 0.82F, alpha);
    }
}