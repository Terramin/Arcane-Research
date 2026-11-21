package arcaneresearch.api.crit.mixin;

import arcaneresearch.api.crit.CritManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * EN: Hooks into the player's melee attack to roll a critical hit.
 * RU: Внедряется в ближнюю атаку игрока для броска критического удара.
 */
@Mixin(Player.class)
public abstract class CriticalHitMixin {
    /**
     * EN: HEAD injection runs before the vanilla attack damage is applied.
     * RU: Инъекция в HEAD выполняется до применения ванильного урона атаки.
     */
    @Inject(method = "attack", at = @At("HEAD"))
    private void arcaneresearch$criticalHit(Entity target, CallbackInfo ci) {
        // EN: Delegate all logic to the API manager; cast is safe because the mixin targets Player.
        // RU: Вся логика в менеджере API; приведение безопасно, так как миксин применяется к Player.
        CritManager.tryCriticalHit((Player) (Object) this, target);
    }
}
