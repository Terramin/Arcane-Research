package arcaneresearch.client.renderers.tile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.common.tiles.crafting.TilePedestal;
@OnlyIn(Dist.CLIENT)
public class PedestalRenderer implements BlockEntityRenderer<TilePedestal> {
    private final ItemRenderer itemRenderer;
    public PedestalRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }
    @Override
    public void render(TilePedestal tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ItemStack stack = tile.getItem(0);
        if (stack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        double itemHeight = 1.28;
        if (tile.getBlockState().getBlock()
                instanceof arcaneresearch.common.blocks.devices.BlockPedestal pedestalBlock) {
            itemHeight = pedestalBlock.getItemRenderHeight();
        }
        poseStack.translate(0.5, itemHeight, 0.5);
        long time = tile.getLevel() != null ? tile.getLevel().getGameTime() : 0;
        float rotation = (time + partialTicks) % 360.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
                packedLight, packedOverlay, poseStack, buffer,
                tile.getLevel(), 0);
        poseStack.popPose();
    }
}
