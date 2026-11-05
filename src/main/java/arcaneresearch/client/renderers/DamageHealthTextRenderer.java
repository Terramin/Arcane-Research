package arcaneresearch.client.renderers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import arcaneresearch.api.items.IGoggles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DamageHealthTextRenderer {

    private static final Minecraft minecraft = Minecraft.getInstance();
    private static final List<TextDisplay> textDisplays = new CopyOnWriteArrayList<>();
    private static final Random random = new Random();

    private static boolean hasGogglesEquipped() {
        Player player = minecraft.player;
        if (player == null) return false;

        if (player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof IGoggles) {
            return true;
        }
        return CuriosApi.getCuriosInventory(player)
                .resolve()
                .map(handler -> handler.findFirstCurio(stack -> stack.getItem() instanceof IGoggles).isPresent())
                .orElse(false);
    }

    public static void showText(String text, BlockPos pos, float scale, int color, double time) {
        final double randomX = -0.2 + 1.0 * random.nextDouble();
        final double randomZ = -0.2 + 1.0 * random.nextDouble();
        textDisplays.add(new TextDisplay(text, pos, System.currentTimeMillis(), scale, color, time, randomX, randomZ));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamageEvent(LivingDamageEvent event) {
        if (event.getEntity() == null) return;
        if (event.getEntity().level().isClientSide()) return;
        if (!hasGogglesEquipped()) return;

        BlockPos pos = event.getEntity().blockPosition();
        String formattedText = (event.getAmount() < 10000)
                ? "-" + String.format("%.1f", event.getAmount())
                : "Infinitely";
        showText(formattedText, pos, 0.02F, 0xFFFF0000, 2);
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingHealEvent(LivingHealEvent event) {
        if (event.getEntity() == null) return;
        if (event.getEntity().level().isClientSide()) return;
        if (!hasGogglesEquipped()) return; // <-- фильтр

        BlockPos pos = event.getEntity().blockPosition();
        String formattedText = (event.getAmount() < 10000)
                ? "+" + String.format("%.1f", event.getAmount())
                : "Infinitely";
        showText(formattedText, pos, 0.01F, 0xFF00FF00, 1.4);
    }

    @SubscribeEvent
    public static void onRenderLevelLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

        if (!hasGogglesEquipped()) {
            if (!textDisplays.isEmpty()) textDisplays.clear();
            return;
        }

        try {
            Vec3 cameraPos = minecraft.gameRenderer.getMainCamera().getPosition();
            Frustum frustum = minecraft.levelRenderer.getFrustum();
            long currentTime = System.currentTimeMillis();

            for (TextDisplay td : textDisplays) {
                long elapsedTime = currentTime - td.displayStartTime;

                if (elapsedTime >= td.time * 1000) {
                    textDisplays.remove(td);
                    continue;
                }

                double x = td.pos.getX() + 0.5 - cameraPos.x;
                double z = td.pos.getZ() + 0.5 - cameraPos.z;
                double yOffset = Math.sin(elapsedTime / 1000.0) * 0.5;
                double y = td.pos.getY() + yOffset - cameraPos.y;
                double distance = Math.sqrt(x * x + y * y + z * z);

                if (distance > 64 || !frustum.isVisible(new AABB(td.pos))) continue;

                PoseStack poseStack = event.getPoseStack();
                Font font = minecraft.font;
                MultiBufferSource bufferSource = minecraft.renderBuffers().bufferSource();

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                poseStack.pushPose();
                poseStack.translate(x + td.randomX, y + 1, z + td.randomZ);
                poseStack.mulPose(minecraft.gameRenderer.getMainCamera().rotation());
                poseStack.scale(-td.scale, -td.scale, td.scale);

                float textWidth = font.width(td.text) / 2.0F;
                poseStack.translate(-textWidth, 0, 0);

                font.drawInBatch(
                        td.text, 0, 0, td.color, false,
                        poseStack.last().pose(), bufferSource,
                        Font.DisplayMode.NORMAL, 0, 0xF000F0);

                RenderSystem.disableBlend();
                poseStack.popPose();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class TextDisplay {
        final String text;
        final BlockPos pos;
        final long displayStartTime;
        final float scale;
        final int color;
        final double time;
        final double randomX;
        final double randomZ;

        TextDisplay(String text, BlockPos pos, long displayStartTime, float scale,
                    int color, double time, double randomX, double randomZ) {
            this.text = text;
            this.pos = pos;
            this.displayStartTime = displayStartTime;
            this.scale = scale;
            this.color = color;
            this.time = time;
            this.randomX = randomX;
            this.randomZ = randomZ;
        }
    }
}