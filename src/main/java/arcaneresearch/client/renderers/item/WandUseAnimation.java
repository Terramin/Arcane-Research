package arcaneresearch.client.renderers.item;

import arcaneresearch.api.wands.FocusItem;
import arcaneresearch.api.wands.IFocusHost;
import arcaneresearch.api.wands.IFocusHostWand;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

@OnlyIn(Dist.CLIENT)
public final class WandUseAnimation {

    private static final float POSE_OFFSET_X = -0.28284F + 0.08F;
    private static final float POSE_OFFSET_Y = -0.5225F + 0.08F;
    private static final float POSE_OFFSET_Z = -0.28284F;
    private static final float BASE_SCALE = 0.8F;
    private static final float ITEM_SCALE = 0.4F;
    private static final float WAVE_ROLL_PERIOD = 10.0F;
    private static final float WAVE_PITCH_PERIOD = 15.0F;
    private static final float WAVE_DEGREES = 10.0F;

    private static final float WAVE_PIVOT_Y = -1.0F;
    public static float lastEquipMain = 0.0F;
    public static float lastEquipOff = 0.0F;
    public static float lastSwingMain = 0.0F;
    public static float lastSwingOff = 0.0F;

    public static final IClientItemExtensions HAND_EXTENSION = new IClientItemExtensions() {
        @Override
        public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm,
                                               ItemStack stack, float partialTick, float equipProgress, float swingProgress) {
            InteractionHand hand = arm == player.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            float mirror = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
            boolean result = applyHandPose(poseStack, player, arm, stack, hand, mirror,
                    partialTick, equipProgress, swingProgress);
            if (hand == InteractionHand.MAIN_HAND) {
                lastEquipMain = equipProgress;
                lastSwingMain = swingProgress;
            } else {
                lastEquipOff = equipProgress;
                lastSwingOff = swingProgress;
            }
            return result;
        }
    };

    private static final Tip TIP_MAIN = new Tip();
    private static final Tip TIP_OFF = new Tip();
    private static final long TIP_MAX_AGE_NS = 250_000_000L;

    public static final class Tip {
        private final org.joml.Matrix4f matrix = new org.joml.Matrix4f();
        private float tipY = 1.16F;
        private long stamp;
        private boolean valid;

        public boolean fresh() {
            return this.valid && System.nanoTime() - this.stamp <= TIP_MAX_AGE_NS;
        }

        public boolean project(float modelDx, float modelDy, float modelDz, org.joml.Vector3f out) {
            if (!fresh()) {
                return false;
            }
            org.joml.Vector4f p = new org.joml.Vector4f(0.5F + modelDx, this.tipY + modelDy,
                    0.5F + modelDz, 1.0F);
            this.matrix.transform(p);
            out.set(p.x, p.y, p.z);
            return true;
        }
    }

    public static boolean projectTip(InteractionHand hand, float modelDx, float modelDy, float modelDz,
                                     org.joml.Vector3f out) {
        return tip(hand).project(modelDx, modelDy, modelDz, out);
    }

    private static Tip tip(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? TIP_MAIN : TIP_OFF;
    }

    public static float tipModelY(ItemStack stack) {
        boolean staff = false;
        boolean sceptre = false;
        boolean hasFocus = false;
        if (stack.getItem() instanceof IFocusHostWand wand) {
            staff = wand.isStaff(stack);
            sceptre = wand.isSceptre(stack);
            hasFocus = wand.getFocus(stack) != null;
        } else if (stack.getItem() instanceof IFocusHost focusHost) {
            hasFocus = focusHost.getFocus(stack) != null;
        }
        if (hasFocus) {
            return staff ? 0.5546875F : 0.6875F;
        }
        if (staff) {
            return 0.36875F;
        }
        return sceptre ? 0.58125F : 0.5625F;
    }

    private static void captureTip(PoseStack pose, InteractionHand hand, ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IFocusHost)) {
            return;
        }
        float tipY = tipModelY(stack);
        org.joml.Vector3f probe = new org.joml.Vector3f(0.5F, tipY, 0.5F);
        pose.last().pose().transformPosition(probe);
        Tip target = tip(hand);
        target.matrix.set(pose.last().pose());
        target.tipY = tipY;
        target.stamp = System.nanoTime();
        target.valid = true;
    }

    private WandUseAnimation() {
    }

    public record AnimState(boolean using, float u, float tilt, ItemStack focusStack) {
    }

    public static AnimState resolveState(LocalPlayer player, ItemStack handStack, InteractionHand hand,
                                         float partialTick) {
        ItemStack useStack = player.getUseItem();
        boolean using = player.isUsingItem() && !useStack.isEmpty()
                && useStack.getItem() instanceof IFocusHost
                && player.getUsedItemHand() == hand && player.getUseItemRemainingTicks() > 0;
        if (!using) {
            return null;
        }
        int elapsedTicks = useStack.getUseDuration() - player.getUseItemRemainingTicks();
        float elapsed = (float) elapsedTicks + partialTick;
        return new AnimState(true, elapsed,
                60.0F * Math.min(elapsed, 3.0F) / 3.0F, useStack);
    }

    public static void applyPoseOffset(PoseStack pose) {
        pose.translate(POSE_OFFSET_X, POSE_OFFSET_Y, POSE_OFFSET_Z);
    }

    public static boolean applyHandPose(PoseStack pose, LocalPlayer player, HumanoidArm arm, ItemStack handStack,
                                        InteractionHand hand, float mirror,
                                        float partialTick, float equipProgress, float swingProgress) {
        if (handStack.isEmpty() || !(handStack.getItem() instanceof IFocusHost)) {
            return false;
        }
        applyPoseOffset(pose);
        applyPoseChain(pose, player, arm, handStack, hand, mirror, partialTick, equipProgress, swingProgress);

        captureTip(pose, hand, handStack);

        pose.translate(0.5F, 0.5F, 0.5F);
        return true;
    }

    public static void applyPoseChain(PoseStack pose, LocalPlayer player, HumanoidArm arm, ItemStack handStack,
                                      InteractionHand hand, float mirror,
                                      float partialTick, float equipProgress, float swingProgress) {
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        AnimState st = resolveState(player, handStack, hand, partialTick);
        boolean usingNow = st != null && st.using;
        applyBasePose(pose, player, arm, i, usingNow, player.getUseItem(), equipProgress, swingProgress, partialTick);
        applyModelBasis(pose, mirror, handStack);
        if (st != null) {
            applyWaveBlock(pose, mirror, st.u(), st.tilt(), st.focusStack());
        }
    }

    public static void applyBasePose(PoseStack pose, LocalPlayer player, HumanoidArm arm, int i, boolean using,
                                     net.minecraft.world.item.ItemStack useStack,
                                     float equipProgress, float swingProgress, float partialTick) {
        float mirror = i;
        if (!using) {
            applyMissSway(pose, mirror, swingProgress);
        }
        applyCarryPose(pose, mirror, equipProgress, swingProgress);
        if (using) {
            applyDrawPose(pose, mirror, player.getTicksUsingItem() + partialTick);
        }
    }

    private static void applyMissSway(PoseStack pose, float mirror, float swing) {
        float lateral = Mth.sin(Mth.sqrt(swing) * Mth.PI);
        float vertical = Mth.sin(Mth.sqrt(swing) * Mth.PI * 2.0F);
        float forward = Mth.sin(swing * Mth.PI);
        pose.translate(mirror * -lateral * 0.4F, vertical * 0.2F, -forward * 0.2F);
    }

    private static void applyCarryPose(PoseStack pose, float mirror, float equip, float swing) {
        pose.translate(mirror * 0.7F * BASE_SCALE, -0.65F * BASE_SCALE - equip * 0.6F, -0.9F * BASE_SCALE);
        pose.mulPose(Axis.YP.rotationDegrees(mirror * 45.0F));
        float snap = Mth.sin(swing * swing * Mth.PI);
        float arc = Mth.sin(Mth.sqrt(swing) * Mth.PI);
        pose.mulPose(Axis.YP.rotationDegrees(mirror * -snap * 20.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(mirror * -arc * 20.0F));
        pose.mulPose(Axis.XP.rotationDegrees(-arc * 80.0F));
        pose.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
    }

    private static void applyDrawPose(PoseStack pose, float mirror, float useTicks) {
        float drawTicks = Math.max(0.0F, useTicks - 1.0F);
        float draw = drawTicks / 20.0F;
        draw = Math.min(1.0F, (draw * draw + draw * 2.0F) / 3.0F);
        pose.mulPose(Axis.ZP.rotationDegrees(mirror * -18.0F));
        pose.mulPose(Axis.YP.rotationDegrees(mirror * -12.0F));
        pose.mulPose(Axis.XP.rotationDegrees(-8.0F));
        pose.translate(mirror * -0.9F, 0.2F, 0.0F);
        if (draw > 0.1F) {
            pose.translate(0.0F, Mth.sin((drawTicks - 0.1F) * 1.3F) * 0.01F * (draw - 0.1F), 0.0F);
        }
        pose.translate(0.0F, 0.0F, draw * 0.1F);
        pose.mulPose(Axis.ZP.rotationDegrees(mirror * -335.0F));
        pose.mulPose(Axis.YP.rotationDegrees(mirror * -50.0F));
        pose.translate(0.0F, 0.5F, 0.0F);
        pose.scale(1.0F, 1.0F, 1.0F + draw * 0.2F);
        pose.translate(0.0F, -0.5F, 0.0F);
        pose.mulPose(Axis.YP.rotationDegrees(mirror * 50.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(mirror * 335.0F));
    }

    public static void applyModelBasis(PoseStack pose, float mirror, ItemStack stack) {
        boolean staff = stack.getItem() instanceof IFocusHostWand wand && wand.isStaff(stack);
        pose.translate(mirror * -0.5F, -0.5F, -0.5F);
        if (staff) {
            pose.translate(0.0F, 0.5F, 0.0F);
        }
        pose.translate(mirror * 0.5F, 1.5F, 0.5F);
        pose.scale(1.0F, 1.1F, 1.0F);
    }

    public static void applyWaveBlock(PoseStack pose, float mirror, float u, float tilt, ItemStack focusStack) {

        float envelope = Mth.clamp(tilt / 60.0F, 0.0F, 1.0F);
        FocusItem.WandFocusAnimation animation = FocusItem.WandFocusAnimation.WAVE;
        try {
            if (focusStack != null && !focusStack.isEmpty()) {
                ItemStack focusItemStack = focusStack;
                if (focusStack.getItem() instanceof IFocusHost host) {
                    focusItemStack = host.getFocusStack(focusStack);
                }
                if (!focusItemStack.isEmpty() && focusItemStack.getItem() instanceof FocusItem focus) {
                    animation = focus.getAnimation(focusItemStack);
                }
            }
        } catch (Exception ignored) {
        }
        boolean charge = animation == FocusItem.WandFocusAnimation.CHARGE;

        pose.translate(0.0F, WAVE_PIVOT_Y, 0.0F);
        pose.mulPose(Axis.XP.rotationDegrees(10.0F));
        pose.mulPose(Axis.ZN.rotationDegrees(mirror * 10.0F));
        pose.mulPose(Axis.XN.rotationDegrees(tilt));
        float rollPeriod = charge ? 0.8F : WAVE_ROLL_PERIOD;
        float pitchPeriod = charge ? 0.7F : WAVE_PITCH_PERIOD;
        float degrees = charge ? 1.0F : WAVE_DEGREES;
        pose.mulPose(Axis.ZN.rotationDegrees(mirror * Mth.sin(u / rollPeriod) * degrees * envelope));
        pose.mulPose(Axis.XP.rotationDegrees(Mth.sin(u / pitchPeriod) * degrees * envelope));
        pose.translate(0.0F, -WAVE_PIVOT_Y, 0.0F);
    }
}
