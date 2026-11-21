package arcaneresearch.client.lib.events;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientTickEvent; 
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import arcaneresearch.Arcaneresearch;

import arcaneresearch.common.golems.ItemGolemBell;
import arcaneresearch.common.items.curios.CloudRingItem;
import arcaneresearch.common.lib.compat.CuriosCompat;
import arcaneresearch.common.lib.network.PacketHandler;
import arcaneresearch.common.lib.network.misc.PacketItemKeyToServer;
import arcaneresearch.common.lib.network.wand.PacketWandFocusChangeToServer;
import arcaneresearch.init.ModItems;

public class KeyHandler {

    public static final String KEY_CATEGORY = "key.categories.arcaneresearch";

    
    public static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath(Arcaneresearch.MODID, "main"));

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
                InputConstants.Type.KEYBOARD,
                
                InputConstants.KEY_F,
                CATEGORY
        );

        keyG = new KeyMapping(
                "key.arcaneresearch.misc",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_G, 
                CATEGORY
        );

        keyRemoveFocus = new KeyMapping(
                "key.arcaneresearch.remove_focus",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_V, 
                CATEGORY
        );
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        if (keyF == null || keyG == null) {
            init();
        }
        event.registerCategory(CATEGORY);
        event.register(keyF);
        event.register(keyG);
        event.register(keyRemoveFocus);
        Arcaneresearch.LOGGER.info("Registered Arcaneresearch key mappings");
    }

    
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.gui.screen() != null) {

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

            boolean hasWand = !WandRenderingHandler.heldWand(player).isEmpty();
            if (!radialLock && hasWand) {
                if (player.isShiftKeyDown()) {

                    if (!keyPressedF) {
                        PacketHandler.sendToServer(new PacketWandFocusChangeToServer(player, "REMOVE"));
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

                
                boolean ctrlDown = InputConstants.isKeyDown(InputConstants.KEY_LCONTROL)
                        || InputConstants.isKeyDown(InputConstants.KEY_RCONTROL);
                boolean shiftDown = InputConstants.isKeyDown(InputConstants.KEY_LSHIFT)
                        || InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);

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
                if (!WandRenderingHandler.heldWand(player).isEmpty()) {
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
        CloudRingItem.tickClient(player, mc.options.keyJump.isDown());
    }

    public static boolean isRadialMenuActive() {
        return radialActive && !radialLock;
    }

    public static void lockRadialMenu() {
        radialLock = true;
        radialActive = false;
    }
}