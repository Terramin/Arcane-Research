package arcaneresearch.client.lib.events;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;
import arcaneresearch.Arcaneresearch;
import arcaneresearch.api.casters.ICaster;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.common.golems.ItemGolemBell;
import arcaneresearch.common.items.baubles.ItemCloudRing;
import arcaneresearch.common.lib.compat.CuriosCompat;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.misc.PacketFocusChangeToServer;
import arcaneresearch.common.lib.network.misc.PacketItemKeyToServer;
import arcaneresearch.common.lib.network.wand.PacketWandFocusChangeToServer;
import arcaneresearch.init.ModItems;
@OnlyIn(Dist.CLIENT)
public class KeyHandler {
    public static final String KEY_CATEGORY = "key.categories.arcaneresearch";
    public static KeyMapping keyF;
    public static KeyMapping keyG;
    public static KeyMapping keyRemoveFocus;
    public static boolean radialActive = false;
    public static boolean radialLock = false;
    public static long lastPressF = 0L;
    public static long lastPressG = 0L;
    private static boolean keyPressedF = false;
    private static boolean keyPressedG = false;
    private static boolean keyPressedRemove = false;
    public static void init() {
        keyF = new KeyMapping(
                "key.arcaneresearch.focus",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F,
                KEY_CATEGORY
        );
        keyG = new KeyMapping(
                "key.arcaneresearch.misc",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                KEY_CATEGORY
        );
        keyRemoveFocus = new KeyMapping(
                "key.arcaneresearch.remove_focus",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                KEY_CATEGORY
        );
    }
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        if (keyF == null || keyG == null) {
            init();
        }
        event.register(keyF);
        event.register(keyG);
        event.register(keyRemoveFocus);
        Arcaneresearch.LOGGER.info("Registered Arcaneresearch key mappings");
    }
    @OnlyIn(Dist.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            if (keyPressedF) {
                radialActive = false;
                keyPressedF = false;
            }
            if (keyPressedG) {
                keyPressedG = false;
            }
            if (keyPressedRemove) {
                keyPressedRemove = false;
            }
            return;
        }
        Player player = mc.player;
        processKeyF(player);
        processKeyG(player);
        processRemoveFocus(player);
        processCloudRing(player, mc);
    }
    private static void processKeyF(Player player) {
        if (keyF == null) return;
        if (keyF.isDown()) {
            if (!keyPressedF) {
                lastPressF = System.currentTimeMillis();
                radialLock = false;
            }
            ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
            boolean hasCaster = (!mainHand.isEmpty() && mainHand.getItem() instanceof ICaster) ||
                    (!offHand.isEmpty() && offHand.getItem() instanceof ICaster);
            boolean hasWand = !WandRenderingHandler.heldWand(player).isEmpty();
            if (!radialLock && (hasCaster || hasWand)) {
                if (player.isShiftKeyDown()) {
                    if (!keyPressedF) {
                        if (hasCaster) {
                            PacketHandler.sendToServer(new PacketFocusChangeToServer("REMOVE"));
                        } else {
                            PacketHandler.sendToServer(new PacketWandFocusChangeToServer(player, "REMOVE"));
                        }
                    }
                } else {
                    radialActive = true;
                }
            } else if (!mainHand.isEmpty() && mainHand.getItem() instanceof ItemGolemBell && !keyPressedF) {
                PacketHandler.sendToServer(new PacketItemKeyToServer(0));
            }
            keyPressedF = true;
        } else {
            radialActive = false;
            if (keyPressedF) {
                lastPressF = System.currentTimeMillis();
            }
            keyPressedF = false;
        }
    }
    private static void processKeyG(Player player) {
        if (keyG == null) return;
        if (keyG.isDown()) {
            if (!keyPressedG) {
                lastPressG = System.currentTimeMillis();
                long windowHandle = Minecraft.getInstance().getWindow().getWindow();
                boolean ctrlDown = InputConstants.isKeyDown(windowHandle, GLFW.GLFW_KEY_LEFT_CONTROL) ||
                        InputConstants.isKeyDown(windowHandle, GLFW.GLFW_KEY_RIGHT_CONTROL);
                boolean shiftDown = InputConstants.isKeyDown(windowHandle, GLFW.GLFW_KEY_LEFT_SHIFT) ||
                        InputConstants.isKeyDown(windowHandle, GLFW.GLFW_KEY_RIGHT_SHIFT);
                int modifier = ctrlDown ? 1 : (shiftDown ? 2 : 0);
                PacketHandler.sendToServer(new PacketItemKeyToServer(1, modifier));
            }
            keyPressedG = true;
        } else {
            if (keyPressedG) {
                lastPressG = System.currentTimeMillis();
            }
            keyPressedG = false;
        }
    }
    private static void processRemoveFocus(Player player) {
        if (keyRemoveFocus == null) return;
        if (keyRemoveFocus.isDown()) {
            if (!keyPressedRemove) {
                ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
                ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
                boolean handsHoldCaster = (!mainHand.isEmpty() && mainHand.getItem() instanceof ICaster)
                        || (!offHand.isEmpty() && offHand.getItem() instanceof ICaster);
                boolean handsHoldWand = (!mainHand.isEmpty() && mainHand.getItem() instanceof IWand)
                        || (!offHand.isEmpty() && offHand.getItem() instanceof IWand);
                if (handsHoldCaster) {
                    PacketHandler.sendToServer(new PacketFocusChangeToServer("REMOVE"));
                } else if (handsHoldWand) {
                    PacketHandler.sendToServer(new PacketWandFocusChangeToServer(player, "REMOVE"));
                }
            }
            keyPressedRemove = true;
        } else {
            keyPressedRemove = false;
        }
    }
    private static void processCloudRing(Player player, Minecraft mc) {
        if (!CuriosCompat.isEquipped(player, ModItems.CLOUD_RING.get())) {
            return;
        }
        ItemCloudRing.tickClient(player, mc.options.keyJump.isDown());
    }
    public static boolean isRadialMenuActive() {
        return radialActive && !radialLock;
    }
    public static void lockRadialMenu() {
        radialLock = true;
        radialActive = false;
    }
}
