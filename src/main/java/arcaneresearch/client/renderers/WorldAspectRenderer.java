package arcaneresearch.client.renderers;


import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;


import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier; 
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Matrix4f;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;





public class WorldAspectRenderer {

    private static final int ROW_SIZE = 5;

      
    private static volatile float tagscale = 0f;

    private record TagRequest(double x, double y, double z, AspectList tags) {
    }

    private record TextRequest(double x, double y, double z, String text) {
    }

    private static final java.util.List<TagRequest> pendingTags = new java.util.ArrayList<>();
    private static final java.util.List<TextRequest> pendingTexts = new java.util.ArrayList<>();

    public static void tick() {
        if (tagscale > 0f) {
            tagscale -= 0.005f;
        }
    }

    public static void drawTagsOnContainer(PoseStack poseStack, double x, double y, double z, AspectList tags,
                                           Direction face, float partialTick) {
        if (tags == null || tags.size() == 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        if (tagscale < 0.5f) {
            tagscale += 0.031f - tagscale / 10f;
        }

        double bx = x + 0.5;
        double by = y + 0.5;
        double bz = z + 0.5;
        if (face != null) {
            bx += tagscale * 2.0 * face.getStepX();
            by += tagscale * 2.0 * face.getStepY();
            bz += tagscale * 2.0 * face.getStepZ();
        }
        synchronized (pendingTags) {
            pendingTags.add(new TagRequest(bx, by, bz, tags));
        }
    }

    public static void drawTagsAt(PoseStack poseStack, double x, double y, double z, AspectList tags) {
        if (tags == null || tags.size() == 0) {
            return;
        }
        if (tagscale < 0.5f) {
            tagscale += 0.031f - tagscale / 10f;
        }
        synchronized (pendingTags) {
            pendingTags.add(new TagRequest(x, y, z, tags));
        }
    }

    private static void drawTagRows(PoseStack poseStack,
                                    net.minecraft.client.renderer.SubmitNodeCollector collector,
                                    Vec3 cameraPos, double x, double y, double z, AspectList tags) {
        Minecraft mc = Minecraft.getInstance();

        java.util.List<Aspect> valid = new java.util.ArrayList<>();
        for (Aspect tag : tags.getAspects()) {
            if (tag != null && tag.getImage() != null) {
                valid.add(tag);
            }
        }
        if (valid.isEmpty()) {
            return;
        }

        
        
        
        
        float scale = Math.max(0.05f, tagscale);
        int total = valid.size();

        for (int i = 0; i < total; i++) {
            Aspect tag = valid.get(i);
            int row = i / ROW_SIZE;
            int col = i % ROW_SIZE;
            int rowSize = Math.min(ROW_SIZE, total - row * ROW_SIZE);

            float slotX = (col - rowSize / 2f + 0.5f) * 1.15f;
            float slotY = row * 1.15f;

            poseStack.pushPose();
            try {

                poseStack.translate(x - cameraPos.x, y - cameraPos.y, z - cameraPos.z);
                poseStack.rotate(mc.gameRenderer.mainCamera().rotation());
                poseStack.scale(scale, scale, scale);
                poseStack.translate(slotX, slotY, 0);

                drawAspectQuad(poseStack, collector, tag.getImage(), tag.getColor());
            } catch (Exception ignored) {
            } finally {
                poseStack.popPose();
            }
        }

        for (int i = 0; i < total; i++) {
            Aspect tag = valid.get(i);
            int row = i / ROW_SIZE;
            int col = i % ROW_SIZE;
            int rowSize = Math.min(ROW_SIZE, total - row * ROW_SIZE);
            float slotX = (col - rowSize / 2f + 0.5f) * 1.15f;
            float slotY = row * 1.15f;

            poseStack.pushPose();
            try {
                poseStack.translate(x - cameraPos.x, y - cameraPos.y, z - cameraPos.z);
                poseStack.rotate(mc.gameRenderer.mainCamera().rotation());
                poseStack.scale(scale, scale, scale);
                poseStack.translate(slotX, slotY, 0);

                drawAmountText(poseStack, collector, String.valueOf(tags.getAmount(tag)));
            } catch (Exception ignored) {
            } finally {
                poseStack.popPose();
            }
        }
    }

    private static void drawAspectQuad(PoseStack poseStack,
                                       net.minecraft.client.renderer.SubmitNodeCollector collector,
                                       Identifier texture, int color) {
        
        
        Identifier stripped = arcaneresearch.client.lib.UtilsFX.strippedAspectTexture(texture);

        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        net.minecraft.client.renderer.rendertype.RenderType type =
                TCRenderTypes.additiveTexturedNoCullNoDepth(stripped);
        
        
        
        
        
        
        
        collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
            consumer.addVertex(pose, -0.5f, -0.5f, 0).setUv(0, 1).setColor(r, g, b, 0.85f)
                    .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                    .setLight(0xF000F0).setNormal(pose, 0, 0, 1);
            consumer.addVertex(pose, 0.5f, -0.5f, 0).setUv(1, 1).setColor(r, g, b, 0.85f)
                    .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                    .setLight(0xF000F0).setNormal(pose, 0, 0, 1);
            consumer.addVertex(pose, 0.5f, 0.5f, 0).setUv(1, 0).setColor(r, g, b, 0.85f)
                    .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                    .setLight(0xF000F0).setNormal(pose, 0, 0, 1);
            consumer.addVertex(pose, -0.5f, 0.5f, 0).setUv(0, 0).setColor(r, g, b, 0.85f)
                    .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                    .setLight(0xF000F0).setNormal(pose, 0, 0, 1);
        });
    }

    private static void drawAmountText(PoseStack poseStack,
                                       net.minecraft.client.renderer.SubmitNodeCollector collector,
                                       String text) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        float textWidth = font.width(text) * 0.04f;
        poseStack.pushPose();
        
        
        
        poseStack.translate(0.48f - textWidth, -0.5f, 0.01f);
        poseStack.scale(-0.04f, -0.04f, 0.04f);
        
        
        
        
        
        
        
        poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(180.0F));
        net.minecraft.util.FormattedCharSequence seq =
                net.minecraft.network.chat.Component.literal(text).getVisualOrderText();
        
        
        
        
        
        
        
        
        
        
        collector.submitText(poseStack, 1, 1, seq, false, Font.DisplayMode.SEE_THROUGH,
                15728880, 0xFF111111, 0, 0);
        collector.submitText(poseStack, 0, 0, seq, false, Font.DisplayMode.SEE_THROUGH,
                15728880, 0xFFFFFFFF, 0, 0);
        poseStack.popPose();
    }

    public static void drawTextInAir(PoseStack poseStack, double x, double y, double z,
                                     float partialTick, String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || text == null) {
            return;
        }
        synchronized (pendingTexts) {
            pendingTexts.add(new TextRequest(x + 0.5, y + 0.5, z + 0.5, text));
        }
    }

    private static void emitText(PoseStack poseStack,
                                 net.minecraft.client.renderer.SubmitNodeCollector collector,
                                 Vec3 cameraPos, double x, double y, double z, String text) {
        Minecraft mc = Minecraft.getInstance();

        poseStack.pushPose();
        poseStack.translate(x - cameraPos.x, y - cameraPos.y, z - cameraPos.z);
        poseStack.rotate(mc.gameRenderer.mainCamera().rotation());
        poseStack.scale(-0.0125f, -0.0125f, 0.0125f);
        
        
        
        poseStack.rotate(com.mojang.math.Axis.YP.rotationDegrees(180.0F));

        Font font = mc.font;
        float w = font.width(text) / 2f;
        net.minecraft.util.FormattedCharSequence seq =
                net.minecraft.network.chat.Component.literal(text).getVisualOrderText();
        
        
        
        
        
        
        
        collector.submitText(poseStack, -w, 0, seq, true, Font.DisplayMode.SEE_THROUGH,
                15728880, 0xFFFFFFFF, 0, 0);
        poseStack.popPose();
    }

    @EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
    public static class Events {
        @SubscribeEvent
        public static void onSubmitGeometry(
                net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) {
                drainQueues();
                return;
            }
            Vec3 cameraPos = mc.gameRenderer.mainCamera().position();
            PoseStack poseStack = new PoseStack();
            net.minecraft.client.renderer.SubmitNodeCollector collector = event.getSubmitNodeCollector();
            synchronized (pendingTags) {
                for (TagRequest req : pendingTags) {
                    drawTagRows(poseStack, collector, cameraPos, req.x(), req.y(), req.z(), req.tags());
                }
                pendingTags.clear();
            }
            synchronized (pendingTexts) {
                for (TextRequest req : pendingTexts) {
                    emitText(poseStack, collector, cameraPos, req.x(), req.y(), req.z(), req.text());
                }
                pendingTexts.clear();
            }
        }

        private static void drainQueues() {
            synchronized (pendingTags) {
                pendingTags.clear();
            }
            synchronized (pendingTexts) {
                pendingTexts.clear();
            }
        }
    }
}
