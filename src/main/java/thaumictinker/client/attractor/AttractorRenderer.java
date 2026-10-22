package thaumictinker.client.attractor;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import thaumictinker.attractor.AttractorBlock;
import thaumictinker.attractor.AttractorBlockEntity;
import thaumictinker.attractor.CorporealAttractorBlockEntity;

 


 
public class AttractorRenderer implements BlockEntityRenderer<AttractorBlockEntity> {

    public AttractorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AttractorBlockEntity entity, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int light, int overlay) {
        if (entity.getLevel() == null) {
            return;
        }
        boolean corporeal = entity instanceof CorporealAttractorBlockEntity;
        boolean pull = entity.getBlockState().getValue(AttractorBlock.PULL);
        int redstone = AttractorBlockEntity.redstone(entity.getLevel(), entity.getBlockPos());
        AttractorRendering.submit(poseStack, bufferSource, corporeal, pull, redstone, light,
                entity.getLevel().getGameTime(), System.currentTimeMillis());
    }

    @Override
    public boolean shouldRenderOffScreen(AttractorBlockEntity entity) {
        return true;
    }
}
