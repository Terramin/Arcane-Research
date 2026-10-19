package arcaneresearch.api.wands;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
public interface IWandRodOnUpdate {
    void onUpdate(ItemStack stack, Player player);
}
