package arcaneresearch.common.lib.aura;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import arcaneresearch.api.aspects.Aspect;
import arcaneresearch.api.aspects.AspectList;
import arcaneresearch.api.wands.IWand;
import java.util.ArrayList;
import java.util.function.ObjIntConsumer;
public class NodeTapHelper {
    private NodeTapHelper() {
    }
    public static Aspect tap(Level level, BlockPos pos, AspectList source,
                             ObjIntConsumer<Aspect> take, Runnable markDirty,
                             ItemStack wandstack, Player player, int count) {
        if (!(wandstack.getItem() instanceof IWand wand)) {
            return null;
        }
        if (count % 5 == 0) {
            int tap = 1;
            if (AuraResearch.isResearchComplete(player, "NODETAPPER1")) {
                tap++;
            }
            if (AuraResearch.isResearchComplete(player, "NODETAPPER2")) {
                tap++;
            }
            boolean preserve = !player.isCrouching()
                    && AuraResearch.isResearchComplete(player, "NODEPRESERVE")
                    && !"wood".equals(wand.getRod(wandstack).getTag())
                    && !"iron".equals(wand.getCap(wandstack).getTag());
            Aspect aspect = chooseRandomFiltered(source,
                    wand.getAspectsWithRoom(wandstack), level, preserve);
            if (aspect != null) {
                int amt = source.getAmount(aspect);
                if (tap > amt) {
                    tap = amt;
                }
                if (preserve && tap == amt) {
                    tap--;
                }
                if (tap > 0) {
                    boolean doit = !level.isClientSide;
                    int rem = wand.addVis(wandstack, aspect, tap, !doit);
                    if (rem < tap) {
                        if (doit) {
                            take.accept(aspect, tap - rem);
                            markDirty.run();
                        }
                        return aspect;
                    }
                }
            }
        }
        return null;
    }
    public static Aspect chooseRandomFiltered(AspectList source, AspectList filter, Level level,
                                              boolean preserve) {
        int min = preserve ? 1 : 0;
        ArrayList<Aspect> valid = new ArrayList<>();
        for (Aspect prim : source.getAspects()) {
            if (prim != null && filter.getAmount(prim) > 0 && source.getAmount(prim) > min) {
                valid.add(prim);
            }
        }
        if (valid.isEmpty()) {
            return null;
        }
        Aspect asp = valid.get(level.random.nextInt(valid.size()));
        if (asp != null && source.getAmount(asp) > min) {
            return asp;
        }
        return null;
    }
}
