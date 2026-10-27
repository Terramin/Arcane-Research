package arcaneresearch.client.renderers.item;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.api.wands.ItemFocusBasic;
@OnlyIn(Dist.CLIENT)
public final class WandUseAnimation {
    private static final int CAST_TICKS = 20;
    private static final float CAST_RISE = 6.0F;
    private static int lastCastTick = -100000;
    private static InteractionHand lastCastHand = InteractionHand.MAIN_HAND;
    public static float lastEquipMain = 0.0F;
    public static float lastEquipOff = 0.0F;
    public static float lastSwingMain = 0.0F;
    public static float lastSwingOff = 0.0F;
    public static final IClientItemExtensions HAND_EXTENSION = new IClientItemExtensions() {
        @Override
        public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm,
                                               ItemStack stack, float partialTick, float equipProgress, float swingProgress) {
            InteractionHand hand = arm == player.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            boolean result = applyHandPose(poseStack, player, arm, stack, hand, partialTick, equipProgress, swingProgress);
            if (hand == InteractionHand.MAIN_HAND) {
                lastEquipMain = equipProgress;
                lastSwingMain = swingProgress;
            } else {
                lastEquipOff = equipProgress;
                lastSwingOff = swingProgress;
            }
            if (result) {
                captureTip(poseStack, player, arm, stack, hand);
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

    private static void captureTip(PoseStack pose, LocalPlayer player, HumanoidArm arm, ItemStack stack,
                                   InteractionHand hand) {
        try {
            if (stack.isEmpty() || !(stack.getItem() instanceof IWand wand)) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            net.minecraft.world.item.ItemDisplayContext ctx = arm == HumanoidArm.RIGHT
                    ? net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                    : net.minecraft.world.item.ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
            var transforms = mc.getItemRenderer().getModel(stack, player.level(), player, 0).getTransforms();
            net.minecraft.client.renderer.block.model.ItemTransform transform = transforms.getTransform(ctx);
            if (transform == net.minecraft.client.renderer.block.model.ItemTransform.NO_TRANSFORM) {
                return;
            }
            float tipY = wand.getFocus(stack) != null ? 1.16F : 1.125F;
            PoseStack tipPose = new PoseStack();
            tipPose.last().pose().set(pose.last().pose());
            tipPose.last().normal().set(pose.last().normal());
            transform.apply(arm == HumanoidArm.LEFT, tipPose);
            tipPose.translate(-0.5F, -0.5F, -0.5F);
            org.joml.Vector3f probe = new org.joml.Vector3f(0.5F, tipY, 0.5F);
            tipPose.last().pose().transformPosition(probe);
            if (probe.z > -0.05F || probe.lengthSquared() < 0.04F || probe.lengthSquared() > 6.25F) {
                return;
            }
            Tip tip = tip(hand);
            tip.matrix.set(tipPose.last().pose());
            tip.tipY = tipY;
            tip.stamp = System.nanoTime();
            tip.valid = true;
        } catch (Throwable ignored) {
        }
    }
    private WandUseAnimation() {
    }
    public static void noteCast(InteractionHand hand) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        if (mc.player.tickCount - lastCastTick < CAST_TICKS) {
            return;
        }
        lastCastTick = mc.player.tickCount;
        lastCastHand = hand;
    }
    public record AnimState(boolean using, boolean pulse, float u, float tilt, ItemStack focusStack) {
    }
    public static AnimState resolveState(LocalPlayer player, ItemStack handStack, InteractionHand hand,
                                         float partialTick) {
        ItemStack useStack = player.getUseItem();
        boolean using = player.isUsingItem() && !useStack.isEmpty()
                && useStack.getItem() instanceof IWand
                && player.getUsedItemHand() == hand && player.getUseItemRemainingTicks() > 0;
        int dt = player.tickCount - lastCastTick;
        boolean pulse = !using && dt >= 0 && dt < CAST_TICKS && hand == lastCastHand;
        if (!using && !pulse) {
            return null;
        }
        if (using) {
            int elapsedTicks = useStack.getUseDuration() - player.getUseItemRemainingTicks();
            float elapsed = (float) elapsedTicks + partialTick;
            return new AnimState(true, false, elapsed,
                    60.0F * Math.min(elapsed, 3.0F) / 3.0F, useStack);
        }
        float dtt = (float) dt + partialTick;
        return new AnimState(false, true, dtt,
                60.0F * (dtt < CAST_RISE ? dtt / CAST_RISE
                        : Math.max(0.0F, 1.0F - (dtt - CAST_RISE) / (CAST_TICKS - CAST_RISE))),
                handStack);
    }
    public static boolean applyHandPose(PoseStack pose, LocalPlayer player, HumanoidArm arm, ItemStack handStack,
                                        InteractionHand hand, float partialTick, float equipProgress, float swingProgress) {
        if (handStack.isEmpty() || !(handStack.getItem() instanceof IWand)) {
            return false;
        }
        int i = arm == HumanoidArm.RIGHT ? 1 : -1;
        AnimState st = resolveState(player, handStack, hand, partialTick);
        ItemStack useStack = player.getUseItem();
        boolean usingNow = st != null && st.using;
        applyBasePose(pose, arm, i, usingNow, player, useStack, equipProgress, swingProgress, partialTick);
        if (st != null) {
            applyWaveBlock(pose, st.focusStack, st.u, st.tilt);
        }
        return true;
    }
    public static void applyBasePose(PoseStack pose, HumanoidArm arm, int i, boolean using,
                                     LocalPlayer player, ItemStack useStack,
                                     float equipProgress, float swingProgress, float partialTick) {
        if (using) {
            pose.translate((float) i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
            pose.translate((float) i * -0.2785682F, 0.18344387F, 0.15731531F);
            pose.mulPose(Axis.XP.rotationDegrees(-13.935F));
            pose.mulPose(Axis.YP.rotationDegrees((float) i * 35.3F));
            pose.mulPose(Axis.ZP.rotationDegrees((float) i * -9.785F));
            float f8 = (float) (useStack.getUseDuration() - player.getUseItemRemainingTicks())
                    + partialTick - 1.0F;
            float f12 = f8 / 20.0F;
            f12 = (f12 * f12 + f12 * 2.0F) / 3.0F;
            if (f12 > 1.0F) {
                f12 = 1.0F;
            }
            if (f12 > 0.1F) {
                float f15 = Mth.sin((f8 - 0.1F) * 1.3F);
                float f18 = f12 - 0.1F;
                float f20 = f15 * f18;
                pose.translate(f20 * 0.0F, f20 * 0.004F, f20 * 0.0F);
            }
            pose.translate(f12 * 0.0F, f12 * 0.0F, f12 * 0.04F);
            pose.scale(1.0F, 1.0F, 1.0F + f12 * 0.2F);
            pose.mulPose(Axis.YN.rotationDegrees((float) i * 45.0F));
        } else if (player.isAutoSpinAttack()) {
            pose.translate((float) i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
            int j = arm == HumanoidArm.RIGHT ? 1 : -1;
            pose.translate((float) j * -0.4F, 0.8F, 0.3F);
            pose.mulPose(Axis.YP.rotationDegrees((float) j * 65.0F));
            pose.mulPose(Axis.ZP.rotationDegrees((float) j * -85.0F));
        } else {
            float f5 = -0.4F * Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
            float f6 = 0.2F * Mth.sin(Mth.sqrt(swingProgress) * ((float) Math.PI * 2F));
            float f10 = -0.2F * Mth.sin(swingProgress * (float) Math.PI);
            pose.translate((float) i * f5, f6, f10);
            pose.translate((float) i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
            float f = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
            pose.mulPose(Axis.YP.rotationDegrees((float) i * (45.0F + f * -20.0F)));
            float f1 = Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
            pose.mulPose(Axis.ZP.rotationDegrees((float) i * f1 * -20.0F));
            pose.mulPose(Axis.XP.rotationDegrees(f1 * -80.0F));
            pose.mulPose(Axis.YP.rotationDegrees((float) i * -45.0F));
        }
    }
    public static void applyWaveBlock(PoseStack pose, ItemStack focusStack, float u, float tilt) {
        IWand focusWand = focusStack.getItem() instanceof IWand w ? w : null;
        ItemFocusBasic focus = focusWand != null ? focusWand.getFocus(focusStack) : null;
        ItemFocusBasic.WandFocusAnimation anim = focus != null
                ? focus.getAnimation(focusWand.getFocusStack(focusStack))
                : ItemFocusBasic.WandFocusAnimation.WAVE;
        pose.translate(0.0D, 0.5D, 0.0D);
        pose.mulPose(Axis.XP.rotationDegrees(10.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(10.0F));
        pose.mulPose(Axis.XN.rotationDegrees(tilt));
        if (anim == ItemFocusBasic.WandFocusAnimation.WAVE) {
            float wave = Mth.sin(u / 10.0F) * 10.0F;
            pose.mulPose(Axis.ZP.rotationDegrees(wave));
            wave = Mth.sin(u / 15.0F) * 10.0F;
            pose.mulPose(Axis.XP.rotationDegrees(wave));
        } else if (anim == ItemFocusBasic.WandFocusAnimation.CHARGE) {
            float wave = Mth.sin(u / 0.8F) * 1.0F;
            pose.mulPose(Axis.ZP.rotationDegrees(wave));
            wave = Mth.sin(u / 0.7F) * 1.0F;
            pose.mulPose(Axis.XP.rotationDegrees(wave));
        }
        pose.translate(0.0D, -0.5D, 0.0D);
    }
}
