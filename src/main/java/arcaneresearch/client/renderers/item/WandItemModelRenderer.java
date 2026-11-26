package arcaneresearch.client.renderers.item;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.wands.FocusItem;
import arcaneresearch.api.wands.IFocusHostWand;
import arcaneresearch.api.wands.WandCap;
import arcaneresearch.api.wands.WandRod;
import arcaneresearch.client.renderers.TCRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class WandItemModelRenderer {
    private static final ResourceLocation FOCUS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/wand/wand.png");
    // Identical to Thaumaturge's textures/misc/script.png.
    private static final ResourceLocation RUNE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/misc/wand_runes.png");

    private static final int WHITE_TINT = 0xFFFFFFFF;
    private static final int WHITE_RGB = 0xFFFFFF;
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final int TEXTURE_SIZE = 32;
    private static final float PIXEL = 1.0F / 16.0F;
    private static final float ROOT_CENTER = 0.5F;
    private static final float FLIP_DEGREES = 180.0F;

    private static final float STAFF_GROUP_Y = 0.2F;
    private static final float STAFF_ROD_Y = -0.1F;
    private static final float STAFF_ROD_WIDTH = 1.2F;
    private static final float STAFF_ROD_LENGTH = 2.0F;
    private static final float STAFF_CAP_OUTER_XZ = 1.3F;
    private static final float STAFF_CAP_OUTER_Y = 1.1F;
    private static final float WAND_CAP_OUTER_XZ = 1.2F;
    private static final float WAND_CAP_OUTER_Y = 1.0F;

    private static final float STAFF_FOCUS_Y = -0.0475F;
    private static final float STAFF_FOCUS_XZ = 0.525F;
    private static final float STAFF_FOCUS_Y_SCALE = 0.5525F;
    private static final float WAND_FOCUS_SCALE = 0.5F;
    /** TC4 ModelWand draws the focus depth layer at these absolute model-space Y values. */
    private static final float DEPTH_Y_PLAIN = -0.09F;
    private static final float DEPTH_Y_STAFF = -0.15F;
    private static final int FOCUS_ALPHA = (int) (0.95F * 255.0F);
    private static final int FOCUS_LIGHT_BASE = 195;
    private static final int FOCUS_LIGHT_AMPLITUDE = 10;
    private static final float FOCUS_LIGHT_PERIOD = 3.0F;
    private static final int GLOW_LIGHT_BASE = 200;
    private static final int GLOW_LIGHT_AMPLITUDE = 5;

    private static final Fit GROUP_FIT = new Fit(STAFF_GROUP_Y, 1.0F, 1.0F);
    private static final Fit ROD_FIT = new Fit(STAFF_ROD_Y, STAFF_ROD_WIDTH, STAFF_ROD_LENGTH);
    private static final Fit FOCUS_STAFF_FIT = new Fit(STAFF_FOCUS_Y, STAFF_FOCUS_XZ, STAFF_FOCUS_Y_SCALE);
    private static final Fit FOCUS_PLAIN_FIT = new Fit(0.0F, WAND_FOCUS_SCALE, WAND_FOCUS_SCALE);
    private static final BoxSpec ROD_BOX = BoxSpec.sized(2, 18, 2).at(-1, 1, -1).uv(0, 8);
    private static final BoxSpec CAP_BOX = BoxSpec.sized(2, 2, 2).at(-1, -1, -1).uv(0, 0);
    private static final BoxSpec FOCUS_BOX = BoxSpec.sized(6, 6, 6).at(-3, -6, -3).uv(0, 0);
    private static final List<CapSlot> CAP_SLOTS = List.of(
            new CapSlot(Presence.SCEPTRE, 0.0F, 1.3F, 1.3F, 0.0F),
            new CapSlot(Presence.SCEPTRE, 0.3F, 1.0F, 0.66F, 0.0F),
            new CapSlot(Presence.NOT_SCEPTRE, 0.0F, 1.0F, 1.0F, 0.0F),
            new CapSlot(Presence.STAFF, 0.225F, 1.0F, 0.66F, 0.0F),
            new CapSlot(Presence.STAFF, 0.875F, 1.0F, 1.0F, 20.0F),
            new CapSlot(Presence.NOT_STAFF, 0.0F, 1.0F, 1.0F, 20.0F));

    private static final int RING_RUNES = 10;
    private static final float RING_YAW_STEP = 36.0F;
    private static final float RING_DISTANCE = 0.16F;
    private static final float RING_DEPTH = -0.125F;
    private static final int SIDE_COUNT = 4;
    private static final int SIDE_RUNES = 14;
    private static final float SIDE_YAW_STEP = 90.0F;
    private static final float SIDE_DISTANCE_BASE = 0.36F;
    private static final float SIDE_DISTANCE_STEP = 0.14F;
    private static final float SIDE_DEPTH = -0.08F;
    private static final int SIDE_GLYPH_STEP = 3;
    private static final int GLYPH_COUNT = 16;
    private static final float RUNE_SIDEWAYS = 0.01F;
    private static final int RUNE_LIGHT = FULL_BRIGHT;
    private static final float RUNE_PHASE_PER_GLYPH = 5.0F;
    private static final float RUNE_RED_BASE = 0.88F;
    private static final float RUNE_RED_AMPLITUDE = 0.1F;
    private static final float RUNE_RED_PERIOD = 5.0F;
    private static final float RUNE_GREEN_BASE = 0.63F;
    private static final float RUNE_GREEN_AMPLITUDE = 0.1F;
    private static final float RUNE_GREEN_PERIOD = 7.0F;
    private static final float RUNE_BLUE = 0.2F;
    private static final float RUNE_WOBBLE_AMPLITUDE = 0.2F;
    private static final float RUNE_WOBBLE_PERIOD = 10.0F;
    private static final float RUNE_ALPHA_BASE = 0.6F;
    private static final float RUNE_HALF_SIZE_BASE = 0.06F;
    private static final float RUNE_HALF_SIZE_WOBBLE = 40.0F;
    private static final float[] RUNE_CORNER_X = {-1.0F, 1.0F, 1.0F, -1.0F};
    private static final float[] RUNE_CORNER_Y = {-1.0F, -1.0F, 1.0F, 1.0F};

    private static final float TIP_PLAIN = -PIXEL;
    private static final float TIP_SCEPTRE = -PIXEL * 1.3F;
    private static final float TIP_STAFF = STAFF_GROUP_Y - PIXEL * 1.1F;
    private static final float TIP_FOCUS_PLAIN = -(6.0F / 16.0F) * WAND_FOCUS_SCALE;
    private static final float TIP_FOCUS_STAFF = STAFF_GROUP_Y + STAFF_FOCUS_Y - (6.0F / 16.0F) * STAFF_FOCUS_Y_SCALE;

    private WandItemModelRenderer() {
    }

    public static void applySubmitTransform(PoseStack poseStack) {
        poseStack.translate(ROOT_CENTER, ROOT_CENTER, ROOT_CENTER);
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(FLIP_DEGREES));
    }

    public static WandArg extract(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IFocusHostWand wand)) {
            return null;
        }
        WandRod rod = wand.getRod(stack);
        WandCap cap = wand.getCap(stack);
        if (rod == null || cap == null) {
            return null;
        }
        FocusItem focus = wand.getFocus(stack);
        boolean hasFocus = focus != null;
        int focusColor = WHITE_RGB;
        if (hasFocus) {
            try {
                focusColor = focus.getFocusColor(wand.getFocusStack(stack));
            } catch (Exception ignored) {
            }
        }
        return new WandArg(stack, cap, rod, wand.isSceptre(stack), hasFocus, focusColor);
    }

    public static void draw(WandArg arg, PoseStack poseStack, MultiBufferSource buffer,
                            int light, int overlay) {
        draw(arg, poseStack, buffer, light, true);
    }

    public static void draw(WandArg arg, PoseStack poseStack, MultiBufferSource buffer,
                            int light, boolean includeRunes) {
        float ticks = clientTicks();
        boolean isStaff = arg.rod().isStaff();
        poseStack.pushPose();
        Fit.of(isStaff, GROUP_FIT, Fit.NONE).apply(poseStack);
        submitRod(arg, isStaff, poseStack, buffer, light, ticks);
        submitCaps(arg, isStaff, poseStack, buffer, light);
        if (arg.hasFocus()) {
            submitFocus(arg, isStaff, poseStack, buffer, ticks);
        }
        if (includeRunes) {
            submitRunes(arg, ticks, poseStack, buffer);
        }
        poseStack.popPose();
    }

    private static void submitRod(WandArg arg, boolean isStaff, PoseStack poseStack,
                                  MultiBufferSource buffer, int light, float ticks) {
        poseStack.pushPose();
        Fit.of(isStaff, ROD_FIT, Fit.NONE).apply(poseStack);
        int rodLight = arg.rod().getTag().contains("blaze") ? FULL_BRIGHT : light;
        int tint = WHITE_TINT;
        if (arg.rod().getTag().contains("blaze")) {
            float pulse = 0.875F + 0.125F * (0.5F + 0.5F * Mth.sin(Mth.floor(ticks) + GLOW_LIGHT_AMPLITUDE));
            tint = scaleTint(tint, pulse);
        }
        submitBox(poseStack, buffer.getBuffer(cutout(rodTexture(arg.rod()))), ROD_BOX, tint, rodLight);
        poseStack.popPose();
    }

    private static void submitCaps(WandArg arg, boolean isStaff, PoseStack poseStack,
                                   MultiBufferSource buffer, int light) {
        RenderType type = cutout(capTexture(arg.cap()));
        float outerXz = isStaff ? STAFF_CAP_OUTER_XZ : WAND_CAP_OUTER_XZ;
        float outerY = isStaff ? STAFF_CAP_OUTER_Y : WAND_CAP_OUTER_Y;
        for (CapSlot slot : CAP_SLOTS) {
            if (!slot.presence().test(arg.sceptre(), isStaff)) {
                continue;
            }
            poseStack.pushPose();
            slot.place(poseStack, outerXz, outerY);
            submitBox(poseStack, buffer.getBuffer(type), CAP_BOX, WHITE_TINT, light);
            poseStack.popPose();
        }
    }

    private static void submitFocus(WandArg arg, boolean isStaff, PoseStack poseStack,
                                    MultiBufferSource buffer, float ticks) {
        poseStack.pushPose();
        Fit.of(isStaff, FOCUS_STAFF_FIT, FOCUS_PLAIN_FIT).apply(poseStack);
        float pulse = 0.92F + 0.08F * (0.5F + 0.5F * Mth.sin(ticks / FOCUS_LIGHT_PERIOD));
        int rgb = scaleTint(arg.focusColor() & 0xFFFFFF, pulse);
        int tint = (FOCUS_ALPHA << 24) | rgb;
        boolean warding = arg.stack().getItem() instanceof arcaneresearch.api.wands.IFocusHost host
                && host.getFocus(arg.stack())
                instanceof arcaneresearch.common.items.wands.foci.ItemFocusWarding;
        // TC4 ModelWand: the 3D focus always uses wand.png tinted by focusColor.
        // A focus with a depth layer is rendered more transparent (0.6 vs 0.95).
        int alpha = warding ? 0x99 : FOCUS_ALPHA;
        if (warding) {
            // Depth cube only; no ornament decals are rendered on the wand.
            submitDepth(poseStack, buffer, ticks, isStaff);
        }
        submitBox(poseStack, buffer.getBuffer(RenderType.entityTranslucent(FOCUS_TEXTURE)),
                FOCUS_BOX, (alpha << 24) | rgb, FULL_BRIGHT);
        poseStack.popPose();
    }

    /** TC4's getFocusDepthLayerIcon: a small, offset, blurred cube. */
    private static void submitDepth(PoseStack poseStack, MultiBufferSource buffer, float ticks,
                                    boolean isStaff) {
        ResourceLocation depth = wandTexture("focus_warding_depth.png");
        // Values are pre-Fit: the focus Fit scale (0.5 / 0.525) is already in
        // the pose, so these match TC4's 0.16 / 0.165 final depth cube.
        float size = isStaff ? 0.314F : 0.32F;
        float height = isStaff ? 0.319F : 0.32F;
        // Undo the whole focus Fit, including the staff translate. Using only
        // DEPTH_Y_STAFF / STAFF_FOCUS_Y_SCALE here places the cube too high.
        float offsetY = isStaff
                ? (DEPTH_Y_STAFF - STAFF_FOCUS_Y) / STAFF_FOCUS_Y_SCALE
                : DEPTH_Y_PLAIN / WAND_FOCUS_SCALE;
        int frame = ((int) ticks) % 9;
        poseStack.pushPose();
        poseStack.translate(0.0F, offsetY, 0.0F);
        poseStack.scale(1.0F, height / size, 1.0F);
        WandBoxGeometry.box(poseStack,
                buffer.getBuffer(RenderType.entityTranslucent(depth)),
                -size / 2.0F, -size / 2.0F, -size / 2.0F,
                size / 2.0F, size / 2.0F, size / 2.0F,
                0, frame * 24, 8, 8, 8, 24, 24 * 9, WHITE_TINT, FULL_BRIGHT, true);
        poseStack.popPose();
    }

    private static void submitRunes(WandArg arg, float ticks, PoseStack poseStack, MultiBufferSource buffer) {
        boolean sceptre = arg.sceptre();
        boolean sideRunes = arg.rod().getTag().contains("primal");
        if (!sceptre && !sideRunes) {
            return;
        }
        VertexConsumer consumer = buffer.getBuffer(TCRenderTypes.additiveTexturedNoCull(RUNE_TEXTURE));
        if (sceptre) {
            for (int i = 0; i < RING_RUNES; i++) {
                emitRune(poseStack, consumer, RING_YAW_STEP * i + ticks, RING_DISTANCE, RING_DEPTH, i, ticks);
            }
        }
        if (sideRunes) {
            for (int side = 0; side < SIDE_COUNT; side++) {
                for (int step = 0; step < SIDE_RUNES; step++) {
                    emitRune(poseStack, consumer, SIDE_YAW_STEP * (side + 1),
                            SIDE_DISTANCE_BASE + SIDE_DISTANCE_STEP * step, SIDE_DEPTH,
                            (step + SIDE_GLYPH_STEP * side) % GLYPH_COUNT, ticks);
                }
            }
        }
    }

    private static void emitRune(PoseStack poseStack, VertexConsumer buffer, float yawDegrees,
                                 float distance, float depth, int glyph, float ticks) {
        float phase = ticks + RUNE_PHASE_PER_GLYPH * glyph;
        float red = Math.min(1.0F, RUNE_RED_BASE + RUNE_RED_AMPLITUDE * (float) Math.sin(phase / RUNE_RED_PERIOD));
        float green = Math.min(1.0F, RUNE_GREEN_BASE + RUNE_GREEN_AMPLITUDE * (float) Math.sin(phase / RUNE_GREEN_PERIOD));
        float wobble = RUNE_WOBBLE_AMPLITUDE * (float) Math.sin(phase / RUNE_WOBBLE_PERIOD);
        int tint = argb(Math.min(1.0F, RUNE_ALPHA_BASE + wobble), red, green, RUNE_BLUE);
        float half = RUNE_HALF_SIZE_BASE + wobble / RUNE_HALF_SIZE_WOBBLE;
        double radians = Math.toRadians(yawDegrees);
        float sin = (float) Math.sin(radians);
        float cos = (float) Math.cos(radians);
        float uLeft = (float) glyph / GLYPH_COUNT;
        float uRight = (float) (glyph + 1) / GLYPH_COUNT;
        PoseStack.Pose pose = poseStack.last();
        for (int corner = 0; corner < RUNE_CORNER_X.length; corner++) {
            float cornerX = RUNE_CORNER_X[corner];
            float cornerY = RUNE_CORNER_Y[corner];
            float localX = RUNE_SIDEWAYS + cornerX * half;
            float localY = distance + cornerY * half;
            float x = localX * cos + depth * sin;
            float z = -localX * sin + depth * cos;
            float u = cornerX < 0.0F ? uRight : uLeft;
            float v = cornerY > 0.0F ? 0.0F : 1.0F;
            buffer.vertex(pose.pose(), x, localY, z)
                    .color(tint)
                    .uv(u, v)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(RUNE_LIGHT)
                    .normal(pose.normal(), sin, 0.0F, cos)
                    .endVertex();
        }
    }

    private static void submitBox(PoseStack poseStack, VertexConsumer buffer, BoxSpec box, int tint, int light) {
        WandBoxGeometry.box(poseStack, buffer,
                box.minX() * PIXEL, box.minY() * PIXEL, box.minZ() * PIXEL,
                (box.minX() + box.width()) * PIXEL, (box.minY() + box.height()) * PIXEL,
                (box.minZ() + box.depth()) * PIXEL,
                box.u(), box.v(), box.width(), box.height(), box.depth(),
                TEXTURE_SIZE, TEXTURE_SIZE, tint, light, true);
    }

    private static ResourceLocation wandTexture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID,
                "textures/item/wand/" + name);
    }

    private static RenderType cutout(ResourceLocation texture) {
        return TCRenderTypes.cutoutTexturedNoCull(texture);
    }

    private static ResourceLocation rodTexture(WandRod rod) {
        String name = rod.getTag();
        if (name.endsWith("_staff")) {
            name = name.substring(0, name.length() - "_staff".length());
        }
        return ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/wand/rod_" + name + ".png");
    }

    private static ResourceLocation capTexture(WandCap cap) {
        return ResourceLocation.fromNamespaceAndPath(Arcaneresearch.MODID, "textures/wand/cap_" + cap.getTag() + ".png");
    }

    private static float clientTicks() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return 0.0F;
        }
        return player.tickCount + Minecraft.getInstance().getPartialTick();
    }

    public static float tipModelY(WandArg arg) {
        boolean isStaff = arg.rod().isStaff();
        if (arg.hasFocus()) {
            return isStaff ? TIP_FOCUS_STAFF : TIP_FOCUS_PLAIN;
        }
        if (isStaff) {
            return TIP_STAFF;
        }
        return arg.sceptre() ? TIP_SCEPTRE : TIP_PLAIN;
    }

    public record WandArg(ItemStack stack, WandCap cap, WandRod rod, boolean sceptre,
                          boolean hasFocus, int focusColor) {
    }

    private record BoxSpec(int minX, int minY, int minZ, int width, int height, int depth, int u, int v) {
        static BoxSpec sized(int width, int height, int depth) {
            return new BoxSpec(0, 0, 0, width, height, depth, 0, 0);
        }

        BoxSpec at(int x, int y, int z) {
            return new BoxSpec(x, y, z, width, height, depth, u, v);
        }

        BoxSpec uv(int newU, int newV) {
            return new BoxSpec(minX, minY, minZ, width, height, depth, newU, newV);
        }
    }

    private enum Presence {
        SCEPTRE, NOT_SCEPTRE, STAFF, NOT_STAFF;

        boolean test(boolean sceptre, boolean staff) {
            return switch (this) {
                case SCEPTRE -> sceptre;
                case NOT_SCEPTRE -> !sceptre;
                case STAFF -> staff;
                case NOT_STAFF -> !staff;
            };
        }
    }

    private record CapSlot(Presence presence, float offsetY, float scaleXz, float scaleY, float pivotPixels) {
        void place(PoseStack poseStack, float outerXz, float outerY) {
            poseStack.scale(outerXz, outerY, outerXz);
            poseStack.translate(0.0F, offsetY, 0.0F);
            poseStack.scale(scaleXz, scaleY, scaleXz);
            poseStack.translate(0.0F, pivotPixels * PIXEL, 0.0F);
        }
    }

    private record Fit(float offsetY, float scaleXz, float scaleY) {
        static final Fit NONE = new Fit(0.0F, 1.0F, 1.0F);

        static Fit of(boolean staff, Fit staffFit, Fit plainFit) {
            return staff ? staffFit : plainFit;
        }

        void apply(PoseStack poseStack) {
            poseStack.translate(0.0F, offsetY, 0.0F);
            poseStack.scale(scaleXz, scaleY, scaleXz);
        }
    }

    private static int scaleTint(int argb, float factor) {
        int a = argb >>> 24;
        int r = (int) ((argb >> 16 & 0xFF) * factor);
        int g = (int) ((argb >> 8 & 0xFF) * factor);
        int b = (int) ((argb & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int argb(float alpha, float red, float green, float blue) {
        int a = Math.round(Math.min(1.0F, alpha) * 255.0F);
        int r = Math.round(Math.min(1.0F, red) * 255.0F);
        int g = Math.round(Math.min(1.0F, green) * 255.0F);
        int b = Math.round(Math.min(1.0F, blue) * 255.0F);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
