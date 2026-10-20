package arcaneresearch.api.research;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Внешний хук явлений для таумометра.
 * Порт {@code thaumcraft.api.research.IScanEventHandler} (TC 4.2.3.5):
 * аддоны возвращают {@link ScanResult}, когда штатный прицел
 * (сущность -&gt; узел -&gt; блок) ничего не нашёл. Регистрация —
 * {@code ArcaneresearchApi.registerScanEventHandler}.
 */
public interface IScanEventHandler {

    ScanResult scanPhenomena(ItemStack stack, Level level, Player player);
}
