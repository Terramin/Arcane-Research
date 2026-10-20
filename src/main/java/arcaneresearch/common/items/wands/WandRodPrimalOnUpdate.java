package arcaneresearch.common.items.wands;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.wands.IWand;
import arcaneresearch.api.wands.IWandRodOnUpdate;

import java.util.ArrayList;

public class WandRodPrimalOnUpdate implements IWandRodOnUpdate {
    Aspect aspect;
    ArrayList<Aspect> primals;

    public WandRodPrimalOnUpdate(Aspect aspect) {
        this.aspect = aspect;
    }

    public WandRodPrimalOnUpdate() {
        this.aspect = null;
        this.primals = Aspect.getPrimalAspects();
    }

    @Override
    public void onUpdate(ItemStack stack, Player player) {
        if (!(stack.getItem() instanceof IWand wand)) {
            return;
        }
        if (aspect != null) {
            if (player.tickCount % 5 == 0 && wand.getVis(stack, aspect) < wand.getMaxVis(stack) / 2) {
                wand.addVis(stack, aspect, 1, false);
            }
        } else if (player.tickCount % 5 == 0) {
            ArrayList<Aspect> lacking = new ArrayList<>();
            for (Aspect primal : primals) {
                if (wand.getVis(stack, primal) < wand.getMaxVis(stack) / 2) {
                    lacking.add(primal);
                }
            }
            if (!lacking.isEmpty()) {
                wand.addVis(stack, lacking.get(player.getRandom().nextInt(lacking.size())), 1, false);
            }
        }
    }
}
