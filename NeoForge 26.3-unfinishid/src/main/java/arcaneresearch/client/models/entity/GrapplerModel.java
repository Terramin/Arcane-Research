package arcaneresearch.client.models.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;

// NEOFORGE_PORT (26.3): Model is generic; base EntityRenderState covers both
// users (FocusMine/Grapple states extend it).
public class GrapplerModel extends Model<net.minecraft.client.renderer.entity.state.EntityRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "grappler"), "main");

    private final ModelPart core;
    private final ModelPart prong1;
    private final ModelPart prong2;
    private final ModelPart prong3;

    public GrapplerModel(ModelPart root) {
        // NEOFORGE_PORT: Model has no 1-arg ctor (only EntityModel does).
        super(root, net.minecraft.client.renderer.rendertype.RenderTypes::entityCutout);
        this.core = root.getChild("core");
        this.prong1 = root.getChild("prong1");
        this.prong2 = root.getChild("prong2");
        this.prong3 = root.getChild("prong3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("core",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, CubeDeformation.NONE),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("prong1",
                CubeListBuilder.create()
                        .texOffs(0, 10)
                        .addBox(-0.5F, -0.5F, -2.5F, 1.0F, 1.0F, 5.0F, CubeDeformation.NONE),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("prong2",
                CubeListBuilder.create()
                        .texOffs(0, 10)
                        .addBox(-0.5F, -0.5F, -2.5F, 1.0F, 1.0F, 5.0F, CubeDeformation.NONE),
                PartPose.rotation(0.0F, (float) (Math.PI / 2), 0.0F));

        partdefinition.addOrReplaceChild("prong3",
                CubeListBuilder.create()
                        .texOffs(0, 10)
                        .addBox(-0.5F, -0.5F, -2.5F, 1.0F, 1.0F, 5.0F, CubeDeformation.NONE),
                PartPose.rotation((float) (Math.PI / 2), (float) (Math.PI / 2), 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    // NEOFORGE_PORT: @Override dropped (base renderToBuffer final, tint int).
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        core.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        prong1.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        prong2.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        prong3.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
    }
}