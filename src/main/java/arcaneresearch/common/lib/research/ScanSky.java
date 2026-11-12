package arcaneresearch.common.lib.research;

import arcaneresearch.api.ArcaneresearchApi;
import arcaneresearch.api.capabilities.ArcaneresearchCapabilities;
import arcaneresearch.api.items.IScribeTools;
import arcaneresearch.api.research.IScanThing;
import arcaneresearch.common.lib.utils.InventoryUtils;
import arcaneresearch.init.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Set;

public class ScanSky implements IScanThing {

    @Override
    public boolean checkThing(Player player, Object obj) {
        if (player == null) return false;
        if (obj != null) return false;
        if (player.getXRot() > 0.0f) return false;
        if (!player.level().canSeeSky(player.blockPosition().above())) return false;
        if (!player.level().dimension().equals(Level.OVERWORLD)) return false;
        if (!ArcaneresearchCapabilities.knowsResearchStrict(player, "CELESTIALSCANNING")) return false;
        SkyState sky = computeSkyState(player, player.level());
        return sky.aligned() || sky.isNight();
    }

    @Override
    public void onSuccess(Player player, Object object) {
        if (player == null) return;
        if (object != null || player.getXRot() > 0.0f) return;

        Level level = player.level();
        if (!level.canSeeSky(player.blockPosition().above())) return;
        if (!ArcaneresearchCapabilities.knowsResearchStrict(player, "CELESTIALSCANNING")) return;

        SkyState sky = computeSkyState(player, level);
        long worldDay = level.getDayTime() / 24000L;
        String dayPrefix = "CEL_" + worldDay + "_";

        if (sky.aligned()) {
            int moonPhase = (int) (level.getDayTime() / 24000L % 8L);
            String key = dayPrefix + (sky.isNight() ? ("Moon" + moonPhase) : "Sun");
            if (ArcaneresearchCapabilities.isResearchKnown(player, key)) {
                player.displayClientMessage(Component.translatable("tc.celestial.fail.1"), true);
                return;
            }
            if (hasScribingTools(player)
                    && InventoryUtils.consumePlayerItem(player, new ItemStack(Items.PAPER), false, true)) {
                giveNote(player, sky.isNight() ? 5 + moonPhase : 0);
                ArcaneresearchApi.internalMethods.progressResearch(player, key);
            } else {
                player.displayClientMessage(Component.translatable("tc.celestial.fail.2"), true);
            }
            cleanResearch(player, dayPrefix);
        } else if (sky.isNight()) {
            int starDirection = getStarDirection(player);
            String key = dayPrefix + "Star" + starDirection;
            if (ArcaneresearchCapabilities.isResearchKnown(player, key)) {
                player.displayClientMessage(Component.translatable("tc.celestial.fail.1"), true);
                return;
            }
            if (hasScribingTools(player)
                    && InventoryUtils.consumePlayerItem(player, new ItemStack(Items.PAPER), false, true)) {
                giveNote(player, 1 + starDirection);
                ArcaneresearchApi.internalMethods.progressResearch(player, key);
            } else {
                player.displayClientMessage(Component.translatable("tc.celestial.fail.2"), true);
            }
            cleanResearch(player, dayPrefix);
        }
    }

    private boolean hasScribingTools(Player player) {
        if (player == null) return false;
        for (ItemStack stack : player.getInventory().items) {
            if (stack != null && !stack.isEmpty() && stack.getItem() instanceof IScribeTools) {
                return true;
            }
        }
        return false;
    }

    private int getStarDirection(Player player) {
        return switch (player.getDirection()) {
            case NORTH -> 0;
            case SOUTH -> 1;
            case WEST -> 2;
            default -> 3;
        };
    }

    private record SkyState(boolean isNight, boolean aligned) {
    }

    private SkyState computeSkyState(Player player, Level level) {
        int yaw = (int) Mth.wrapDegrees(player.getYRot() + 90.0f);
        int pitch = (int) Math.abs(player.getXRot());

        int celestialAngle = (int) ((level.getDayTime() % 24000L) * 360.0 / 24000.0) % 360;
        boolean isNight = celestialAngle > 180;
        int bodyAngle = isNight ? celestialAngle - 180 : celestialAngle;

        boolean inRangeYaw;
        boolean inRangePitch;
        if (bodyAngle > 90) {
            inRangeYaw = Math.abs(Math.abs(yaw) - 180) < 10;
            inRangePitch = Math.abs(180 - bodyAngle - pitch) < 7;
        } else {
            inRangeYaw = Math.abs(yaw) < 10;
            inRangePitch = Math.abs(bodyAngle - pitch) < 7;
        }
        return new SkyState(isNight, inRangeYaw && inRangePitch);
    }

    private void giveNote(Player player, int meta) {
        ItemStack stack = switch (meta) {
            case 0  -> new ItemStack(ModItems.CELESTIAL_NOTES_SUN.get());
            case 1  -> new ItemStack(ModItems.CELESTIAL_NOTES_STARS_1.get());
            case 2  -> new ItemStack(ModItems.CELESTIAL_NOTES_STARS_2.get());
            case 3  -> new ItemStack(ModItems.CELESTIAL_NOTES_STARS_3.get());
            case 4  -> new ItemStack(ModItems.CELESTIAL_NOTES_STARS_4.get());
            case 5  -> new ItemStack(ModItems.CELESTIAL_NOTES_MOON_1.get());
            case 6  -> new ItemStack(ModItems.CELESTIAL_NOTES_MOON_2.get());
            case 7  -> new ItemStack(ModItems.CELESTIAL_NOTES_MOON_3.get());
            case 8  -> new ItemStack(ModItems.CELESTIAL_NOTES_MOON_4.get());
            case 9  -> new ItemStack(ModItems.CELESTIAL_NOTES_MOON_5.get());
            case 10 -> new ItemStack(ModItems.CELESTIAL_NOTES_MOON_6.get());
            case 11 -> new ItemStack(ModItems.CELESTIAL_NOTES_MOON_7.get());
            case 12 -> new ItemStack(ModItems.CELESTIAL_NOTES_MOON_8.get());
            default -> ItemStack.EMPTY;
        };
        if (stack.isEmpty()) return;
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private void cleanResearch(Player player, String dayPrefix) {
        if (player == null) return;

        var knowledge = ArcaneresearchCapabilities.getKnowledge(player);
        if (knowledge == null) return;

        Set<String> research = knowledge.getResearchList();
        String playerName = player.getName().getString();

        if (research == null || research.isEmpty()) {
            ResearchManager.syncList.put(playerName, true);
            return;
        }

        ArrayList<String> toRemove = null;
        for (String key : research) {
            if (key != null && key.startsWith("CEL_") && !key.startsWith(dayPrefix)) {
                if (toRemove == null) toRemove = new ArrayList<>();
                toRemove.add(key);
            }
        }
        if (toRemove != null) {
            for (String key : toRemove) {
                knowledge.removeResearch(key);
            }
        }
        ResearchManager.syncList.put(playerName, true);
    }

    @Override
    public String getResearchKey(Player player, Object object) {
        return "";
    }
}