package thaumictinker.kami.tool;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

 


 
public enum ToolKind {
    PICKAXE("pick"),
    SHOVEL("shovel"),
    AXE("axe"),
    SWORD("sword");

    private final String type;

    ToolKind(String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }

    public boolean matches(BlockState state) {
        return switch (this) {
            case PICKAXE -> state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.IMPERMEABLE);
            case SHOVEL -> state.is(BlockTags.MINEABLE_WITH_SHOVEL);
            case AXE -> state.is(BlockTags.MINEABLE_WITH_AXE) || state.is(BlockTags.LEAVES)
                    || state.getBlock() instanceof BushBlock || state.getBlock() instanceof VineBlock;
            case SWORD -> false;
        };
    }
}
