package arcaneresearch.client.renderers.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.client.models.block.JarNodeModel;
import arcaneresearch.common.tiles.aura.TileJarNode;

@OnlyIn(Dist.CLIENT)
public class JarNodeRenderer implements BlockEntityRenderer<TileJarNode> {
    private static final ResourceLocation JAR_TEX =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/models/jar.png");
    private static final RenderType LID_TYPE = RenderType.entityCutout(JAR_TEX);
    private static final RenderType GLASS_TYPE = RenderType.entityTranslucent(JAR_TEX);
    private static final Quaternionf ROT_XP_180 = Axis.XP.rotationDegrees(180.0F);
    private static final Vector3f CENTER = new Vector3f(0.5F, 0.4F, 0.5F);

    private final JarNodeModel model;

    public JarNodeRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new JarNodeModel(context.bakeLayer(JarNodeModel.LAYER_LOCATION));
    }

    @Override
    public void render(@NotNull TileJarNode tile, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (model == null) return;
        BlockPos pos = tile.getBlockPos();
        Level level = tile.getLevel();

        Minecraft mc = Minecraft.getInstance();
        Entity camEntity = mc.getCameraEntity();
        LivingEntity viewer = null;
        if (camEntity instanceof LivingEntity living) {
            viewer = living;
        }
        Player player = null;
        if (viewer instanceof Player p) {
            player = p;
        }

        poseStack.pushPose();
        try {
            AuraNodeRenderHelper.renderNode(poseStack, buffer, tile, pos, viewer,
                    CENTER, 0.7F,
                    AuraNodeRenderHelper.resolve(player, true), partialTicks);
            if (level != null) {
                AuraNodeRenderHelper.drawDrainBeam(poseStack, buffer, pos, CENTER,
                        tile.getDrainTarget(), tile.getDrainAspect(),
                        level.getGameTime(), tile.getLastTapGameTime(), partialTicks);
            }
        } finally {
            poseStack.popPose();
        }

        poseStack.pushPose();
        try {
            poseStack.translate(0.5D, 0.01D, 0.5D);
            poseStack.mulPose(ROT_XP_180);
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
            VertexConsumer lid = buffer.getBuffer(LID_TYPE);
            model.renderLid(poseStack, lid, packedLight, packedOverlay);
            VertexConsumer glass = buffer.getBuffer(GLASS_TYPE);
            model.renderCore(poseStack, glass, packedLight, packedOverlay, 0.55F);
        } finally {
            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(@NotNull TileJarNode tile) {
        return true;
    }
}