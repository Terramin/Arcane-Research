package arcaneresearch.invscan;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.common.items.tools.ItemThaumometer;
import arcaneresearch.discovered.scan.ScanTargets;
import arcaneresearch.discovered.scan.ScanWallet;
import arcaneresearch.helper.RegsHelper;

@OnlyIn(Dist.CLIENT)
public final class InvScanClient {

    private static final int SCAN_TICKS = 20;
    private static final int PLAYER_X1 = 26;
    private static final int PLAYER_Y1 = 8;
    private static final int PLAYER_X2 = 75;
    private static final int PLAYER_Y2 = 78;
    private static final String SELF_SUBJECT = "self";

    private static final Set<String> LOCAL_SCANNED = new HashSet<>();

    private static int mouseX;
    private static int mouseY;
    private static String scanKey = "";
    private static AspectList subjectAspects;
    private static int scanTicks;
    private static boolean attempted;
    private static boolean subjectIsSelf;

    private static AspectList cachedSelfAspects;
    private static boolean cachedSelfAspectsValid;

    private InvScanClient() {
    }

    private static void reset() {
        scanKey = "";
        subjectAspects = null;
        scanTicks = 0;
        attempted = false;
        subjectIsSelf = false;
        cachedSelfAspectsValid = false;
    }

    private static void resetAll() {
        reset();
        LOCAL_SCANNED.clear();
    }

    private static boolean hasBeenScanned(Player player, String key) {
        return LOCAL_SCANNED.contains(key) || ScanWallet.hasScanned(player, key);
    }

    private static boolean holdingThaumometer(AbstractContainerScreen<?> screen) {
        return screen.getMenu().getCarried().getItem() instanceof ItemThaumometer;
    }

    private static AbstractContainerScreen<?> activeScreen(Minecraft minecraft) {
        if (minecraft.screen instanceof AbstractContainerScreen<?> screen
                && !(screen instanceof CreativeModeInventoryScreen)
                && holdingThaumometer(screen)) {
            return screen;
        }
        return null;
    }

    private static boolean hoveringSelf(AbstractContainerScreen<?> screen, Player player) {
        if (screen.getMenu() != player.inventoryMenu) {
            return false;
        }
        int x = mouseX - ScreenAccess.leftPos(screen);
        int y = mouseY - ScreenAccess.topPos(screen);
        return x >= PLAYER_X1 && x < PLAYER_X2 && y >= PLAYER_Y1 && y < PLAYER_Y2;
    }

    private static AspectList getSelfAspects(Player player) {
        if (!cachedSelfAspectsValid) {
            cachedSelfAspects = ScanTargets.entityAspects(player);
            cachedSelfAspectsValid = true;
        }
        return cachedSelfAspects;
    }

    private static boolean resolveSubject(AbstractContainerScreen<?> screen, Player player) {
        boolean self = hoveringSelf(screen, player);
        String key;
        Slot slot = null;

        if (self) {
            key = SELF_SUBJECT;
        } else {
            slot = ScreenAccess.hoveredSlot(screen);
            if (slot == null || slot.getItem().isEmpty() || slot instanceof ResultSlot
                    || !slot.mayPickup(player)) {
                return false;
            }
            key = slot.index + ":" + RegsHelper.itemKey(slot.getItem().getItem());
        }
        if (key.equals(scanKey)) {
            return true;
        }
        scanKey = key;
        subjectIsSelf = self;
        subjectAspects = self ? getSelfAspects(player)
                : AspectHelper.getObjectAspects(slot.getItem());
        scanTicks = 0;
        attempted = false;
        return true;
    }

    private static boolean scannable(Player player) {
        return subjectAspects != null && subjectAspects.size() > 0
                && !hasBeenScanned(player, scanKey);
    }

    @Mod.EventBusSubscriber(modid = Arcaneresearch.MODID,
            bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            resetAll();
        }

        @SubscribeEvent
        public static void onClone(ClientPlayerNetworkEvent.Clone event) {
            reset();
        }

        @SubscribeEvent
        @SuppressWarnings("resource")
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;
            AbstractContainerScreen<?> screen = player == null ? null : activeScreen(minecraft);
            boolean resolved = screen != null && resolveSubject(screen, player);
            if (!resolved) {
                reset();
                return;
            }
            if (attempted) {
                return;
            }
            if (!scannable(player)) {
                scanTicks = 0;
                attempted = true;
                return;
            }
            scanTicks++;
            if (scanTicks % 2 == 0) {
                try {
                    player.level().playLocalSound(
                            player.getX(), player.getY(), player.getZ(),
                            arcaneresearch.init.ModSounds.TICKS.get(),
                            SoundSource.PLAYERS,
                            0.2F,
                            0.45F + player.getRandom().nextFloat() * 0.1F,
                            false);
                } catch (Exception e) {
                    Arcaneresearch.LOGGER.debug("InvScan: tick sound failed", e);
                }
            }
            if (scanTicks >= SCAN_TICKS) {
                attempted = true;
                if (subjectAspects != null && ScanTargets.validScan(player, subjectAspects)) {
                    if (subjectIsSelf) {
                        InvScanNetwork.CHANNEL.sendToServer(new MsgScanSelf());
                    } else {
                        Slot slot = ScreenAccess.hoveredSlot(screen);
                        if (slot != null) {
                            InvScanNetwork.CHANNEL.sendToServer(
                                    new MsgScanSlot(screen.getMenu().containerId, slot.index));
                        }
                    }
                    LOCAL_SCANNED.add(scanKey);
                }
                scanTicks = 0;
            }
        }

        @SubscribeEvent
        public static void onScreenRender(ScreenEvent.Render.Post event) {
            mouseX = event.getMouseX();
            mouseY = event.getMouseY();
            if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
                return;
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (scanTicks > 0 && !attempted) {
                drawProgress(event.getGuiGraphics(), minecraft);
            } else if (minecraft.player != null && activeScreen(minecraft) == screen
                    && hoveringSelf(screen, minecraft.player)) {
                drawSelfAspects(event.getGuiGraphics(), minecraft.player);
            }
        }

        private static void drawProgress(GuiGraphics graphics, Minecraft minecraft) {
            float progress = scanTicks / (float) SCAN_TICKS;
            StringBuilder text = new StringBuilder(
                    Component.translatable("arcaneresearch.invscan.scanning").getString());
            if (progress >= 0.75F) {
                text.append("...");
            } else if (progress >= 0.5F) {
                text.append("..");
            } else if (progress >= 0.25F) {
                text.append(".");
            }
            graphics.drawString(minecraft.font, text.toString(),
                    mouseX, mouseY - 30, 0xFFFFAA00, true);
        }

        private static void drawSelfAspects(GuiGraphics graphics, Player player) {
            if (!hasBeenScanned(player, InvScanServer.SELF_KEY)) {
                return;
            }
            AspectList aspects = getSelfAspects(player);
            if (aspects == null || aspects.size() <= 0) {
                return;
            }
            int index = 0;
            for (Aspect aspect : aspects.getAspectsSortedByAmount()) {
                if (aspect == null) {
                    continue;
                }
                int x = mouseX + 9 + index * 18;
                int y = mouseY - 34;
                graphics.fill(x - 1, y - 1, x + 17, y + 17, 0x66000000);
                int amount = aspects.getAmount(aspect);
                boolean known = ScanTargets.known(player, aspect);
                AspectRenderer.drawAspect(graphics, x, y, aspect,
                        amount, 0, known ? 1.0F : 0.45F, !known);
                index++;
            }
        }
    }
}