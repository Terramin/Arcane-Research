package arcaneresearch.client.models.block;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
@OnlyIn(Dist.CLIENT)
public class BellowsNozzleModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(Arcaneresearch.MODID, "bellows_nozzle"), "main");
    private final ModelPart root;
    public BellowsNozzleModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition nozzle = partdefinition.addOrReplaceChild("nozzle",
                CubeListBuilder.create(),
                PartPose.ZERO);
        nozzle.addOrReplaceChild("nozzle1",
                CubeListBuilder.create()
                        .texOffs(106, 42)
                        .addBox(2.5F, -2.0F, -2.0F, 5.0F, 4.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        nozzle.addOrReplaceChild("nozzle2",
                CubeListBuilder.create()
                        .texOffs(106, 51)
                        .addBox(7.0F, -2.5F, -2.5F, 1.0F, 5.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 8.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 128, 64);
    }
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
