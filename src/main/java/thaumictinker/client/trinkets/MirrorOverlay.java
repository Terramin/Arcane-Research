package thaumictinker.client.trinkets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import thaumictinker.kami.trinkets.WorldshaperGlassItem;

 





 
@Mod.EventBusSubscriber(modid = thaumictinker.TtCore.MODID, value = Dist.CLIENT)
public final class MirrorOverlay {

    private static final float ALPHA = 0x99 / 255.0F;

    private MirrorOverlay() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderType type = null;
        for (Player player : level.players()) {
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof WorldshaperGlassItem)) {
                continue;
            }
            Item item = WorldshaperGlassItem.getItem(stack);
            if (!(item instanceof BlockItem blockItem)) {
                continue;
            }
            List<BlockPos> targets = WorldshaperGlassItem.getBlocksToPlace(stack, player);
            if (targets.isEmpty() || !WorldshaperGlassItem.hasBlocks(stack, player, targets)) {
                continue;
            }
            BlockState state = blockItem.getBlock().defaultBlockState();
            TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper()
                    .getTexture(state, level, player.blockPosition());
            type = RenderType.entityTranslucent(sprite.atlasLocation());
            VertexConsumer consumer = buffers.getBuffer(type);
            for (BlockPos target : targets) {
                emitCube(consumer, poseStack,
                        (float) (target.getX() - camera.x),
                        (float) (target.getY() - camera.y),
                        (float) (target.getZ() - camera.z),
                        sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
            }
        }
        if (type != null) {
            buffers.endBatch(type);
        }
    }

    private static void emitCube(VertexConsumer consumer, PoseStack poseStack, float x, float y, float z,
                                 float u0, float v0, float u1, float v1) {
        float x1 = x + 1;
        float y1 = y + 1;
        float z1 = z + 1;
        
        quad(consumer, poseStack, x, y, z, x1, y, z, x1, y1, z, x, y1, z, u0, v1, u1, v1, u1, v0, u0, v0);
        
        quad(consumer, poseStack, x, y, z1, x, y1, z1, x1, y1, z1, x1, y, z1, u0, v0, u0, v1, u1, v1, u1, v0);
        
        quad(consumer, poseStack, x, y, z, x, y, z1, x1, y, z1, x1, y, z, u0, v0, u0, v1, u1, v1, u1, v0);
        
        quad(consumer, poseStack, x, y1, z, x, y1, z1, x1, y1, z1, x1, y1, z, u0, v0, u0, v1, u1, v1, u1, v0);
        
        quad(consumer, poseStack, x, y, z, x, y, z1, x, y1, z1, x, y1, z, u0, v0, u0, v1, u1, v1, u1, v0);
        
        quad(consumer, poseStack, x1, y, z, x1, y, z1, x1, y1, z1, x1, y1, z, u0, v0, u0, v1, u1, v1, u1, v0);
    }

    private static void quad(VertexConsumer consumer, PoseStack poseStack,
                             float x0, float y0, float z0, float x1, float y1, float z1,
                             float x2, float y2, float z2, float x3, float y3, float z3,
                             float u0, float v0, float u1, float v1, float u2, float v2,
                             float u3, float v3) {
        vertex(consumer, poseStack, x0, y0, z0, u0, v0);
        vertex(consumer, poseStack, x1, y1, z1, u1, v1);
        vertex(consumer, poseStack, x2, y2, z2, u2, v2);
        vertex(consumer, poseStack, x3, y3, z3, u3, v3);
    }

    private static void vertex(VertexConsumer consumer, PoseStack poseStack,
                               float x, float y, float z, float u, float v) {
        consumer.vertex(poseStack.last().pose(), x, y, z);
        consumer.uv(u, v);
        consumer.color(1.0F, 1.0F, 1.0F, ALPHA);
        consumer.uv2(0xF0);
        consumer.overlayCoords(0);
    }
}
