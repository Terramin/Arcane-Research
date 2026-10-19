package arcaneresearch.client.renderers.tile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.common.tiles.aura.TransducerTile;
import arcaneresearch.client.renderers.tile.NodeConverterRenderer;
@OnlyIn(Dist.CLIENT)
public class TransducerRenderer implements BlockEntityRenderer<TransducerTile> {
    public TransducerRenderer(BlockEntityRendererProvider.Context context) {
    }
    @Override
    public void render(TransducerTile tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tile.getLevel() == null) {
            return;
        }
        int scaled = Math.max(0, Math.min(50, tile.count * 50 / 1000));
        NodeConverterRenderer.renderDevice(poseStack, buffer,
                scaled, tile.status, packedLight, packedOverlay);
    }
}
