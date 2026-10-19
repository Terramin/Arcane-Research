package arcaneresearch.client.renderers.tile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.block.JarNodeModel;
import arcaneresearch.common.tiles.aura.TileJarNode;
@OnlyIn(Dist.CLIENT)
public class JarNodeRenderer implements BlockEntityRenderer<TileJarNode> {
    private static final ResourceLocation JAR_TEX =
            new ResourceLocation(Arcaneresearch.MODID, "textures/models/jar.png");
    private final JarNodeModel model;
    public JarNodeRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new JarNodeModel(context.bakeLayer(JarNodeModel.LAYER_LOCATION));
    }
    @Override
    public void render(TileJarNode tile, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        net.minecraft.world.entity.Entity camEntity = Minecraft.getInstance().getCameraEntity();
        LivingEntity viewer = camEntity instanceof LivingEntity ? (LivingEntity) camEntity : null;
        Player player = viewer instanceof Player ? (Player) viewer : null;
        poseStack.pushPose();
        Vector3f center = new Vector3f(0.5F, 0.4F, 0.5F);
        AuraNodeRenderHelper.renderNode(poseStack, buffer, tile, tile.getBlockPos(), viewer,
                center, 0.7F,
                AuraNodeRenderHelper.resolve(player, true), partialTicks);
        if (tile.getLevel() != null) {
            AuraNodeRenderHelper.drawDrainBeam(poseStack, buffer, tile.getBlockPos(), center,
                    tile.getDrainTarget(), tile.getDrainAspect(),
                    tile.getLevel().getGameTime(), tile.getLastTapGameTime(), partialTicks);
        }
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.01D, 0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        long animate = tile.getAnimate();
        if (animate > 0L) {
            long now = System.currentTimeMillis();
            if (animate > now) {
                float size = 1.0F + 2.0F * (animate - now) / 1000.0F;
                poseStack.scale(size, size, size);
            } else {
                tile.setAnimate(0L);
            }
        }
        VertexConsumer lid = buffer.getBuffer(RenderType.entityCutout(JAR_TEX));
        this.model.renderLid(poseStack, lid, packedLight, packedOverlay);
        VertexConsumer glass = buffer.getBuffer(RenderType.entityTranslucent(JAR_TEX));
        this.model.renderCore(poseStack, glass, packedLight, packedOverlay, 0.55F);
        poseStack.popPose();
    }
    @Override
    public boolean shouldRenderOffScreen(TileJarNode tile) {
        return true;
    }
}
