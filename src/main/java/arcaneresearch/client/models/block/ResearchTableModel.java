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
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;

// NEOFORGE_PORT: Model is generic in 26.x (Model<S>); state type matches the
// ResearchTableRenderer stub (BlockEntityRenderState) вЂ” Thaumaturge oracle.
public class ResearchTableModel extends Model<BlockEntityRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "research_table_model"), "main");

    private final ModelPart inkwell;
    private final ModelPart scrollTube;
    private final ModelPart scrollRibbon;

    public ResearchTableModel(ModelPart root) {
        // NEOFORGE_PORT: generic Model<S> pins the Function to Identifier ->
        // RenderType, so the method ref picks entityCutout(Identifier).
        super(root, RenderTypes::entityCutout);
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

    // NEOFORGE_PORT: ModelPart.render(PoseStack, VertexConsumer, light, overlay[, tint])
    // in 26.x вЂ” colors packed ARGB int (was r/g/b/a floats); Model.renderToBuffer final.
    private static int pack(float red, float green, float blue, float alpha) {
        return ((int) (alpha * 255.0F) << 24) | ((int) (red * 255.0F) << 16)
                | ((int) (green * 255.0F) << 8) | (int) (blue * 255.0F);
    }

    public void renderInkwell(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                              int packedOverlay, float red, float green, float blue, float alpha) {

        inkwell.render(poseStack, buffer, packedLight, packedOverlay, pack(red, green, blue, alpha));
    }

    public void renderScroll(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                             int packedOverlay, float red, float green, float blue, float alpha) {

        scrollTube.render(poseStack, buffer, packedLight, packedOverlay, pack(1.0f, 1.0f, 1.0f, 1.0f));
        poseStack.pushPose();
        poseStack.scale(1.2F, 1.2F, 1.2F);
        scrollRibbon.render(poseStack, buffer, packedLight, packedOverlay, pack(red, green, blue, 1.0f));
        poseStack.popPose();
    }

    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {

        inkwell.render(poseStack, buffer, packedLight, packedOverlay, pack(red, green, blue, alpha));
        scrollTube.render(poseStack, buffer, packedLight, packedOverlay, pack(red, green, blue, alpha));
        scrollRibbon.render(poseStack, buffer, packedLight, packedOverlay, pack(red, green, blue, alpha));
    }
}