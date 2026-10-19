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
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;

public class TubeValveModel extends Model<net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "tube_valve"), "main");

    private final ModelPart valveRod;
    private final ModelPart valveRing;

    public TubeValveModel(ModelPart root) {
        // NEOFORGE_PORT: Model has no 1-arg ctor (only EntityModel does).
        super(root, net.minecraft.client.renderer.rendertype.RenderTypes::entityCutout);
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

    // NEOFORGE_PORT: @Override dropped (base renderToBuffer final, tint int).
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        valveRod.render(poseStack, buffer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        valveRing.render(poseStack, buffer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
    }

    public void renderRod(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                          int packedOverlay, float red, float green, float blue, float alpha) {
        valveRod.render(poseStack, buffer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
    }

    public void renderRing(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                           int packedOverlay, float red, float green, float blue, float alpha) {
        valveRing.render(poseStack, buffer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
    }
}