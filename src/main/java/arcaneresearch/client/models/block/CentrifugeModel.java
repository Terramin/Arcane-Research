package arcaneresearch.client.models.block;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
@OnlyIn(Dist.CLIENT)
public class CentrifugeModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(Arcaneresearch.MODID, "centrifuge"), "main");
    private final ModelPart top;
    private final ModelPart bottom;
    private final ModelPart spinnyBit;
    public CentrifugeModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.top = root.getChild("top");
        this.bottom = root.getChild("bottom");
        this.spinnyBit = root.getChild("spinny_bit");
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("top", CubeListBuilder.create()
                        .texOffs(20, 16)
                        .addBox(-4.0f, -8.0f, -4.0f, 8.0f, 4.0f, 8.0f),
                PartPose.ZERO);
        partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create()
                        .texOffs(20, 16)
                        .addBox(-4.0f, 4.0f, -4.0f, 8.0f, 4.0f, 8.0f),
                PartPose.ZERO);
        PartDefinition spinnyBit = partdefinition.addOrReplaceChild("spinny_bit", CubeListBuilder.create(),
                PartPose.ZERO);
        spinnyBit.addOrReplaceChild("crossbar", CubeListBuilder.create()
                        .texOffs(16, 0)
                        .addBox(-4.0f, -1.0f, -1.0f, 8.0f, 2.0f, 2.0f),
                PartPose.ZERO);
        spinnyBit.addOrReplaceChild("core", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.5f, -4.0f, -1.5f, 3.0f, 8.0f, 3.0f),
                PartPose.ZERO);
        spinnyBit.addOrReplaceChild("dingus1", CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(4.0f, -3.0f, -2.0f, 4.0f, 6.0f, 4.0f),
                PartPose.ZERO);
        spinnyBit.addOrReplaceChild("dingus2", CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-8.0f, -3.0f, -2.0f, 4.0f, 6.0f, 4.0f),
                PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 64, 32);
    }
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        top.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        bottom.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        spinnyBit.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
    public void renderStaticParts(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                                  int packedOverlay, float red, float green, float blue, float alpha) {
        top.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        bottom.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
    public void renderSpinnyBit(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                                int packedOverlay, float red, float green, float blue, float alpha) {
        spinnyBit.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
    public void setSpinRotation(float rotationY) {
        spinnyBit.yRot = rotationY;
    }
}
