package arcaneresearch.discovered.scan;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.init.ModItems;
import arcaneresearch.init.ModSounds;
@OnlyIn(Dist.CLIENT)
public final class ScanClient {
    private static final int STUDY_TICKS = 20;
    private ScanClient() {
    }
    private static String studyingKey;
    private static ScanTargets.Target studyingTarget;
    private static int studyingTicks;
    private static boolean released = true;
    private static boolean sentCooldown;
    private static boolean holdingThaumometer(Player player) {
        return ScanTargets.holdingThaumometer(player);
    }
    private static void resetStudy() {
        studyingKey = null;
        studyingTarget = null;
        studyingTicks = 0;
    }
    private static final class Orb {
        Aspect aspect;
        float startX;
        float startY;
        long created;
        long expire;
    }
    private static final class Line {
        Component text;
        long expire;
    }
    private static final List<Orb> orbs = new ArrayList<>();
    private static final List<Line> lines = new ArrayList<>();
    public static void notifyReward(AspectList granted) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || granted == null) {
            return;
        }
        long now = System.currentTimeMillis();
        int stagger = 0;
        for (Aspect aspect : granted.getAspects()) {
            if (aspect == null) {
                continue;
            }
            int amount = granted.getAmount(aspect);
            Line line = new Line();
            line.text = Component.literal("+" + amount + " " + aspect.getName());
            line.expire = now + 2500L + stagger * 150L;
            lines.add(line);
            for (int i = 0; i < amount; i++) {
                Orb orb = new Orb();
                orb.aspect = aspect;
                orb.startX = 0.4F + mc.level.random.nextFloat() * 0.2F;
                orb.startY = 0.4F + mc.level.random.nextFloat() * 0.2F;
                orb.created = now + stagger * 150L + mc.level.random.nextInt(400);
                orb.expire = orb.created + 1500L;
                orbs.add(orb);
                stagger++;
            }
        }
        while (lines.size() > 6) {
            lines.remove(0);
        }
        Player player = mc.player;
        if (player != null) {
            player.level().playLocalSound(player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.0F, false);
        }
    }
    @Mod.EventBusSubscriber(modid = Arcaneresearch.MODID,
            bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            Player player = mc == null ? null : mc.player;
            if (player == null || mc.level == null) {
                resetStudy();
                released = true;
                return;
            }
            boolean useHeld = mc.options.keyUse.isDown() && holdingThaumometer(player)
                    && mc.screen == null;
            if (!useHeld) {
                resetStudy();
                released = true;
                sentCooldown = false;
                return;
            }
            if (sentCooldown) {
                return;
            }
            if (released) {
                released = false;
                resetStudy();
            }
            ScanTargets.Target target = ScanTargets.resolve(mc.level, player);
            if (target.kind == ScanTargets.Kind.NONE) {
                resetStudy();
                return;
            }
            String key = target.steadyKey();
            if (!key.equals(studyingKey)) {
                studyingKey = key;
                studyingTarget = target;
                studyingTicks = 0;
                runes(mc, player, target);
                return;
            }
            studyingTicks++;
            if (studyingTicks % 2 == 0) {
                try {
                    player.level().playLocalSound(player.getX(), player.getY(), player.getZ(),
                            ModSounds.TICKS.get(), SoundSource.PLAYERS, 0.2F,
                            0.45F + player.getRandom().nextFloat() * 0.1F, false);
                } catch (Exception ignored) {
                }
            }
            if (studyingTicks >= STUDY_TICKS && studyingTarget != null) {
                ScanNetwork.CHANNEL.sendToServer(new MsgStudyComplete(studyingTarget));
                resetStudy();
                sentCooldown = true;
            }
        }
        private static void runes(Minecraft mc, Player player, ScanTargets.Target target) {
            try {
                if (target.kind == ScanTargets.Kind.BLOCK && target.pos != null) {
                    for (int a = 0; a < 10; a++) {
                        FXDispatcher.INSTANCE.blockRunes(target.pos.getX(), target.pos.getY() + 0.25D,
                                target.pos.getZ(), 0.3F + mc.level.random.nextFloat() * 0.7F, 0.0F,
                                0.3F + mc.level.random.nextFloat() * 0.7F, 15, 0.03F);
                    }
                } else if (target.kind == ScanTargets.Kind.NODE && target.pos != null) {
                    for (int a = 0; a < 10; a++) {
                        FXDispatcher.INSTANCE.blockRunes(target.pos.getX(), target.pos.getY() + 0.25D,
                                target.pos.getZ(), 0.3F + mc.level.random.nextFloat() * 0.7F, 0.0F,
                                0.3F + mc.level.random.nextFloat() * 0.7F, 15, 0.03F);
                    }
                } else if (target.kind == ScanTargets.Kind.ENTITY && target.entity != null) {
                    Entity entity = target.entity;
                    for (int a = 0; a < 10; a++) {
                        FXDispatcher.INSTANCE.blockRunes(entity.getX() - 0.5D,
                                entity.getY() + entity.getEyeHeight() / 2.0D, entity.getZ() - 0.5D,
                                0.3F + mc.level.random.nextFloat() * 0.7F, 0.0F,
                                0.3F + mc.level.random.nextFloat() * 0.7F,
                                (int) (entity.getBbHeight() * 15.0F), 0.03F);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        @SubscribeEvent
        public static void onRightClickBlock(
                net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
            try {
                if (event.getLevel().isClientSide()) {
                    Player player = event.getEntity();
                    if (holdingThaumometer(player)
                            && arcaneresearch.discovered.table.HexTableFormer.findPairSide(
                                    event.getLevel(), event.getPos()) != null) {
                        arcaneresearch.discovered.table.HexTableNetwork.CHANNEL.sendToServer(
                                new arcaneresearch.discovered.table.MsgStudyTable(event.getPos()));
                    }
                }
            } catch (Exception ignored) {
            }
        }
        @SubscribeEvent
        public static void onRenderGui(RenderGuiEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null || (orbs.isEmpty() && lines.isEmpty())) {
                return;
            }
            long now = System.currentTimeMillis();
            orbs.removeIf(orb -> orb.expire < now);
            lines.removeIf(line -> line.expire < now);
            if (orbs.isEmpty() && lines.isEmpty()) {
                return;
            }
            GuiGraphics graphics = event.getGuiGraphics();
            int sw = event.getWindow().getGuiScaledWidth();
            int sh = event.getWindow().getGuiScaledHeight();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            drawOrbs(graphics, sw, sh, now);
            drawLines(graphics, mc, sw, sh, now);
        }
        private static void drawOrbs(GuiGraphics graphics, int sw, int sh, long now) {
            for (Orb orb : orbs) {
                if (orb.created > now) {
                    continue;
                }
                double startX = sw * orb.startX;
                double startY = sh * orb.startY;
                double endX = sw;
                double endY = -8.0D;
                double bezierX = sw * (0.25D + orb.startX);
                double bezierY = sh * orb.startY;
                double t = (double) (now - orb.created) / Math.max(1L, orb.expire - orb.created);
                if (t < 0.0D || t > 1.0D) {
                    continue;
                }
                double x = (1.0D - t) * (1.0D - t) * startX
                        + 2.0D * (1.0D - t) * t * bezierX + t * t * endX;
                double y = (1.0D - t) * (1.0D - t) * startY
                        + 2.0D * (1.0D - t) * t * bezierY + t * t * endY;
                float alpha = 1.0F;
                if (t < 0.3D) {
                    alpha = (float) (t / 0.3D);
                } else if (t > 0.66D) {
                    alpha = (float) (1.0D - (t - 0.66D) / 0.34D);
                }
                float scale = 1.2F * alpha;
                if (scale <= 0.01F) {
                    continue;
                }
                graphics.pose().pushPose();
                graphics.pose().translate(x, y, 0.0F);
                graphics.pose().scale(scale, scale, 1.0F);
                AspectRenderer.drawAspect(graphics, -8, -8, orb.aspect,
                        0, 0, alpha * 0.66F, false);
                graphics.pose().popPose();
            }
        }
        private static void drawLines(GuiGraphics graphics, Minecraft mc,
                int sw, int sh, long now) {
            int i = 0;
            Iterator<Line> it = lines.iterator();
            while (it.hasNext()) {
                Line line = it.next();
                int width = mc.font.width(line.text);
                int x = sw - width - 12;
                int y = sh - 64 - i * 12;
                float alpha = 1.0F;
                long left = line.expire - now;
                if (left < 500L) {
                    alpha = Math.max(0.0F, left / 500.0F);
                }
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
                graphics.drawString(mc.font, line.text, x, y, 0xFFFFFF, true);
                i++;
            }
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
