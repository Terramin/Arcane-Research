package arcaneresearch.client.renderers.tile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import arcaneresearch.common.tiles.aura.AuraNodeTile;
@OnlyIn(Dist.CLIENT)
public class AuraNodeRenderer implements BlockEntityRenderer<AuraNodeTile> {
    public AuraNodeRenderer(BlockEntityRendererProvider.Context context) {
    }
    @Override
    public void render(AuraNodeTile tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        net.minecraft.world.entity.Entity camEntity = Minecraft.getInstance().getCameraEntity();
        LivingEntity viewer = camEntity instanceof LivingEntity ? (LivingEntity) camEntity : null;
        Player player = viewer instanceof Player ? (Player) viewer : null;
        poseStack.pushPose();
        Vector3f center = new Vector3f(0.5F, 0.5F, 0.5F);
        AuraNodeRenderHelper.renderNode(poseStack, buffer, tile, tile.getBlockPos(), viewer,
                center, 1.0F,
                AuraNodeRenderHelper.resolve(player, false), partialTicks);
        if (tile.getLevel() != null) {
            AuraNodeRenderHelper.drawDrainBeam(poseStack, buffer, tile.getBlockPos(), center,
                    tile.getDrainTarget(), tile.getDrainAspect(),
                    tile.getLevel().getGameTime(), tile.getLastTapGameTime(), partialTicks);
            net.minecraft.core.BlockPos flowFrom = tile.getHungryFlowFrom();
            if (flowFrom != null && tile.getLevel().getGameTime() - tile.getHungryFlowTime() < 12L) {
                Vector3f from = new Vector3f(
                        flowFrom.getX() - tile.getBlockPos().getX() + 0.5F,
                        flowFrom.getY() - tile.getBlockPos().getY() + 0.5F,
                        flowFrom.getZ() - tile.getBlockPos().getZ() + 0.5F);
                int flowColor = 0xFFFFFF;
                arcaneresearch.api.aspects.Aspect[] flowAspects = tile.getAspects().getAspects();
                if (flowAspects.length > 0 && flowAspects[0] != null) {
                    flowColor = flowAspects[0].getColor();
                }
                AuraNodeRenderHelper.drawFloatyLine(poseStack, buffer, from, center,
                        flowColor, -0.05F, 1.0F, 0.12F);
            }
        }
        poseStack.popPose();
    }
    @Override
    public boolean shouldRenderOffScreen(AuraNodeTile tile) {
        return true;
    }
}
