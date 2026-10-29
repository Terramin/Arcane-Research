package arcaneresearch.discovered.client;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.discovered.note.DiscoveredNoteData;
import arcaneresearch.discovered.note.DiscoveredNotes;
import arcaneresearch.discovered.note.ItemDiscoveredResearchNote;

@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class DiscoveredNoteHandRenderer {

    private static final Identifier PARCHMENT =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/parchment.png");
    private static final Identifier UNKNOWN_TAG =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/aspects/_unknown.png");
    private static final int TEXT_COLOR = 0xFF685E4A;

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        ItemStack off = player.getOffhandItem();
        if (!off.isEmpty()) {
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemDiscoveredResearchNote)) {
            return;
        }
        event.setCanceled(true);
        PoseStack pose = event.getPoseStack();
        SubmitNodeCollector collector = event.getSubmitNodeCollector();
        PlayerRenderState playerState = event.getPlayerState();
        int light = event.getPackedLight();
        float swing = event.getSwingProgress();
        float sqrtSwing = Mth.sqrt(swing);
        pose.pushPose();
        float ySwing = -0.2F * Mth.sin(swing * Mth.PI);
        float zSwing = -0.4F * Mth.sin(sqrtSwing * Mth.PI);
        pose.translate(0.0F, -ySwing / 2.0F, zSwing);
        float tilt = mapTilt(event.getInterpolatedPitch());
        pose.translate(0.0F, 0.04F + event.getEquipProgress() * -1.2F + tilt * -0.5F, -0.72F);
        pose.rotateDegrees(Axis.XP, tilt * -85.0F);
        pose.pushPose();
        pose.rotateDegrees(Axis.YP, 90.0F);
        renderMapHand(mc, pose, collector, light, HumanoidArm.RIGHT, playerState);
        renderMapHand(mc, pose, collector, light, HumanoidArm.LEFT, playerState);
        pose.popPose();
        pose.rotateDegrees(Axis.XP, Mth.sin(sqrtSwing * Mth.PI) * 20.0F);
        pose.scale(2.0F, 2.0F, 2.0F);
        pose.rotateDegrees(Axis.YP, 180.0F);
        pose.rotateDegrees(Axis.ZP, 180.0F);
        pose.scale(0.38F, 0.38F, 0.38F);
        pose.translate(-0.5F, -0.5F, 0.0F);
        renderNotes(mc, stack, pose, collector, light);
        pose.popPose();
    }

    private static float mapTilt(float xRot) {
        float tilt = Mth.clamp(1.0F - xRot / 45.0F + 0.1F, 0.0F, 1.0F);
        return -Mth.cos(tilt * Mth.PI) * 0.5F + 0.5F;
    }

    private static void renderMapHand(Minecraft mc, PoseStack pose, SubmitNodeCollector collector,
                                      int light, HumanoidArm arm, PlayerRenderState playerState) {
        AvatarRenderState avatar = playerState == null ? null : playerState.avatarRenderState;
        if (avatar == null) {
            return;
        }
        pose.pushPose();
        float invert = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        pose.rotateDegrees(Axis.YP, 92.0F);
        pose.rotateDegrees(Axis.XP, 45.0F);
        pose.rotateDegrees(Axis.ZP, invert * -41.0F);
        pose.translate(invert * 0.3F, -1.1F, 0.45F);
        AvatarRenderer<?> renderer = mc.getEntityRenderDispatcher().getRenderer(avatar);
        Identifier skin = avatar.skin.body().texturePath();
        if (arm == HumanoidArm.RIGHT) {
            renderer.renderRightHand(pose, collector, light, skin, avatar.showRightSleeve);
        } else {
            renderer.renderLeftHand(pose, collector, light, skin, avatar.showLeftSleeve);
        }
        pose.popPose();
    }

    private static void quad(PoseStack pose, OrderedSubmitNodeCollector collector, RenderType type,
                             float x0, float y0, float x1, float y1,
                             int r, int g, int b, int a, int light) {
        collector.submitCustomGeometry(pose, type, (p, vc) -> {
            vc.addVertex(p, x0, y1, (float) 0.0).setColor(r, g, b, a).setUv((float) 0.0, (float) 1.0).setLight(light);
            vc.addVertex(p, x1, y1, (float) 0.0).setColor(r, g, b, a).setUv((float) 1.0, (float) 1.0).setLight(light);
            vc.addVertex(p, x1, y0, (float) 0.0).setColor(r, g, b, a).setUv((float) 1.0, (float) 0.0).setLight(light);
            vc.addVertex(p, x0, y0, (float) 0.0).setColor(r, g, b, a).setUv((float) 0.0, (float) 0.0).setLight(light);
        });
    }

    private static void text(PoseStack pose, OrderedSubmitNodeCollector collector,
                             FormattedCharSequence text, float x, float y, int light) {
        collector.submitText(pose, x, y, text, false, Font.DisplayMode.NORMAL, light,
                TEXT_COLOR, 0, 0);
    }

    private static Component scrambled(Component in) {
        return in.copy().withStyle(s -> s.withObfuscated(true));
    }

    private static void renderNotes(Minecraft mc, ItemStack stack, PoseStack pose,
                                    SubmitNodeCollector collector, int light) {
        if (mc == null || stack == null || pose == null || collector == null) {
            return;
        }
        Font font = mc.font;
        OrderedSubmitNodeCollector icons = collector.order(1);
        OrderedSubmitNodeCollector texts = collector.order(2);
        pose.scale(0.0078125F, 0.0078125F, 0.0078125F);
        quad(pose, collector, RenderTypes.text(PARCHMENT),
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
        text(pose, texts, title.getVisualOrderText(), 0.0F, 0.0F, light);
        pose.popPose();
        int index = 0;
        if (tags != null) {
            for (int a = 0; a < tags.length; a++) {
                Aspect tag = tags[a];
                if (tag == null) {
                    continue;
                }
                String tagStr = tag.getTag();
                if (tagStr == null) {
                    continue;
                }
                int have = a < haveLen ? haveArr[a] : 0;
                if (have <= 0 && total < 0.5F) {
                    continue;
                }
                float tagProgress = data.getTagProgress(tag);
                boolean known = tagProgress >= 0.3F;
                Identifier icon = UNKNOWN_TAG;
                if (known) {
                    Identifier image = tag.getImage();
                    if (image != null) {
                        Identifier stripped = UtilsFX.strippedAspectTexture(image);
                        if (stripped != null) {
                            icon = stripped;
                        }
                    }
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
                quad(pose, icons, RenderTypes.text(icon),
                        -1.0F, -1.0F, 1.0F, 1.0F,
                        r, g, b, alpha, light);
                pose.popPose();
                if (tagProgress >= 0.333332F) {
                    int need = a < needLen ? needArr[a] : 0;
                    int completion = need <= 0 ? 100 : (int) ((float) Math.min(have, need) / need * 100.0F);
                    pose.pushPose();
                    pose.translate(x, y, -0.02F);
                    pose.scale(0.56F, 0.56F, 1.0F);
                    text(pose, texts, Component.literal(completion + "%").getVisualOrderText(), 13.0F, -3.0F, light);
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
                text(pose, texts, lines.get(i), 0.0F, i * font.lineHeight, light);
            }
            pose.popPose();
        }
    }

    private static Component describe(String key) {
        return DiscoveredNotes.researchText(key);
    }
}
