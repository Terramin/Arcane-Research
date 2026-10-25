package arcaneresearch.common.items.wands;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.api.wands.IWandRodOnUpdate;

import java.util.ArrayList;
import java.util.List;

public class WandRodPrimalOnUpdate implements IWandRodOnUpdate {
    private static final List<Aspect> PRIMALS = List.copyOf(Aspect.getPrimalAspects());

    Aspect aspect;
    List<Aspect> primals;

    public WandRodPrimalOnUpdate(Aspect aspect) {
        this.aspect = aspect;
        this.primals = PRIMALS;
    }

    public WandRodPrimalOnUpdate() {
        this.aspect = null;
        this.primals = PRIMALS;
    }

    @Override
    public void onUpdate(ItemStack stack, Player player) {
        if (!(stack.getItem() instanceof IWand wand)) {
            return;
        }
        if (player.tickCount % 5 != 0) {
            return;
        }

        int half = wand.getMaxVis(stack) / 2;

        if (aspect != null) {
            if (wand.getVis(stack, aspect) < half) {
                wand.addVis(stack, aspect, 1, false);
            }
            return;
        }

        List<Aspect> lacking = new ArrayList<>();
        for (Aspect primal : primals) {
            if (wand.getVis(stack, primal) < half) {
                lacking.add(primal);
            }
        }
        if (!lacking.isEmpty()) {
            wand.addVis(stack, lacking.get(player.getRandom().nextInt(lacking.size())), 1, false);
        }
    }
}