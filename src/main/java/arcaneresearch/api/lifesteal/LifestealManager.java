package arcaneresearch.api.lifesteal;

import arcaneresearch.api.lifesteal.events.LifestealAppliedEvent;
import arcaneresearch.api.lifesteal.events.LifestealEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

/**
 * EN: Core engine of the lifesteal API: rolls the chance, fires events and applies healing.
 * RU: Ядро API вампиризма: бросает шанс, вызывает события и применяет лечение.
 */
public final class LifestealManager {
    private LifestealManager() {
    }

    /**
     * EN: Stats from globally registered sources, used by API consumers.
     * RU: Статы от глобально зарегистрированных источников — для потребителей API.
     */
    public static LifestealStats resolveStats(Player player) {
        return LifestealModifiers.apply(player, LifestealStats.NONE);
    }

    /**
     * EN: Rolls the chance using stats from global sources.
     * RU: Бросает шанс, используя статы от глобальных источников.
     */
    public static boolean tryLifesteal(Player player, LivingEntity target, float damage) {
        return apply(player, target, resolveStats(player), damage);
    }

    /**
     * EN: Applies lifesteal with explicit stats, e.g. from an item's own implementation.
     * RU: Применяет вампиризм с явно заданными статами, например из реализации самого предмета.
     */
    public static boolean apply(Player player, LivingEntity target, LifestealStats stats, float damage) {
        if (damage <= 0.0F || player == target || !stats.canLifesteal()) {
            return false;
        }
        // EN: Chance is stored in percent, so compare with a 0..1 random float.
        // RU: Шанс хранится в процентах, поэтому сравниваем со случайным числом 0..1.
        if (player.getRandom().nextFloat() >= stats.getChanceMultiplier()) {
            return false;
        }

        float heal = (float) stats.getHeal();
        LifestealEvent event = new LifestealEvent(player, target, stats, heal);
        // EN: Other mods may cancel the heal or change its amount.
        // RU: Другие моды могут отменить лечение или изменить его количество.
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return false;
        }
        // EN: Never heal past the player's maximum health.
        // RU: Никогда не лечим выше максимального здоровья игрока.
        float finalHeal = Math.min(event.getAmount(), player.getMaxHealth() - player.getHealth());
        if (finalHeal <= 0.0F) {
            return false;
        }
        player.heal(finalHeal);
        // EN: Notify listeners that healing actually happened.
        // RU: Уведомляем слушателей, что лечение действительно произошло.
        MinecraftForge.EVENT_BUS.post(new LifestealAppliedEvent(player, target, finalHeal));
        return true;
    }

    /**
     * EN: Applies a flat heal amount, bypassing the LifestealStats caps meant for enchantments.
     *     Still fires LifestealEvent / LifestealAppliedEvent so other mods can react.
     *     The exact requested amount is carried by the event's {@code amount} field.
     * RU: Применяет фиксированное лечение, минуя лимиты LifestealStats, предназначенные
     *     для зачарований. События LifestealEvent / LifestealAppliedEvent всё равно вызываются.
     *     Точное запрошенное количество передаётся в поле {@code amount} события.
     *
     * @param player healer / лечащийся игрок
     * @param target damaged entity / сущность, получившая урон
     * @param amount requested heal in health points / запрошенное лечение в очках здоровья
     * @return true if health was actually restored / true, если здоровье действительно восстановлено
     */
    public static boolean applyFlatHeal(Player player, LivingEntity target, float amount) {
        if (amount <= 0.0F || player == target) {
            return false;
        }
        LifestealEvent event = new LifestealEvent(player, target, LifestealStats.NONE, amount);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return false;
        }
        // EN: Health still cannot go above the player's maximum.
        // RU: Здоровье по-прежнему не может превысить максимум игрока.
        float finalHeal = Math.min(event.getAmount(), player.getMaxHealth() - player.getHealth());
        if (finalHeal <= 0.0F) {
            return false;
        }
        player.heal(finalHeal);
        MinecraftForge.EVENT_BUS.post(new LifestealAppliedEvent(player, target, finalHeal));
        return true;
    }
}
