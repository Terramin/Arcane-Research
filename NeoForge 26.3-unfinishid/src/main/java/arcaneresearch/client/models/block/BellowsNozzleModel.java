package arcaneresearch.client.models.block;

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

public class BellowsNozzleModel extends Model<net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "bellows_nozzle"), "main");

    private final ModelPart root;

    public BellowsNozzleModel(ModelPart root) {
        // NEOFORGE_PORT: Model has no 1-arg ctor (only EntityModel does).
        super(root, net.minecraft.client.renderer.rendertype.RenderTypes::entityCutout);
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

    // NEOFORGE_PORT: @Override dropped (base renderToBuffer final, tint int).
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
    }
}