package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier; 
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber; 
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.items.IGoggles;
import arcaneresearch.client.models.armor.GogglesHeadModel;
import arcaneresearch.common.items.armor.ItemFortressArmor;
import arcaneresearch.common.lib.compat.CuriosCompat;

import java.util.List;

 



 


public class GogglesCurioLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final Identifier GOGGLES_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/armor/goggles.png");

    private final GogglesHeadModel model;

    public GogglesCurioLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
        this.model = new GogglesHeadModel(Minecraft.getInstance().getEntityModels()
                .bakeLayer(GogglesHeadModel.LAYER_LOCATION));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                       AvatarRenderState state, float yRot, float xRot) {
        if (state.isInvisible) {
            return;
        }
        
        ItemStack helm = state.headEquipment;
        if (!helm.isEmpty()) {
            if (helm.getItem() instanceof IGoggles) {
                return;
            }
            if (helm.getItem() instanceof ItemFortressArmor && ItemFortressArmor.hasGoggles(helm)) {
                return;
            }
        }
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        if (!(level.getEntity(state.id) instanceof Player player)) {
            return;
        }
        List<ItemStack> headCurios = CuriosCompat.findHeadSlotItems(player,
                s -> !s.isEmpty() && s.getItem() instanceof IGoggles);
        if (headCurios.isEmpty()) {
            return;
        }
        
        
        net.minecraft.client.model.geom.PartPose headPose = getParentModel().head.storePose();
        RenderType type = RenderTypes.armorCutoutNoCull(GOGGLES_TEXTURE);
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            this.model.loadHeadPose(headPose);
            PoseStack work = new PoseStack();
            work.last().pose().set(pose.pose());
            work.last().normal().set(pose.normal());
            this.model.renderToBuffer(work, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                    1.0F, 1.0F, 1.0F, 1.0F);
        });
    }

    @EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
    public static class Setup {
        @SubscribeEvent
        public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
            
            
            for (net.minecraft.world.entity.player.PlayerModelType skin : event.getSkins()) {
                var renderer = event.getPlayerRenderer(skin);
                if (renderer != null) {
                    renderer.addLayer(new GogglesCurioLayer(renderer));
                }
            }
        }
    }
}