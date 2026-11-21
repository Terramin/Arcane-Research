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
import arcaneresearch.client.lib.obj.MeshLoader;
import arcaneresearch.client.lib.obj.MeshModel;
import arcaneresearch.client.lib.obj.MeshPart;
import arcaneresearch.client.models.armor.GogglesHeadModel;
import arcaneresearch.common.items.armor.ItemFortressArmor;
import arcaneresearch.common.lib.compat.CuriosCompat;

import java.util.List;

 



 


public class GogglesCurioLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final Identifier GOGGLES_TEXTURE =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/armor/goggles.png");
    private static final Identifier GOGGLES_OBJ =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "models/obj/googles_head.obj");

    private static volatile MeshModel cachedMesh;
    private final GogglesHeadModel fallbackModel;

    public GogglesCurioLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
        this.fallbackModel = new GogglesHeadModel(Minecraft.getInstance().getEntityModels()
                .bakeLayer(GogglesHeadModel.LAYER_LOCATION));
        String forgeVersion = net.neoforged.fml.ModList.get()
                .getModContainerById("forge")
                .map(c -> c.getModInfo().getVersion().toString())
                .orElse("unknown");
        if (!isForgeAtLeast(forgeVersion, 47, 1, 38)) {
            Arcaneresearch.LOGGER.warn(
                    "Forge {} is below 47.1.38 — 45°-normal flickering fix is missing. Update Forge to reduce goggles shimmering.",
                    forgeVersion);
        }
    }

    private static boolean isForgeAtLeast(String version, int major, int minor, int patch) {
        try {
            String[] parts = version.split("[.-]");
            int ma = Integer.parseInt(parts[0]);
            int mi = Integer.parseInt(parts[1]);
            int pa = Integer.parseInt(parts[2]);
            return ma > major || (ma == major && mi > minor) || (ma == major && mi == minor && pa >= patch);
        } catch (Exception e) {
            return true;
        }
    }

    private static MeshModel mesh() {
        MeshModel mesh = cachedMesh;
        if (mesh == null) {
            synchronized (GogglesCurioLayer.class) {
                mesh = cachedMesh;
                if (mesh == null) {
                    try {
                        mesh = new MeshLoader().loadFromResource(GOGGLES_OBJ);
                    } catch (Exception e) {
                        Arcaneresearch.LOGGER.error("Failed to load goggles OBJ model {}", GOGGLES_OBJ, e);
                        mesh = null;
                    }
                    cachedMesh = mesh;
                }
            }
        }
        return mesh;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                       AvatarRenderState state, float yRot, float xRot) {
        if (state.isInvisible) {
            return;
        }
        
        ItemStack helm = state.headEquipment;
        boolean helmetGoggles = !helm.isEmpty() && helm.getItem() instanceof IGoggles
                && !(helm.getItem() instanceof ItemFortressArmor);
        if (helm.getItem() instanceof ItemFortressArmor && ItemFortressArmor.hasGoggles(helm)) {
            return;
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
        if (headCurios.isEmpty() && !helmetGoggles) {
            return;
        }

        MeshModel mesh = mesh();
        net.minecraft.client.model.geom.PartPose headPose = getParentModel().head.storePose();
        RenderType type = RenderTypes.armorCutoutNoCull(GOGGLES_TEXTURE);
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            this.fallbackModel.loadHeadPose(headPose);
            PoseStack work = new PoseStack();
            work.last().pose().set(pose.pose());
            work.last().normal().set(pose.normal());
            this.fallbackModel.renderToBuffer(work, consumer, packedLight, OverlayTexture.NO_OVERLAY,
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