package arcaneresearch.api.casters;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
public class CasterTriggerRegistry {
    private static final HashMap<String, LinkedHashMap<BlockState, List<Trigger>>> triggers = new HashMap<>();
    private static final String DEFAULT = "default";
    private static class Trigger {
        final ICasterTriggerManager manager;
        final int event;
        Trigger(ICasterTriggerManager manager, int event) {
            this.manager = manager;
            this.event = event;
        }
    }
    public static void registerWandBlockTrigger(ICasterTriggerManager manager, int event, BlockState state, String modid) {
        LinkedHashMap<BlockState, List<Trigger>> temp = triggers.computeIfAbsent(modid, k -> new LinkedHashMap<>());
        List<Trigger> ts = temp.get(state);
        if (ts == null) {
            ts = new ArrayList<>();
        }
        ts.add(new Trigger(manager, event));
        temp.put(state, ts);
    }
    public static void registerCasterBlockTrigger(ICasterTriggerManager manager, int event, BlockState state) {
        registerWandBlockTrigger(manager, event, state, DEFAULT);
    }
    public static boolean hasTrigger(BlockState state) {
        for (LinkedHashMap<BlockState, List<Trigger>> temp : triggers.values()) {
            if (temp.containsKey(state)) {
                return true;
            }
        }
        return false;
    }
    public static boolean hasTrigger(BlockState state, String modid) {
        LinkedHashMap<BlockState, List<Trigger>> temp = triggers.get(modid);
        return temp != null && temp.containsKey(state);
    }
    public static boolean performTrigger(Level world, ItemStack casterStack, Player player,
                                         BlockPos pos, Direction side, BlockState state) {
        for (LinkedHashMap<BlockState, List<Trigger>> temp : triggers.values()) {
            List<Trigger> l = temp.get(state);
            if (l == null || l.isEmpty()) {
                continue;
            }
            for (Trigger trig : l) {
                if (trig.manager.performTrigger(world, casterStack, player, pos, side, trig.event)) {
                    return true;
                }
            }
        }
        return false;
    }
    public static boolean performTrigger(Level world, ItemStack casterStack, Player player,
                                         BlockPos pos, Direction side, BlockState state, String modid) {
        LinkedHashMap<BlockState, List<Trigger>> temp = triggers.get(modid);
        if (temp == null) {
            return false;
        }
        List<Trigger> l = temp.get(state);
        if (l == null || l.isEmpty()) {
            return false;
        }
        for (Trigger trig : l) {
            if (trig.manager.performTrigger(world, casterStack, player, pos, side, trig.event)) {
                return true;
            }
        }
        return false;
    }
}
