package arcaneresearch.client.models.entity;

import arcaneresearch.api.portals.IPortal;
import arcaneresearch.client.renderers.entity.PortalRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public class PortalModel extends EntityModel<PortalRenderer.State> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath("arcaneresearch", "model_portal_java"), "main");

    public final ModelPart top;
    public final ModelPart bottom;
    private float fadeAlpha = 1.0F;

    public PortalModel(ModelPart root) {
        super(root);
        this.top = root.getChild("top");
        this.bottom = root.getChild("bottom");
    }

    public void setFadeAlpha(float alpha) {
        this.fadeAlpha = alpha;
    }

    public float getFadeAlpha() {
        return this.fadeAlpha;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition top = partdefinition.addOrReplaceChild("top", CubeListBuilder.create().texOffs(0, 8).addBox(-24.0F, -63.5F, -1.0F, 48.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 16).addBox(-25.0F, -60.5F, -2.0F, 50.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-24.0F, -58.5F, 0F, 48.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 22.5F, 0.0F));
        PartDefinition bottom = partdefinition.addOrReplaceChild("bottom", CubeListBuilder.create(), PartPose.offset(0.0F, 22.5F, 0.0F));
        bottom.addOrReplaceChild("group3_box3_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-24.0F, -2.5F, 0.0F, 48.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, (float) Math.PI, 0.0F, 0.0F));
        bottom.addOrReplaceChild("group3_box2_r1", CubeListBuilder.create().texOffs(0, 16).addBox(-25.0F, -1.0F, -2.0F, 50.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.5F, 0.0F, (float) Math.PI, 0.0F, 0.0F));
        bottom.addOrReplaceChild("group3_r1", CubeListBuilder.create().texOffs(0, 8).addBox(-24.0F, -1.5F, -1.0F, 48.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, (float) Math.PI, 0.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(PortalRenderer.State state) {
        float open = state.openFraction;
        float slide = (1.0F - open) * IPortal.OPEN_SLIDE_PX;

        float time = (state.ageInTicks / 10.0F) % 2.0F;
        float sineValue = (float) Math.sin(time * Math.PI) * open;
        this.top.y = 22.5F + slide + sineValue;
        this.bottom.y = 22.5F - slide - sineValue;
    }

    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        float a = alpha * this.fadeAlpha;
        int color = ARGB.colorFromFloat(a, red, green, blue);
        this.top.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        this.bottom.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
