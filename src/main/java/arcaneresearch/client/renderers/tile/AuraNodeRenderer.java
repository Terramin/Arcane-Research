package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.common.tiles.aura.AuraNodeTile;

@OnlyIn(Dist.CLIENT)
public class AuraNodeRenderer implements BlockEntityRenderer<AuraNodeTile> {
    private static final Vector3f CENTER = new Vector3f(0.5F, 0.5F, 0.5F);

    public AuraNodeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AuraNodeTile tile, float partialTicks, PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Minecraft mc = Minecraft.getInstance();
        Entity camEntity = mc.getCameraEntity();

        LivingEntity viewer = camEntity instanceof LivingEntity le ? le : null;
        Player player = viewer instanceof Player p ? p : null;

        Level level = tile.getLevel();
        BlockPos pos = tile.getBlockPos();

        poseStack.pushPose();

        AuraNodeRenderHelper.renderNode(poseStack, buffer, tile, pos, viewer,
                CENTER, 1.0F,
                AuraNodeRenderHelper.resolve(player, false), partialTicks);

        if (level != null) {
            AuraNodeRenderHelper.drawDrainBeam(poseStack, buffer, pos, CENTER,
                    tile.getDrainTarget(), tile.getDrainAspect(),
                    level.getGameTime(), tile.getLastTapGameTime(), partialTicks);

            BlockPos flowFrom = tile.getHungryFlowFrom();

            if (flowFrom != null && level.getGameTime() - tile.getHungryFlowTime() < 12L) {
                Vector3f from = new Vector3f(
                        flowFrom.getX() - pos.getX() + 0.5F,
                        flowFrom.getY() - pos.getY() + 0.5F,
                        flowFrom.getZ() - pos.getZ() + 0.5F);

                int flowColor = 0xFFFFFF;

                AspectList aspects = tile.getAspects();

                if (aspects != null) {
                    Aspect[] flowAspects = aspects.getAspects();

                    if (flowAspects.length > 0 && flowAspects[0] != null) {
                        flowColor = flowAspects[0].getColor();
                    }
                }

                AuraNodeRenderHelper.drawFloatyLine(poseStack, buffer, from, CENTER,
                        flowColor, -0.05F, 1.0F, 0.12F);
            }
        }

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull AuraNodeTile tile) {
        return true;
    }
}