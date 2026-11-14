package arcaneresearch.client.renderers.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.items.IGoggles;
import arcaneresearch.client.lib.obj.MeshLoader;
import arcaneresearch.client.lib.obj.MeshModel;
import arcaneresearch.client.lib.obj.MeshPart;
import arcaneresearch.client.models.armor.GogglesHeadModel;
import arcaneresearch.common.items.armor.ItemFortressArmor;
import arcaneresearch.common.lib.compat.CuriosCompat;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class GogglesCurioLayer extends RenderLayer<Player, PlayerModel<Player>> {
    private static final ResourceLocation GOGGLES_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/entity/armor/googles_obj.png");
    private static final ResourceLocation GOGGLES_OBJ =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "models/obj/googles_head.obj");
    private static final String HEAD_BOX_GROUP = "goggles_head_cube";
    private static final float OBJ_SCALE_Y = 0.8F;
    private static final float OBJ_SCALE_XZ = 0.85F;
    private static final float OBJ_SHIFT_DOWN_PX = 1.0F;

    private static final float EXTRA_Z_OFFSET = 0.001F;

    private static RenderType renderType(ResourceLocation tex) {
        // стандартный рендеринг cutout без ZOffset: фикс Z-fighting сделан в самой модели
        return RenderType.entityCutoutNoCull(tex);
    }

    private static volatile MeshModel cachedMesh;
    private final GogglesHeadModel fallbackModel;

    public GogglesCurioLayer(RenderLayerParent<Player, PlayerModel<Player>> parent) {
        super(parent);
        this.fallbackModel = new GogglesHeadModel(Minecraft.getInstance().getEntityModels()
                .bakeLayer(GogglesHeadModel.LAYER_LOCATION));
        String forgeVersion = net.minecraftforge.fml.ModList.get()
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
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, Player player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (player.isInvisible()) {
            return;
        }
        ItemStack helm = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!helm.isEmpty()) {
            if (helm.getItem() instanceof IGoggles) {
                return;
            }
            if (helm.getItem() instanceof ItemFortressArmor && ItemFortressArmor.hasGoggles(helm)) {
                return;
            }
        }
        List<ItemStack> headCurios = CuriosCompat.findHeadSlotItems(player,
                s -> !s.isEmpty() && s.getItem() instanceof IGoggles);
        if (headCurios.isEmpty()) {
            return;
        }

        VertexConsumer consumer = buffer.getBuffer(renderType(GOGGLES_TEXTURE));
        MeshModel mesh = mesh();
        if (mesh == null || mesh.positions == null || mesh.texCoords == null) {
            this.fallbackModel.copyHeadPose(getParentModel().head);
            this.fallbackModel.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                    1.0F, 1.0F, 1.0F, 1.0F);
            return;
        }

        poseStack.pushPose();
        try {
            getParentModel().head.translateAndRotate(poseStack);
            poseStack.translate(0.0F, 0.0F, -EXTRA_Z_OFFSET);
            poseStack.scale(OBJ_SCALE_XZ, -OBJ_SCALE_Y, OBJ_SCALE_XZ);
            poseStack.translate(0.0F, -OBJ_SHIFT_DOWN_PX / 16.0F / OBJ_SCALE_Y, 0.0F);

            Matrix4f pose = poseStack.last().pose();
            Matrix3f normal = poseStack.last().normal();
            for (MeshPart part : mesh.parts) {
                if (HEAD_BOX_GROUP.equals(part.name)) {
                    continue;
                }
                List<int[]> indices = part.indices;
                for (int i = 0; i + 3 < indices.size(); i += 4) {
                    emitVertex(consumer, pose, normal, mesh, indices.get(i), packedLight);
                    emitVertex(consumer, pose, normal, mesh, indices.get(i + 1), packedLight);
                    emitVertex(consumer, pose, normal, mesh, indices.get(i + 2), packedLight);
                    emitVertex(consumer, pose, normal, mesh, indices.get(i + 3), packedLight);
                }
            }
        } finally {
            poseStack.popPose();
        }
    }

    private static void emitVertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal,
                                   MeshModel mesh, int[] idx, int packedLight) {
        Vector3f position = mesh.positions.get(idx[0]);
        float u = 0.0F, v = 0.0F;
        if (mesh.texCoords != null && idx.length >= 2) {
            Vector2f tex = mesh.texCoords.get(idx[1]);
            u = tex.x;
            v = 1.0F - tex.y;
        }
        consumer.vertex(pose, position.x, position.y, position.z)
                .color(1.0F, 1.0F, 1.0F, 1.0F)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight);

        Vector3f n;
        if (mesh.normals != null && idx.length >= 3) {
            n = mesh.normals.get(idx[2]);
        } else {
            n = new Vector3f(0.0F, 1.0F, 0.0F);
        }
        float len = (float) Math.sqrt(n.x * n.x + n.y * n.y + n.z * n.z);
        if (len > 1.0e-4F) {
            n.x /= len;
            n.y /= len;
            n.z /= len;
        }
        consumer.normal(normal, n.x, n.y, n.z);
        consumer.endVertex();
    }

    @Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class Setup {
        @SuppressWarnings({"rawtypes", "unchecked"})
        @SubscribeEvent
        public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
            for (String skin : event.getSkins()) {
                net.minecraft.client.renderer.entity.LivingEntityRenderer renderer = event.getSkin(skin);
                if (renderer != null) {
                    renderer.addLayer(new GogglesCurioLayer(renderer));
                }
            }
        }
    }
}