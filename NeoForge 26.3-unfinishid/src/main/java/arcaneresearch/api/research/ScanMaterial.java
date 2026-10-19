package arcaneresearch.api.research;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class ScanMaterial implements IScanThing {

    private final String research;
    private final Fluid[] fluids;

    public ScanMaterial(Fluid fluid) {
        this.research = "!" + fluid.getClass().getTypeName();
        this.fluids = new Fluid[] { fluid };
    }

    public ScanMaterial(String research, Fluid... fluids) {
        this.research = research;
        this.fluids = fluids;
    }

    @Override
    public boolean checkThing(Player player, Object obj) {
        if (obj instanceof BlockPos pos) {
            var fluidState = player.level().getFluidState(pos);
            for (Fluid fluid : fluids) {
                if (fluidState.is(fluid)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getResearchKey(Player player, Object object) {
        return research;
    }
}