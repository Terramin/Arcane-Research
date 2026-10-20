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

public class BannerModel extends Model<net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "banner"), "main");

    private final ModelPart pole;
    private final ModelPart beam;
    private final ModelPart b1;
    private final ModelPart b2;
    private final ModelPart banner;

    public BannerModel(ModelPart root) {
        // NEOFORGE_PORT: Model has no 1-arg ctor (only EntityModel does).
        super(root, net.minecraft.client.renderer.rendertype.RenderTypes::entityCutout);
        this.pole = root.getChild("pole");
        this.beam = root.getChild("beam");
        this.b1 = root.getChild("b1");
        this.b2 = root.getChild("b2");
        this.banner = root.getChild("banner");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("pole", CubeListBuilder.create()
                        .texOffs(62, 0)
                        .addBox(-1.0F, -7.0F, -3.0F, 2, 31, 2),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("beam", CubeListBuilder.create()
                        .texOffs(30, 0)
                        .addBox(-7.0F, -7.0F, -1.0F, 14, 2, 2),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("b1", CubeListBuilder.create()
                        .texOffs(0, 29)
                        .addBox(-5.0F, -7.5F, -1.5F, 2, 3, 3),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("b2", CubeListBuilder.create()
                        .texOffs(0, 29)
                        .addBox(3.0F, -7.5F, -1.5F, 2, 3, 3),
                PartPose.ZERO);

        partdefinition.addOrReplaceChild("banner", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-7.0F, 0.0F, -0.5F, 14, 28, 1),
                PartPose.offset(0.0F, -5.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    // NEOFORGE_PORT: @Override dropped (base renderToBuffer final, tint int).
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {

        pole.render(poseStack, buffer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        beam.render(poseStack, buffer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        b1.render(poseStack, buffer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        b2.render(poseStack, buffer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        banner.render(poseStack, buffer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
    }

    public void renderPole(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                           int packedOverlay, float red, float green, float blue, float alpha) {
        pole.render(poseStack, buffer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
    }

    public void renderBeam(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                           int packedOverlay, float red, float green, float blue, float alpha) {
        beam.render(poseStack, buffer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
    }

    public void renderTabs(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                           int packedOverlay, float red, float green, float blue, float alpha) {
        b1.render(poseStack, buffer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
        b2.render(poseStack, buffer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
    }

    public void renderBanner(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                             int packedOverlay, float red, float green, float blue, float alpha) {
        banner.render(poseStack, buffer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
    }

    public void setBannerRotation(float xRot) {
        banner.xRot = xRot;
    }
}