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
public class TubeValveModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(Arcaneresearch.MODID, "tube_valve"), "main");
    private final ModelPart valveRod;
    private final ModelPart valveRing;
    public TubeValveModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.valveRod = root.getChild("valve_rod");
        this.valveRing = root.getChild("valve_ring");
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("valve_rod", CubeListBuilder.create()
                        .texOffs(0, 10)
                        .addBox(-1.0f, 2.0f, -1.0f, 2.0f, 2.0f, 2.0f),
                PartPose.ZERO);
        partdefinition.addOrReplaceChild("valve_ring", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.0f, 4.0f, -2.0f, 4.0f, 1.0f, 4.0f),
                PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 64, 32);
    }
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        valveRod.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        valveRing.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
    public void renderRod(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                          int packedOverlay, float red, float green, float blue, float alpha) {
        valveRod.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
    public void renderRing(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                           int packedOverlay, float red, float green, float blue, float alpha) {
        valveRing.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
