package arcaneresearch.client.models.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.entities.monster.tainted.EntityTaintSeed;
@OnlyIn(Dist.CLIENT)
public class TaintSeedModel<T extends EntityTaintSeed> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(Arcaneresearch.MODID, "taint_seed"), "main");
    private static final int SEGMENTS = 7;
    private final ModelPart tentacle;
    private final ModelPart[] tents = new ModelPart[SEGMENTS];
    private float age;
    public TaintSeedModel(ModelPart root) {
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
            part.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
            childToHide.visible = visible;
        } else {
            part.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.age = ageInTicks;
        float ht = entity.hurtTime / 200.0f;
        float attackAnim = entity.attackAnim;
        float mod = 0.0625f * 0.2f;
        float fi = (0.1f - mod) * 3.0f;
        for (int k = 0; k < SEGMENTS; k++) {
            this.tents[k].xRot = 0.1f / fi * Mth.sin(ageInTicks * 0.06f - k / 2.0f) / 5.0f
                    + ht + attackAnim;
            this.tents[k].zRot = 0.1f / fi * Mth.sin(ageInTicks * 0.05f - k / 2.0f) / 5.0f;
        }
    }
    @Override
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
