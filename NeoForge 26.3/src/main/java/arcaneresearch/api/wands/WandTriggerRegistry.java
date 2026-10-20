package arcaneresearch.api.wands;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class WandTriggerRegistry {
    private static LinkedHashMap<String, LinkedHashMap<BlockState, List<Trigger>>> triggers = new LinkedHashMap<>();
    private static final String DEFAULT = "default";

    private static class Trigger {
        final IWandTriggerManager manager;
        final int event;

        Trigger(IWandTriggerManager manager, int event) {
            this.manager = manager;
            this.event = event;
        }
    }

    public static void registerWandBlockTrigger(IWandTriggerManager manager, int event, BlockState state, String modid) {
        triggers.computeIfAbsent(modid, k -> new LinkedHashMap<>());
        LinkedHashMap<BlockState, List<Trigger>> temp = triggers.get(modid);
        List<Trigger> ts = temp.get(state);
        if (ts == null) {
            ts = new ArrayList<>();
        }
        ts.add(new Trigger(manager, event));
        temp.put(state, ts);
    }

    public static void registerWandBlockTrigger(IWandTriggerManager manager, int event, BlockState state) {
        registerWandBlockTrigger(manager, event, state, DEFAULT);
    }

    public static boolean hasTrigger(BlockState state) {
        for (String modid : triggers.keySet()) {
            LinkedHashMap<BlockState, List<Trigger>> temp = triggers.get(modid);
            if (temp.containsKey(state)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasTrigger(BlockState state, String modid) {
        if (!triggers.containsKey(modid)) {
            return false;
        }
        LinkedHashMap<BlockState, List<Trigger>> temp = triggers.get(modid);
        return temp.containsKey(state);
    }

    public static boolean performTrigger(Level level, ItemStack wand, Player player, BlockPos pos, Direction side,
            BlockState state) {
        for (String modid : new ArrayList<>(triggers.keySet())) {
            LinkedHashMap<BlockState, List<Trigger>> temp = triggers.get(modid);
            List<Trigger> l = temp.get(state);
            if (l == null || l.isEmpty()) {
                continue;
            }
            for (Trigger trig : l) {
                boolean result = trig.manager.performTrigger(level, wand, player, pos, side, trig.event);
                if (result) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean performTrigger(Level level, ItemStack wand, Player player, BlockPos pos, Direction side,
            BlockState state, String modid) {
        if (!triggers.containsKey(modid)) {
            return false;
        }
        LinkedHashMap<BlockState, List<Trigger>> temp = triggers.get(modid);
        List<Trigger> l = temp.get(state);
        if (l == null || l.isEmpty()) {
            return false;
        }
        for (Trigger trig : l) {
            boolean result = trig.manager.performTrigger(level, wand, player, pos, side, trig.event);
            if (result) {
                return true;
            }
        }
        return false;
    }
}
