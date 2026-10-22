package arcaneresearch.common.lib.research;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.items.IScribeTools;
import arcaneresearch.api.research.IScanThing;
import arcaneresearch.common.lib.utils.InventoryUtils;
import arcaneresearch.init.ModItems;

public class ScanSky implements IScanThing {

    @Override
    public boolean checkThing(Player player, Object obj) {

        if (obj != null) return false;

        if (player.getXRot() > 0.0f) return false;

        if (!player.level().canSeeSky(player.blockPosition().above())) return false;

        if (!player.level().dimension().equals(Level.OVERWORLD)) return false;

        if (!ArcaneresearchCapabilities.knowsResearchStrict(player, "CELESTIALSCANNING")) return false;

        return true;
    }

    @Override
    public void onSuccess(Player player, Object object) {
        if (object != null || player.getXRot() > 0.0f) return;
        if (!player.level().canSeeSky(player.blockPosition().above())) return;
        if (!ArcaneresearchCapabilities.knowsResearchStrict(player, "CELESTIALSCANNING")) return;

        Level level = player.level();

        
        
        
        
        int yaw = (int) (player.getYRot() + 90.0f) % 360;
        int pitch = (int) Math.abs(player.getXRot());
        int celestialAngle = (int) ((level.getOverworldClockTime() / 24000.0 + 0.25) * 360.0) % 360;
        boolean isNight = celestialAngle > 180;

        if (isNight) {
            celestialAngle -= 180;
        }

        boolean inRangeYaw;
        boolean inRangePitch;

        if (celestialAngle > 90) {
            inRangeYaw = Math.abs(Math.abs(yaw) - 180) < 10;
            inRangePitch = Math.abs(180 - celestialAngle - pitch) < 7;
        } else {
            inRangeYaw = Math.abs(yaw) < 10;
            inRangePitch = Math.abs(celestialAngle - pitch) < 7;
        }

        long worldDay = level.getOverworldClockTime() / 24000L;
        String dayPrefix = "CEL_" + worldDay + "_";

        if (inRangeYaw && inRangePitch) {

            int moonPhase = (int) (level.getOverworldClockTime() / 24000L % 8L);
            String key = dayPrefix + (isNight ? ("Moon" + moonPhase) : "Sun");

            if (ArcaneresearchCapabilities.isResearchKnown(player, key)) {
                player.sendOverlayMessage(Component.translatable("tc.celestial.fail.1"));
                return;
            }

            if (hasScribingTools(player)
                    && InventoryUtils.consumePlayerItem(player, new ItemStack(Items.PAPER), false, true)) {

                giveNote(player, isNight ? 5 + (int) (level.getOverworldClockTime() / 24000L % 8L) : 0);
                ArcaneresearchApi.internalMethods.progressResearch(player, key);
            } else {
                player.sendOverlayMessage(Component.translatable("tc.celestial.fail.2"));
            }
            cleanResearch(player, dayPrefix);

        } else if (isNight) {

            int starDirection = getStarDirection(player);
            String key = dayPrefix + "Star" + starDirection;

            if (ArcaneresearchCapabilities.isResearchKnown(player, key)) {
                player.sendOverlayMessage(Component.translatable("tc.celestial.fail.1"));
                return;
            }

            if (hasScribingTools(player)
                    && InventoryUtils.consumePlayerItem(player, new ItemStack(Items.PAPER), false, true)) {

                giveNote(player, 1 + starDirection);
                ArcaneresearchApi.internalMethods.progressResearch(player, key);
            } else {
                player.sendOverlayMessage(Component.translatable("tc.celestial.fail.2"));
            }
            cleanResearch(player, dayPrefix);
        }
    }

    private boolean hasScribingTools(Player player) {
        
        
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (!stack.isEmpty() && stack.getItem() instanceof IScribeTools) {
                return true;
            }
        }
        return false;
    }

    private int getStarDirection(Player player) {
        float yaw = player.getYRot();

        while (yaw < 0) yaw += 360;
        while (yaw >= 360) yaw -= 360;

        if (yaw < 45 || yaw >= 315) return 0;
        if (yaw < 135) return 1;
        if (yaw < 225) return 2;
        return 3;
    }

    private void giveNote(Player player, int meta) {
        ItemStack stack = switch (meta) {
            case 0 -> new ItemStack(ModItems.CELESTIAL_NOTES_SUN.get());
            case 1 -> new ItemStack(ModItems.CELESTIAL_NOTES_STARS_1.get());
            case 2 -> new ItemStack(ModItems.CELESTIAL_NOTES_STARS_2.get());
            case 3 -> new ItemStack(ModItems.CELESTIAL_NOTES_STARS_3.get());
            case 4 -> new ItemStack(ModItems.CELESTIAL_NOTES_STARS_4.get());
            case 5 -> new ItemStack(ModItems.CELESTIAL_NOTES_MOON_1.get());
            case 6 -> new ItemStack(ModItems.CELESTIAL_NOTES_MOON_2.get());
            case 7 -> new ItemStack(ModItems.CELESTIAL_NOTES_MOON_3.get());
            case 8 -> new ItemStack(ModItems.CELESTIAL_NOTES_MOON_4.get());
            case 9 -> new ItemStack(ModItems.CELESTIAL_NOTES_MOON_5.get());
            case 10 -> new ItemStack(ModItems.CELESTIAL_NOTES_MOON_6.get());
            case 11 -> new ItemStack(ModItems.CELESTIAL_NOTES_MOON_7.get());
            case 12 -> new ItemStack(ModItems.CELESTIAL_NOTES_MOON_8.get());
            default -> ItemStack.EMPTY;
        };
        if (stack.isEmpty()) return;
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false, net.minecraft.util.Prediction.SERVER_ONLY);
        }
    }

    private void cleanResearch(Player player, String dayPrefix) {

        var knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge == null) return;

        var toRemove = new java.util.ArrayList<String>();
        for (String key : knowledge.getResearchList()) {
            if (key.startsWith("CEL_") && !key.startsWith(dayPrefix)) {
                toRemove.add(key);
            }
        }

        for (String key : toRemove) {
            knowledge.removeResearch(key);
        }

        ResearchManager.syncList.put(player.getName().getString(), true);
    }

    @Override
    public String getResearchKey(Player player, Object object) {
        return "";
    }
}
