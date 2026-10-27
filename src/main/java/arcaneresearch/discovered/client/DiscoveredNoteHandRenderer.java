package arcaneresearch.discovered.client;

import arcaneresearch.helper.IDsHelper;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.discovered.note.ItemDiscoveredResearchNote;
import arcaneresearch.discovered.note.DiscoveredNoteData;
import arcaneresearch.discovered.note.DiscoveredNotes;

@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DiscoveredNoteHandRenderer {
    private static final ResourceLocation PARCHMENT =
            IDsHelper.mod("textures/misc/parchment.png");
    private static final ResourceLocation UNKNOWN_TAG =
            IDsHelper.mod("textures/aspects/_unknown.png");
    private static final int TEXT_COLOR = 0xFF685E4A;

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        AbstractClientPlayer player = mc.player;
        if (player == null) {
            return;
        }
        ItemStack off = player.getOffhandItem();
        if (!off.isEmpty()) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }
        if (!(stack.getItem() instanceof ItemDiscoveredResearchNote)) {
            return;
        }
        event.setCanceled(true);
        PoseStack pose = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        int light = event.getPackedLight();
        float swing = event.getSwingProgress();
        float sqrtSwing = Mth.sqrt(swing);
        pose.pushPose();
        float ySwing = -0.2F * Mth.sin(swing * Mth.PI);
        float zSwing = -0.4F * Mth.sin(sqrtSwing * Mth.PI);
        pose.translate(0.0F, -ySwing / 2.0F, zSwing);
        float tilt = mapTilt(event.getInterpolatedPitch());
        pose.translate(0.0F, 0.04F + event.getEquipProgress() * -1.2F + tilt * -0.5F, -0.72F);
        pose.mulPose(Axis.XP.rotationDegrees(tilt * -85.0F));
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(90.0F));
        renderMapHand(mc, pose, buffer, light, HumanoidArm.RIGHT, player);
        renderMapHand(mc, pose, buffer, light, HumanoidArm.LEFT, player);
        pose.popPose();
        pose.mulPose(Axis.XP.rotationDegrees(Mth.sin(sqrtSwing * Mth.PI) * 20.0F));
        pose.scale(2.0F, 2.0F, 2.0F);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
        pose.scale(0.38F, 0.38F, 0.38F);
        pose.translate(-0.5F, -0.5F, 0.0F);
        renderNotes(mc, stack, pose, buffer, light);
        pose.popPose();
        if (buffer instanceof MultiBufferSource.BufferSource source) {
            source.endBatch();
        }
    }

    private static float mapTilt(float xRot) {
        float tilt = Mth.clamp(1.0F - xRot / 45.0F + 0.1F, 0.0F, 1.0F);
        return -Mth.cos(tilt * Mth.PI) * 0.5F + 0.5F;
    }

    private static void renderMapHand(Minecraft mc, PoseStack pose, MultiBufferSource buffer,
                                      int light, HumanoidArm arm, AbstractClientPlayer player) {
        if (mc == null || player == null) {
            return;
        }
        var dispatcher = mc.getEntityRenderDispatcher();
        var renderer = dispatcher.getRenderer(player);
        if (!(renderer instanceof PlayerRenderer playerRenderer)) {
            return;
        }
        pose.pushPose();
        float invert = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        pose.mulPose(Axis.YP.rotationDegrees(92.0F));
        pose.mulPose(Axis.XP.rotationDegrees(45.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(invert * -41.0F));
        pose.translate(invert * 0.3F, -1.1F, 0.45F);
        if (arm == HumanoidArm.RIGHT) {
            playerRenderer.renderRightHand(pose, buffer, light, player);
        } else {
            playerRenderer.renderLeftHand(pose, buffer, light, player);
        }
        pose.popPose();
    }

    private static void quad(PoseStack pose, MultiBufferSource buffer, RenderType type,
                             float x0, float y0, float x1, float y1,
                             int r, int g, int b, int a, int light) {
        VertexConsumer vc = buffer.getBuffer(type);
        var m = pose.last().pose();
        vc.vertex(m, x0, y1, (float) 0.0).color(r, g, b, a).uv((float) 0.0, (float) 1.0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, -1.0F).endVertex();
        vc.vertex(m, x1, y1, (float) 0.0).color(r, g, b, a).uv((float) 1.0, (float) 1.0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, -1.0F).endVertex();
        vc.vertex(m, x1, y0, (float) 0.0).color(r, g, b, a).uv((float) 1.0, (float) 0.0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, -1.0F).endVertex();
        vc.vertex(m, x0, y0, (float) 0.0).color(r, g, b, a).uv((float) 0.0, (float) 0.0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, -1.0F).endVertex();
    }

    private static void text(Minecraft mc, PoseStack pose, MultiBufferSource buffer,
                             FormattedCharSequence text, float x, float y, int light) {
        if (mc == null || text == null) {
            return;
        }
        mc.font.drawInBatch(text, x, y, TEXT_COLOR, false,
                pose.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, light);
    }

    private static Component scrambled(Component in) {
        if (in == null) {
            return Component.empty();
        }
        return in.copy().withStyle(s -> s.withObfuscated(true));
    }

    private static void renderNotes(Minecraft mc, ItemStack stack, PoseStack pose,
                                    MultiBufferSource buffer, int light) {
        if (mc == null || stack == null || pose == null || buffer == null) {
            return;
        }
        Font font = mc.font;
        pose.scale(0.0078125F, 0.0078125F, 0.0078125F);
        quad(pose, buffer, RenderType.text(PARCHMENT),
                -7.0F, -7.0F, 135.0F, 135.0F,
                255, 255, 255, 255, light);
        DiscoveredNoteData data = DiscoveredNoteData.read(stack);
        if (data.key == null) {
            return;
        }
        Aspect[] tags = data.tags;
        int[] haveArr = data.have;
        int[] needArr = data.need;
        int haveLen = haveArr == null ? 0 : haveArr.length;
        int needLen = needArr == null ? 0 : needArr.length;
        float total = data.getTotalProgress();
        Component name = DiscoveredNotes.researchName(data.key);
        Component title = name == null ? Component.literal(data.key) : name;
        if (total < 0.2F) {
            title = scrambled(title);
        }
        pose.pushPose();
        pose.translate(10.0F, 10.0F, -0.04F);
        int width = font.width(title);
        if (width > 110) {
            float scale = 110.0F / width;
            pose.scale(scale, scale, scale);
        }
        text(mc, pose, buffer, title.getVisualOrderText(), 0.0F, 0.0F, light);
        pose.popPose();
        int index = 0;
        if (tags != null) {
            for (int a = 0; a < tags.length; a++) {
                Aspect tag = tags[a];
                if (tag == null) {
                    continue;
                }
                int have = a < haveLen ? haveArr[a] : 0;
                if (have <= 0 && total < 0.5F) {
                    continue;
                }
                float tagProgress = data.getTagProgress(tag);
                boolean known = tagProgress >= 0.3F;
                ResourceLocation icon;
                if (known) {
                    ResourceLocation image = tag.getImage();
                    icon = image == null ? UNKNOWN_TAG : UtilsFX.strippedAspectTexture(image);
                    if (icon == null) {
                        icon = UNKNOWN_TAG;
                    }
                } else {
                    icon = UNKNOWN_TAG;
                }
                float x = 15.0F + (index % 4) * 30.0F;
                float y = 28.0F + ((float) index / 4) * 13.0F;
                int color = known ? tag.getColor() : 0xFFFFFF;
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = color & 0xFF;
                int alpha = known ? 150 : 255;
                pose.pushPose();
                pose.translate(x, y, -0.02F);
                pose.scale(6.0F, 6.0F, 3.0F);
                quad(pose, buffer, RenderType.text(icon),
                        -1.0F, -1.0F, 1.0F, 1.0F,
                        r, g, b, alpha, light);
                pose.popPose();
                if (tagProgress >= 0.333332F) {
                    int need = a < needLen ? needArr[a] : 0;
                    int completion = need <= 0 ? 100
                            : (int) ((float) Math.min(have, need) / need * 100.0F);
                    pose.pushPose();
                    pose.translate(x, y, -0.02F);
                    pose.scale(0.56F, 0.56F, 1.0F);
                    text(mc, pose, buffer,
                            Component.literal(completion + "%").getVisualOrderText(),
                            13.0F, -3.0F, light);
                    pose.popPose();
                }
                index++;
            }
        }
        Component desc = describe(data.key);
        if (desc != null) {
            float startY = 25.0F + (1 + (float) (index - 1) / 4) * 13.0F;
            float availH = Math.max(0.0F, 130.0F - startY);
            DiscoveredNoteText.Fit fit = DiscoveredNoteText.parchmentFit(
                    font, desc, total < 0.5F, 180, 0.63F, availH, 0.30F);
            List<FormattedCharSequence> lines = fit.lines();
            float scale = fit.scale();
            pose.pushPose();
            pose.translate(11.0F, startY, -0.02F);
            pose.scale(scale, scale, 1.0F);
            for (int i = 0; i < lines.size(); i++) {
                text(mc, pose, buffer, lines.get(i), 0.0F, i * font.lineHeight, light);
            }
            pose.popPose();
        }
    }

    private static Component describe(String key) {
        return DiscoveredNotes.researchText(key);
    }
}