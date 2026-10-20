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
public class ResearchTableModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(Arcaneresearch.MODID, "research_table_model"), "main");
    private final ModelPart inkwell;
    private final ModelPart scrollTube;
    private final ModelPart scrollRibbon;
    public ResearchTableModel(ModelPart root) {
        super(RenderType::entityCutout);
        this.inkwell = root.getChild("inkwell");
        this.scrollTube = root.getChild("scroll_tube");
        this.scrollRibbon = root.getChild("scroll_ribbon");
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("inkwell", CubeListBuilder.create()
                        .mirror()
                        .texOffs(0, 16)
                        .addBox(0.0F, 0.0F, 0.0F, 3, 2, 3),
                PartPose.offset(-6.0F, -2.0F, 3.0F));
        partdefinition.addOrReplaceChild("scroll_tube", CubeListBuilder.create()
                        .mirror()
                        .texOffs(0, 0)
                        .addBox(-8.0F, -0.5F, 0.0F, 8, 2, 2),
                PartPose.offsetAndRotation(-2.0F, -2.0F, 2.0F, 0.0F, 10.0F, 0.0F));
        partdefinition.addOrReplaceChild("scroll_ribbon", CubeListBuilder.create()
                        .mirror()
                        .texOffs(0, 4)
                        .addBox(-4.25F, -0.275F, 0.0F, 1, 2, 2),
                PartPose.offsetAndRotation(-2.0F, -2.0F, 2.0F, 0.0F, 10.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 32);
    }
    public void renderInkwell(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                              int packedOverlay, float red, float green, float blue, float alpha) {
        inkwell.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
    public void renderScroll(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                             int packedOverlay, float red, float green, float blue, float alpha) {
        scrollTube.render(poseStack, buffer, packedLight, packedOverlay, 1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.pushPose();
        poseStack.scale(1.2F, 1.2F, 1.2F);
        scrollRibbon.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, 1.0f);
        poseStack.popPose();
    }
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        inkwell.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        scrollTube.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        scrollRibbon.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}