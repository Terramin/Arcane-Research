package arcaneresearch.api.lifesteal.events;

import arcaneresearch.api.lifesteal.LifestealStats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * EN: Fired before lifesteal is applied. Canceling prevents the healing.
 * RU: Вызывается до применения вампиризма. Отмена предотвращает лечение.
 * EN: Stats and heal amount can be modified by other mods.
 * RU: Статы и количество лечения могут быть изменены другими модами.
 */
@Cancelable
public class LifestealEvent extends Event {
    /** EN: Attacking player. RU: Атакующий игрок. */
    private final Player player;
    /** EN: Damaged entity. RU: Сущность, получившая урон. */
    private final LivingEntity target;
    /** EN: Stats used for this roll. RU: Статы, использованные для этого броска. */
    private LifestealStats stats;
    /** EN: Amount to heal, modifiable by listeners. RU: Количество лечения, изменяемое слушателями. */
    private float amount;

    public LifestealEvent(Player player, LivingEntity target, LifestealStats stats, float amount) {
        this.player = player;
        this.target = target;
        this.stats = stats;
        this.amount = amount;
    }

    /** EN: Returns the attacker. RU: Возвращает атакующего. */
    public Player getPlayer() {
        return player;
    }

    /** EN: Returns the target. RU: Возвращает цель. */
    public LivingEntity getTarget() {
        return target;
    }

    /** EN: Returns the lifesteal stats. RU: Возвращает статы вампиризма. */
    public LifestealStats getStats() {
        return stats;
    }

    /** EN: Replaces the lifesteal stats. RU: Заменяет статы вампиризма. */
    public void setStats(LifestealStats stats) {
        this.stats = stats;
    }

    /** EN: Returns the heal amount. RU: Возвращает количество лечения. */
    public float getAmount() {
        return amount;
    }

    /** EN: Sets the heal amount. RU: Устанавливает количество лечения. */
    public void setAmount(float amount) {
        this.amount = amount;
    }
}
