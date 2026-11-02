package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import arcaneresearch.common.tiles.aura.TransducerTile;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class TransducerRenderer implements BlockEntityRenderer<TransducerTile> {
    public TransducerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(@NotNull TransducerTile tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = tile.getLevel();
        if (level == null) return;
        int scaled = tile.count * 50 / 1000;
        if (scaled < 0) {
            scaled = 0;
        } else if (scaled > 50) {
            scaled = 50;
        }
        NodeConverterRenderer.renderDevice(poseStack, buffer,
                scaled, tile.status, packedLight, packedOverlay);
    }
}