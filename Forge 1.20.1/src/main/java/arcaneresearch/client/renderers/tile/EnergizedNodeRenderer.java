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
import arcaneresearch.common.tiles.aura.TileNodeEnergized;
@OnlyIn(Dist.CLIENT)
public class EnergizedNodeRenderer implements BlockEntityRenderer<TileNodeEnergized> {
    public EnergizedNodeRenderer(BlockEntityRendererProvider.Context context) {
    }
    @Override
    public void render(TileNodeEnergized tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        net.minecraft.world.entity.Entity camEntity = Minecraft.getInstance().getCameraEntity();
        LivingEntity viewer = camEntity instanceof LivingEntity ? (LivingEntity) camEntity : null;
        Player player = viewer instanceof Player ? (Player) viewer : null;
        poseStack.pushPose();
        AuraNodeRenderHelper.renderNode(poseStack, buffer, tile.getAuraBase(), tile.getNodeType(),
                tile.getNodeModifier(), tile.getBlockPos(), viewer,
                new Vector3f(0.5F, 0.5F, 0.5F), 1.0F,
                AuraNodeRenderHelper.resolve(player, true), partialTicks);
        poseStack.popPose();
    }
    @Override
    public boolean shouldRenderOffScreen(TileNodeEnergized tile) {
        return true;
    }
}
