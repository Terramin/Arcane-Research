package arcaneresearch.api.lifesteal.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

/**
 * EN: Fired after lifesteal healing has been applied.
 * RU: Вызывается после применения лечения от вампиризма.
 */
public class LifestealAppliedEvent extends Event {
    /** EN: Attacking player. RU: Атакующий игрок. */
    private final Player player;
    /** EN: Damaged entity. RU: Сущность, получившая урон. */
    private final LivingEntity target;
    /** EN: Health actually restored. RU: Фактически восстановленное здоровье. */
    private final float amount;

    public LifestealAppliedEvent(Player player, LivingEntity target, float amount) {
        this.player = player;
        this.target = target;
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

    /** EN: Returns the restored health. RU: Возвращает восстановленное здоровье. */
    public float getAmount() {
        return amount;
    }
}
