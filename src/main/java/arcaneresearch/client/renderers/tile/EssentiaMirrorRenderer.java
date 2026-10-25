package arcaneresearch.client.renderers.tile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.common.tiles.devices.TileMirrorEssentia;
@OnlyIn(Dist.CLIENT)
public class EssentiaMirrorRenderer implements BlockEntityRenderer<TileMirrorEssentia> {
    public EssentiaMirrorRenderer(BlockEntityRendererProvider.Context context) {
    }
    @Override
    public void render(TileMirrorEssentia tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        MirrorRenderer.renderMirror(tile.getBlockPos(), tile.getLevel(), tile.getBlockState(),
                tile.linked, MirrorRenderer.frame2Texture(),
                partialTicks, poseStack, buffer, packedLight);
    }
}
