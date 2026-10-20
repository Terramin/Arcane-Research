package arcaneresearch.client.renderers.tile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.common.tiles.devices.TileRechargePedestal;
@OnlyIn(Dist.CLIENT)
public class RechargePedestalRenderer implements BlockEntityRenderer<TileRechargePedestal> {
    private final ItemRenderer itemRenderer;
    public RechargePedestalRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }
    @Override
    public void render(TileRechargePedestal tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = tile.getLevel();
        if (level == null) return;
        ItemStack stack = tile.getSyncedStackInSlot(0);
        if (stack.isEmpty()) return;
        float ticks;
        if (Minecraft.getInstance().player != null) {
            ticks = Minecraft.getInstance().player.tickCount + partialTicks;
        } else {
            ticks = level.getGameTime() + partialTicks;
        }
        ItemStack renderStack = stack.copy();
        renderStack.setCount(1);
        BakedModel model = itemRenderer.getModel(renderStack, level, null, 0);
        float bob = Mth.sin(ticks / 10.0f) * 0.1f + 0.1f;
        float lift = bob + 0.25f * model.getTransforms().ground.scale.y() + 0.05f;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.75, 0.5);
        poseStack.scale(1.5f, 1.5f, 1.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(ticks % 360.0f));
        poseStack.translate(0.0, lift, 0.0);
        itemRenderer.renderStatic(renderStack, ItemDisplayContext.GROUND, packedLight,
                OverlayTexture.NO_OVERLAY, poseStack, buffer, level, 0);
        poseStack.popPose();
    }
}
