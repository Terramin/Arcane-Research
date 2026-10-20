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
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.items.IGoggles;
import arcaneresearch.client.models.armor.GogglesHeadModel;
import arcaneresearch.common.items.armor.ItemFortressArmor;
import arcaneresearch.common.lib.compat.CuriosCompat;
import java.util.List;
@OnlyIn(Dist.CLIENT)
public class GogglesCurioLayer extends RenderLayer<Player, PlayerModel<Player>> {
    private static final ResourceLocation GOGGLES_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/entity/armor/goggles.png");
    private final GogglesHeadModel model;
    public GogglesCurioLayer(RenderLayerParent<Player, PlayerModel<Player>> parent) {
        super(parent);
        this.model = new GogglesHeadModel(Minecraft.getInstance().getEntityModels()
                .bakeLayer(GogglesHeadModel.LAYER_LOCATION));
    }
    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Player player,
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
        this.model.copyHeadPose(getParentModel().head);
        VertexConsumer consumer = buffer.getBuffer(RenderType.armorCutoutNoCull(GOGGLES_TEXTURE));
        this.model.renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
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
