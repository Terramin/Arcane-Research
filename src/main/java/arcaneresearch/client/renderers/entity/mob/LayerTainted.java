package arcaneresearch.client.renderers.entity.mob;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.common.entities.monster.mods.ChampionModifier;
@OnlyIn(Dist.CLIENT)
public class LayerTainted<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation TAINT_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/models/taint_fibres.png");
    public LayerTainted(RenderLayerParent<T, M> parent) {
        super(parent);
    }
    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (ChampionModifier.getChampionId(entity) != 13) {
            return;
        }
        boolean invisible = entity.isInvisible();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TAINT_TEXTURE));
        getParentModel().renderToBuffer(poseStack, consumer, invisible ? 0 : packedLight,
                OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, invisible ? 0.0f : 0.66f);
    }
    @Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class Setup {
        @SuppressWarnings({"rawtypes", "unchecked"})
        @SubscribeEvent
        public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
            for (String skin : event.getSkins()) {
                LivingEntityRenderer renderer = event.getSkin(skin);
                if (renderer != null) {
                    renderer.addLayer(new LayerTainted(renderer));
                }
            }
            for (Object renderer : Minecraft.getInstance().getEntityRenderDispatcher().renderers.values()) {
                if (renderer instanceof LivingEntityRenderer living) {
                    living.addLayer(new LayerTainted(living));
                }
            }
        }
    }
}
