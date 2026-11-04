package arcaneresearch.invscan;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.common.items.tools.ItemThaumometer;
import arcaneresearch.common.lib.crafting.ArcaneresearchCraftingManager;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.discovered.scan.ScanTargets;
import arcaneresearch.init.ModSounds;
import arcaneresearch.invscan.client.ScanFlightSystem;
import arcaneresearch.invscan.mixin.AbstractContainerScreenInvoker;

@OnlyIn(Dist.CLIENT)
public final class InvScanClient {

    private static final int SCAN_TICKS = 20;
    private static final int PLAYER_X1 = 26;
    private static final int PLAYER_Y1 = 8;
    private static final int PLAYER_X2 = 75;
    private static final int PLAYER_Y2 = 78;

    private static final Set<String> SCANNED_SYNCED = new HashSet<>();

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

    public static void applyScannedSync(List<String> keys, boolean replace) {
        if (keys == null) {
            return;
        }
        if (replace) {
            SCANNED_SYNCED.clear();
        }
        SCANNED_SYNCED.addAll(keys);
    }

    public static boolean hasBeenScanned(String key) {
        return SCANNED_SYNCED.contains(key);
    }

    public static boolean canUseThaumometer(AbstractContainerScreen<?> screen) {
        return holdingThaumometer(screen);
    }

    public static boolean isScannableSlot(AbstractContainerScreen<?> screen, Slot slot) {
        if (slot == null || !slot.hasItem() || !slot.isActive()) return false;
        Player p = Minecraft.getInstance().player;
        if (p == null) return false;
        if (!slot.mayPickup(p)) return false;
        return !(slot instanceof ResultSlot);
    }

    public static Slot scannableSlot(AbstractContainerScreen<?> screen, double mx, double my) {
        Slot slot = ((AbstractContainerScreenInvoker) screen).invscan$findSlot(mx, my);
        return isScannableSlot(screen, slot) ? slot : null;
    }

    public static int activeTicks(AbstractContainerScreen<?> screen, Slot slot) {
        if (Minecraft.getInstance().gui.screen() != screen) return 0;
        if (attempted || scanTicks <= 0) return 0;
        if (slot == null) return 0;
        String expected = InvScanServer.itemScanKey(slot.getItem());
        if (!expected.equals(scanKey)) return 0;
        return scanTicks;
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
        ScanFlightSystem.clear();
    }

    private static boolean holdingThaumometer(AbstractContainerScreen<?> screen) {
        return screen.getMenu().getCarried().getItem() instanceof ItemThaumometer;
    }

    private static AbstractContainerScreen<?> activeScreen(Minecraft minecraft) {
        if (minecraft.gui.screen() instanceof AbstractContainerScreen<?> screen
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
            key = InvScanServer.SELF_KEY;
        } else {
            slot = ScreenAccess.hoveredSlot(screen);
            if (slot == null || slot.getItem().isEmpty() || slot instanceof ResultSlot
                    || !slot.mayPickup(player)) {
                return false;
            }
            key = InvScanServer.itemScanKey(slot.getItem());
        }
        if (key.equals(scanKey)
                && subjectAspects != null && subjectAspects.size() > 0) {
            return true;
        }
        scanKey = key;
        subjectIsSelf = self;
        subjectAspects = self ? getSelfAspects(player)
                : ArcaneresearchCraftingManager.getObjectTags(slot.getItem());
        scanTicks = 0;
        attempted = false;
        return true;
    }

    private static boolean scannable(Player player) {
        return subjectAspects != null && subjectAspects.size() > 0
                && !hasBeenScanned(scanKey);
    }

    @EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
    public static class ClientEvents {

        @SubscribeEvent
        public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            resetAll();
        }

        @SubscribeEvent
        public static void onClone(ClientPlayerNetworkEvent.Clone event) {
            reset();
            ScanFlightSystem.clear();
        }

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            ScanFlightSystem.tick();

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
                            ModSounds.TICKS.get(),
                            SoundSource.PLAYERS,
                            0.2F,
                            0.45F + player.getRandom().nextFloat() * 0.1F,
                            false);
                } catch (Exception ignored) {
                }
            }
            if (scanTicks >= SCAN_TICKS) {
                attempted = true;
                if (subjectAspects != null && subjectAspects.size() > 0) {
                    if (subjectIsSelf) {
                        float ox = ScreenAccess.leftPos(screen) + (PLAYER_X1 + PLAYER_X2) * 0.5F;
                        float oy = ScreenAccess.topPos(screen) + (PLAYER_Y1 + PLAYER_Y2) * 0.5F;
                        ScanFlightSystem.launchFromPoint(screen, ox, oy, subjectAspects, player);

                        PacketHandler.sendToServer(new MsgScanSelf());
                    } else {
                        Slot slot = ScreenAccess.hoveredSlot(screen);
                        if (slot != null) {
                            ScanFlightSystem.launchFromSlot(screen, slot, subjectAspects, player);

                            PacketHandler.sendToServer(
                                    new MsgScanSlot(screen.getMenu().containerId, slot.index));
                        }
                    }
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

            ScanFlightSystem.render(event.getGuiGraphics(), event.getPartialTick());
        }

        private static void drawProgress(GuiGraphicsExtractor graphics, Minecraft minecraft) {
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
            graphics.text(minecraft.font, text.toString(),
                    mouseX, mouseY - 30, 0xFFFFAA00, true);
        }

        private static void drawSelfAspects(GuiGraphicsExtractor graphics, Player player) {
            if (!hasBeenScanned(InvScanServer.SELF_KEY)) {
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