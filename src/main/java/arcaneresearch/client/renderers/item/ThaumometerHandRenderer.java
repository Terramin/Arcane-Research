package arcaneresearch.client.renderers.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.research.ScanningManager;
import arcaneresearch.client.lib.UtilsFX;
import arcaneresearch.client.renderers.ScanPopupRenderer;
import arcaneresearch.client.renderers.TCRenderTypes;
import arcaneresearch.client.renderers.entity.TurretObjModel;
import arcaneresearch.common.items.tools.ItemThaumometer;
import arcaneresearch.common.lib.aura.INode;
import arcaneresearch.common.lib.utils.EntityUtils;

 























































 
@EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
public class ThaumometerHandRenderer {

    private static final Identifier SCANNER_MODEL =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "models/item/scanner.obj");
    private static final Identifier SCANNER_TEX =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/item/scanner.png");
    private static final Identifier SCANSCREEN =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/item/scanscreen.png");
    private static final Identifier UNKNOWN_TAG =
            Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/aspects/_unknown.png");

    private static final double SCAN_RANGE = 10.0D;

      
    private static final int SWING_QUIET_TICKS = 8;
    private static int useStartTick = -1000;
    private static int useEndTick = -1000;
    private static boolean wasUsing;
    private static boolean wasDown;

    private static volatile TurretObjModel scannerObj = null;
    private static volatile boolean scannerTried = false;

    private static TurretObjModel scannerModel() {
        if (!scannerTried) {
            synchronized (ThaumometerHandRenderer.class) {
                if (!scannerTried) {
                    scannerTried = true;
                    try {
                        
                        scannerObj = TurretObjModel.load(SCANNER_MODEL, true);
                    } catch (Throwable ignored) {
                        scannerObj = null;
                    }
                }
            }
        }
        return scannerObj;
    }

    @SubscribeEvent
    public static void onRenderHand(net.neoforged.neoforge.client.event.RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
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
        SubmitNodeCollector collector = event.getSubmitNodeCollector();
        PlayerRenderState playerState = event.getPlayerState();
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
        pose.rotateDegrees(Axis.XP, (player.getXRot() - player.xRotO) * 0.1F);
        pose.rotateDegrees(Axis.YP, (player.getYRot() - player.yRotO) * 0.1F);
        pose.pushPose();
        pose.rotateDegrees(Axis.YP, 90.0F);
        
        renderMapHand(pose, collector, light, HumanoidArm.RIGHT, playerState);
        renderMapHand(pose, collector, light, HumanoidArm.LEFT, playerState);
        
        pose.popPose();
        pose.pushPose();
        pose.rotateDegrees(Axis.XP, Mth.sin(sqrtSwing * Mth.PI) * 8.0F);
        pose.rotateDegrees(Axis.XP, 80.0F);
        pose.scale(0.4F, 0.4F, 0.4F);
        
        
        
        TurretObjModel scanner = scannerModel();
        if (scanner != null) {
            RenderType bodyType = TCRenderTypes.objSolidFlat(SCANNER_TEX);
            
            
            
            
            
            
            RenderType paneType = TCRenderTypes.translucentPortal(SCANSCREEN);
            emitGroup(pose, collector, bodyType, scanner, "scanner", light);
            emitGroup(pose, collector, paneType, scanner, "screen", light);
            emitGroup(pose, collector, paneType, scanner, "screen_back", light);
        }

        
        
        
        
        
        
        pose.pushPose();
        pose.translate(0.0F, 0.02F, 0.0F);
        pose.rotateDegrees(Axis.XP, 90.0F);
        int flick = flicker(player);
        quad(pose, collector, RenderTypes.text(SCANSCREEN),
                -1.25F, -1.25F, 1.25F, 1.25F, 0.0F,
                0.0F, 0.0F, 1.0F, 1.0F, 255, 255, 255, 255, flick);

        renderScanOverlay(mc, player, level, pose, collector, flick);
        pose.popPose(); 

        pose.popPose(); 
        pose.popPose(); 
    }

    

    private static void renderScanOverlay(Minecraft mc, Player player, Level level,
            PoseStack pose, SubmitNodeCollector collector, int flick) {
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
            collector.submitText(pose, -sw / 2.0F, -40.0F,
                    FormattedCharSequence.forward(t, Style.EMPTY),
                    false, Font.DisplayMode.NORMAL, flick, 0xFFEEAE16, 0, 0);
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
                        "!" + aspect.getTag().toLowerCase(java.util.Locale.ROOT));
                Identifier icon = known
                        ? UtilsFX.strippedAspectTexture(aspect.getImage())
                        : UNKNOWN_TAG;
                int color = known ? aspect.getColor() : 0xFFFFFF;
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = color & 0xFF;
                quad(pose, collector, RenderTypes.text(icon),
                        px, py, px + 16.0F, py + 16.0F, 0.01F,
                        0.0F, 0.0F, 1.0F, 1.0F, r, g, b, 255, flick);
                int amount = aspects.getAmount(aspect);
                if (amount > 1) {
                    pose.pushPose();
                    pose.scale(0.5F, 0.5F, 0.5F);
                    String am = String.valueOf(amount);
                    collector.submitText(pose, (px + 16) * 2.0F - mc.font.width(am),
                            (py + 16) * 2.0F - 8.0F,
                            FormattedCharSequence.forward(am, Style.EMPTY),
                            false, Font.DisplayMode.NORMAL, flick, 0xFFFFFFFF, 0, 0);
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
        collector.submitText(pose, -sw / 2.0F, 0.0F,
                FormattedCharSequence.forward(text, Style.EMPTY),
                false, Font.DisplayMode.NORMAL, flick, 0xFFFFFFFF, 0, 0);
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
                    stack = state.getCloneItemStack(pos, level, true, player);
                } catch (Exception ignored) {
                }
                if (stack.isEmpty()) {
                    try {
                        stack = state.getCloneItemStack(level, pos, true);
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
        float s = Mth.sin((player.tickCount + player.level().getRandom().nextInt(2)) * 0.7F);
        return (int) (190.0F + s * 10.0F + 10.0F);
    }

     


 
    private static void renderMapHand(PoseStack pose, SubmitNodeCollector collector,
            int light, HumanoidArm arm, PlayerRenderState playerState) {
        AvatarRenderState avatar = playerState.avatarRenderState;
        if (avatar == null || avatar.isInvisible) {
            return;
        }
        pose.pushPose();
        float invert = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        pose.rotateDegrees(Axis.YP, 92.0F);
        pose.rotateDegrees(Axis.XP, 45.0F);
        pose.rotateDegrees(Axis.ZP, invert * -41.0F);
        pose.translate(invert * 0.3F, -1.1F, 0.45F);
        try {
            Minecraft mc = Minecraft.getInstance();
            AvatarRenderer<?> renderer = (AvatarRenderer<?>)
                    mc.getEntityRenderDispatcher().getRenderer(avatar);
            Identifier skin = avatar.skin.body().texturePath();
            if (!net.neoforged.neoforge.client.ClientHooks.renderSpecificFirstPersonArm(
                    pose, collector, light, skin,
                    arm == HumanoidArm.RIGHT ? avatar.showRightSleeve : avatar.showLeftSleeve,
                    playerState, arm,
                    arm == HumanoidArm.RIGHT ? renderer.getModel().rightArm : renderer.getModel().leftArm)) {
                if (arm == HumanoidArm.RIGHT) {
                    renderer.renderRightHand(pose, collector, light, skin, avatar.showRightSleeve);
                } else {
                    renderer.renderLeftHand(pose, collector, light, skin, avatar.showLeftSleeve);
                }
            }
        } catch (Exception ignored) {
        }
        pose.popPose();
    }

    private static void emitGroup(PoseStack pose, SubmitNodeCollector collector,
            RenderType type, TurretObjModel model, String group, int light) {
        if (!model.hasGroup(group)) {
            return;
        }
        collector.submitCustomGeometry(pose, type, (poseState, consumer) -> {
            PoseStack work = new PoseStack();
            work.last().pose().set(poseState.pose());
            work.last().normal().set(poseState.normal());
            model.renderGroup(work, consumer, group, light, OverlayTexture.NO_OVERLAY,
                    1.0F, 1.0F, 1.0F, 1.0F);
        });
    }

     


 
    private static void quad(PoseStack pose, SubmitNodeCollector collector, RenderType type,
            float x0, float y0, float x1, float y1, float z,
            float u0, float v0, float u1, float v1,
            int r, int g, int b, int a, int light) {
        collector.submitCustomGeometry(pose, type, (poseState, consumer) -> {
            consumer.addVertex(poseState, x0, y1, z).setColor(r, g, b, a).setUv(u0, v1).setLight(light);
            consumer.addVertex(poseState, x1, y1, z).setColor(r, g, b, a).setUv(u1, v1).setLight(light);
            consumer.addVertex(poseState, x1, y0, z).setColor(r, g, b, a).setUv(u1, v0).setLight(light);
            consumer.addVertex(poseState, x0, y0, z).setColor(r, g, b, a).setUv(u0, v0).setLight(light);
        });
    }
}
