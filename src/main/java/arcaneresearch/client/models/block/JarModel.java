package arcaneresearch.client.models.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier; 
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;

public class JarModel extends Model<net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "jar_fittings"), "main");

    private final ModelPart lid;
    private final ModelPart lidExtension;
    private final ModelPart brine;

    public JarModel(ModelPart root) {
        
        super(root, net.minecraft.client.renderer.rendertype.RenderTypes::entityCutout);
        this.lid = root.getChild("lid");
        this.lidExtension = root.getChild("lid_extension");
        this.brine = root.getChild("brine");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("lid",
                CubeListBuilder.create()
                        .texOffs(32, 24)
                        .addBox(-3.0F, 0.0F, -3.0F, 6.0F, 2.0F, 6.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -14.0F, 0.0F));

        partdefinition.addOrReplaceChild("lid_extension",
                CubeListBuilder.create()
                        .texOffs(0, 23)
                        .addBox(-2.0F, -16.0F, -2.0F, 4.0F, 2.0F, 4.0F, CubeDeformation.NONE),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("brine",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -11.0F, -4.0F, 8.0F, 10.0F, 8.0F, CubeDeformation.NONE),
                PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    public void renderLidBrace(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.scale(1.001F, 1.001F, 1.001F);
        lid.render(poseStack, buffer, packedLight, packedOverlay, -1);
        poseStack.popPose();
    }

    public void renderLidExtension(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.scale(0.9F, 1.0F, 0.9F);
        lidExtension.render(poseStack, buffer, packedLight, packedOverlay, -1);
        poseStack.popPose();
    }

    public void renderBrine(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
        brine.render(poseStack, buffer, packedLight, packedOverlay, -1);
    }

    
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        lid.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        lidExtension.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
    }
}