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
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.renderers.entity.TaintacleRenderer;
import arcaneresearch.common.entities.monster.tainted.EntityTaintacle;

// NEOFORGE_PORT (26.3): EntityModel<State>; tip/anim inputs via renderer State
// (chainLengthFor runs in extract). Tip constants visible for the renderer.
public class TaintacleModel extends EntityModel<TaintacleRenderer.State> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "taintacle"), "main");

    private static final int MAX_SEGMENTS = 13;

    public static final int SMALL_TIP = 4;
    public static final int NORMAL_TIP = 8;
    public static final int GIANT_TIP = 12;

    private final ModelPart base;
    private final ModelPart[] segments = new ModelPart[MAX_SEGMENTS];
    private final ModelPart[] orbs = new ModelPart[MAX_SEGMENTS];
    private final ModelPart[] heads = new ModelPart[MAX_SEGMENTS];
    private int visibleTip = NORMAL_TIP;
    private int visibleAnchor = NORMAL_TIP;

    public TaintacleModel(ModelPart root) {
        super(root);
        this.base = root.getChild("base");
        ModelPart current = this.base;
        for (int i = 0; i < MAX_SEGMENTS; i++) {
            current = current.getChild("segment" + i);
            this.segments[i] = current;
        }
        for (int tip : new int[]{SMALL_TIP, NORMAL_TIP, GIANT_TIP}) {
            this.orbs[tip] = this.segments[tip].getChild("tip_orb");
            this.heads[tip] = this.segments[tip].getChild("head");
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition base = partdefinition.addOrReplaceChild("base",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 12.0F, 0.0F));

        PartDefinition currentParent = base;
        for (int i = 0; i < MAX_SEGMENTS; i++) {
            currentParent = currentParent.addOrReplaceChild("segment" + i,
                    CubeListBuilder.create()
                            .texOffs(0, 16)
                            .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, CubeDeformation.NONE),
                    PartPose.offset(0.0F, -8.0F, 0.0F));
        }

        for (int tip : new int[]{SMALL_TIP, NORMAL_TIP, GIANT_TIP}) {
            PartDefinition anchor = findSegment(partdefinition, tip);
            anchor.addOrReplaceChild("tip_orb",
                    CubeListBuilder.create()
                            .texOffs(0, 56)
                            .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, CubeDeformation.NONE),
                    PartPose.offset(0.0F, -8.0F, 0.0F));
            anchor.addOrReplaceChild("head",
                    CubeListBuilder.create()
                            .texOffs(0, 32)
                            .addBox(-6.0F, -6.0F, -6.0F, 12.0F, 12.0F, 12.0F, CubeDeformation.NONE),
                    PartPose.offset(0.0F, -8.0F, 0.0F));
        }

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    private static PartDefinition findSegment(PartDefinition root, int index) {

        PartDefinition current = root.getChild("base");
        for (int i = 0; i <= index; i++) {
            current = current.getChild("segment" + i);
        }
        return current;
    }

    public static int chainLengthFor(Entity entity) {
        if (entity instanceof EntityTaintacle taintacle) {
            return Math.max(2, Math.min(MAX_SEGMENTS + 1, taintacle.getLength()));
        }
        return NORMAL_TIP + 2;
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

    private static void renderOwnTip(ModelPart part, ModelPart next, ModelPart orb, ModelPart head,
                                     PoseStack poseStack, VertexConsumer vertexConsumer,
                                     int packedLight, int packedOverlay,
                                     float red, float green, float blue, float alpha) {
        boolean nextVis = next != null && next.visible;
        boolean orbVis = orb != null && orb.visible;
        boolean headVis = head != null && head.visible;
        if (next != null) next.visible = false;
        if (orb != null) orb.visible = false;
        if (head != null) head.visible = false;
        part.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        if (next != null) next.visible = nextVis;
        if (orb != null) orb.visible = orbVis;
        if (head != null) head.visible = headVis;
    }

    @Override
    public void setupAnim(TaintacleRenderer.State state) {
        super.setupAnim(state);
        float ageInTicks = state.ageInTicks;
        int tipIndex = state.tipIndex;
        int anchor = state.anchor;
        this.visibleTip = tipIndex;
        this.visibleAnchor = anchor;

        for (int i = 0; i < MAX_SEGMENTS; i++) {
            this.segments[i].visible = i <= tipIndex;
        }
        for (int tip : new int[]{SMALL_TIP, NORMAL_TIP, GIANT_TIP}) {
            boolean show = tip == anchor && tip == tipIndex;
            this.orbs[tip].visible = show;
            this.heads[tip].visible = show;
        }

        float flail = state.flail;
        float ht = state.hurt;
        float mod = 0.0625f * 0.2f;
        float fs = (flail > 1.0f) ? 3.0f : (1.0f - mod);
        float fi = flail + ((ht > 0.0f) ? mod : (-mod));

        this.base.xRot = 0.0f;
        // NEOFORGE_PORT: base submit renders root() directly (the old recursive
        // renderToBuffer has no submit path); the per-level 0.88 shrink moves
        // into part scales (same compounding as the pose-stack version).
        // NEOFORGE_PORT (26.3): Forge scaled before each child's translate, so the
        // offsets shrink by 0.88 too (part scale applies only after the offset).
        for (int i = 0; i < MAX_SEGMENTS; i++) {
            this.segments[i].xScale = 0.88F;
            this.segments[i].yScale = 0.88F;
            this.segments[i].zScale = 0.88F;
            shrinkOffset(this.segments[i]);
        }
        for (int tip : new int[]{SMALL_TIP, NORMAL_TIP, GIANT_TIP}) {
            this.orbs[tip].xScale = 0.88F;
            this.orbs[tip].yScale = 0.88F;
            this.orbs[tip].zScale = 0.88F;
            this.heads[tip].xScale = 0.88F;
            this.heads[tip].yScale = 0.88F;
            this.heads[tip].zScale = 0.88F;
            shrinkOffset(this.orbs[tip]);
            shrinkOffset(this.heads[tip]);
        }
        for (int k = 0; k <= tipIndex; k++) {
            this.segments[k].xRot = 0.15f * fi * Mth.sin(ageInTicks * 0.1f * fs - k / 2.0f);
            this.segments[k].zRot = 0.1f / fi * Mth.sin(ageInTicks * 0.15f - k / 2.0f);
        }
    }

    // NEOFORGE_PORT: custom recursive segment rendering kept (base root render
    // would double-draw); old @Override dropped (base is final with tint int).
    // The 0.88 shrink now also lives in setupAnim part scales, so base submit
    // draws the same shape.
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {

        int tipIndex = this.visibleTip;
        int anchor = this.visibleAnchor;
        if (tipIndex < 0) {
            this.base.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
            return;
        }

        renderOwn(this.base, this.segments[0],
                poseStack, vertexConsumer, packedLight, packedOverlay,
                red, green, blue, alpha);

        poseStack.pushPose();
        this.base.translateAndRotate(poseStack);
        poseStack.scale(0.88F, 0.88F, 0.88F);
        for (int k = 0; k <= tipIndex; k++) {
            boolean isTip = k == tipIndex;
            boolean isAnchor = k == anchor;
            if (isTip && isAnchor) {

                renderOwnTip(this.segments[k], k + 1 <= tipIndex ? this.segments[k + 1] : null,
                        this.orbs[anchor], this.heads[anchor],
                        poseStack, vertexConsumer, packedLight, packedOverlay,
                        red, green, blue, alpha);

                poseStack.pushPose();
                this.segments[k].translateAndRotate(poseStack);
                poseStack.scale(0.88F, 0.88F, 0.88F);
                this.orbs[anchor].render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
                this.heads[anchor].render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
                poseStack.popPose();
            } else {
                renderOwn(this.segments[k], k + 1 <= tipIndex ? this.segments[k + 1] : null,
                        poseStack, vertexConsumer, packedLight, packedOverlay,
                        red, green, blue, alpha);
            }
            if (!isTip) {

                this.segments[k].translateAndRotate(poseStack);
                poseStack.scale(0.88F, 0.88F, 0.88F);
            }
        }
        poseStack.popPose();
    }

    private static void shrinkOffset(ModelPart part) {
        part.x *= 0.88F;
        part.y *= 0.88F;
        part.z *= 0.88F;
    }

    public void renderWithAlpha(PoseStack poseStack, VertexConsumer vertexConsumer,
                                int packedLight, int packedOverlay, float alpha) {

        renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay,
                0.88F, 0.88F, 0.88F, alpha);
    }
}