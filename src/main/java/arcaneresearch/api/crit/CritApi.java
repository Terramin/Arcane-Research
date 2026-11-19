package arcaneresearch.api.crit;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * EN: Public entry point of the critical hit API.
 * RU: Публичная точка входа API критического удара.
 */
public final class CritApi {
    private CritApi() {
    }

    /**
     * EN: Call once from the mod constructor with the mod loading context.
     * RU: Вызвать один раз в конструкторе мода, передав контекст загрузки.
     */
    public static void init(FMLJavaModLoadingContext context) {
        CritAttributes.ATTRIBUTES.register(context.getModEventBus());
        // EN: Register the editable TOML config (config/arcaneresearch-crit.toml).
        // RU: Регистрируем редактируемый TOML-конфиг (config/arcaneresearch-crit.toml).
        context.registerConfig(ModConfig.Type.COMMON, CritConfig.SPEC, "arcaneresearch-crit.toml");
    }

    /**
     * EN: Adds crit attributes to the player. Call from the mod's EntityAttributeModificationEvent listener.
     * RU: Добавляет атрибуты крита игроку. Вызвать в слушателе EntityAttributeModificationEvent мода.
     */
    public static void addPlayerAttributes(EntityAttributeModificationEvent event) {
        CritAttributes.addPlayerAttributes(event);
    }

    /**
     * EN: Registers a global modifier that contributes to every player's crit stats.
     * RU: Регистрирует глобальный модификатор, влияющий на статы крита каждого игрока.
     */
    public static void registerModifier(ICriticalModifier modifier) {
        CritModifiers.register(modifier);
    }

    /**
     * EN: Removes a previously registered modifier.
     * RU: Удаляет ранее зарегистрированный модификатор.
     */
    public static void unregisterModifier(ICriticalModifier modifier) {
        CritModifiers.unregister(modifier);
    }

    /**
     * EN: Current crit stats of a player (attributes + registered modifiers).
     * RU: Текущие статы крита игрока (атрибуты + зарегистрированные модификаторы).
     */
    public static CritStats getStats(Player player) {
        return CritManager.resolveStats(player);
    }
}
