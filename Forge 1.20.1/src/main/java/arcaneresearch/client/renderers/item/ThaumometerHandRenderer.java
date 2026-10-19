package arcaneresearch.client.renderers.item;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.research.ScanningManager;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.client.renderers.ScanPopupRenderer;
import arcaneresearch.common.items.tools.ItemThaumometer;
import arcaneresearch.common.lib.aura.INode;
import arcaneresearch.common.lib.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.common.lib.utils.EntityUtils;
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ThaumometerHandRenderer {
    private static final ResourceLocation SCANSCREEN =
            new ResourceLocation(Arcaneresearch.MODID, "textures/item/scanscreen.png");
    private static final ResourceLocation UNKNOWN_TAG =
            new ResourceLocation(Arcaneresearch.MODID, "textures/aspects/_unknown.png");
    private static final double SCAN_RANGE = 10.0D;
    private static final int SWING_QUIET_TICKS = 8;
    private static int useStartTick = -1000;
    private static int useEndTick = -1000;
    private static boolean wasUsing;
    private static boolean wasDown;
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        AbstractClientPlayer player = mc.player;
        Level level = mc.level;
        if (player == null || level == null) {
            return;
        }
        boolean off = event.getHand() == InteractionHand.OFF_HAND;
        if (off && isThaumometer(player.getMainHandItem())) {
            event.setCanceled(true);
            return;
        }
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemThaumometer)) {
            return;
        }
        if (off) {
            return;
        }
        if (!mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        event.setCanceled(true);
        PoseStack pose = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        int light = event.getPackedLight();
        boolean using = player.isUsingItem() && isThaumometer(player.getUseItem());
        boolean useDown = mc.options.keyUse.isDown()
                && (isThaumometer(player.getMainHandItem())
                        || isThaumometer(player.getOffhandItem()));
        if (useDown && !wasDown) {
            useStartTick = player.tickCount;
        }
        wasDown = useDown;
        if (!using && wasUsing) {
            useEndTick = player.tickCount;
        }
        wasUsing = using;
        boolean inWindow = player.tickCount - useStartTick < SWING_QUIET_TICKS
                || player.tickCount - useEndTick < SWING_QUIET_TICKS;
        float swing = (using || inWindow) ? 0.0F : event.getSwingProgress();
        pose.pushPose();
        float sqrtSwing = Mth.sqrt(swing);
        float ySwing = -0.2F * Mth.sin(swing * Mth.PI);
        float zSwing = -0.4F * Mth.sin(sqrtSwing * Mth.PI);
        pose.translate(0.0F, -ySwing / 2.0F, zSwing);
        pose.translate(0.0F, 0.04F + event.getEquipProgress() * -1.2F, -0.72F);
        pose.mulPose(Axis.XP.rotationDegrees((player.getXRot() - player.xRotO) * 0.1F));
        pose.mulPose(Axis.YP.rotationDegrees((player.getYRot() - player.yRotO) * 0.1F));
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(90.0F));
        renderMapHand(mc, pose, buffer, light, HumanoidArm.RIGHT, player);
        renderMapHand(mc, pose, buffer, light, HumanoidArm.LEFT, player);
        pose.popPose();
        pose.pushPose();
        pose.mulPose(Axis.XP.rotationDegrees(Mth.sin(sqrtSwing * Mth.PI) * 8.0F));
        pose.mulPose(Axis.XP.rotationDegrees(80.0F));
        pose.scale(0.4F, 0.4F, 0.4F);
        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.scale(1.6666667F, 1.6666667F, 1.6666667F);
        try {
            mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light,
                    OverlayTexture.NO_OVERLAY, pose, buffer, level, 0);
        } catch (Exception ignored) {
        }
        pose.popPose();
        pose.pushPose();
        pose.translate(0.0F, 0.02F, 0.0F);
        pose.mulPose(Axis.XP.rotationDegrees(90.0F));
        int flick = flicker(player);
        quad(pose, buffer, RenderType.text(SCANSCREEN),
                -1.25F, -1.25F, 1.25F, 1.25F, 0.0F,
                0.0F, 0.0F, 1.0F, 1.0F, 255, 255, 255, 255, flick);
        renderScanOverlay(mc, player, level, pose, buffer, flick);
        pose.popPose();
        pose.popPose();
        pose.popPose();
        if (buffer instanceof MultiBufferSource.BufferSource source) {
            source.endBatch();
        }
    }
    private static void renderScanOverlay(Minecraft mc, Player player, Level level,
            PoseStack pose, MultiBufferSource buffer, int flick) {
        Target target = resolveTarget(player, level);
        if (target == null) {
            return;
        }
        if (target.node != null) {
            String t = "nodetype." + target.node.getNodeType();
            if (target.node.getNodeModifier() != null) {
                t = t + ", " + target.node.getNodeModifier();
            }
            pose.pushPose();
            pose.translate(0.0F, 0.0F, -0.01F);
            pose.scale(0.006F, 0.006F, 0.006F);
            Font font = mc.font;
            int sw = font.width(t);
            font.drawInBatch(t, -sw / 2.0F, -40.0F, 15642134, false,
                    pose.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, flick);
            pose.popPose();
        }
        AspectList aspects = target.aspects;
        if (aspects != null && aspects.size() > 0) {
            int posX = 0;
            int posY = 0;
            int aa = aspects.size();
            int baseX = Math.min(5, aa) * 8;
            for (Aspect aspect : aspects.getAspectsSortedByAmount()) {
                if (aspect == null) {
                    continue;
                }
                pose.pushPose();
                pose.translate(0.0F, 0.0F, -0.01F);
                pose.scale(0.01125F, 0.01125F, 0.01125F);
                int px = -baseX + posX * 16;
                int py = -8 + posY * 16;
                boolean known = ArcaneresearchCapabilities.isResearchKnown(player,
                        "!" + aspect.getTag().toLowerCase());
                ResourceLocation icon = known
                        ? UtilsFX.strippedAspectTexture(aspect.getImage())
                        : UNKNOWN_TAG;
                int color = known ? aspect.getColor() : 0xFFFFFF;
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = color & 0xFF;
                quad(pose, buffer, RenderType.text(icon),
                        px, py, px + 16.0F, py + 16.0F, 0.01F,
                        0.0F, 0.0F, 1.0F, 1.0F, r, g, b, 255, flick);
                int amount = aspects.getAmount(aspect);
                if (amount > 1) {
                    pose.pushPose();
                    pose.scale(0.5F, 0.5F, 0.5F);
                    String am = String.valueOf(amount);
                    mc.font.drawInBatch(am, (px + 16) * 2.0F - mc.font.width(am), (py + 16) * 2.0F - 8.0F,
                            0xFFFFFF, false, pose.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, flick);
                    pose.popPose();
                }
                pose.popPose();
                posX++;
                if (posX >= 5 - posY) {
                    posX = 0;
                    posY++;
                    aa -= 5 - posY;
                    baseX = Math.min(Math.max(5 - posY, 1), Math.max(aa, 1)) * 8;
                }
            }
        }
        String text = target.name;
        if (text == null || text.isEmpty()) {
            text = "?";
        }
        pose.pushPose();
        pose.translate(0.0F, -0.25F, -0.01F);
        Font font = mc.font;
        int sw = font.width(text);
        float scale = 0.0075F;
        if (sw > 90) {
            scale -= 3.75E-5F * (sw - 90);
        }
        pose.scale(scale, scale, scale);
        font.drawInBatch(text, -sw / 2.0F, 0.0F, 0xFFFFFF, false,
                pose.last().pose(), buffer, Font.DisplayMode.NORMAL, 0, flick);
        pose.popPose();
    }
    private static Target resolveTarget(Player player, Level level) {
        Entity pointed = null;
        try {
            pointed = EntityUtils.getPointedEntity(level, player, 0.5D, SCAN_RANGE, 0.0F, true);
        } catch (Exception ignored) {
        }
        if (pointed != null) {
            String name;
            ItemStack stack = ItemStack.EMPTY;
            if (pointed instanceof ItemEntity item) {
                stack = item.getItem();
                name = stack.isEmpty() ? "?" : stack.getHoverName().getString();
            } else {
                try {
                    name = pointed.getName().getString();
                } catch (Exception e) {
                    name = "?";
                }
            }
            AspectList aspects = !stack.isEmpty()
                    ? AspectHelper.getObjectAspects(stack)
                    : AspectHelper.getEntityAspects(pointed);
            if (aspects != null && ScanningManager.isThingStillScannable(player, pointed)) {
                aspects = null;
            }
            return new Target(name, aspects, null);
        }
        BlockHitResult mop = null;
        try {
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            mop = level.clip(new ClipContext(eye, eye.add(look.scale(SCAN_RANGE)),
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player));
        } catch (Exception ignored) {
        }
        if (mop != null && mop.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = mop.getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (!state.isAir()) {
                INode node = null;
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof INode n) {
                    node = n;
                }
                ItemStack stack = ItemStack.EMPTY;
                try {
                    stack = state.getBlock().getCloneItemStack(state, mop, level, pos, player);
                } catch (Exception ignored) {
                }
                if (stack.isEmpty()) {
                    try {
                        stack = state.getBlock().getCloneItemStack(level, pos, state);
                    } catch (Exception ignored) {
                    }
                }
                if (stack.isEmpty()) {
                    stack = new ItemStack(state.getBlock());
                }
                String name = stack.isEmpty() ? state.getBlock().getName().getString()
                        : stack.getHoverName().getString();
                AspectList aspects;
                if (node != null) {
                    aspects = node.getAspects();
                    if (ScanningManager.isThingStillScannable(player, pos)) {
                        aspects = null;
                    }
                } else {
                    aspects = AspectHelper.getObjectAspects(stack);
                    if (aspects != null && ScanningManager.isThingStillScannable(player, pos)) {
                        aspects = null;
                    }
                }
                return new Target(name, aspects, node);
            }
        }
        Object cur = ScanPopupRenderer.getCurrentTarget(player);
        AspectList aspects = ScanPopupRenderer.getCurrentAspects(player);
        if (cur == null) {
            return null;
        }
        String name = "?";
        try {
            if (cur instanceof Entity e) {
                name = e.getName().getString();
                if (aspects != null && ScanningManager.isThingStillScannable(player, e)) {
                    aspects = null;
                }
            } else if (cur instanceof BlockPos pos) {
                BlockState state = level.getBlockState(pos);
                ItemStack s = new ItemStack(state.getBlock());
                name = s.isEmpty() ? "?" : s.getHoverName().getString();
                if (aspects != null && ScanningManager.isThingStillScannable(player, pos)) {
                    aspects = null;
                }
            }
        } catch (Exception ignored) {
        }
        return new Target(name, aspects, null);
    }
    private record Target(String name, AspectList aspects, INode node) {
    }
    private static boolean isThaumometer(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() instanceof ItemThaumometer;
    }
    private static int flicker(Player player) {
        float s = Mth.sin((player.tickCount + player.level().random.nextInt(2)) * 0.7F);
        return (int) (190.0F + s * 10.0F + 10.0F);
    }
    private static void renderMapHand(Minecraft mc, PoseStack pose, MultiBufferSource buffer,
            int light, HumanoidArm arm, AbstractClientPlayer player) {
        pose.pushPose();
        float invert = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        pose.mulPose(Axis.YP.rotationDegrees(92.0F));
        pose.mulPose(Axis.XP.rotationDegrees(45.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(invert * -41.0F));
        pose.translate(invert * 0.3F, -1.1F, 0.45F);
        PlayerRenderer renderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(player);
        if (arm == HumanoidArm.RIGHT) {
            renderer.renderRightHand(pose, buffer, light, player);
        } else {
            renderer.renderLeftHand(pose, buffer, light, player);
        }
        pose.popPose();
    }
    private static void quad(PoseStack pose, MultiBufferSource buffer, RenderType type,
            float x0, float y0, float x1, float y1, float z,
            float u0, float v0, float u1, float v1,
            int r, int g, int b, int a, int light) {
        VertexConsumer vc = buffer.getBuffer(type);
        var m = pose.last().pose();
        vc.vertex(m, x0, y1, z).color(r, g, b, a).uv(u0, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, -1.0F).endVertex();
        vc.vertex(m, x1, y1, z).color(r, g, b, a).uv(u1, v1)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, -1.0F).endVertex();
        vc.vertex(m, x1, y0, z).color(r, g, b, a).uv(u1, v0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, -1.0F).endVertex();
        vc.vertex(m, x0, y0, z).color(r, g, b, a).uv(u0, v0)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 0.0F, -1.0F).endVertex();
    }
}
