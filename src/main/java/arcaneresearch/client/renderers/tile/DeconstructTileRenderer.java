package arcaneresearch.client.renderers.tile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.lib.GhostItem;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.common.tiles.crafting.DeconstructTile;
import arcaneresearch.init.ModItems;
import org.joml.Matrix4f;
@OnlyIn(Dist.CLIENT)
public class DeconstructTileRenderer implements BlockEntityRenderer<DeconstructTile> {
    private static final float TAG_SCALE = 0.024F;
    private static final float GHOST_ALPHA = 0.75F;
    private final ItemRenderer itemRenderer;
    public DeconstructTileRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }
    @Override
    public void render(DeconstructTile tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = tile.getLevel();
        if (level == null) {
            return;
        }
        float ticks = (Minecraft.getInstance().player != null
                ? Minecraft.getInstance().player.tickCount : level.getGameTime()) + partialTicks;
        if (ModItems.THAUMOMETER.get() != null) {
            float scale = 0.640625F;
            float total = scale * 0.35F;
            float comp = 0.5F * total;
            poseStack.pushPose();
            poseStack.translate(0.5F + comp, 1.024F + comp, 0.5F + comp);
            poseStack.scale(scale, scale, scale);
            itemRenderer.renderStatic(new ItemStack(ModItems.THAUMOMETER.get()), ItemDisplayContext.GROUND,
                    packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, level, 0);
            poseStack.popPose();
        }
        ItemStack stack = tile.getItem(0);
        if (!stack.isEmpty()) {
            ItemStack renderStack = stack.copy();
            renderStack.setCount(1);
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.15F + Mth.sin(ticks / 14.0F) * 0.1F + 0.1F, 0.5F);
            poseStack.mulPose(Axis.YP.rotationDegrees(ticks % 360.0F));
            GhostItem.render(renderStack, ItemDisplayContext.GROUND, GHOST_ALPHA,
                    poseStack, buffer, level, packedLight);
            poseStack.popPose();
        }
        Aspect aspect = tile.currentAspect();
        if (aspect != null && aspect.getImage() != null) {
            int rgb = aspect.getColor();
            float r = ((rgb >> 16) & 0xFF) / 255.0F;
            float g = ((rgb >> 8) & 0xFF) / 255.0F;
            float b = (rgb & 0xFF) / 255.0F;
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.081F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(ticks % 360.0F));
            poseStack.scale(TAG_SCALE, TAG_SCALE, TAG_SCALE);
            VertexConsumer consumer = buffer.getBuffer(
                    RenderType.entityTranslucent(UtilsFX.strippedAspectTexture(aspect.getImage())));
            Matrix4f matrix = poseStack.last().pose();
            consumer.vertex(matrix, -8.0F, -8.0F, 0.0F).color(r, g, b, 0.8F).uv(0.0F, 0.0F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0.0F, 0.0F, 1.0F).endVertex();
            consumer.vertex(matrix, -8.0F, 8.0F, 0.0F).color(r, g, b, 0.8F).uv(0.0F, 1.0F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0.0F, 0.0F, 1.0F).endVertex();
            consumer.vertex(matrix, 8.0F, 8.0F, 0.0F).color(r, g, b, 0.8F).uv(1.0F, 1.0F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0.0F, 0.0F, 1.0F).endVertex();
            consumer.vertex(matrix, 8.0F, -8.0F, 0.0F).color(r, g, b, 0.8F).uv(1.0F, 0.0F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0.0F, 0.0F, 1.0F).endVertex();
            poseStack.popPose();
        }
    }
}
