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
import arcaneresearch.client.renderers.entity.FireBatRenderer;

// NEOFORGE_PORT (26.3): EntityModel<State>; hanging via renderer State.
public class FireBatModel extends EntityModel<FireBatRenderer.State> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "fire_bat"), "main");

    private final ModelPart batHead;
    private final ModelPart ear1;
    private final ModelPart ear2;
    private final ModelPart batBody;
    private final ModelPart batRightWing;
    private final ModelPart batOuterRightWing;
    private final ModelPart batLeftWing;
    private final ModelPart batOuterLeftWing;

    public FireBatModel(ModelPart root) {
        super(root);
        this.batHead = root.getChild("head");
        this.ear1 = this.batHead.getChild("ear1");
        this.ear2 = this.batHead.getChild("ear2");
        this.batBody = root.getChild("body");
        this.batRightWing = this.batBody.getChild("right_wing");
        this.batOuterRightWing = this.batRightWing.getChild("outer_right_wing");
        this.batLeftWing = this.batBody.getChild("left_wing");
        this.batOuterLeftWing = this.batLeftWing.getChild("outer_left_wing");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        head.addOrReplaceChild("ear1",
                CubeListBuilder.create()
                        .texOffs(24, 0)
                        .addBox(-4.0F, -6.0F, -2.0F, 3.0F, 4.0F, 1.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        head.addOrReplaceChild("ear2",
                CubeListBuilder.create()
                        .texOffs(24, 0).mirror()
                        .addBox(1.0F, -6.0F, -2.0F, 3.0F, 4.0F, 1.0F, CubeDeformation.NONE),
                PartPose.ZERO);

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-3.0F, 4.0F, -3.0F, 6.0F, 12.0F, 6.0F, CubeDeformation.NONE)
                        .texOffs(0, 34)
                        .addBox(-5.0F, 16.0F, 0.0F, 10.0F, 6.0F, 1.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        PartDefinition rightWing = body.addOrReplaceChild("right_wing",
                CubeListBuilder.create()
                        .texOffs(42, 0)
                        .addBox(-12.0F, 1.0F, 1.5F, 10.0F, 16.0F, 1.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        rightWing.addOrReplaceChild("outer_right_wing",
                CubeListBuilder.create()
                        .texOffs(24, 16)
                        .addBox(-8.0F, 1.0F, 0.0F, 8.0F, 12.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offset(-12.0F, 1.0F, 1.5F));
        PartDefinition leftWing = body.addOrReplaceChild("left_wing",
                CubeListBuilder.create()
                        .texOffs(42, 0).mirror()
                        .addBox(2.0F, 1.0F, 1.5F, 10.0F, 16.0F, 1.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        leftWing.addOrReplaceChild("outer_left_wing",
                CubeListBuilder.create()
                        .texOffs(24, 16).mirror()
                        .addBox(0.0F, 1.0F, 0.0F, 8.0F, 12.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offset(12.0F, 1.0F, 1.5F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(FireBatRenderer.State state) {
        super.setupAnim(state);
        float ageInTicks = state.ageInTicks;
        float netHeadYaw = state.yRot;
        float headPitch = state.xRot;
        if (state.hanging) {
            this.batHead.xRot = headPitch / 57.295776f;
            this.batHead.yRot = (float) Math.PI - netHeadYaw / 57.295776f;
            this.batHead.zRot = (float) Math.PI;
            this.batHead.setPos(0.0F, -2.0F, 0.0F);
            this.batRightWing.setPos(-3.0F, 0.0F, 3.0F);
            this.batLeftWing.setPos(3.0F, 0.0F, 3.0F);
            this.batBody.xRot = (float) Math.PI;
            this.batRightWing.setRotation(-0.15707964f, -1.2566371f, 0.0f);
            this.batOuterRightWing.yRot = -1.7278761f;
            this.batLeftWing.xRot = this.batRightWing.xRot;
            this.batLeftWing.yRot = -this.batRightWing.yRot;
            this.batOuterLeftWing.yRot = -this.batOuterRightWing.yRot;
        } else {
            this.batHead.xRot = headPitch / 57.295776f;
            this.batHead.yRot = netHeadYaw / 57.295776f;
            this.batHead.zRot = 0.0f;
            this.batHead.setPos(0.0F, 0.0F, 0.0F);
            this.batRightWing.setPos(0.0F, 0.0F, 0.0F);
            this.batLeftWing.setPos(0.0F, 0.0F, 0.0F);
            this.batBody.xRot = 0.7853982f + Mth.cos(ageInTicks * 0.1f) * 0.15f;
            this.batBody.yRot = 0.0f;
            this.batRightWing.yRot = Mth.cos(ageInTicks * 1.3f) * (float) Math.PI * 0.25f;
            this.batLeftWing.yRot = -this.batRightWing.yRot;
            this.batOuterRightWing.yRot = this.batRightWing.yRot * 0.5f;
            this.batOuterLeftWing.yRot = -this.batRightWing.yRot * 0.5f;
        }
    }

    // NEOFORGE_PORT: base Model.renderToBuffer (final, root part) draws head +
    // body; the old 8-float overload is removed (same coverage).
}