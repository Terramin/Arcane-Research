package arcaneresearch.client.lib.events;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
@OnlyIn(Dist.CLIENT)
public final class WandTipHelper {
    public static final float TIP_DZ_SIDE = -0.12F;
    public static final float TIP_DZ_BEAM = -0.16F;
    private WandTipHelper() {
    }
    public static boolean isRightHand(Player player) {
        try {
            boolean mainHolds =
                    player.getMainHandItem().getItem() instanceof arcaneresearch.api.wands.IWand;
            boolean offHolds =
                    player.getOffhandItem().getItem() instanceof arcaneresearch.api.wands.IWand;
            InteractionHand hand = mainHolds ? InteractionHand.MAIN_HAND
                    : (offHolds ? InteractionHand.OFF_HAND : player.getUsedItemHand());
            return (hand == InteractionHand.MAIN_HAND)
                    == (player.getMainArm() == HumanoidArm.RIGHT);
        } catch (Exception e) {
            return true;
        }
    }
    public static float charge(Player player) {
        try {
            if (!player.isUsingItem()) {
                return 0.0F;
            }
            ItemStack use = player.getUseItem();
            if (!(use.getItem() instanceof arcaneresearch.api.wands.IWand)) {
                return 0.0F;
            }
            float f = (float) use.getUseDuration() - (float) player.getUseItemRemainingTicks();
            float f1 = f / 20.0F;
            f1 = (f1 * f1 + f1 * 2.0F) / 3.0F;
            return Mth.clamp(f1, 0.0F, 1.0F);
        } catch (Exception e) {
            return 0.0F;
        }
    }
    public static Vec3 tipWorld(Player player, float partialTicks) {
        return tipWorld(player, partialTicks, 0.0F, 0.0F, 0.0F);
    }
    public static Vec3 tipWorld(Player player, float partialTicks,
                                float modelDx, float modelDy, float modelDz) {
        try {
            Minecraft mc = Minecraft.getInstance();
            Camera camera = mc.gameRenderer.getMainCamera();
            boolean first = false;
            try {
                first = mc.options.getCameraType().isFirstPerson() && mc.cameraEntity == player;
            } catch (Exception ignored) {
            }
            if (first) {
                Vec3 live = liveTip(player, modelDx, modelDy, modelDz);
                if (live != null) {
                    return live;
                }
                Vec3 tip = solveTip(player, partialTicks, modelDx, modelDy, modelDz);
                if (tip != null) {
                    return tip;
                }
            } else {
                return thirdPersonTip(player, partialTicks);
            }
            Vec3 eye = player.getEyePosition(partialTicks);
            Vec3 look = player.getViewVector(partialTicks);
            return eye.add(look.scale(1.0D));
        } catch (Exception e) {
            try {
                Vec3 eye = player.getEyePosition(partialTicks);
                Vec3 look = player.getViewVector(partialTicks);
                return eye.add(look.scale(1.0D));
            } catch (Exception ignored) {
                return new Vec3(player.getX(), player.getY() + 1.0D, player.getZ());
            }
        }
    }
    private static Vec3 thirdPersonTip(Player player, float partialTicks) {
        Vec3 look = player.getViewVector(partialTicks);
        Vec3 right = new Vec3(-look.z, 0.0D, look.x);
        if (right.lengthSqr() < 1.0E-6D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }
        double side = isRightHand(player) ? 0.32D : -0.32D;
        Vec3 base = new Vec3(player.getX(), player.getY() + player.getEyeHeight() * 0.8D, player.getZ());
        return base.add(look.scale(0.55D)).add(right.scale(side)).add(0.0D, 0.1D, 0.0D);
    }
    private static Vec3 liveTip(Player player, float modelDx, float modelDy, float modelDz) {
        InteractionHand hand = heldWandHand(player);
        if (hand == null) {
            return null;
        }
        Vec3 world = viewToWorld(player, hand, modelDx, modelDy, modelDz);
        if (world != null) {
            return world;
        }
        InteractionHand other = hand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        return viewToWorld(player, other, modelDx, modelDy, modelDz);
    }
    private static InteractionHand heldWandHand(Player player) {
        if (player.getMainHandItem().getItem() instanceof arcaneresearch.api.wands.IWand) {
            return InteractionHand.MAIN_HAND;
        }
        if (player.getOffhandItem().getItem() instanceof arcaneresearch.api.wands.IWand) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }
    private static Vec3 viewToWorld(Player player, InteractionHand hand,
                                    float modelDx, float modelDy, float modelDz) {
        org.joml.Vector3f view = new org.joml.Vector3f();
        if (!arcaneresearch.client.renderers.item.WandUseAnimation.projectTip(hand,
                modelDx, modelDy, modelDz, view)) {
            return null;
        }
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        // 1.20.1 camera.rotation() maps the (x/z flipped) view frame used by the hand pose
        // into world space, hence the inversions; 26.3 does not need them.
        org.joml.Vector3f v = new org.joml.Vector3f(-view.x, view.y, -view.z);
        v.rotate(camera.rotation());
        Vec3 pos = camera.getPosition();
        return new Vec3(pos.x + v.x, pos.y + v.y, pos.z + v.z);
    }
    private static Vec3 solveTip(Player player, float partialTicks,
                                   float modelDx, float modelDy, float modelDz) {
        try {
            if (!(player instanceof net.minecraft.client.player.LocalPlayer local)) {
                return null;
            }
            ItemStack main = player.getMainHandItem();
            ItemStack off = player.getOffhandItem();
            boolean mainWand = main.getItem() instanceof arcaneresearch.api.wands.IWand;
            boolean offWand = off.getItem() instanceof arcaneresearch.api.wands.IWand;
            if (!mainWand && !offWand) {
                return null;
            }
            InteractionHand hand = mainWand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            ItemStack handStack = mainWand ? main : off;
            HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm()
                    : player.getMainArm().getOpposite();
            int i = arm == HumanoidArm.RIGHT ? 1 : -1;
            arcaneresearch.client.renderers.item.WandUseAnimation.AnimState st =
                    arcaneresearch.client.renderers.item.WandUseAnimation.resolveState(
                            local, handStack, hand, partialTicks);
            com.mojang.blaze3d.vertex.PoseStack ps = new com.mojang.blaze3d.vertex.PoseStack();
            float bx = net.minecraft.util.Mth.lerp(partialTicks, local.xBobO, local.xBob);
            float by = net.minecraft.util.Mth.lerp(partialTicks, local.yBobO, local.yBob);
            ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(
                    (local.getViewXRot(partialTicks) - bx) * 0.1F));
            ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(
                    (local.getViewYRot(partialTicks) - by) * 0.1F));
            float equip = hand == InteractionHand.MAIN_HAND
                    ? arcaneresearch.client.renderers.item.WandUseAnimation.lastEquipMain
                    : arcaneresearch.client.renderers.item.WandUseAnimation.lastEquipOff;
            InteractionHand swinging = player.swingingArm;
            if (swinging == null) {
                swinging = InteractionHand.MAIN_HAND;
            }
            float sw = swinging == hand ? player.getAttackAnim(partialTicks) : 0.0F;
            arcaneresearch.client.renderers.item.WandUseAnimation.applyBasePose(ps, arm, i,
                    st != null && st.using(), local, local.getUseItem(), equip, sw, partialTicks);
            if (st != null) {
                arcaneresearch.client.renderers.item.WandUseAnimation.applyWaveBlock(
                        ps, st.focusStack(), st.u(), st.tilt());
            }
            arcaneresearch.api.wands.IWand wand =
                    (arcaneresearch.api.wands.IWand) handStack.getItem();
            boolean right = arm == HumanoidArm.RIGHT;
            float dtx = (right ? 1.13F : -1.13F) * 0.0625F;
            float dty = 3.2F * 0.0625F;
            float dtz = 1.13F * 0.0625F;
            ps.translate(dtx, dty, dtz);
            ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(right ? -90.0F : 90.0F));
            ps.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(right ? 25.0F : -25.0F));
            ps.scale(0.68F, 0.68F, 0.68F);
            ps.translate(-0.5F, -0.5F, -0.5F);
            org.joml.Matrix4f m = new org.joml.Matrix4f(ps.last().pose());
            boolean hasFocus = wand.getFocus(handStack) != null;
            float tipY = hasFocus ? 1.16F : 1.125F;
            org.joml.Vector4f p = new org.joml.Vector4f(0.5F + modelDx, tipY + modelDy, 0.5F + modelDz, 1.0F);
            m.transform(p);
            Minecraft mc = Minecraft.getInstance();
            Camera camera = mc.gameRenderer.getMainCamera();
            // Инверсия x/z ОСТАЁТСЯ (в отличие от 26.3): в 1.20.1
            // camera.rotation() маппит другую локальную рамку — 26.3-комментарий
            // это фиксирует («в 1.20.1 инверсия компенсировала, без неё луч
            // уходил левее жезла»); в 26.3 rotation() маппит стандартное
            // GL view-пространство, поэтому там без инверсии.
            org.joml.Vector3f v = new org.joml.Vector3f(-p.x, p.y, -p.z);
            v.rotate(camera.rotation());
            Vec3 pos = camera.getPosition();
            return new Vec3(pos.x + v.x, pos.y + v.y, pos.z + v.z);
        } catch (Exception ignored) {
            return null;
        }
    }
}
