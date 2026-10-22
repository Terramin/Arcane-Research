package thaumictinker.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

 


 
public final class TtCurios {

    private TtCurios() {
    }

      
    public static List<ItemStack> stacks(Player player) {
        return CuriosApi.getCuriosInventory(player).map(handler -> {
            List<ItemStack> out = new ArrayList<>();
            for (SlotResult result : handler.findCurios(stack -> !stack.isEmpty())) {
                out.add(result.stack());
            }
            return out;
        }).orElse(new ArrayList<>());
    }

      
    public static boolean isEquipped(Player player, Item item) {
        return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.findFirstCurio(item).isPresent())
                .orElse(false);
    }
}
