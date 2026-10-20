package arcaneresearch.api.crafting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface IDustTrigger {

    Placement getValidFace(Level level, Player player, BlockPos pos, Direction face);

    void execute(Level level, Player player, BlockPos pos, Placement placement, Direction side);

    default List<BlockPos> sparkle(Level level, Player player, BlockPos pos, Placement placement) {
        return Collections.singletonList(pos);
    }

    class Placement {
        public int xOffset, yOffset, zOffset;
        public Direction facing;

        public Placement(int xOffset, int yOffset, int zOffset, Direction facing) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.zOffset = zOffset;
            this.facing = facing;
        }
    }

    ArrayList<IDustTrigger> triggers = new ArrayList<>();

    static void registerDustTrigger(IDustTrigger trigger) {
        triggers.add(trigger);
    }

    static List<IDustTrigger> getTriggers() {
        return triggers;
    }

    static void clearTriggers() {
        triggers.clear();
    }
}
