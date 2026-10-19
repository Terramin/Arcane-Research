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
import net.minecraft.resources.Identifier; // NEOFORGE_PORT: was Identifier
import net.neoforged.api.distmarker.Dist;
import arcaneresearch.Arcaneresearch;

/**
 * РЁР»РµРј-РєРѕСЂРѕР±РєР° РґР»СЏ РѕС‡РєРѕРІ РёР· curio-СЃР»РѕС‚Р° РіРѕР»РѕРІС‹: С‚Р° Р¶Рµ РіРµРѕРјРµС‚СЂРёСЏ Рё UV,
 * С‡С‚Рѕ Сѓ РІР°РЅРёР»СЊРЅРѕРіРѕ СЂРµРЅРґРµСЂР° Р±СЂРѕРЅРё (С€Р»РµРј), РїРѕСЌС‚РѕРјСѓ РѕС‡РєРё РІС‹РіР»СЏРґСЏС‚
 * РѕРґРёРЅ РІ РѕРґРёРЅ РєР°Рє РІ СЃР»РѕС‚Рµ С€Р»РµРјР°. РўРµРєСЃС‚СѓСЂР° вЂ” goggles.png.
 */
// NEOFORGE_PORT (26.3): EntityModel<State> (was <Player>).
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
        // NEOFORGE_PORT: ModelPart.copyFrom gone -> pose round-trip.
        this.head.loadPose(playerHead.storePose());
    }

    @Override
    public void setupAnim(AvatarRenderState state) {
        super.setupAnim(state);
    }

    // NEOFORGE_PORT: @Override dropped (base renderToBuffer final, tint int).
    public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack poseStack,
                               com.mojang.blaze3d.vertex.VertexConsumer buffer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        this.head.render(poseStack, buffer, packedLight, packedOverlay, net.minecraft.util.ARGB.colorFromFloat(alpha, red, green, blue));
    }
}