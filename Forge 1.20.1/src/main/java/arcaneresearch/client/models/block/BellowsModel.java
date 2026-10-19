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
public class BellowsModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(Arcaneresearch.MODID, "bellows"), "main");
    private final ModelPart bottomPlank;
    private final ModelPart middlePlank;
    private final ModelPart topPlank;
    private final ModelPart bag;
    private final ModelPart nozzle;
    public BellowsModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.bottomPlank = root.getChild("bottom_plank");
        this.middlePlank = root.getChild("middle_plank");
        this.topPlank = root.getChild("top_plank");
        this.bag = root.getChild("bag");
        this.nozzle = root.getChild("nozzle");
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("bottom_plank", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-6.0f, 0.0f, -6.0f, 12.0f, 2.0f, 12.0f),
                PartPose.offset(0.0f, 22.0f, 0.0f));
        partdefinition.addOrReplaceChild("middle_plank", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-6.0f, -1.0f, -6.0f, 12.0f, 2.0f, 12.0f),
                PartPose.offset(0.0f, 16.0f, 0.0f));
        partdefinition.addOrReplaceChild("top_plank", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-6.0f, 0.0f, -6.0f, 12.0f, 2.0f, 12.0f),
                PartPose.offset(0.0f, 8.0f, 0.0f));
        partdefinition.addOrReplaceChild("bag", CubeListBuilder.create()
                        .texOffs(48, 0)
                        .addBox(-10.0f, -12.03333f, -10.0f, 20.0f, 24.0f, 20.0f),
                PartPose.offset(0.0f, 16.0f, 0.0f));
        partdefinition.addOrReplaceChild("nozzle", CubeListBuilder.create()
                        .texOffs(0, 36)
                        .addBox(-2.0f, -2.0f, 0.0f, 4.0f, 4.0f, 2.0f),
                PartPose.offset(0.0f, 16.0f, 6.0f));
        return LayerDefinition.create(meshdefinition, 128, 128);
    }
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        bottomPlank.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        middlePlank.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        topPlank.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        bag.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        nozzle.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
    public void setInflation(float inflation) {
        float tscale = 0.125f + inflation * 0.875f;
        bag.yScale = (inflation + 0.1f) / 2.0f;
        bag.xScale = 0.5f;
        bag.zScale = 0.5f;
        bag.y = 16.0f + 0.5f * bag.yScale;
        float offsetUnits = (0.5f - tscale / 2.0f) * 16.0f;
        topPlank.y = 8.0f + offsetUnits;
        bottomPlank.y = 22.0f - offsetUnits;
    }
    public ModelPart getTopPlank() {
        return topPlank;
    }
    public ModelPart getBottomPlank() {
        return bottomPlank;
    }
    public ModelPart getBag() {
        return bag;
    }
}
