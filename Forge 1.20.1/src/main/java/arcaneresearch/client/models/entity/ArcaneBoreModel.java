package arcaneresearch.client.models.entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.entities.construct.EntityArcaneBore;
@OnlyIn(Dist.CLIENT)
public class ArcaneBoreModel extends EntityModel<EntityArcaneBore> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(Arcaneresearch.MODID, "arcane_bore"), "main");
    private final ModelPart tripod;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart base;
    public ArcaneBoreModel(ModelPart root) {
        this.tripod = root.getChild("tripod");
        this.leg1 = root.getChild("leg1");
        this.leg2 = root.getChild("leg2");
        this.leg3 = root.getChild("leg3");
        this.leg4 = root.getChild("leg4");
        this.base = root.getChild("base");
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("tripod",
                CubeListBuilder.create()
                        .texOffs(13, 0)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild("leg1",
                CubeListBuilder.create()
                        .texOffs(20, 10)
                        .addBox(-1.0F, 1.0F, -1.0F, 2.0F, 13.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.5236F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("leg2",
                CubeListBuilder.create()
                        .texOffs(20, 10)
                        .addBox(-1.0F, 1.0F, -1.0F, 2.0F, 13.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.5236F, 1.5708F, 0.0F));
        partdefinition.addOrReplaceChild("leg3",
                CubeListBuilder.create()
                        .texOffs(20, 10)
                        .addBox(-1.0F, 1.0F, -1.0F, 2.0F, 13.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.5236F, 3.1416F, 0.0F));
        partdefinition.addOrReplaceChild("leg4",
                CubeListBuilder.create()
                        .texOffs(20, 10)
                        .addBox(-1.0F, 1.0F, -1.0F, 2.0F, 13.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.5236F, 4.7124F, 0.0F));
        PartDefinition basePart = partdefinition.addOrReplaceChild("base",
                CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 13.0F, 0.0F));
        basePart.addOrReplaceChild("crystal",
                CubeListBuilder.create()
                        .texOffs(32, 25)
                        .addBox(-1.0F, -4.0F, 5.0F, 2.0F, 2.0F, 2.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        basePart.addOrReplaceChild("domebase",
                CubeListBuilder.create()
                        .texOffs(32, 19)
                        .addBox(-2.0F, -5.0F, 3.0F, 4.0F, 4.0F, 1.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        basePart.addOrReplaceChild("dome",
                CubeListBuilder.create()
                        .texOffs(44, 16)
                        .addBox(-2.0F, -5.0F, 4.0F, 4.0F, 4.0F, 4.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        basePart.addOrReplaceChild("magbase",
                CubeListBuilder.create()
                        .texOffs(0, 18)
                        .addBox(-1.0F, -4.0F, -6.0F, 2.0F, 2.0F, 3.0F, CubeDeformation.NONE)
                        .mirror(),
                PartPose.ZERO);
        basePart.addOrReplaceChild("tip",
                CubeListBuilder.create()
                        .texOffs(0, 9)
                        .addBox(-1.5F, 0.0F, -1.5F, 3.0F, 3.0F, 3.0F, CubeDeformation.NONE)
                        .mirror(),
                PartPose.offsetAndRotation(0.0F, -3.0F, -6.0F, -1.5708F, 0.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 32);
    }
    @Override
    public void setupAnim(EntityArcaneBore entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.base.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.base.xRot = headPitch * ((float) Math.PI / 180F);
    }
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        tripod.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leg1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leg2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leg3.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leg4.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        base.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
