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

public class BrainModel extends Model<net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "brain"), "main");

    private final ModelPart mainBrain;
    private final ModelPart brainStem;
    private final ModelPart spinalCord;

    public BrainModel(ModelPart root) {
        // NEOFORGE_PORT: Model has no 1-arg ctor (only EntityModel does).
        super(root, net.minecraft.client.renderer.rendertype.RenderTypes::entityCutout);
        this.mainBrain = root.getChild("main_brain");
        this.brainStem = root.getChild("brain_stem");
        this.spinalCord = root.getChild("spinal_cord");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("main_brain", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(0.0f, 0.0f, 0.0f, 12.0f, 10.0f, 16.0f),
                PartPose.offset(-6.0f, 8.0f, -8.0f));

        partdefinition.addOrReplaceChild("brain_stem", CubeListBuilder.create()
                        .texOffs(64, 0)
                        .addBox(0.0f, 0.0f, 0.0f, 8.0f, 3.0f, 7.0f),
                PartPose.offset(-4.0f, 18.0f, 0.0f));

        partdefinition.addOrReplaceChild("spinal_cord", CubeListBuilder.create()
                        .texOffs(0, 32)
                        .addBox(0.0f, 0.0f, 0.0f, 2.0f, 6.0f, 2.0f),
                PartPose.offsetAndRotation(-1.0f, 18.0f, -2.0f, 0.4089647f, 0.0f, 0.0f));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    // NEOFORGE_PORT: @Override dropped (base renderToBuffer final, tint int).
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        mainBrain.render(poseStack, buffer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        brainStem.render(poseStack, buffer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        spinalCord.render(poseStack, buffer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
    }
}