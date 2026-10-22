package arcaneresearch.invscan;

import java.util.HashSet;
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
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.aspects.AspectHelper;
import arcaneresearch.client.lib.AspectRenderer;
import arcaneresearch.common.items.tools.ItemThaumometer;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.discovered.scan.ScanTargets;
import arcaneresearch.discovered.scan.ScanWallet;
import arcaneresearch.helper.RegsHelper;
import arcaneresearch.init.ModSounds;

 








 
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

    private InvScanClient() {
    }

    private static void reset() {
        scanKey = "";
        subjectAspects = null;
        scanTicks = 0;
        attempted = false;
    }

    private static boolean hasBeenScanned(Player player, String key) {
        return LOCAL_SCANNED.contains(key) || ScanWallet.hasScanned(player, key);
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

    private static boolean resolveSubject(AbstractContainerScreen<?> screen, Player player) {
        boolean self = hoveringSelf(screen, player);
        AspectList aspects;
        String key;
        if (self) {
            aspects = ScanTargets.entityAspects(player);
            key = SELF_SUBJECT;
        } else {
            Slot slot = ScreenAccess.hoveredSlot(screen);
            if (slot == null || slot.getItem().isEmpty() || slot instanceof ResultSlot
                    || !slot.mayPickup(player)) {
                return false;
            }
            ItemStack stack = slot.getItem();
            aspects = AspectHelper.getObjectAspects(stack);
            key = String.valueOf(slot.index) + ":" + RegsHelper.itemKey(stack.getItem());
        }
        if (key.equals(scanKey)) {
            return true;
        }
        scanKey = key;
        subjectAspects = aspects;
        subjectIsSelf = self;
        scanTicks = 0;
        attempted = false;
        return true;
    }

    private static boolean scannable(Player player) {
        return subjectAspects != null && subjectAspects.size() > 0
                && !hasBeenScanned(player, scanKey);
    }

    

    @EventBusSubscriber(modid = Arcaneresearch.MODID, value = Dist.CLIENT)
    public static class ClientEvents {

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft == null ? null : minecraft.player;
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
                            0.45F + minecraft.level.getRandom().nextFloat() * 0.1F,
                            false);
                } catch (Exception ignored) {
                }
            }
            if (scanTicks >= SCAN_TICKS) {
                attempted = true;
                if (subjectAspects != null
                        && ScanTargets.validScan(player, subjectAspects)) {
                    if (subjectIsSelf) {
                        PacketHandler.sendToServer(new MsgScanSelf());
                    } else {
                        Slot slot = ScreenAccess.hoveredSlot(screen);
                        if (slot != null) {
                            PacketHandler.sendToServer(
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
            if (!hasBeenScanned(player, InvScanServer.SELF_KEY)) {
                return;
            }
            AspectList aspects = ScanTargets.entityAspects(player);
            if (aspects == null || aspects.size() <= 0) {
                return;
            }
            int index = 0;
            for (Aspect aspect : aspects.getAspectsSortedByAmount()) {
                if (aspect == null) {
                    continue;
                }
                int x = mouseX + 17 + index * 18 - 8;
                int y = mouseY + 7 - 33 - 8;
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
