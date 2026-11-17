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
import arcaneresearch.common.entities.construct.EntityTurretCrossbow;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class CrossbowModel extends EntityModel<EntityTurretCrossbow> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "turret_crossbow"), "main");
    private final ModelPart crossbow;
    private final ModelPart tripod;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart crossl1;
    private final ModelPart crossl2;
    private final ModelPart crossl3;
    private final ModelPart crossr1;
    private final ModelPart crossr2;
    private final ModelPart crossr3;
    private final ModelPart loadbarcross;
    private final ModelPart loadbarl;
    private final ModelPart loadbarr;
    public CrossbowModel(ModelPart root) {
        this.crossbow = root.getChild("crossbow");
        this.tripod = root.getChild("tripod");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
        this.crossl1 = crossbow.getChild("crossl1");
        this.crossl2 = crossbow.getChild("crossl2");
        this.crossl3 = crossbow.getChild("crossl3");
        this.crossr1 = crossbow.getChild("crossr1");
        this.crossr2 = crossbow.getChild("crossr2");
        this.crossr3 = crossbow.getChild("crossr3");
        this.loadbarcross = crossbow.getChild("loadbarcross");
        this.loadbarl = crossbow.getChild("loadbarl");
        this.loadbarr = crossbow.getChild("loadbarr");
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("tripod",
                CubeListBuilder.create()
                        .texOffs(13, 0)
                        .mirror()
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild("leg1",
                CubeListBuilder.create()
                        .texOffs(20, 10)
                        .mirror()
                        .addBox(-1.0F, 1.0F, -1.0F, 2.0F, 13.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.5236F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("leg2",
                CubeListBuilder.create()
                        .texOffs(20, 10)
                        .mirror()
                        .addBox(-1.0F, 1.0F, -1.0F, 2.0F, 13.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.5236F, 1.5708F, 0.0F));
        partdefinition.addOrReplaceChild("leg3",
                CubeListBuilder.create()
                        .texOffs(20, 10)
                        .mirror()
                        .addBox(-1.0F, 1.0F, -1.0F, 2.0F, 13.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.5236F, 3.1416F, 0.0F));
        partdefinition.addOrReplaceChild("leg4",
                CubeListBuilder.create()
                        .texOffs(20, 10)
                        .mirror()
                        .addBox(-1.0F, 1.0F, -1.0F, 2.0F, 13.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.5236F, 4.7124F, 0.0F));
        PartDefinition crossbowPart = partdefinition.addOrReplaceChild("crossbow",
                CubeListBuilder.create()
                        .texOffs(28, 14)
                        .mirror()
                        .addBox(-2.0F, 0.0F, -7.0F, 4.0F, 2.0F, 14.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 10.0F, 0.0F));
        crossbowPart.addOrReplaceChild("ammobox",
                CubeListBuilder.create()
                        .texOffs(38, 0)
                        .mirror()
                        .addBox(-2.0F, -5.0F, -6.0F, 4.0F, 5.0F, 9.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        crossbowPart.addOrReplaceChild("barrel",
                CubeListBuilder.create()
                        .texOffs(20, 28)
                        .mirror()
                        .addBox(-1.0F, -1.0F, -8.0F, 2.0F, 2.0F, 2.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        crossbowPart.addOrReplaceChild("basebarcross",
                CubeListBuilder.create()
                        .texOffs(0, 13)
                        .mirror()
                        .addBox(-2.0F, 0.5F, 10.0F, 4.0F, 1.0F, 1.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        crossbowPart.addOrReplaceChild("basebarr",
                CubeListBuilder.create()
                        .texOffs(40, 23)
                        .mirror()
                        .addBox(-1.0F, 0.0F, 7.0F, 1.0F, 2.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.1396F, 0.0F));
        crossbowPart.addOrReplaceChild("basebarl",
                CubeListBuilder.create()
                        .texOffs(40, 23)
                        .mirror()
                        .addBox(0.0F, 0.0F, 7.0F, 1.0F, 2.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.1396F, 0.0F));
        crossbowPart.addOrReplaceChild("loadbarcross",
                CubeListBuilder.create()
                        .texOffs(0, 13)
                        .mirror()
                        .addBox(-2.0F, -8.5F, -0.5F, 4.0F, 1.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5585F, 0.0F, 0.0F));
        crossbowPart.addOrReplaceChild("loadbarl",
                CubeListBuilder.create()
                        .texOffs(0, 15)
                        .mirror()
                        .addBox(2.0F, -9.0F, -1.0F, 1.0F, 11.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5585F, 0.0F, 0.0F));
        crossbowPart.addOrReplaceChild("loadbarr",
                CubeListBuilder.create()
                        .texOffs(0, 15)
                        .mirror()
                        .addBox(-3.0F, -9.0F, -1.0F, 1.0F, 11.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5585F, 0.0F, 0.0F));
        crossbowPart.addOrReplaceChild("crossl1",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .mirror()
                        .addBox(0.0F, 0.0F, -6.0F, 5.0F, 2.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.2443F, 0.0F));
        crossbowPart.addOrReplaceChild("crossl2",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .mirror()
                        .addBox(4.0F, 0.0F, -5.0F, 3.0F, 2.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.2443F, 0.0F));
        crossbowPart.addOrReplaceChild("crossl3",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .mirror()
                        .addBox(6.0F, 0.0F, -4.0F, 2.0F, 2.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.2443F, 0.0F));
        crossbowPart.addOrReplaceChild("crossr1",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .mirror()
                        .addBox(-5.0F, 0.0F, -6.0F, 5.0F, 2.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.2443F, 0.0F));
        crossbowPart.addOrReplaceChild("crossr2",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .mirror()
                        .addBox(-7.0F, 0.0F, -5.0F, 3.0F, 2.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.2443F, 0.0F));
        crossbowPart.addOrReplaceChild("crossr3",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .mirror()
                        .addBox(-8.0F, 0.0F, -4.0F, 2.0F, 2.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.2443F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 32);
    }
    @Override
    public void setupAnim(EntityTurretCrossbow entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.crossbow.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.crossbow.xRot = headPitch * ((float) Math.PI / 180F);
        this.crossl3.yRot = -0.2F;
        this.crossr3.yRot = 0.2F;
        this.loadbarr.xRot = -0.5F + Mth.sin(Mth.sqrt(entity.loadProgressForRender) * (float) Math.PI * 2.0F) * 0.5F;
        if (entity.isPassenger() && entity.getVehicle() instanceof net.minecraft.world.entity.vehicle.AbstractMinecart) {
            this.leg1.y = 11.5F;
            this.leg2.y = 11.5F;
            this.leg3.y = 11.5F;
            this.leg4.y = 11.5F;
            this.leg1.xRot = 0.1F;
            this.leg2.xRot = 0.1F;
            this.leg3.xRot = 0.1F;
            this.leg4.xRot = 0.1F;
        } else {
            this.leg1.y = 12.0F;
            this.leg2.y = 12.0F;
            this.leg3.y = 12.0F;
            this.leg4.y = 12.0F;
            this.leg1.xRot = 0.5F;
            this.leg2.xRot = 0.5F;
            this.leg3.xRot = 0.5F;
            this.leg4.xRot = 0.5F;
        }
    }
    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        crossbow.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        tripod.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
