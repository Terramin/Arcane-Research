package arcaneresearch.api.crit.events;

import arcaneresearch.api.crit.CritStats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * EN: Fired before a critical strike is applied. Canceling prevents the extra hit.
 * RU: Вызывается до нанесения критического удара. Отмена предотвращает дополнительный удар.
 * EN: Damage and stats can be tweaked by other mods.
 * RU: Урон и статы могут быть изменены другими модами.
 */
public class CriticalHitEvent extends Event implements ICancellableEvent {
    // EN: Attacking player.
    // RU: Атакующий игрок.
    private final Player player;
    // EN: Entity being hit.
    // RU: Сущность, по которой наносится удар.
    private final Entity target;
    // EN: Mutable crit stats used for this roll.
    // RU: Изменяемые статы крита, использованные для этого броска.
    private CritStats stats;
    // EN: Damage of the extra hit, modifiable by listeners.
    // RU: Урон дополнительного удара, изменяемый слушателями.
    private float damage;

    public CriticalHitEvent(Player player, Entity target, CritStats stats, float damage) {
        this.player = player;
        this.target = target;
        this.stats = stats;
        this.damage = damage;
    }

    /** EN: Returns the attacker. RU: Возвращает атакующего. */
    public Player getPlayer() {
        return player;
    }

    /** EN: Returns the target. RU: Возвращает цель. */
    public Entity getTarget() {
        return target;
    }

    /** EN: Returns the crit stats of this strike. RU: Возвращает статы крита этого удара. */
    public CritStats getStats() {
        return stats;
    }

    /** EN: Replaces the crit stats. RU: Заменяет статы крита. */
    public void setStats(CritStats stats) {
        this.stats = stats;
    }

    /** EN: Returns the extra hit damage. RU: Возвращает урон дополнительного удара. */
    public float getDamage() {
        return damage;
    }

    /** EN: Sets the extra hit damage. RU: Устанавливает урон дополнительного удара. */
    public void setDamage(float damage) {
        this.damage = damage;
    }
}
