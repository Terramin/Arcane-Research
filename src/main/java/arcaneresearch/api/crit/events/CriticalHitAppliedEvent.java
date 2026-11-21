package arcaneresearch.api.crit.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

/**
 * EN: Fired after a critical strike has been dealt, for achievements, effects, tracking, etc.
 * RU: Вызывается после нанесения критического удара — для ачивок, эффектов, статистики и т.д.
 */
public class CriticalHitAppliedEvent extends Event {
    // EN: Attacking player.
    // RU: Атакующий игрок.
    private final Player player;
    // EN: Entity that was hit.
    // RU: Сущность, по которой был нанесён удар.
    private final Entity target;
    // EN: Final damage that was dealt.
    // RU: Итоговый нанесённый урон.
    private final float damage;

    public CriticalHitAppliedEvent(Player player, Entity target, float damage) {
        this.player = player;
        this.target = target;
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

    /** EN: Returns the dealt damage. RU: Возвращает нанесённый урон. */
    public float getDamage() {
        return damage;
    }
}
