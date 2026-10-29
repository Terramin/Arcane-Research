package arcaneresearch.discovered.scan;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.client.fx.FXDispatcher;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.common.lib.network.PacketHandler;
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
        if (granted == null || mc.level == null) {
            return;
        }
        Aspect[] aspects = granted.getAspects();
        if (aspects == null) {
            return;
        }
        long now = System.currentTimeMillis();
        int stagger = 0;
        for (Aspect aspect : aspects) {
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
                orb.startX = 0.4F + mc.level.getRandom().nextFloat() * 0.2F;
                orb.startY = 0.4F + mc.level.getRandom().nextFloat() * 0.2F;
                orb.created = now + stagger * 150L + mc.level.getRandom().nextInt(400);
                orb.expire = orb.created + 1500L;
                orbs.add(orb);
                stagger++;
            }
        }
        while (lines.size() > 6) {
            lines.removeFirst();
        }
        Player player = mc.player;
        if (player != null) {

            player.level().playLocalSound(player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.0F, false);
        }
    }


    @EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
    public static class ClientEvents {

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null || mc.level == null) {
                resetStudy();
                released = true;
                return;
            }
            boolean useHeld = mc.options.keyUse.isDown() && holdingThaumometer(player)
                    && mc.gui.screen() == null;
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
            if ((studyingTicks & 1) == 0) {

                try {
                    player.level().playLocalSound(player.getX(), player.getY(), player.getZ(),
                            ModSounds.TICKS.get(), SoundSource.PLAYERS, 0.2F,
                            0.45F + player.getRandom().nextFloat() * 0.1F, false);
                } catch (Exception ignored) {
                }
            }
            if (studyingTicks >= STUDY_TICKS && studyingTarget != null) {
                try {
                    PacketHandler.sendToServer(new MsgStudyComplete(studyingTarget));
                } catch (Exception ignored) {
                }
                resetStudy();
                sentCooldown = true;
            }
        }

        private static void runes(Minecraft mc, Player player, ScanTargets.Target target) {
            try {
                if (target.kind == ScanTargets.Kind.BLOCK && target.pos != null) {
                    for (int a = 0; a < 10; a++) {
                        assert mc.level != null;
                        FXDispatcher.INSTANCE.blockRunes(target.pos.getX(), target.pos.getY() + 0.25D,
                                target.pos.getZ(), 0.3F + mc.level.getRandom().nextFloat() * 0.7F, 0.0F,
                                0.3F + mc.level.getRandom().nextFloat() * 0.7F, 15, 0.03F);
                    }
                } else if (target.kind == ScanTargets.Kind.NODE && target.pos != null) {

                    for (int a = 0; a < 10; a++) {
                        assert mc.level != null;
                        FXDispatcher.INSTANCE.blockRunes(target.pos.getX(), target.pos.getY() + 0.25D,
                                target.pos.getZ(), 0.3F + mc.level.getRandom().nextFloat() * 0.7F, 0.0F,
                                0.3F + mc.level.getRandom().nextFloat() * 0.7F, 15, 0.03F);
                    }
                } else if (target.kind == ScanTargets.Kind.ENTITY && target.entity != null) {
                    Entity entity = target.entity;
                    for (int a = 0; a < 10; a++) {
                        assert mc.level != null;
                        FXDispatcher.INSTANCE.blockRunes(entity.getX() - 0.5D,
                                entity.getY() + entity.getEyeHeight() / 2.0D, entity.getZ() - 0.5D,
                                0.3F + mc.level.getRandom().nextFloat() * 0.7F, 0.0F,
                                0.3F + mc.level.getRandom().nextFloat() * 0.7F,
                                (int) (entity.getBbHeight() * 15.0F), 0.03F);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        @SubscribeEvent
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {


            try {
                if (event.getLevel().isClientSide()) {
                    Player player = event.getEntity();
                    if (holdingThaumometer(player)
                            && arcaneresearch.discovered.table.HexTableFormer.findPairSide(
                            event.getLevel(), event.getPos()) != null) {
                        PacketHandler.sendToServer(
                                new arcaneresearch.discovered.table.MsgStudyTable(event.getPos()));
                    }
                }
            } catch (Exception ignored) {
            }
        }

        public static final GuiLayer SCAN_LAYER = (graphics, deltaTracker) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || orbs.isEmpty() && lines.isEmpty()) {
                return;
            }
            long now = System.currentTimeMillis();
            orbs.removeIf(orb -> orb.expire < now);
            lines.removeIf(line -> line.expire < now);
            if (orbs.isEmpty() && lines.isEmpty()) {
                return;
            }
            int sw = mc.getWindow().getGuiScaledWidth();
            int sh = mc.getWindow().getGuiScaledHeight();
            drawOrbs(graphics, sw, sh, now);
            drawLines(graphics, mc, sw, sh, now);
        };

        @SubscribeEvent
        public static void registerLayers(RegisterGuiLayersEvent event) {
            event.registerAbove(VanillaGuiLayers.EXPERIENCE_LEVEL,
                    Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "scan_notifications"), SCAN_LAYER);
        }


        private static void drawOrbs(GuiGraphicsExtractor graphics, int sw, int sh, long now) {
            for (Orb orb : orbs) {
                if (orb.created > now || orb.aspect == null) {
                    continue;
                }
                double startX = sw * orb.startX;
                double startY = sh * orb.startY;
                double endY = -8.0D;
                double bezierX = sw * (0.25D + orb.startX);
                double bezierY = sh * orb.startY;
                double t = (double) (now - orb.created) / Math.max(1L, orb.expire - orb.created);
                if (t < 0.0D || t > 1.0D) {
                    continue;
                }
                double x = (1.0D - t) * (1.0D - t) * startX
                        + 2.0D * (1.0D - t) * t * bezierX + t * t * (double) sw;
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
                graphics.pose().pushMatrix();
                graphics.pose().translate((float) x, (float) y);
                graphics.pose().scale(scale, scale);
                AspectRenderer.drawAspect(graphics, -8, -8, orb.aspect,
                        0, 0, alpha * 0.66F, false);
                graphics.pose().popMatrix();
            }
        }


        private static void drawLines(GuiGraphicsExtractor graphics, Minecraft mc,
                                      int sw, int sh, long now) {
            int i = 0;
            for (Line line : lines) {
                if (line.text == null) {
                    i++;
                    continue;
                }
                int width = mc.font.width(line.text);
                int x = sw - width - 12;
                int y = sh - 64 - i * 12;
                float alpha = 1.0F;
                long left = line.expire - now;
                if (left < 500L) {
                    alpha = Math.max(0.0F, left / 500.0F);
                }
                graphics.text(mc.font, line.text, x, y, ARGB.white(alpha), true);
                i++;
            }
        }
    }
}
