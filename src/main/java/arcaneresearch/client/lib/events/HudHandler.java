package arcaneresearch.client.lib.events;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.capabilities.IPlayerKnowledge;
import arcaneresearch.api.casters.ICaster;
import arcaneresearch.api.research.ResearchCategory;
import arcaneresearch.api.research.ScanningManager;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.client.renderers.ScanPopupRenderer;
import arcaneresearch.common.config.ModConfig;
import arcaneresearch.common.items.casters.ItemFocus;
import arcaneresearch.common.items.tools.ItemSanityChecker;
import arcaneresearch.common.items.tools.ItemThaumometer;
import arcaneresearch.common.world.aura.AuraChunk;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HudHandler {
    public static final Queue<KnowledgeGainTracker> knowledgeGainTrackers = new ConcurrentLinkedQueue<>();
    private static final ResourceLocation HUD_TEXTURE =
            new ResourceLocation(Arcaneresearch.MODID, "textures/gui/hud.png");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#######.#");
    private static final float MAX_VIS = 525.0f;
    private static final ResourceLocation BOOK =
            new ResourceLocation(Arcaneresearch.MODID, "textures/item/thaumonomicon.png");
    private static final ResourceLocation[] KNOW_TYPE = {
            new ResourceLocation(Arcaneresearch.MODID, "textures/research/knowledge_theory.png"),
            new ResourceLocation(Arcaneresearch.MODID, "textures/research/knowledge_observation.png")
    };
    private static final ResourceLocation VIGNETTE_TEX =
            new ResourceLocation(Arcaneresearch.MODID, "textures/misc/vignette.png");
    public static AuraChunk currentAura = new AuraChunk(null, (short) 0, 0.0f, 0.0f);
    public static float kgFade = 0f;
    private static int warpVignette = 0;
    private static float targetBrightness = 1.0f;
    private static float prevVignetteBrightness = 0.0f;
    private static ItemStack lastPickedItem = ItemStack.EMPTY;
    private static int lastPickedCount = 0;
    private static long lastPickedRecountTick = -1;
    public static final IGuiOverlay WARP_VIGNETTE = (gui, graphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        float brightness = 1.0f - targetBrightness;
        prevVignetteBrightness += (brightness - prevVignetteBrightness) * 0.01f;
        if (prevVignetteBrightness <= 0.0f) return;
        float b = prevVignetteBrightness
                * (1.0f + Mth.sin(mc.player.tickCount / 2.0f) * 0.1f);
        com.mojang.blaze3d.platform.GlStateManager._disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ZERO,
                com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR);
        RenderSystem.setShaderColor(b, b, b, 1.0f);
        graphics.blit(VIGNETTE_TEX, 0, 0, 0, 0.0f, 0.0f,
                screenWidth, screenHeight, screenWidth, screenHeight);
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        com.mojang.blaze3d.platform.GlStateManager._enableDepthTest();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    };
    public static final IGuiOverlay ARCANERESEARCH_HUD = (gui, graphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui) return;
        int yOffset = 0;
        int bottomOffset = 0;
        boolean holdingThaumometer = false;
        for (int hand = 0; hand < 2; hand++) {
            ItemStack stack = hand == 0 ? player.getMainHandItem() : player.getOffhandItem();
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof ICaster) {
                if (ModConfig.CONFIG_GRAPHICS.dialBottom) {
                    renderCasterHud(graphics, mc, player, stack,
                            screenHeight - 36 - bottomOffset, partialTick);
                    bottomOffset += 36;
                } else {
                    renderCasterHud(graphics, mc, player, stack, yOffset, partialTick);
                    yOffset += 36;
                }
            } else if (stack.getItem() instanceof ItemThaumometer) {
                renderThaumometerHud(graphics, mc, player, yOffset, partialTick);
                yOffset += 80;
                holdingThaumometer = true;
            } else if (stack.getItem() instanceof ItemSanityChecker) {
                renderSanityHud(graphics, mc, player, yOffset);
                yOffset += 76;
            }
        }
        if (false && holdingThaumometer) {
            renderScanTargetHud(graphics, mc, player, screenWidth, screenHeight);
        }
        if (kgFade > 0f) {
            renderKnowledgeGains(graphics, mc, partialTick);
        }
    };
    public static ResourceLocation getKnowledgeTypeIcon(int ordinal) {
        if (ordinal < 0 || ordinal >= KNOW_TYPE.length) {
            return KNOW_TYPE[0];
        }
        return KNOW_TYPE[ordinal];
    }
    public static void tickKnowledgeGains() {
        if (knowledgeGainTrackers.isEmpty()) {
            if (kgFade > 0f) {
                kgFade--;
            }
            return;
        }
        kgFade += 10f;
        if (kgFade > 40f) {
            kgFade = 40f;
        }
        for (KnowledgeGainTracker current : knowledgeGainTrackers.toArray(new KnowledgeGainTracker[0])) {
            if (current.progress > 0) {
                current.progress--;
            } else {
                knowledgeGainTrackers.remove(current);
            }
        }
    }
    public static void tickWarpVignette() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.hasEffect(
                arcaneresearch.init.ModEffects.DEATH_GAZE.get())) {
            warpVignette = 10;
        }
        try {
            if (warpVignette > 0) {
                --warpVignette;
                targetBrightness = 0.0f;
            } else {
                targetBrightness = 1.0f;
            }
        } catch (Exception ignored) {
        }
    }
    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("arcaneresearch_hud", ARCANERESEARCH_HUD);
        event.registerAboveAll("arcaneresearch_warp_vignette", WARP_VIGNETTE);
        Arcaneresearch.LOGGER.info("Registered Arcaneresearch HUD overlay");
    }
    private static void renderKnowledgeGains(GuiGraphics graphics, Minecraft mc, float partialTick) {
        int ww = mc.getWindow().getGuiScaledWidth();
        int hh = mc.getWindow().getGuiScaledHeight();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, kgFade / 40f);
        graphics.blit(BOOK, ww - 17, hh - 17, 16, 16, 0f, 0f, 32, 32, 32, 32);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        int depth = 0;
        for (KnowledgeGainTracker current : knowledgeGainTrackers.toArray(new KnowledgeGainTracker[0])) {
            Random rand = new Random(current.seed);
            float s = 16f;
            float x = ww / 4f + rand.nextInt(32);
            float y = hh / 3f + rand.nextInt(32);
            if (current.progress < current.max * 0.66f) {
                float q = (current.progress - partialTick) / (current.max * 0.66f);
                s *= q;
                float m = (float) Math.sin(q * Math.PI - Math.PI / 2) * 0.5f + 0.5f;
                y *= m;
                float d = (float) Math.sin(m * Math.PI * 0.5);
                x *= d;
            } else {
                float wot = current.max - current.progress + partialTick;
                float wot2 = wot / (current.max * 0.33f);
                float m = (float) Math.sin(wot2 * Math.PI * 2 - Math.PI / 2) * 0.5f + 1.5f;
                if (wot2 < 0.5f) {
                    s *= wot2 * 2f;
                }
                s *= m;
            }
            float xx = ww - 12 + rand.nextInt(8) - x;
            float yy = hh - 12 + rand.nextInt(8) - y;
            Player hudPlayer = mc.player;
            if (current.sparks && hudPlayer != null
                    && hudPlayer.getRandom().nextInt((int) (1.0f + current.progress / (float) current.max * 10.0f)) == 0) {
                net.minecraft.util.RandomSource prand = hudPlayer.getRandom();
                float r = 1.0f;
                float g = (189 + prand.nextInt(67)) / 255.0f;
                float b = (64 + prand.nextInt(192)) / 255.0f;
                FXDispatcher.INSTANCE.drawSimpleSparkleGui(prand,
                        xx + prand.nextGaussian() * 5.0, yy + prand.nextGaussian() * 5.0,
                        prand.nextGaussian(), prand.nextGaussian(),
                        24.0f, r, g, b, prand.nextInt(5), 0.9f, -1.0f);
            }
            graphics.pose().pushPose();
            graphics.pose().translate(xx + 8, yy + 8, -80 + depth);
            graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(84 + rand.nextInt(12)));
            float half = s / 2f;
            graphics.blit(KNOW_TYPE[current.type.ordinal()], (int) -half, (int) -half,
                    (int) s, (int) s, 0, 0, 16, 16, 16, 16);
            if (current.categoryIcon != null) {
                int cs = (int) (s * 0.75f);
                graphics.blit(current.categoryIcon, -cs / 2, -cs / 2,
                        cs, cs, 0, 0, 32, 32, 32, 32);
            }
            float flash = 0f;
            if (current.progress > current.max * 0.9f) {
                flash = (current.max - current.progress + partialTick) / (current.max * 0.1f);
            } else if (current.progress < current.max * 0.1f) {
                flash = 1f - (current.progress - partialTick) / (current.max * 0.1f);
            }
            if (flash > 0f && flash <= 1f) {
                RenderSystem.setShaderColor(1f, 0.9f, 0.5f, 0.5f * (1f - flash * 0.5f));
                int flashSize = (int) (64f * (0.25f + 0.25f
                        * (float) Math.sin(flash * Math.PI * 2 - Math.PI / 2)));
                graphics.blit(KNOW_TYPE[current.type.ordinal()], -flashSize / 2, -flashSize / 2,
                        flashSize, flashSize, 0, 0, 16, 16, 16, 16);
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            }
            graphics.pose().popPose();
            depth++;
        }
    }
    private static void renderThaumometerHud(GuiGraphics graphics, Minecraft mc, Player player,
                                             int yOffset, float partialTicks) {
        float base = Mth.clamp(currentAura != null ? currentAura.getBase() : 100, 0, MAX_VIS) / MAX_VIS;
        float vis = Mth.clamp(currentAura != null ? currentAura.getVis() : 50, 0, MAX_VIS) / MAX_VIS;
        float flux = Mth.clamp(currentAura != null ? currentAura.getFlux() : 10, 0, MAX_VIS) / MAX_VIS;
        float count = player.tickCount + partialTicks;
        float count2 = player.tickCount / 3f + partialTicks;
        if (flux + vis > 1f) {
            float m = 1f / (flux + vis);
            base *= m;
            vis *= m;
            flux *= m;
        }
        RenderSystem.setShaderTexture(0, HUD_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        graphics.pose().pushPose();
        graphics.pose().translate(2, yOffset, 0);
        if (vis > 0f) {
            float start = 10f + (1f - vis) * 64f;
            graphics.pose().pushPose();
            graphics.pose().translate(5, start, 0);
            graphics.pose().scale(1f, vis, 1f);
            RenderSystem.setShaderColor(0.7f, 0.4f, 0.9f, 1f);
            graphics.blit(HUD_TEXTURE, 0, 0, 88, 56, 8, 64, 256, 256);
            graphics.pose().popPose();
            RenderSystem.blendFunc(770, 1);
            RenderSystem.setShaderColor(1f, 1f, 1f, 0.5f);
            graphics.blit(HUD_TEXTURE, 5, (int) start, 96, (int) (56 + count % 64), 8, (int) (vis * 64), 256, 256);
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            if (player.isShiftKeyDown()) {
                graphics.pose().pushPose();
                graphics.pose().translate(16, start, 0);
                graphics.pose().scale(0.5f, 0.5f, 1f);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                graphics.drawString(mc.font, DECIMAL_FORMAT.format(currentAura != null ? currentAura.getVis() : 0),
                        0, 0, 0xEE99FF, false);
                graphics.pose().popPose();
                RenderSystem.setShaderTexture(0, HUD_TEXTURE);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
            }
        }
        if (flux > 0f) {
            float start = 10f + (1f - flux - vis) * 64f;
            graphics.pose().pushPose();
            graphics.pose().translate(5, start, 0);
            graphics.pose().scale(1f, flux, 1f);
            RenderSystem.setShaderColor(0.25f, 0.1f, 0.3f, 1f);
            graphics.blit(HUD_TEXTURE, 0, 0, 88, 56, 8, 64, 256, 256);
            graphics.pose().popPose();
            RenderSystem.blendFunc(770, 1);
            RenderSystem.setShaderColor(0.7f, 0.4f, 1f, 0.5f);
            graphics.blit(HUD_TEXTURE, 5, (int) start, 104, (int) (120 - count2 % 64), 8, (int) (flux * 64), 256, 256);
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            if (player.isShiftKeyDown()) {
                graphics.pose().pushPose();
                graphics.pose().translate(16, start - 4, 0);
                graphics.pose().scale(0.5f, 0.5f, 1f);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                graphics.drawString(mc.font, DECIMAL_FORMAT.format(currentAura != null ? currentAura.getFlux() : 0),
                        0, 0, 0xAA33BB, false);
                graphics.pose().popPose();
                RenderSystem.setShaderTexture(0, HUD_TEXTURE);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
            }
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        graphics.blit(HUD_TEXTURE, 1, 1, 72, 48, 16, 80, 256, 256);
        graphics.blit(HUD_TEXTURE, 2, (int) (8 + (1 - base) * 64), 117, 61, 14, 5, 256, 256);
        graphics.pose().popPose();
        RenderSystem.disableBlend();
    }
    @Deprecated(forRemoval = true)
    private static void renderScanTargetHud(GuiGraphics graphics, Minecraft mc, Player player,
                                            int screenWidth, int screenHeight) {
        AspectList tags = ScanPopupRenderer.getCurrentAspects(player);
        if (tags == null || tags.size() == 0) {
            return;
        }
        Object target = ScanPopupRenderer.getCurrentTarget(player);
        boolean scannable = false;
        try {
            scannable = target != null && ScanningManager.isThingStillScannable(player, target);
        } catch (Exception ignored) {
        }
        int cx = screenWidth / 2;
        int y = screenHeight / 2 + 12;
        Component status = Component.translatable(scannable ? "tc.hud.scan_new" : "tc.hud.scan_known");
        int sw = mc.font.width(status);
        graphics.drawString(mc.font, status, cx - sw / 2, y, scannable ? 0x55FFFF : 0xAAAAAA, true);
        y += 10;
        java.util.List<Aspect> aspects = java.util.Arrays.asList(tags.getAspectsSortedByAmount());
        final int perRow = 8;
        final int pitch = 20;
        for (int row = 0; row * perRow < aspects.size(); row++) {
            int from = row * perRow;
            int to = Math.min(aspects.size(), from + perRow);
            java.util.List<Aspect> slice = aspects.subList(from, to);
            int rowWidth = slice.size() * pitch - (pitch - 16);
            int x = cx - rowWidth / 2;
            for (Aspect aspect : slice) {
                AspectRenderer.drawAspect(graphics, x, y, aspect, tags.getAmount(aspect));
                x += pitch;
            }
            y += 20;
        }
    }
    private static void renderSanityHud(GuiGraphics graphics, Minecraft mc, Player player, int yOffset) {
        arcaneresearch.api.capabilities.IPlayerWarp warp =
                arcaneresearch.api.capabilities.ArcaneresearchCapabilities.getWarp(player);
        int p = warp != null ? warp.get(arcaneresearch.api.capabilities.IPlayerWarp.EnumWarpType.PERMANENT) : 0;
        int s = warp != null ? warp.get(arcaneresearch.api.capabilities.IPlayerWarp.EnumWarpType.NORMAL) : 0;
        int t = warp != null ? warp.get(arcaneresearch.api.capabilities.IPlayerWarp.EnumWarpType.TEMPORARY) : 0;
        float tw = p + s + t;
        float mod = 1f;
        if (tw > 100f) {
            mod = 100f / tw;
            tw = 100f;
        }
        int gap = (int) ((100f - tw) / 100f * 48f);
        int wt = (int) (t / 100f * 48f * mod);
        int ws = (int) (s / 100f * 48f * mod);
        RenderSystem.setShaderTexture(0, HUD_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.pose().pushPose();
        graphics.pose().translate(0, yOffset, 0);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        graphics.blit(HUD_TEXTURE, 1, 1, 152, 0, 20, 76, 256, 256);
        if (t > 0) {
            RenderSystem.setShaderColor(1f, 0.5f, 1f, 1f);
            graphics.blit(HUD_TEXTURE, 7, 21 + gap, 200, gap, 8, wt + gap, 256, 256);
        }
        if (s > 0) {
            RenderSystem.setShaderColor(0.75f, 0f, 0.75f, 1f);
            graphics.blit(HUD_TEXTURE, 7, 21 + wt + gap, 200, wt + gap, 8, wt + ws + gap, 256, 256);
        }
        if (p > 0) {
            RenderSystem.setShaderColor(0.5f, 0f, 0.5f, 1f);
            graphics.blit(HUD_TEXTURE, 7, 21 + wt + ws + gap, 200, wt + ws + gap, 8, 48, 256, 256);
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        graphics.blit(HUD_TEXTURE, 1, 1, 176, 0, 20, 76, 256, 256);
        if (tw >= 100f) {
            java.util.Random rng = new java.util.Random();
            graphics.pose().pushPose();
            graphics.pose().scale(0.75f, 0.75f, 1f);
            graphics.blit(HUD_TEXTURE, 3 + rng.nextInt(2), 3 + rng.nextInt(2), 216, 0, 20, 16, 256, 256);
            graphics.pose().popPose();
        }
        graphics.pose().popPose();
        RenderSystem.disableBlend();
    }
    private static void renderCasterHud(GuiGraphics graphics, Minecraft mc, Player player,
                                        ItemStack casterStack, int yOffset, float partialTicks) {
        ICaster caster = (ICaster) casterStack.getItem();
        float maxVis = currentAura != null ? currentAura.getBase() : 100;
        float currentVis = currentAura != null ? currentAura.getVis() : 50;
        RenderSystem.setShaderTexture(0, HUD_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        graphics.pose().pushPose();
        graphics.pose().translate(0, yOffset, 0);
        graphics.pose().pushPose();
        graphics.pose().scale(0.5f, 0.5f, 1f);
        graphics.blit(HUD_TEXTURE, 0, 0, 0, 0, 64, 64, 256, 256);
        graphics.pose().popPose();
        graphics.pose().translate(16, 16, 0);
        int amt = (int) currentVis;
        int loc = maxVis > 0 ? (int) (30f * amt / maxVis) : 0;
        Color ac = new Color(Aspect.ENERGY.getColor());
        RenderSystem.setShaderColor(ac.getRed() / 255f, ac.getGreen() / 255f, ac.getBlue() / 255f, 0.8f);
        graphics.pose().pushPose();
        graphics.pose().translate(16, -10, 0);
        graphics.pose().scale(0.5f, 0.5f, 1f);
        graphics.blit(HUD_TEXTURE, -4, 35 - loc, 104, 0, 8, loc, 256, 256);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        graphics.pose().popPose();
        graphics.pose().pushPose();
        graphics.pose().translate(16, -10, 0);
        graphics.pose().scale(0.5f, 0.5f, 1f);
        graphics.blit(HUD_TEXTURE, -8, -3, 72, 0, 16, 42, 256, 256);
        graphics.pose().popPose();
        ItemStack focusStack = caster.getFocusStack(casterStack);
        ItemFocus focus = focusStack != null && !focusStack.isEmpty()
                && focusStack.getItem() instanceof ItemFocus f ? f : null;
        if (player.isShiftKeyDown()) {
            graphics.pose().pushPose();
            graphics.pose().translate(16, -10, 0);
            graphics.pose().scale(0.5f, 0.5f, 1f);
            graphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-90f));
            graphics.drawString(mc.font, DECIMAL_FORMAT.format(amt), -32, -4, 0xFFFFFF, false);
            if (focus != null && focus.getVisCost(focusStack) > 0f) {
                float mod = caster.getConsumptionModifier(casterStack, player, false);
                String msg = DECIMAL_FORMAT.format(focus.getVisCost(focusStack) * mod);
                graphics.drawString(mc.font, msg,
                        -32 - mc.font.width(msg) / 2, 32, 0xFFFFFF, false);
            }
            graphics.pose().popPose();
            RenderSystem.setShaderTexture(0, HUD_TEXTURE);
        }
        graphics.pose().popPose();
        if (focus != null) {
            ItemStack picked = caster.getPickedBlock(player.getMainHandItem());
            if (picked != null && !picked.isEmpty()) {
                renderPickedBlockHud(graphics, mc, player, picked, yOffset);
            } else {
                graphics.pose().pushPose();
                graphics.pose().translate(0, yOffset, 90);
                arcaneresearch.client.gui.GuiRenderHelper.renderItem(graphics, focusStack, 8, 8);
                graphics.pose().popPose();
            }
        }
        RenderSystem.disableBlend();
    }
    private static void renderPickedBlockHud(GuiGraphics graphics, Minecraft mc, Player player,
                                             ItemStack picked, int yOffset) {
        graphics.pose().pushPose();
        graphics.pose().translate(0, yOffset, 90);
        arcaneresearch.client.gui.GuiRenderHelper.renderItem(graphics, picked, 8, 8);
        long gameTime = player.level().getGameTime();
        if (lastPickedItem.isEmpty() || !ItemStack.isSameItemSameTags(picked, lastPickedItem)
                || gameTime - lastPickedRecountTick > 20) {
            int amount = 0;
            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && ItemStack.isSameItemSameTags(stack, picked)) {
                    amount += stack.getCount();
                }
            }
            lastPickedItem = picked.copy();
            lastPickedCount = amount;
            lastPickedRecountTick = gameTime;
        }
        String count = String.valueOf(lastPickedCount);
        int width = mc.font.width(count);
        graphics.pose().translate(16, 16, 110);
        graphics.pose().scale(0.5f, 0.5f, 1f);
        int tx = 16 - width;
        int ty = 24 - mc.font.lineHeight;
        graphics.drawString(mc.font, count, tx - 1, ty, 0x000000, false);
        graphics.drawString(mc.font, count, tx + 1, ty, 0x000000, false);
        graphics.drawString(mc.font, count, tx, ty - 1, 0x000000, false);
        graphics.drawString(mc.font, count, tx, ty + 1, 0x000000, false);
        graphics.drawString(mc.font, count, tx, ty, 0xFFFFFF, false);
        graphics.pose().popPose();
    }
    private static void drawColoredRect(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + height, color);
    }
    private static void drawColoredRect(GuiGraphics graphics, int x, int y, int width, int height,
                                        int color, float alpha) {
        int a = (int) (((color >> 24) & 0xFF) * alpha);
        int finalColor = (a << 24) | (color & 0x00FFFFFF);
        graphics.fill(x, y, x + width, y + height, finalColor);
    }
    public static void updateAura(AuraChunk aura) {
        currentAura = aura;
    }
    public static void updateAura(float base, float vis, float flux) {
        if (currentAura == null) {
            currentAura = new AuraChunk(null, (short) 0, vis, flux);
        }
        currentAura.setBase((short) base);
        currentAura.setVis(vis);
        currentAura.setFlux(flux);
    }
    public static class KnowledgeGainTracker {
        final IPlayerKnowledge.EnumKnowledgeType type;
        final ResourceLocation categoryIcon;
        final int max;
        final long seed;
        int progress;
        boolean sparks;
        public KnowledgeGainTracker(IPlayerKnowledge.EnumKnowledgeType type, ResearchCategory category,
                                    int progress, long seed) {
            this.sparks = false;
            this.type = type;
            this.categoryIcon = category != null ? category.icon : null;
            if (type == IPlayerKnowledge.EnumKnowledgeType.THEORY) {
                progress += 10;
            }
            this.progress = progress;
            this.max = progress;
            this.seed = seed;
        }
    }
}
