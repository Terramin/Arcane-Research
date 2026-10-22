package arcaneresearch.client.models.armor;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier; 
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;

 



 

public class GogglesHeadModel extends EntityModel<AvatarRenderState> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "goggles_head"), "main");

    private final ModelPart head;

    public GogglesHeadModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F,
                                new CubeDeformation(1.0F)),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    public void copyHeadPose(ModelPart playerHead) {
        
        this.head.loadPose(playerHead.storePose());
    }

    
    public void loadHeadPose(PartPose pose) {
        this.head.loadPose(pose);
    }

    @Override
    public void setupAnim(AvatarRenderState state) {
        super.setupAnim(state);
    }

    
    public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack poseStack,
                               com.mojang.blaze3d.vertex.VertexConsumer buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.head.render(poseStack, buffer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
    }
}