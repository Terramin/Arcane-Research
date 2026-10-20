package arcaneresearch.client.models.block;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.Arcaneresearch;
@OnlyIn(Dist.CLIENT)
public class JarNodeModel extends EntityModel<Entity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation(Arcaneresearch.MODID, "jar_node"), "main");
    private final ModelPart core;
    private final ModelPart brine;
    private final ModelPart lid;
    public JarNodeModel(ModelPart root) {
        super(RenderType::entityCutout);
        this.core = root.getChild("core");
        this.brine = root.getChild("brine");
        this.lid = root.getChild("lid");
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("core",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-5.0F, -12.0F, -5.0F, 10.0F, 12.0F, 10.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        root.addOrReplaceChild("brine",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -11.0F, -4.0F, 8.0F, 10.0F, 8.0F, CubeDeformation.NONE),
                PartPose.ZERO);
        root.addOrReplaceChild("lid",
                CubeListBuilder.create()
                        .texOffs(0, 24)
                        .addBox(-3.0F, 0.0F, -3.0F, 6.0F, 2.0F, 6.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -14.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }
    public void renderLid(PoseStack poseStack, VertexConsumer builder, int packedLight,
                          int packedOverlay) {
        this.lid.render(poseStack, builder, packedLight, packedOverlay);
    }
    public void renderCore(PoseStack poseStack, VertexConsumer builder, int packedLight,
                           int packedOverlay, float alpha) {
        this.core.render(poseStack, builder, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, alpha);
    }
    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
    }
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer builder, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        this.core.render(poseStack, builder, packedLight, packedOverlay, red, green, blue, alpha);
        this.lid.render(poseStack, builder, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
