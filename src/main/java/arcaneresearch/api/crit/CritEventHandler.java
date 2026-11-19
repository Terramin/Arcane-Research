package arcaneresearch.api.crit;

import arcaneresearch.Arcaneresearch;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * EN: Forge-side hooks of the critical hit API.
 * RU: Хуки API критического удара на стороне Forge.
 */
@Mod.EventBusSubscriber(modid = Arcaneresearch.MODID)
public final class CritEventHandler {
    private CritEventHandler() {
    }

    /**
     * EN: Keeps crit attribute base values across death and dimension changes.
     * RU: Сохраняет базовые значения атрибутов крита при смерти и смене измерений.
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // EN: Original player before the clone (death / dimension travel).
        // RU: Оригинальный игрок до клонирования (смерть / переход в другое измерение).
        Player original = event.getOriginal();
        // EN: Newly created player instance.
        // RU: Ново созданный экземпляр игрока.
        Player clone = event.getEntity();
        clone.getAttribute(CritAttributes.CRIT_CHANCE.get()).setBaseValue(
                original.getAttribute(CritAttributes.CRIT_CHANCE.get()).getBaseValue());
        clone.getAttribute(CritAttributes.CRIT_DAMAGE.get()).setBaseValue(
                original.getAttribute(CritAttributes.CRIT_DAMAGE.get()).getBaseValue());
    }
}
